package tuttifrutti.models;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.joda.time.DateTime.now;
import static org.springframework.util.StringUtils.isEmpty;
import static play.libs.F.Promise.promise;
import static tuttifrutti.models.enums.DuplaScore.ALONE_SCORE;
import static tuttifrutti.models.enums.DuplaScore.DUPLICATE_SCORE;
import static tuttifrutti.models.enums.DuplaScore.UNIQUE_SCORE;
import static tuttifrutti.models.enums.DuplaScore.ZERO_SCORE;
import static tuttifrutti.models.enums.DuplaState.WRONG;
import static tuttifrutti.models.enums.MatchState.FINISHED;
import static tuttifrutti.models.enums.MatchState.OPPONENT_TURN;
import static tuttifrutti.models.enums.MatchState.PLAYER_TURN;
import static tuttifrutti.models.enums.MatchState.REJECTED;
import static tuttifrutti.models.enums.MatchState.TO_BE_APPROVED;
import static tuttifrutti.models.enums.MatchState.WAITING_FOR_OPPONENTS;
import static tuttifrutti.models.enums.MatchType.PRIVATE;
import static tuttifrutti.models.enums.MatchType.PUBLIC;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tuttifrutti.elastic.ElasticUtil;
import tuttifrutti.models.enums.MatchState;
import tuttifrutti.models.enums.MatchType;
import tuttifrutti.models.views.ActiveMatch;
import tuttifrutti.serializers.ObjectIdSerializer;
import tuttifrutti.services.PlayerService;
import tuttifrutti.services.PushService;

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
@Component
public class Match {
	@Id 
	@JsonSerialize(using = ObjectIdSerializer.class)
	private ObjectId id;
	
	private MatchState state;
	
	private String name;
	
	@Embedded
	private MatchConfig config;
	
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
	
	@Transient
	@Autowired
	private Datastore mongoDatastore;
	
	@Transient
	@Autowired
	private Category categoryService;
	
	@Transient
	@Autowired
	private Round roundService;
	
	@Transient
	@Autowired
	private PlayerService playerService;
	
	@Transient
	@Autowired
	private ElasticUtil elasticUtil;
	
	@Transient
	@Autowired
	private PushService pushUtil;

	public List<ActiveMatch> activeMatches(String playerId) {
		List<ActiveMatch> activeMatches = new ArrayList<>();
		Query<Match> query = mongoDatastore.find(Match.class, "state <>", FINISHED.toString());
		query.and(query.criteria("state").notEqual(REJECTED),
				  query.criteria("playerResults.player.id").equal(new ObjectId(playerId)));
		for(Match match : query.asList()){
			ActiveMatch activeMatch = new ActiveMatch();
			Round round = new Round();
			round.setNumber(match.getLastRound().getNumber());
			round.setLetter(match.getLastRound().getLetter());
			activeMatch.setCurrentRound(round);
			activeMatch.setId(match.getId().toString());
			activeMatch.setName(match.getName());
			changeMatchStateDependingOnPlayersGame(playerId, match);
			activeMatch.setState(match.getState().toString());
			activeMatches.add(activeMatch);
		}
		return activeMatches;
	}


	public Match match(String matchId, String playerId) {
		Match match = mongoDatastore.get(Match.class,new ObjectId(matchId));
		if(playerId != null){
			changeMatchStateDependingOnPlayersGame(playerId, match);
		}
		return match;
	}
	
	public Match endedMatch(String matchId){
		Query<Match> query = mongoDatastore.find(Match.class,"state =",MatchState.FINISHED.toString());
		query.and(query.criteria("id").equal(new ObjectId(matchId)));
		return query.get();
	}
	
	private void changeMatchStateDependingOnPlayersGame(String playerId,Match match) {
		boolean playerHasAlreadyPlayed = playerHasAlreadyPlayed(match, playerId);
		MatchState matchState = match.getState();
		if(matchState.equals(TO_BE_APPROVED) && playerHasAlreadyPlayed){
			match.setState(WAITING_FOR_OPPONENTS);
		}
		if(!matchState.equals(TO_BE_APPROVED) && playerHasAlreadyPlayed){
			match.setState(OPPONENT_TURN);
		}
	}

	public Match findPublicMatch(String playerId, MatchConfig config) {
		return findMatch(playerId, config, PUBLIC);
	}

