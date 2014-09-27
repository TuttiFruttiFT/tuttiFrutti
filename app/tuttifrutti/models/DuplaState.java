/**
 * 
 */
package tuttifrutti.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

/**
 * @author rfanego
 *
 */
@Getter
public enum DuplaState {
	PERFECT("PERFECT"),CORRECTED("CORRECTED"),WRONG("WRONG");
	
	public String name;
	
	private DuplaState(String name) {
		this.name = name;
	}
	
	@JsonCreator
	public DuplaState forValue(String value) {
	    return valueOf(value);
	}
	
	@JsonValue
	public String toValue() {
		return this.getName();
	}
}
