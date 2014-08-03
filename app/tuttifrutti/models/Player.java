package tuttifrutti.models;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
@Component
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
	
	@Autowired
	private Datastore mongoDatastore;
	
	public Player player(String idJugador){
		//TODO implementar
		return null;
	}
	
	public Player validateMail(String mail,String clave){
		//TODO implementar
		return null;
	}
	
	public Player validateFacebook(String facebookId){
		//TODO implementar
		return null;
	}
	
	public Player validateTwitter(String twitterId){
		//TODO implementar
		return null;
	}

	public Player registerMail(String mail, String clave) {
		// TODO implementar
		Player player = new Player();
		player.setNickname(mail);
		player.setPassword(clave);
		
		mongoDatastore.save(player);
		
		return player;
	}

	public Player registerFacebook(String facebookId) {
		// TODO implementar
		return null;		
	}

	public Player registerTwitter(String twitterId) {
		// TODO implementar
		return null;
	}

	public boolean editarPerfil(JsonNode json) {
		// TODO implementar
		return false;
	}

	public void addFriend(String idJugador, String idAmigo) {
		// TODO implementar
		
	}
	
	public void powerUp(String idJugador, String idPowerUp) {
		//TODO implementar
	}

	public void buy(String idJugador, Pack pack) {
		// TODO implementar
		
	}

	public Player getPlayers(String palabraABuscar) {
		// TODO implementar
		return null;
	}

	public Player getOthersPlayers(String idJugador) {
		// TODO implementar
		return null;
	}
}
