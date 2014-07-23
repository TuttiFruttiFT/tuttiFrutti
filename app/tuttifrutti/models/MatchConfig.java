package tuttifrutti.models;

import lombok.Getter;
import lombok.Setter;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

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
public class MatchConfig {
	
	private Integer rounds;
	
	@Property("match_type")
	private String matchType;
	
	@Property("power_ups_enabled")
	private boolean powerUpsEnabled;
	
	@JsonProperty(value="players")
	private Integer numberOfPlayers;
	
	private String language;
}
