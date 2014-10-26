package tuttifrutti.services;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static tuttifrutti.models.Letter.W;
import static tuttifrutti.models.Letter.X;
import static tuttifrutti.models.Letter.values;
import static tuttifrutti.models.enums.LanguageType.ES;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import tuttifrutti.models.Alphabet;
import tuttifrutti.models.Category;
import tuttifrutti.models.Letter;

/**
 * @author rfanego
 */
@Component
public class AlphabetService {
	private Map<String,List<Letter>> BANNED_LETTERS;
	private static final List<Letter> VALUES = unmodifiableList(Arrays.asList(values()));
	
	public AlphabetService(){
		BANNED_LETTERS = new HashMap<>();
		List<Letter> spanishBannedLetters = new ArrayList<>();
		spanishBannedLetters.add(X);
		spanishBannedLetters.add(W);
		BANNED_LETTERS.put(ES.toString(), spanishBannedLetters);
	}
	
	public Alphabet alphabetForCategoriesAndLanguage(String language,List<Category> categories){
		Alphabet alphabet = this.alphabetForLanguage(language);
		
		return alphabet;
	}
	
	private Alphabet alphabetForLanguage(String language){
		List<Letter> bannedLetters = BANNED_LETTERS.get(language);
		List<Letter> letters = VALUES.stream().filter(letter -> !bannedLetters.stream().anyMatch(bannedLetter -> bannedLetter.equals(letter.getLetter()))).collect(toList());
		return new Alphabet(letters,language);
	}
}
