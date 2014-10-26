package tuttifrutti.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * @author rfanego
 */
@Component
public class AlphabetCache {
	private static final String SEPARATOR = "-";
	private Map<String,String> categoryLetter = new HashMap<>();
	
	public void addUnavailableLetter(String categoryId, String letter) {
		
	}
	
}
