package tuttifrutti.models;

import lombok.Getter;
import lombok.Setter;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import tuttifrutti.models.enums.MatchMode;
import tuttifrutti.models.enums.MatchType;

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

	private MatchType type;

	private MatchMode mode;
	
	@Property("power_ups_enabled")
	@JsonProperty(value = "powerups_enabled")
	private boolean powerUpsEnabled;
	
	@Property("number_of_players")
	@JsonProperty(value = "number_of_players")
	private Integer numberOfPlayers;
	
	@Property("current_total_number_of_players")
	@JsonProperty(value = "current_total_number_of_players")
	private int currentTotalNumberOfPlayers;
	
	@Property("incorporated_number_of_players")
	@JsonProperty(value = "incorporated_number_of_players")
	private int incorporatedNumberOfPlayers;
	
	private String language;

	public void incrementIncorporatedPlayers() {
		this.incorporatedNumberOfPlayers = this.incorporatedNumberOfPlayers + 1;
	}

	public void decrementCurrentPlayers() {
		this.currentTotalNumberOfPlayers = this.currentTotalNumberOfPlayers - 1;
	}

	public void decrementIncorporatedPlayers() {
		this.incorporatedNumberOfPlayers = this.incorporatedNumberOfPlayers - 1;
	}

	public void decrementMatchPlayers() {
		this.numberOfPlayers = this.numberOfPlayers - 1;
	}
}
