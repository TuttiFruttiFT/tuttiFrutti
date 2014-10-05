package tuttifrutti.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author rfanego
 */
@Getter @Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Device {
	private String registrationId;
	
	private String hardwareId;
	
	public Device(String registrationId,String hardwareId){
		this.registrationId = registrationId;
		this.hardwareId = hardwareId;
	}
}
