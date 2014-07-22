package tuttifrutti.models;

import java.util.Date;
import java.util.List;

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

import tuttifrutti.models.views.ActiveMatch;
import tuttifrutti.utils.ElasticUtil;
import tuttifrutti.utils.MongoUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

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
	private ObjectId id;
	
	private String type;
	
	private String state;
	
	private String name;
	
	@Embedded
	private MatchConfig config;
	
	@Property("winner_id")
	private String winnerId;
	
	@Property("start_date")
	private Date startDate;
	
	@Embedded
	private List<Category> categories;
	
	@Embedded
	private List<Player> players;
	
	@Transient
	private List<PowerUp> powerUps;
	
	@Transient
	private Round lastRound;
	
	@Autowired
	private MongoUtil mongoUtil;

	public List<ActiveMatch> activeMatches(String idJugador) {
		// TODO implementar, partidas de idJugador que no estén en PARTIDA_FINALIZADA
		return null;
	}

	public Match match(String idPartida) {
		// TODO implementar
		return null;
	}

	public Match findMatch(Integer numberOfPlayers, String language) {
		// TODO implementar
		Datastore datastore = mongoUtil.getDatastore();
		Query<Match> query = datastore.find(Match.class,"config.players =",numberOfPlayers);
		query.and(query.criteria("config.language").equal(language));
		return null;
	}

	public Match create(Integer cantJugadores, String idioma) {
		
		return null;
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
