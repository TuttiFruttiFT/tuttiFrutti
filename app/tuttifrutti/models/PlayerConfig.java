package tuttifrutti.models;

import lombok.Getter;
import lombok.Setter;

import org.mongodb.morphia.annotations.Embedded;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rfanego
 */
@Embedded
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlayerConfig {
	private boolean sound;
	private boolean notifications;
	@JsonProperty("notifications_sound")
	private boolean notificationsSound;
}
