package tuttifrutti.models;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.mongodb.morphia.annotations.Embedded;

/**
 * @author rfanego
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Embedded
public class Alphabet {
	private List<Letter> letters;
	
	private String language;
	
	private int size;
	
	public Alphabet(String language,List<Letter> letters){
		this.language = language;
		this.letters = letters;
		this.size = letters.size();
	}

	public void remove(Set<String> lettersToRemove) {
		Iterator<Letter> it = letters.iterator();
		while(it.hasNext()){
			Letter letter = it.next();
			if(lettersToRemove.contains(letter.getLetter())){
				it.remove();
			}
		}
		this.size = this.letters.size();
	}
}
