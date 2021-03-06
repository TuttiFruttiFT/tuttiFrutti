package tuttifrutti.models;

import static tuttifrutti.models.enums.DuplaState.WRONG;

import java.util.List;

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
public class Turn {
	private Player player;
	
	@Property("end_time")
	@JsonProperty(value = "end_time")
	private Integer endTime;
	
	private Integer score;
	
	private boolean bpmbpt;
	
	@Embedded
	private List<Dupla> duplas;
	
	public static final int TURN_DURATION_IN_MINUTES = 10;
	
	public void setDuplas(List<Dupla> duplas){
		this.duplas = duplas;
		this.bpmbpt = !duplas.stream().anyMatch(dupla -> dupla.getState().equals(WRONG));
	}
}
