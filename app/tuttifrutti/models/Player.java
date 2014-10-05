package tuttifrutti.models;

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
	
	@JsonIgnore
	private List<Device> devices;
	
	@JsonSerialize(using = DateSerializer.class)
	@JsonDeserialize(using = DateDeserializer.class)
	private Date last;
	
	public Player(ObjectId id,String nickname){
		this.id = id;
		this.nickname = nickname;
	}
}
