package tuttifrutti.utils;

/**
 * @author rfanego
 */
public class WordUtil {
	public static String getLetter(String word) {
		return word.substring(0, 1);
	}
	
	public static String processWord(String unprocessedWord) {
		return unprocessedWord.toLowerCase();
	}
}