	public Match findMatch(String playerId, MatchConfig config, MatchType type) {
		Query<Match> query = mongoDatastore.find(Match.class, "config.number_of_players =", config.getNumberOfPlayers());
		query.and(query.criteria("config.language").equal(config.getLanguage()), 
				  query.criteria("config.type").equal(type.toString()),
				  query.criteria("config.mode").equal(config.getMode()),
				  query.criteria("state").equal(TO_BE_APPROVED),
				  query.criteria("playerResults.player.id").notEqual(new ObjectId(playerId)));
		
		return query.get();
	}

	public Match createPublic(MatchConfig config) {
		return create(config, PUBLIC);
	}

	public void addPlayer(Match match, String playerId) {
		PlayerResult playerResult = new PlayerResult();
		Player player = playerService.player(playerId);
		if(player == null){
			throw new RuntimeException("Player " + playerId + " does not exist");
		}
		playerResult.setPlayer(player);
		playerResult.setScore(0);
		match.getPlayerResults().add(playerResult);
	}

	public Match createPrivate(String playerId, MatchConfig config, List<String> playerIds, List<String> categoryIds) {
		return create(config, PRIVATE,playerService.playerResultsFromIds(playerIds),categoryService.categoriesFromIds(categoryIds));
	}

	public List<Dupla> play(Match match, String playerId, List<Dupla> duplas, int time) {		
		Round round = match.getLastRound();
		elasticUtil.validate(duplas,round.getLetter());
		
		this.createTurn(match,playerId, duplas, time);
		
		mongoDatastore.save(match);
		
		calculateResult(match);
		
		return getWrongDuplas(duplas);
	}

	private List<Dupla> getWrongDuplas(List<Dupla> duplas) {
		return duplas.stream().filter(dupla -> dupla.getState().equals(WRONG)).collect(toList());
	}

	private void calculateResult(Match match) {
		if(match.isRoundOver()){
			promise(() -> {				
				Round round = match.getLastRound();
				List<Turn> turns = round.getTurns();
				
				Integer minTime = getMinimumTime(turns);
				
				List<Dupla> allDuplas = flatDuplasFromTurns(turns);
				
				List<Dupla> validDuplas = processInvalidDuplas(minTime,allDuplas);
				
				processValidDuplas(match, validDuplas);
				
				calculateTurnScores(turns, match);
				
				saveOldRound(match, round, minTime);
				
				if(matchIsFinished(match, round)){
					match.calculateWinner();
					match.setState(FINISHED);
					pushUtil.matchResult(match);
				}else{				
					match.setState(PLAYER_TURN);
					roundService.create(match);
					pushUtil.roundResult(match,round.getNumber());
				}
				mongoDatastore.save(match);
				return null;
			});
		}
	}

	private boolean isRoundOver() {
		return this.getLastRound().getTurns().size() == this.getConfig().getCurrentTotalNumberOfPlayers();
	}


	private void calculateWinner() {
		this.winnerId = playerResults.stream().max(new PlayerResultComparator()::compare).get().getPlayer().getId().toString();
	}

	private boolean matchIsFinished(Match match, Round round) {
		return round.getNumber() == match.getConfig().getRounds();
	}

