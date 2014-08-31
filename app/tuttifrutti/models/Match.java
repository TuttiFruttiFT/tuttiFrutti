package tuttifrutti.models;

import static java.util.stream.Collectors.toList;
import static tuttifrutti.models.DuplaState.WRONG;
import static tuttifrutti.models.MatchConfig.PUBLIC_TYPE;

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
import org.springframework.util.StringUtils;

import tuttifrutti.elastic.ElasticUtil;
import tuttifrutti.models.views.ActiveMatch;
import tuttifrutti.serializers.ObjectIdSerializer;

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
	public static final String TO_BE_APPROVED = "TO_BE_APPROVED";
	public static final String PLAYER_TURN = "PLAYER_TURN";
	public static final String OPPONENT_TURN = "OPPONENT_TURN";
	public static final String FINISHED = "FINISHED";
	public static final String REJECTED = "REJECTED";
	
	@Id 
	@JsonSerialize(using = ObjectIdSerializer.class)
	private ObjectId id;
	
	private String state;
	
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
	
	@Autowired
	private Category categoryService;
	
	@Autowired
	private Round roundService;
	
	@Autowired
	private ElasticUtil elasticUtil;

	public List<ActiveMatch> activeMatches(String idJugador) {
		// TODO implementar, partidas de idJugador que no estén en PARTIDA_FINALIZADA
		return null;
	}

	public Match match(String matchId) {
		return mongoDatastore.get(Match.class,matchId);
	}

	public Match findPublicMatch(String playerId, MatchConfig config) {
		return findMatch(playerId, config, PUBLIC_TYPE);
	}

	public Match findMatch(String playerId, MatchConfig config, String type) {
		Query<Match> query = mongoDatastore.find(Match.class, "config.number_of_players =", config.getNumberOfPlayers());
		Player player = new Player();
		player.setId(new ObjectId(playerId));
		query.and(query.criteria("config.language").equal(config.getLanguage()), 
				  query.criteria("config.type").equal(type),
				  query.criteria("config.mode").equal(config.getMode()),
				  query.criteria("players").not().hasThisElement(player));
		
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
		match.setName(null); //TODO ver qué poner de nombre
		match.setState(TO_BE_APPROVED);
		match.setStartDate(DateTime.now().toDate());
		match.setCategories(categoryService.getPublicMatchCategories(config.getLanguage()));
		roundService.create(match);
		mongoDatastore.save(match);
		return match;
	}

	public void addPlayer(String idJugador) {
		// TODO implementar
		
	}

	public Match create(String idJugador, MatchConfig configuracion, List<String> jugadores) {
		// TODO implementar
		return null;
	}

	public List<Dupla> play(String idJugador, List<Dupla> duplas) {		
		elasticUtil.validar(duplas,roundService.getLetter());
		
		this.createTurn(idJugador,duplas);
		
		calculateResult();
		
		return getWrongDuplas(duplas);
	}

	private List<Dupla> getWrongDuplas(List<Dupla> duplas) {
		return duplas.stream().filter(dupla -> dupla.getState().equals(WRONG)).collect(toList());
	}

	private void calculateResult() {
		List<Turn> turns = lastRound.getTurns();
		if(turns.size() == players.size()){
			Integer minTime = getMinimumTime(turns); 
			List<Dupla> allDuplas = flatDuplasFromTurns(turns);
			scoreEmptyWrongAndOutOfTimeDuplas(allDuplas, minTime);
			List<Dupla> filteredDuplas = filterEmptyWrongAndOutOfTimeDuplas(allDuplas, minTime);
			for(Category category : categories){
				List<Dupla> categoryDuplas = getDuplasByCategory(filteredDuplas, category);
				
				List<Dupla> validDuplas = getValidDuplas(categoryDuplas);
				if(!validDuplas.isEmpty()){
					if(validDuplas.size() == 1){
						validDuplas.get(0).setScore(ALONE_SCORE);
					}else{
						comparingAndScoring(validDuplas);
					}
				}
			}
			
			calculateTurnScores(turns);
			
			lastRound.setEndTime(minTime);
			lastRound.setStopPlayerId(getStopPlayerId(lastRound.getTurns(),minTime));
			// TODO hacer las push a los jugadores
		}
	}


	private void calculateTurnScores(List<Turn> turns) {
		for(Turn turn : turns){
			int turnScore = turn.getDuplas().stream().mapToInt(dupla -> dupla.getScore()).sum();
			turn.setScore(turnScore);
			PlayerResult playerResult = players.stream().filter(player -> player.getPlayer().getId().toString().equals(turn.getPlayerId())).findFirst().get();
			playerResult.setScore(playerResult.getScore() + turnScore);
		}
	}

	private String getStopPlayerId(List<Turn> turns, Integer minTime) {
		return turns.stream().filter(turn -> turn.getEndTime() == minTime).findFirst().get().getPlayerId();
	}

	private Integer getMinimumTime(List<Turn> turns) {
		return turns.stream().map(turn -> turn.getScore()).min(Integer::compareTo).get();
	}

	private List<Dupla> flatDuplasFromTurns(List<Turn> turns) {
		List<Dupla> allDuplas = new ArrayList<>();
		turns.stream().forEach(turn -> allDuplas.addAll(turn.getDuplas()));
		return allDuplas;
	}

	private List<Dupla> getValidDuplas(List<Dupla> categoryDuplas) {
		return categoryDuplas.stream().filter(dupla -> !dupla.getState().equals(WRONG)).collect(toList());
	}

	private void scoreEmptyWrongAndOutOfTimeDuplas(List<Dupla> categoryDuplas, Integer minTime) {
		categoryDuplas.stream().filter(emptyWrongAndOutOfTime(minTime)).forEach(dupla -> dupla.setScore(ZERO_SCORE));
	}
	
	private List<Dupla> filterEmptyWrongAndOutOfTimeDuplas(List<Dupla> allDuplas,Integer minTime) {
		return allDuplas.stream().filter(emptyWrongAndOutOfTime(minTime)).collect(toList());
	}

	private Predicate<? super Dupla> emptyWrongAndOutOfTime(Integer minTime) {
		return dupla -> dupla.getState().equals(WRONG) || StringUtils.isEmpty(dupla.getWrittenWord()) || dupla.getTime() > minTime;
	}

	private List<Dupla> getDuplasByCategory(List<Dupla> allDuplas,Category category) {
		return allDuplas.stream().filter(dupla -> dupla.getCategory().getId().equals(category.getId())).collect(toList());
	}

	private void comparingAndScoring(List<Dupla> validDuplas) {
		Dupla dupla = validDuplas.get(0);
		for(Dupla otherDupla : validDuplas.subList(1, validDuplas.size() - 1)){
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
			if(validDuplas.get(0).getScore() == null){
				dupla.setScore(UNIQUE_SCORE);
			}
			return;
		}
		comparingAndScoring(validDuplas);
	}

	private void createTurn(String playerId, List<Dupla> duplas) {
		Turn turn = new Turn();
		turn.setDuplas(duplas);
		turn.setPlayerId(playerId);
		lastRound.addTurn(turn);
		mongoDatastore.save(lastRound);
	}

	public void rejected() {
		// TODO implementar
		
	}

	public void playerReject(String playerId) {
		// TODO implementar
		
	}
}
