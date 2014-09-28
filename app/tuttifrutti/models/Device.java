/**
 * 
 */
package tuttifrutti.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author rfanego
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class Device {
	@Id
	private String playerId;
	
	@Property("push_token")
	private String pushToken;
	
	@Property("hwid")
	private String hardwareId;
	
	@Autowired
	private Datastore mongoDatastore;
	
	public Device(String playerId,String pushToken,String hardwareId){
		this.playerId = playerId;
		this.pushToken = pushToken;
		this.hardwareId = hardwareId;
	}
	
	public Device device(String playerId){
		return mongoDatastore.get(Device.class,playerId);
	}
	
}
