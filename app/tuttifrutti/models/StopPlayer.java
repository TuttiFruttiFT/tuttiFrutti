/**
 * 
 */
package tuttifrutti.models;

import org.mongodb.morphia.annotations.Embedded;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rfanego
 */
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Embedded
public class StopPlayer{
	@JsonProperty(value = "player_id")
	private String stopPlayerId;
	private String nickname;
}
