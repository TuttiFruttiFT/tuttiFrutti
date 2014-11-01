package tuttifrutti.cache;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import play.cache.Cache;

/**
 * @author rfanego
 */
@Component
public class AlphabetCache {
	private static final String SEPARATOR = "-";
	private static final String PREFIX = "ALPHABET_";
	
	public void addUnavailableLetter(String categoryId, String letter) {
		String unavailableLetters = (String)Cache.get(getKey(categoryId));
		if(unavailableLetters == null){
			unavailableLetters = letter;
		}else{
			if(!unavailableLettersList(unavailableLetters).contains(letter)){				
				unavailableLetters = unavailableLetters + SEPARATOR + letter;
				Cache.set(getKey(categoryId), unavailableLetters);
			}
		}
		return;
	}

	public List<String> unavailableLetters(String categoryId){
		String unavailableLetters = (String)Cache.get(getKey(categoryId));
		if(unavailableLetters != null){
			return unavailableLettersList(unavailableLetters);
		}
		return new ArrayList<>();
	}

	private ArrayList<String> unavailableLettersList(String unavailableLetters) {
		return new ArrayList<String>(asList(unavailableLetters.split(SEPARATOR)));
	}
	
	private String getKey(String categoryId) {
		return PREFIX + categoryId;
	}
}
