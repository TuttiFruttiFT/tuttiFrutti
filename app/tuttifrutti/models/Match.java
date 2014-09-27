package tuttifrutti.models;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.joda.time.DateTime.now;
import static org.springframework.util.StringUtils.isEmpty;
import static play.libs.F.Promise.promise;
import static tuttifrutti.models.DuplaScore.ALONE_SCORE;
import static tuttifrutti.models.DuplaScore.DUPLICATE_SCORE;
import static tuttifrutti.models.DuplaScore.UNIQUE_SCORE;
import static tuttifrutti.models.DuplaScore.ZERO_SCORE;
import static tuttifrutti.models.DuplaState.WRONG;
import static tuttifrutti.models.MatchState.FINISHED;
import static tuttifrutti.models.MatchState.OPPONENT_TURN;
import static tuttifrutti.models.MatchState.REJECTED;
import static tuttifrutti.models.MatchState.TO_BE_APPROVED;
import static tuttifrutti.models.MatchState.WAITING_FOR_OPPONENTS;
import static tuttifrutti.models.MatchType.PRIVATE_TYPE;
import static tuttifrutti.models.MatchType.PUBLIC_TYPE;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
	
	@Transient
	private List<PowerUp> powerUps;
	
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
			activeMatch.setCurrentRound(match.getLastRound());
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
	
	private void changeMatchStateDependingOnPlayersGame(String playerId,Match match) {
		if(!match.getState().equals(TO_BE_APPROVED) && playerHasAlreadyPlayed(match, playerId)){
			match.setState(OPPONENT_TURN);
		}
	}

	public Match findPublicMatch(String playerId, MatchConfig config) {
		return findMatch(playerId, config, PUBLIC_TYPE);
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
		return create(config, PUBLIC_TYPE);
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
		match.getConfig().setCurrentNumberOfPlayers(match.getConfig().getCurrentNumberOfPlayers() + 1);
	}

	public Match createPrivate(String playerId, MatchConfig config, List<String> playerIds, List<String> categoryIds) {
		return create(config, PRIVATE_TYPE,playerService.playersFromIds(playerIds),categoryService.categoriesFromIds(categoryIds));
	}

	public List<Dupla> play(Match match, String playerId, List<Dupla> duplas, int time) {		
		Round round = match.getLastRound();
		elasticUtil.validar(duplas,round.getLetter());
		
		this.createTurn(match,playerId, duplas, time);
		
		if(round.getNumber() == 1 && match.thereAreMissingOpponents()){
			match.setState(WAITING_FOR_OPPONENTS);
		}
		
		mongoDatastore.save(match);
		
		promise(() -> {
			calculateResult(match);
			return null;
		});
		
		return getWrongDuplas(duplas);
	}

	private List<Dupla> getWrongDuplas(List<Dupla> duplas) {
		return duplas.stream().filter(dupla -> dupla.getState().equals(WRONG)).collect(toList());
	}

	private void calculateResult(Match match) {
		if(match.isRoundOver()){
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
				roundService.create(match);
				pushUtil.roundResult(match,round.getNumber());
			}
			mongoDatastore.save(match);
		}
	}

	private boolean isRoundOver() {
		return this.getLastRound().getTurns().size() == this.getConfig().getCurrentNumberOfPlayers();
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
					categoryDuplas.get(0).setScore(ALONE_SCORE);
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

	private Match create(MatchConfig config, MatchType type,List<Category> categories,List<PlayerResult> playerResults) {
		Match match = new Match();
		config.setType(type);
		config.setPowerUpsEnabled(true);
		config.setRounds(25);
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
		return this.create(config, type, categoryService.getPublicMatchCategories(config.getLanguage()), new ArrayList<>());
	}
	
	private void calculateTurnScores(List<Turn> turns, Match match) {
		for(Turn turn : turns){
			int turnScore = turn.getDuplas().stream().mapToInt(dupla -> dupla.getScore().getScore()).sum();
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
		categoryDuplas.stream().filter(emptyWrongAndOutOfTime(minTime)).forEach(dupla -> dupla.setScore(ZERO_SCORE));
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
				dupla.setScore(DUPLICATE_SCORE);
				otherDupla.setScore(DUPLICATE_SCORE);
			}
		}
		
		if(dupla.getScore() == null){
			dupla.setScore(UNIQUE_SCORE);
		}
		
		validDuplas.remove(0);
		if(validDuplas.size() == 1){
			Dupla remainingDupla = validDuplas.get(0); 
			if(remainingDupla.getScore() == null){
				remainingDupla.setScore(UNIQUE_SCORE);
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

	private boolean thereAreMissingOpponents() {
		MatchConfig config = this.getConfig();
		return config.getCurrentNumberOfPlayers() < config.getNumberOfPlayers();
	}


	public void rejected() {
		pushUtil.rejected(this.players(),this);
	}

	public void playerReject(String playerId) {
		List<Player> players = this.players();
		
		if(players.size() == 1){
			pushUtil.rejected(players,this);
		}else{
			pushUtil.rejectedByPlayer(players,playerId,this);
		}
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
				.filter(id -> !id.equals(playerId)).collect(Collectors.toList());
	}

	public List<Player> players() {
		return this.getPlayerResults().stream().map(result -> result.getPlayer()).collect(Collectors.toList());
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


	public void publicMatchReady(String playerId, Match match) {
		pushUtil.publicMatchReady(match.playerIdsExcept(playerId),match);
	}


	public void privateMatchReady(List<String> players, Match match) {
		pushUtil.privateMatchReady(players, match);
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
