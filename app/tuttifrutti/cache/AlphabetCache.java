package tuttifrutti.cache;

import java.util.ArrayList;
import java.util.Arrays;
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
		String unavailableLetters = (String)Cache.get(categoryId);
		if(unavailableLetters == null){
			unavailableLetters = letter;
		}else{
			unavailableLetters = unavailableLetters + SEPARATOR + letter;
		}
		Cache.set(PREFIX + categoryId, unavailableLetters);
	}
	
	public List<String> unavailableLetters(String categoryId){
		String unavailableLetters = (String)Cache.get(PREFIX + categoryId);
		if(unavailableLetters != null){
			return new ArrayList<String>(Arrays.asList(unavailableLetters.split(SEPARATOR)));
		}
		return null;
	}
	
}
