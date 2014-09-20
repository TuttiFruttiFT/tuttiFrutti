package tuttifrutti.models;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.StringUtils.isEmpty;
import static tuttifrutti.models.DuplaState.WRONG;
import static tuttifrutti.models.MatchConfig.PUBLIC_TYPE;
import static tuttifrutti.models.MatchState.FINISHED;
import static tuttifrutti.models.MatchState.TO_BE_APPROVED;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
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
import tuttifrutti.utils.PushUtil;

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
	private static final int ALONE_SCORE = 20;
	private static final int UNIQUE_SCORE = 10;
	private static final int DUPLICATE_SCORE = 5;
	private static final int ZERO_SCORE = 0;
	
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
	private List<PlayerResult> players;
	
	@Transient
	private List<PowerUp> powerUps;
	
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
	private Player playerService;
	
	@Transient
	@Autowired
	private ElasticUtil elasticUtil;
	
	@Transient
	@Autowired
	private PushUtil pushUtil;

	public List<ActiveMatch> activeMatches(String playerId) {
		// TODO implementar, partidas de idJugador que no estén en FINISHED
		List<ActiveMatch> activeMatches = new ArrayList<>();
		Query<Match> query = mongoDatastore.find(Match.class, "state <>", FINISHED.toString());
		query.and(query.criteria("players.player.id").equal(new ObjectId(playerId)));
		for(Match match : query.asList()){
			ActiveMatch activeMatch = new ActiveMatch();
			activeMatch.setCurrentRound(match.getLastRound());
			activeMatch.setMatchId(match.getId().toString());
			activeMatch.setName(match.getName());
			activeMatch.setState(match.getState().toString());
			activeMatches.add(activeMatch);
		}
		return activeMatches;
	}

	public Match match(String matchId) {
		return mongoDatastore.get(Match.class,new ObjectId(matchId));
	}

	public Match findPublicMatch(String playerId, MatchConfig config) {
		return findMatch(playerId, config, PUBLIC_TYPE);
	}

	public Match findMatch(String playerId, MatchConfig config, String type) {
		Query<Match> query = mongoDatastore.find(Match.class, "config.number_of_players =", config.getNumberOfPlayers());
		query.and(query.criteria("config.language").equal(config.getLanguage()), 
				  query.criteria("config.type").equal(type),
				  query.criteria("config.mode").equal(config.getMode()),
				  query.criteria("players.player.id").notEqual(new ObjectId(playerId)));
		
		return query.get();
	}

	public Match createPublic(MatchConfig matchConfig) {
		return create(matchConfig, PUBLIC_TYPE);
	}

	public Match create(MatchConfig config, String type) {
		Match match = new Match();
		config.setType(type);
		config.setPowerUpsEnabled(true);
		config.setRounds(25);
		match.setConfig(config);
		match.setName("Nombre Partida"); //TODO ver qué poner de nombre
		match.setState(TO_BE_APPROVED);
		match.setStartDate(DateTime.now().toDate());
		match.setCategories(categoryService.getPublicMatchCategories(config.getLanguage()));
		match.setPlayers(new ArrayList<>());
		roundService.create(match);
		return match;
	}

	public void addPlayer(Match match, String playerId) {
		PlayerResult playerResult = new PlayerResult();
		Player player = playerService.player(playerId);
		if(player == null){
			throw new RuntimeException("Player " + playerId + " does not exist");
		}
		playerResult.setPlayer(player);
		playerResult.setScore(0);
		match.getPlayers().add(playerResult);
	}

	public Match create(String idJugador, MatchConfig configuracion, List<String> jugadores) {
		// TODO implementar
		return null;
	}

	public List<Dupla> play(Match match, String idJugador, List<Dupla> duplas, int time) {		
		elasticUtil.validar(duplas,match.getLastRound().getLetter());
		
		this.createTurn(match,idJugador, duplas, time);
		
		calculateResult(match);
		
		return getWrongDuplas(duplas);
	}

	private List<Dupla> getWrongDuplas(List<Dupla> duplas) {
		return duplas.stream().filter(dupla -> dupla.getState().equals(WRONG)).collect(toList());
	}

	private void calculateResult(Match match) {
		Round round = match.getLastRound();
		List<Turn> turns = round.getTurns();
		if(turns.size() == match.getPlayers().size()){
			Integer minTime = getMinimumTime(turns); 
			List<Dupla> allDuplas = flatDuplasFromTurns(turns);
			scoreEmptyWrongAndOutOfTimeDuplas(allDuplas, minTime);
			List<Dupla> filteredDuplas = filterEmptyWrongAndOutOfTimeDuplas(allDuplas, minTime);
			for(Category category : match.getCategories()){
				List<Dupla> categoryDuplas = getDuplasByCategory(filteredDuplas, category);
				
				if(!categoryDuplas.isEmpty()){
					if(categoryDuplas.size() == 1){
						categoryDuplas.get(0).setScore(ALONE_SCORE);
					}else{
						comparingAndScoring(categoryDuplas);
					}
				}
			}
			
			calculateTurnScores(turns, match);
			
			round.setEndTime(minTime);
			round.setStopPlayer(getStopPlayer(round.getTurns(),minTime, match));
			round.setMatchId(match.getId().toString());
			mongoDatastore.save(round);
			roundService.create(match);
			mongoDatastore.save(match);
			pushUtil.roundResult(match.getId().toString(),round.getNumber(),playerIds(match.getPlayers()));
		}
	}

	private List<String> playerIds(List<PlayerResult> players) {
		return players.stream().map(playerResult -> playerResult.getPlayer().getId().toString()).collect(toList());
	}

	private void calculateTurnScores(List<Turn> turns, Match match) {
		for(Turn turn : turns){
			int turnScore = turn.getDuplas().stream().mapToInt(dupla -> dupla.getScore()).sum();
			turn.setScore(turnScore);
			PlayerResult playerResult = match.getPlayers().stream().filter(player -> player.getPlayer().getId().toString().equals(turn.getPlayerId())).findFirst().get();
			playerResult.setScore(playerResult.getScore() + turnScore);
		}
	}

	private StopPlayer getStopPlayer(List<Turn> turns, Integer minTime, Match match) {
		StopPlayer stopPlayer = new StopPlayer();
		String playerId = turns.stream().filter(turn -> turn.getEndTime() == minTime).findFirst().get().getPlayerId();
		PlayerResult playerResult = match.getPlayers().stream().filter(player -> player.getPlayer().getId().toString().equals(playerId))
									.findFirst().get();
		stopPlayer.setStopPlayerId(playerId);
		stopPlayer.setNickname(playerResult.getPlayer().getNickname());
		return stopPlayer;
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
		turn.setPlayerId(playerId);
		turn.setEndTime(time);
		Round round = match.getLastRound();
		round.addTurn(turn);
		mongoDatastore.save(match);
	}

	public void rejected() {
		// TODO implementar
		
	}

	public void playerReject(String playerId) {
		// TODO implementar
		
	}

	public boolean readyToStart() {
		return this.getPlayers().size() == this.config.getNumberOfPlayers();
	}
}
