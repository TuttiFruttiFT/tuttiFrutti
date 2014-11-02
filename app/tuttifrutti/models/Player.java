package tuttifrutti.models;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import tuttifrutti.serializers.ObjectIdSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

/**
 * @author rfanego
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Player {
	@Id 
	@JsonSerialize(using = ObjectIdSerializer.class)
	private ObjectId id;
	
	private String nickname;
	
	private String mail;
	
	@Property("facebook_id")
	@JsonProperty("facebook_id")
	private String facebookId;
	
	@Property("twitter_id")
	@JsonProperty("twitter_id")
	private String twitterId;
		
	@JsonIgnore
	private String password;
	
	private int balance;
	
	private int won;
	
	private int lost;
	
	private int best;
	
	private String image;

	private List<Player> friends;
	
	@JsonIgnore
	private List<Device> devices;
	
	@JsonSerialize(using = DateSerializer.class)
	@JsonDeserialize(using = DateDeserializer.class)
	private Date last;
	
	public Player(ObjectId id,String nickname){
		this.id = id;
		this.nickname = nickname;
	}

	public Player reducedPlayer() {
		Player reducedPlayer = new Player();
		reducedPlayer.setId(this.getId());
		reducedPlayer.setNickname(this.getNickname());
		reducedPlayer.setImage(this.getImage());
		return reducedPlayer;
	}

	public void addFriend(Player friend) {
		if(isEmpty(this.friends)){
			this.friends = new ArrayList<>();
		}
		this.friends.add(friend);
	}
}
