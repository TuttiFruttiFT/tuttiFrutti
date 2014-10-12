package tuttifrutti.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.stereotype.Component;

import play.cache.Cache;
import tuttifrutti.models.Letter;

@Component
public class CategoryCache {
	private static final String SEPARATOR = "-";
	private Map<String,Integer> categoryCount = new HashMap<>();

	public void saveWord(String categoryId, Letter letter, String word) {
		String mapKey = mapKey(categoryId,letter);
		Integer count = categoryCount.get(mapKey) != null ? categoryCount.get(mapKey) + 1 : 1;
		categoryCount.put(mapKey, count);
		Cache.set(cacheKey(mapKey,count), word);
	}
	
	public String retrieveWord(String categoryId, Letter letter){
		String mapKey = mapKey(categoryId,letter);
		Integer numberOfWords = categoryCount.get(mapKey);
		return numberOfWords != null ? (String)Cache.get(cacheKey(mapKey,randomWordNumber(numberOfWords))) : null;
	}

	private int randomWordNumber(Integer numberOfWords) {
		return new Random().nextInt(numberOfWords) + 1;
	}
	
	private String mapKey(String categoryId, Letter letter){
		return categoryId + SEPARATOR + letter.getLetter();
	}
	
	private String cacheKey(String mapKey, Integer wordNumber){
		return mapKey + SEPARATOR + wordNumber;
	}
}
