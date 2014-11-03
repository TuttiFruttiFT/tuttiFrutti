package tuttifrutti.models;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static tuttifrutti.models.enums.DuplaScore.ALONE_SCORE;
import static tuttifrutti.models.enums.DuplaScore.DUPLICATE_SCORE;
import static tuttifrutti.models.enums.DuplaScore.UNIQUE_SCORE;
import static tuttifrutti.models.enums.MatchState.OPPONENT_TURN;
import static tuttifrutti.models.enums.MatchState.PLAYER_TURN;
import static tuttifrutti.models.enums.MatchState.TO_BE_APPROVED;
import static tuttifrutti.models.enums.MatchState.WAITING_FOR_OPPONENTS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import tuttifrutti.models.enums.MatchState;
import tuttifrutti.serializers.ObjectIdSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

@Entity
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Match {
	@Id 
	@JsonSerialize(using = ObjectIdSerializer.class)
	private ObjectId id;
	
	private MatchState state;
	
	private String name;
	
	@Embedded
	@JsonProperty(value = "match_name")
	private MatchName matchName;
	
	@Embedded
	private MatchConfig config;
	
	@Embedded
	private Player winner;
	
	@Property("winner_id")
	private String winnerId;
	
	@Property("start_date")
	@JsonProperty(value = "start_date")
	@JsonSerialize(using = DateSerializer.class)
	@JsonDeserialize(using = DateDeserializer.class)
	private Date startDate;
	
	@Embedded
	private List<Category> categories;
	
	@Embedded
	@JsonProperty(value = "players")
	private List<PlayerResult> playerResults;
	
	@JsonProperty(value = "current_round")
	private Round lastRound;
	
	@JsonIgnore
	@Embedded
	private Alphabet alphabet;
	
	@Property("played_letters")
	@JsonProperty(value = "played_letters")
	private List<Letter> playedLetters;
	
	public void changeMatchDependingOnPlayer(String playerId){
		this.changeMatchNameDependingOnPlayer(playerId);
		this.changeMatchStateDependingOnPlayersGame(playerId);
	}
	
	private void changeMatchNameDependingOnPlayer(String playerId) {
		if(this.getMatchName().isCalculated()){
			List<String> nicknames = new ArrayList<>();
			this.getPlayerResults().stream()
			.filter(playerResult -> !playerResult.getPlayer().getId().toString().equals(playerId))
			.forEach(playerResult -> nicknames.add(playerResult.getPlayer().getNickname()));
			if(nicknames.size() > 0){
				Collections.sort(nicknames);
				this.getMatchName().setValue(nicknames.get(0));
			}else{
				this.getMatchName().setValue("");
			}
			this.setName(this.getMatchName().getValue());
		}
	}
	
	private void changeMatchStateDependingOnPlayersGame(String playerId) {
		boolean playerHasAlreadyPlayed = playerHasAlreadyPlayed(playerId);
		PlayerResult player = this.getPlayerResults().stream().filter(playerResult -> playerResult.getPlayer().getId().toString().equals(playerId))
																	 .findFirst().get();
		MatchState matchState = this.getState();
		if(matchState.equals(TO_BE_APPROVED)){
			if(player.isAccepted() && !playerHasAlreadyPlayed){
				this.setState(PLAYER_TURN);
			}
			
			if(player.isAccepted() && playerHasAlreadyPlayed){
				this.setState(WAITING_FOR_OPPONENTS);
			}
		}
		
		if(matchState.equals(PLAYER_TURN)){
			if(playerHasAlreadyPlayed){
				this.setState(OPPONENT_TURN);
			}
		}
	}

	@JsonIgnore
	public boolean isRoundOver() {
		return this.getLastRound().getTurns().size() == this.getConfig().getCurrentTotalNumberOfPlayers();
	}


	public void calculateWinner() {
		PlayerResult winnerPlayer = playerResults.stream().max(new PlayerResultScoreComparator()::compare).get();
		int winnerScore = winnerPlayer.getScore();
		List<PlayerResult> playersWithMaxScore = playerResults.stream().filter(playerResult -> playerResult.getScore() == winnerScore)
																	   .collect(toList());
		if(playersWithMaxScore.size() > 1){
			winnerPlayer = playerResults.stream().min(new PlayerResultTimeComparator()::compare).get();
		}
		this.winner = new Player(winnerPlayer.getPlayer().getId(),winnerPlayer.getPlayer().getNickname());
	}

	public boolean isFinished(Round round) {
		return round.getNumber() == this.getConfig().getRounds();
	}

	public void processValidDuplas(List<Dupla> validDuplas) {
		for(Category category : this.getCategories()){
			List<Dupla> categoryDuplas = getDuplasByCategory(validDuplas, category);
			
			if(!categoryDuplas.isEmpty()){
				if(categoryDuplas.size() == 1){
					categoryDuplas.get(0).setScore(ALONE_SCORE.getScore());
				}else{
					comparingAndScoring(categoryDuplas);
				}
			}
		}
	}

	public List<String> playerIds() {
		return playerResults.stream().map(playerResult -> playerResult.getPlayer().getId().toString()).collect(toList());
	}

	public Player getStopPlayer(List<Turn> turns, Integer minTime) {
		Optional<Turn> optionalPlayer = turns.stream().filter(turn -> turn.getEndTime() == minTime).findFirst();
		if(optionalPlayer.isPresent()){			
			return optionalPlayer.get().getPlayer();
		}
		return null;
	}

	private List<Dupla> getDuplasByCategory(List<Dupla> allDuplas,Category category) {
		return allDuplas.stream().filter(dupla -> dupla.getCategory().getId().equals(category.getId())).collect(toList());
	}

	private void comparingAndScoring(List<Dupla> validDuplas) {
		Dupla dupla = validDuplas.get(0);
		for(Dupla otherDupla : validDuplas.subList(1, validDuplas.size())){
			if(dupla.getFinalWord().equals(otherDupla.getFinalWord())){
				dupla.setScore(DUPLICATE_SCORE.getScore());
				otherDupla.setScore(DUPLICATE_SCORE.getScore());
			}
		}
		
		if(dupla.getScore() == null){
			dupla.setScore(UNIQUE_SCORE.getScore());
		}
		
		validDuplas.remove(0);
		if(validDuplas.size() == 1){
			Dupla remainingDupla = validDuplas.get(0); 
			if(remainingDupla.getScore() == null){
				remainingDupla.setScore(UNIQUE_SCORE.getScore());
			}
			return;
		}
		comparingAndScoring(validDuplas);
	}

	public Turn createTurn(Match match, String playerId, List<Dupla> duplas, int time) {
		Turn turn = new Turn();
		turn.setDuplas(duplas);
		Player player = new Player();
		player.setId(new ObjectId(playerId));
		player.setNickname(nicknameFrom(playerId,match));
		turn.setPlayer(player);
		turn.setEndTime(time);
		Round round = match.getLastRound();
		round.addTurn(turn);
		return turn;
	}

	private String nicknameFrom(String playerId, Match match) {
		return match.getPlayerResults().stream().filter(playerResult -> playerResult.getPlayer().getId().toString().equals(playerId))
					.findFirst().get().getPlayer().getNickname();
	}

	public boolean readyToStart() {
		return this.getPlayerResults().size() == this.config.getNumberOfPlayers();
	}

	public List<String> playerIdsExcept(String playerId) {
		return this.getPlayerResults().stream().map(result -> result.getPlayer().getId().toString())
				.filter(id -> !id.equals(playerId)).collect(toList());
	}

	public List<Player> players() {
		return this.getPlayerResults().stream().map(result -> result.getPlayer()).collect(toList());
	}
	
	public List<Player> playersExcept(String playerId) {
		return this.getPlayerResults().stream().map(result -> result.getPlayer())
				.filter(player -> !player.getId().toString().equals(playerId)).collect(toList());
	}
	
	public List<Player> playersThatHaveNotPlayedExcept(String playerid){
		return this.playersExcept(playerid).stream()
		.filter(player -> !this.lastRound.getTurns().stream().anyMatch(turn -> turn.getPlayer().getId().toString().equals(player.getId().toString())))
		.collect(toList());
	}

	public boolean playerHasAlreadyPlayed(String playerId) {
		List<Turn> turns = this.lastRound.getTurns();
		if(isEmpty(turns)){
			return false;
		}
		return turns.stream().anyMatch(turn -> turn.getPlayer().getId().toString().equals(playerId));
	}
	
	public boolean playerHasAccepted(String playerId) {
		return this.getPlayerResults().stream().filter(playerResult -> playerResult.getPlayer().getId().toString().equals(playerId))
											   .findFirst().get().isAccepted();
	}

	public boolean bestBpmbpt(Turn playerTurn) {
		List<Turn> turns = this.getLastRound().getTurns();
		if(turns.size() == 1){
			return true;
		}
		List<Turn> othersTurn = turns.stream()
								.filter(turn -> turn.isBpmbpt() && !turn.getPlayer().getId().toString().equals(playerTurn.getPlayer().getId().toString()))
								.collect(toList());
		if(isEmpty(othersTurn)){
			return true;
		}
		
		for(Turn otherTurn : othersTurn){
			if(otherTurn.getEndTime() < playerTurn.getEndTime()){
				return false;
			}
		}
		
		return true;
	}

	public void addPlayedLetter(Letter letter) {
		if(isEmpty(this.playedLetters)){
			this.playedLetters = new ArrayList<>();
		}
		this.playedLetters.add(letter);
	}
}

class PlayerResultScoreComparator implements Comparator<PlayerResult>{

	@Override
	public int compare(PlayerResult p1, PlayerResult p2) {
		if(p1.getScore() > p2.getScore()){
			return 1;
		}else{
			if(p1.getScore() < p2.getScore()){
				return -1;
			}
		}
		return 0;
	}
	
}

class PlayerResultTimeComparator implements Comparator<PlayerResult>{

	@Override
	public int compare(PlayerResult p1, PlayerResult p2) {
		if(p1.getPlayedTime() > p2.getPlayedTime()){
			return 1;
		}else{
			if(p1.getPlayedTime() < p2.getPlayedTime()){
				return -1;
			}
		}
		return 0;
	}
	
}
