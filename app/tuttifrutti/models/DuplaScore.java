package tuttifrutti.models;

import lombok.Getter;

@Getter
public enum DuplaScore {
	ALONE_SCORE(20),
	UNIQUE_SCORE(10),
	DUPLICATE_SCORE(5),
	ZERO_SCORE(0);
	
	private int score;
	
	DuplaScore(int score){
		this.score = score;
	}
}
