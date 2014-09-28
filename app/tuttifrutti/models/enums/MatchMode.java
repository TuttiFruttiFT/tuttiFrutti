package tuttifrutti.models.enums;

import lombok.Getter;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@Getter
@JsonFormat(shape=JsonFormat.Shape.OBJECT)
public enum MatchMode {
	Q("Q"),
	N("N");
	
	private String name;
	
	MatchMode(String name){
		this.name = name;
	}
	
	@JsonCreator
	public MatchMode forValue(String value) {
	    return valueOf(value);
	}
	
	@JsonValue
	public String toValue() {
		return this.getName();
	}
}