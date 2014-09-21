package tuttifrutti.models.views;

import lombok.Getter;
import lombok.Setter;
import tuttifrutti.models.Round;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rfanego
 */
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActiveMatch {

	private String id;
	
	private String name;
	
	@JsonProperty(value = "current_round")
	private Round currentRound;
	
	private String state;
}
