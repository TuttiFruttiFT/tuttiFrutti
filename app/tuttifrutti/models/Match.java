package tuttifrutti.models;

import static tuttifrutti.models.MatchConfig.PUBLIC_TYPE;

import java.util.Date;
import java.util.List;

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
		//TODO implementar
		return null;
	}

	private void calculateResult() {
		// TODO hacer las push a los jugadores en el caso de que sea el último turno
	}

	private void createTurn(String idJugador, List<Dupla> categoriasTurno) {
		// TODO implementar
	}

	public void rejected() {
		// TODO implementar
		
	}

	public void playerReject(String playerId) {
		// TODO implementar
		
	}
}
