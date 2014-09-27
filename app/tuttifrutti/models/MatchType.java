package tuttifrutti.models;

import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@Getter
public enum MatchType {
	PUBLIC_TYPE("PUBLIC"),

	PRIVATE_TYPE("PRIVATE");

	private String name;
	
	MatchType(String name){
		this.name = name;
	}
	
	@JsonCreator
	public MatchType forValue(String value) {
	    return MatchType.valueOf(value);
	}
	
	@JsonValue
	public String toValue() {
		return this.getName();
	}
}
