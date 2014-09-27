package tuttifrutti.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;

@Getter
public enum MatchState {
	TO_BE_APPROVED("TO_BE_APPROVED"),
	PLAYER_TURN("PLAYER_TURN"),
	OPPONENT_TURN("OPPONENT_TURN"),
	FINISHED("FINISHED"),
	WAITING_FOR_OPPONENTS("WAITING_FOR_OPPONENTS"),
	REJECTED("REJECTED");
	
	private String name;
	
	MatchState(String name){
		this.name = name;
	}
	
	@JsonCreator
	public MatchState forValue(String value) {
	    return valueOf(value);
	}
	
	@JsonValue
	public String toValue() {
		return this.getName();
	}
}
