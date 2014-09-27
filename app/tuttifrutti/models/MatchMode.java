package tuttifrutti.models;

import lombok.Getter;

@Getter
public enum MatchMode {
	QUICK_MODE("Q"),
	NORMAL_MODE("N");
	
	private String name;
	
	MatchMode(String name){
		this.name = name;
	}
}
