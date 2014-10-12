/**
 * 
 */
package tuttifrutti.models;

import org.mongodb.morphia.annotations.Embedded;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.Setter;

/**
 * @author rfanego
 *
 */
@Embedded
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public enum Letter {
	A("A"),B("B"),C("C"),D("D"),E("E"),F("F"),G("G"),H("H"),I("I"),J("J"),K("K"),L("L"),M("M"),
	N("N"),O("O"),P("P"),Q("Q"),R("R"),S("S"),T("T"),U("U"),V("V"),/*W("W"),X("X"),*/Y("Y"),Z("Z");
	
	@Getter @Setter
	private String letter;
	
	Letter(String letter){
		this.letter = letter;
	}
}
