package models;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.Mongo;

/**
 * @author rfanego
 */
@Entity
//@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Jugador {
	@Id 
	private ObjectId id;
	
	private String nickname;
	
	private String mail;
	
	@Property("facebook_id")
	private String facebook;
	
	@Property("twitter_id")
	private String twitter;
	
	private Integer saldo;
	
	private String clave;
	
	@Property("partidas_ganadas")
	private Integer partidasGanadas;
	
	@Property("partidas_perdidas")
	private Integer partidasPerdidas;
	
	@Property("mejor_ganadas")
	private Integer mejorPuntaje;
	
	private String imagen;

	private List<String> amigos;
	
	@Property("fecha_ultimo_juego")
	private Date fechaUltimoJuego;
	
	public static Jugador obtenerJugador(String idJugador){
		//TODO implementar
		return null;
	}
	
	public static Jugador validacionMail(String mail,String clave){
		//TODO implementar
		return null;
	}
	
	public static Jugador validacionFacebook(String facebookId){
		//TODO implementar
		return null;
	}
	
	public static Jugador validacionTwitter(String twitterId){
		//TODO implementar
		return null;
	}

	public static Jugador registrarMail(String mail, String clave) {
		// TODO implementar
		String dbName = new String("tutti");
		Mongo mongo = null;
		try {
			mongo = new Mongo( "localhost", 27017 );
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		Morphia morphia = new Morphia();
		Datastore datastore = morphia.createDatastore(mongo, dbName); 
		
		morphia.mapPackage("models");
		
		Jugador jugador = new Jugador();
//		jugador.setNickname(mail);
//		jugador.setClave(clave);
		
		datastore.save(jugador);
		
		return null;
	}

	public static Jugador registrarFacebook(String facebookId) {
		// TODO implementar
		return null;		
	}

	public static Jugador registrarTwitter(String twitterId) {
		// TODO implementar
		return null;
	}

	public static boolean editarPerfil(JsonNode json) {
		// TODO implementar
		return false;
	}

	public static void agregarAmigo(String idJugador, String idAmigo) {
		// TODO implementar
		
	}
	
	public static void powerUp(String idJugador, String idPowerUp) {
		//TODO implementar
	}

	public static void compra(String idJugador, Pack pack) {
		// TODO implementar
		
	}

	public static Jugador obtenerJugadores(String palabraABuscar) {
		// TODO implementar
		return null;
	}

	public static Jugador obtenerOtrosJugadores(String idJugador) {
		// TODO implementar
		return null;
	}
}
