package tuttifrutti.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author rfanego
 */
@Embedded
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Turn {
	private String playerId;
	
	@Property("end_time")
	private Integer endTime;
	
	private Integer score;
	
	@Embedded
	private List<Dupla> duplas;
}
