package models;

import lombok.Getter;
import lombok.Setter;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author rfanego
 */
@Entity
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchConfig {
	
	private Integer round;
	
	@Property("match_type")
	private String matchType;
	
	@Property("power_ups_enabled")
	private boolean powerUpsEnabled;
}
