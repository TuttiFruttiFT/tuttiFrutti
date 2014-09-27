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
	
	public static final String QUICK_MODE = "Q";

	public static final String NORMAL_MODE = "N";
	
	public static final String PUBLIC_TYPE = "PUBLIC";

	public static final String PRIVATE_TYPE = "PRIVATE";

	private Integer rounds;
	
	private String type;

	private String mode;
	
	@Property("power_ups_enabled")
	@JsonProperty(value = "powerups_enabled")
	private boolean powerUpsEnabled;
	
	@Property("number_of_players")
	@JsonProperty(value = "number_of_players")
	private Integer numberOfPlayers;
	
	@Property("current_number_of_players")
	@JsonProperty(value = "current_number_of_players")
	private int currentNumberOfPlayers;
	
	private String language;
}
