/**
 * 
 */
package tuttifrutti.models;

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
}
