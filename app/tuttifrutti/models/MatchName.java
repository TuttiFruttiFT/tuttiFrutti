package tuttifrutti.models;

import org.mongodb.morphia.annotations.Embedded;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embedded
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchName {
	private String value;
	private boolean calculated;
	@JsonProperty(value = "number_of_other_players")
	private Integer numberOfOtherPlayers;
	
	public MatchName(Integer numberOfPlayers){
		this.numberOfOtherPlayers = numberOfPlayers - 1;
		this.calculated = true;
	}
	
	public MatchName(String value,Integer numberOfPlayers){
		this.value = value;
		this.numberOfOtherPlayers = numberOfPlayers - 1;
		this.calculated = false;
	}

	public void incrementPlayers() {
		this.numberOfOtherPlayers = this.numberOfOtherPlayers + 1;
	}

	public void decrementPlayers() {
		this.numberOfOtherPlayers = this.numberOfOtherPlayers - 1;
	}
}
