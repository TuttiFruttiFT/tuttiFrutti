package models;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import utils.MongoUtil;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
@Entity
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {
	@Id 
	private ObjectId id;
	
	private String nickname;
	
	private String mail;
	
	@Property("facebook_id")
	private String facebookId;
	
	@Property("twitter_id")
	private String twitterId;
	
	private Integer balance;
	
	private String password;
	
	private Integer won;
	
	private Integer lost;
	
	private Integer best;
	
	private String image;

	private List<String> friends;
	
	private Date last;
	
	public static Player player(String idJugador){
		//TODO implementar
		return null;
	}
	
	public static Player validateMail(String mail,String clave){
		//TODO implementar
		return null;
	}
	
	public static Player validateFacebook(String facebookId){
		//TODO implementar
		return null;
	}
	
	public static Player validateTwitter(String twitterId){
		//TODO implementar
		return null;
	}

	public static Player registerMail(String mail, String clave) {
		// TODO implementar
		
		Datastore datastore = MongoUtil.getDatastore(); 
		
		Player jugador = new Player();
		jugador.setNickname(mail);
		jugador.setPassword(clave);
		
		datastore.save(jugador);
		
		return jugador;
	}

	public static Player registerFacebook(String facebookId) {
		// TODO implementar
		return null;		
	}

	public static Player registerTwitter(String twitterId) {
		// TODO implementar
		return null;
	}

	public static boolean editarPerfil(JsonNode json) {
		// TODO implementar
		return false;
	}

	public static void addFriend(String idJugador, String idAmigo) {
		// TODO implementar
		
	}
	
	public static void powerUp(String idJugador, String idPowerUp) {
		//TODO implementar
	}

	public static void buy(String idJugador, Pack pack) {
		// TODO implementar
		
	}

	public static Player getPlayers(String palabraABuscar) {
		// TODO implementar
		return null;
	}

	public static Player getOthersPlayers(String idJugador) {
		// TODO implementar
		return null;
	}
}
