package tuttifrutti.models;

import lombok.Getter;

@Getter
public enum MatchType {
	PUBLIC_TYPE("PUBLIC"),

	PRIVATE_TYPE("PRIVATE");

	private String name;
	
	MatchType(String name){
		this.name = name;
	}
}
