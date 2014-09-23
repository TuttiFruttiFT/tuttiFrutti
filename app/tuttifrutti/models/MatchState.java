package tuttifrutti.models;

import lombok.Getter;

@Getter
public enum MatchState {
	TO_BE_APPROVED("TO_BE_APPROVED"),
	PLAYER_TURN("PLAYER_TURN"),
	OPPONENT_TURN("OPPONENT_TURN"),
	FINISHED("FINISHED"),
	REJECTED("REJECTED");
	
	private String name;
	
	MatchState(String name){
		this.name = name;
	}
}