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
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tuttifrutti.serializers.ObjectIdSerializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

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
	@JsonSerialize(using = ObjectIdSerializer.class)
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
	
	@JsonSerialize(using = DateSerializer.class)
	@JsonDeserialize(using = DateDeserializer.class)
	private Date last;
	
	@Autowired
	private Datastore mongoDatastore;
	
	public Player player(String playerId){
		return mongoDatastore.get(Player.class,new ObjectId(playerId));
	}
	
	public Boolean validateMail(String mail,String clave){
		//TODO implementar
		return true;
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
		String[] splittedMail = mail.split("@");
		player.setNickname((splittedMail.length > 0) ? splittedMail[0] : mail);
		player.setMail(mail);
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

	public boolean editProfile(JsonNode json) {
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

	public List<Player> searchPlayers(String palabraABuscar) {
		return mongoDatastore.find(Player.class).asList();
	}

	public List<Player> searchOthersPlayers(String idJugador) {
		// TODO implementar
		return null;
	}

	public Player search(String mail) {
		Query<Player> query = mongoDatastore.find(Player.class, "mail =", mail);
		return query.get();
	}
}
