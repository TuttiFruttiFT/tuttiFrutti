package tuttifrutti.models;

import static java.util.Collections.singletonList;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.enums.LanguageType.ES;
import static tuttifrutti.utils.TestUtils.cleanAlphabetCache;

import org.junit.Test;

import tuttifrutti.cache.AlphabetCache;
import tuttifrutti.services.AlphabetService;
import tuttifrutti.utils.SpringApplicationContext;

public class LetterWrapperTest {

	@Test
	public void whenObtainingRandomAndNextLetterWrapper() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			cleanAlphabetCache();
			
			AlphabetService service = SpringApplicationContext.getBeanNamed("alphabetService", AlphabetService.class);
			AlphabetCache cache = SpringApplicationContext.getBeanNamed("alphabetCache", AlphabetCache.class);
			
			cache.addUnavailableLetter("bands", "N");
			cache.addUnavailableLetter("bands", "P");
			cache.addUnavailableLetter("bands", "A");
			
			Alphabet alphabet = service.alphabetForCategoriesAndLanguage(ES.toString(),singletonList(new Category("bands")));
			
			LetterWrapper letterWrapper = LetterWrapper.random(alphabet);
			LetterWrapper anotherLetterWrapper = letterWrapper.next(alphabet);
			
			assertThat(alphabet.getLetters()).contains(letterWrapper.getLetter());
			assertThat(alphabet.getLetters()).contains(anotherLetterWrapper.getLetter());
			assertThat(anotherLetterWrapper.getPreviousLetters()).contains(letterWrapper.getLetter().getLetter());
		});
	}

}
