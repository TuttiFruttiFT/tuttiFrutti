package tuttifrutti.models;

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

import tuttifrutti.models.views.ActiveMatch;
import tuttifrutti.serializers.ObjectIdSerializer;
import tuttifrutti.utils.ElasticUtil;

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
	private List<Player> players;
	
	@Transient
	private List<PowerUp> powerUps;
	
	@Transient
	private Round lastRound;
	
	@Transient
	@Autowired
	private Datastore mongoDatastore;

	public List<ActiveMatch> activeMatches(String idJugador) {
		// TODO implementar, partidas de idJugador que no estén en PARTIDA_FINALIZADA
		return null;
	}

	public Match match(String idPartida) {
		// TODO implementar
		return null;
	}

	public Match findPublicMatch(String playerId, MatchConfig config) {
		return findMatch(playerId, config, MatchConfig.PUBLIC_TYPE);
	}

	public Match findMatch(String playerId, MatchConfig config, String type) {
		Query<Match> query = mongoDatastore.find(Match.class, "config.number_of_players =", config.getNumberOfPlayers());
		query.and(query.criteria("config.language").equal(config.getLanguage()), 
				  query.criteria("config.type").equal(type),
				  query.criteria("config.mode").equal(config.getMode()),
				  query.criteria("players.id").notEqual(new ObjectId(playerId)));
		
		return query.get();
	}

	public Match createPublic(MatchConfig matchConfig) {
		return create(matchConfig, MatchConfig.PUBLIC_TYPE);
	}

	public Match create(MatchConfig matchConfig, String type) {
		Match match = new Match();
		matchConfig.setType(type);
		matchConfig.setPowerUpsEnabled(true);
		matchConfig.setRounds(25);
		match.setConfig(matchConfig);
		match.setName(null); //TODO ver qué poner de nombre
		match.setState(TO_BE_APPROVED);
		match.setStartDate(DateTime.now().toDate());
		match.setCategories(Category.getPublicMatchCategories());
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

	public ResultModel play(String idJugador, List<Dupla> categoriasTurno) {
		for(Dupla categoriaTurno : categoriasTurno){
			ElasticUtil.validar(categoriaTurno);
			this.calcularPuntaje(categoriaTurno);
		}
		
		this.crearTurno(idJugador,categoriasTurno);
		
		return calcularResultado();
	}

	private ResultModel calcularResultado() {
		// TODO implementar
		return null;
	}

	private void crearTurno(String idJugador, List<Dupla> categoriasTurno) {
		// TODO implementar
		
	}

	private void calcularPuntaje(Dupla categoriaTurno) {
		// TODO implementar
		
	}

	public void rejected() {
		// TODO implementar
		
	}

	public void playerReject(String playerId) {
		// TODO implementar
		
	}
}