	private void processValidDuplas(Match match, List<Dupla> validDuplas) {
		for(Category category : match.getCategories()){
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

	private List<Dupla> processInvalidDuplas(Integer minTime,List<Dupla> allDuplas) {
		scoreEmptyWrongAndOutOfTimeDuplas(allDuplas, minTime);
		List<Dupla> filteredDuplas = filterEmptyWrongAndOutOfTimeDuplas(allDuplas, minTime);
		return filteredDuplas;
	}

	private void saveOldRound(Match match, Round round,Integer minTime) {
		round.setEndTime(minTime);
		round.setStopPlayer(getStopPlayer(round.getTurns(),minTime, match));
		round.setMatchId(match.getId().toString());
		mongoDatastore.save(round);
	}
	
	public List<String> playerIds() {
		return playerResults.stream().map(playerResult -> playerResult.getPlayer().getId().toString()).collect(toList());
	}

	private Match create(MatchConfig config, MatchType type,List<PlayerResult> playerResults,List<Category> categories) {
		Match match = new Match();
		config.setType(type);
		config.setCurrentTotalNumberOfPlayers(config.getNumberOfPlayers());
		match.setConfig(config);
		match.setName("Nombre Partida"); //TODO ver qué poner de nombre
		match.setState(TO_BE_APPROVED);
		match.setStartDate(now().toDate());
		match.setCategories(categories);
		match.setPlayerResults(playerResults);
		roundService.create(match);
		return match;
	}

	private Match create(MatchConfig config, MatchType type) {
		return create(config, type, new ArrayList<PlayerResult>(), categoryService.getPublicMatchCategories(config.getLanguage()));
	}
	
	private void calculateTurnScores(List<Turn> turns, Match match) {
		for(Turn turn : turns){
			int turnScore = turn.getDuplas().stream().mapToInt(dupla -> dupla.getScore()).sum();
			turn.setScore(turnScore);
			PlayerResult playerResult = match.getPlayerResults().stream().filter(player -> player.getPlayer().getId().toString().equals(turn.getPlayer().getId().toString())).findFirst().get();
			playerResult.setScore(playerResult.getScore() + turnScore);
		}
	}

	private Player getStopPlayer(List<Turn> turns, Integer minTime, Match match) {
		return turns.stream().filter(turn -> turn.getEndTime() == minTime).findFirst().get().getPlayer();
	}

	private Integer getMinimumTime(List<Turn> turns) {
		return turns.stream().mapToInt(turn -> turn.getEndTime()).min().getAsInt();
	}

	private List<Dupla> flatDuplasFromTurns(List<Turn> turns) {
		List<Dupla> allDuplas = new ArrayList<>();
		turns.stream().forEach(turn -> allDuplas.addAll(turn.getDuplas()));
		return allDuplas;
	}

	private void scoreEmptyWrongAndOutOfTimeDuplas(List<Dupla> categoryDuplas, Integer minTime) {
		categoryDuplas.stream().filter(emptyWrongAndOutOfTime(minTime)).forEach(dupla -> dupla.setScore(ZERO_SCORE.getScore()));
	}
	
	private List<Dupla> filterEmptyWrongAndOutOfTimeDuplas(List<Dupla> allDuplas,Integer minTime) {
		return allDuplas.stream().filter(emptyWrongAndOutOfTime(minTime).negate()).collect(toList());
	}

	private Predicate<? super Dupla> emptyWrongAndOutOfTime(Integer minTime) {
		return dupla -> (dupla.getState().equals(WRONG) || isEmpty(dupla.getWrittenWord()) || dupla.getTime() > minTime);
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

	private void createTurn(Match match, String playerId, List<Dupla> duplas, int time) {
		Turn turn = new Turn();
		turn.setDuplas(duplas);
		Player player = new Player();
		player.setId(new ObjectId(playerId));
		player.setNickname(nicknameFrom(playerId,match));
		turn.setPlayer(player);
		turn.setEndTime(time);
		Round round = match.getLastRound();
		round.addTurn(turn);
	}

	public boolean playerReject(String playerId, Match match) {
		Iterator<PlayerResult> it = match.getPlayerResults().iterator();
		Player rejectorPlayer = null;
		
		while(it.hasNext() && rejectorPlayer == null){
			PlayerResult playerResult = it.next();
			
			Player player = playerResult.getPlayer();
			if(player.getId().toString().equals(playerId)){
				rejectorPlayer = new Player(player.getId(),player.getNickname());
				it.remove();
			}
		}
		
		if(rejectorPlayer != null){			
			List<Player> playerIds = match.players();
			
			if(playerIds.size() == 1){
				match.setState(REJECTED);
				pushUtil.rejected(playerIds,match);
			}else{
				match.getConfig().setCurrentTotalNumberOfPlayers(match.getConfig().getCurrentTotalNumberOfPlayers() - 1);
				if(match.getLastRound().getTurns().size() == match.getConfig().getCurrentTotalNumberOfPlayers()){
					this.calculateResult(match);
				}
				pushUtil.rejectedByPlayer(rejectorPlayer,match);
			}
			mongoDatastore.save(match);
			return true;
		}
		return false;		
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

	public boolean playerHasAlreadyPlayed(String playerId) {
		List<Turn> turns = this.lastRound.getTurns();
		if(isEmpty(turns)){
			return false;
		}
		return turns.stream().anyMatch(turn -> turn.getPlayer().getId().toString().equals(playerId));
	}
	
	private boolean playerHasAlreadyPlayed(Match match, String playerId) {
		List<Turn> turns = match.getLastRound().getTurns();
		return isNotEmpty(turns) && turns.stream().anyMatch(turn -> turn.getPlayer().getId().toString().equals(playerId));
	}

	public void privateMatchReady(Match match, List<Player> players) {
		pushUtil.privateMatchReady(match, players);
	}
}

class PlayerResultComparator implements Comparator<PlayerResult>{

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
