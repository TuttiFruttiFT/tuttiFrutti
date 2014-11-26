package tuttifrutti.services;

import static java.util.Collections.singletonList;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.enums.LanguageType.ES;
import static tuttifrutti.utils.TestUtils.cleanAlphabetCache;

import org.junit.Test;

import tuttifrutti.cache.AlphabetCache;
import tuttifrutti.models.Alphabet;
import tuttifrutti.models.Category;
import tuttifrutti.utils.SpringApplicationContext;

public class AlphabetServiceTest {

	@Test
	public void whenObtainingAlphabetForLanguageThenAlphabetMustNotIncludeBannedLetters() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			cleanAlphabetCache();
			
			AlphabetService service = SpringApplicationContext.getBeanNamed("alphabetService", AlphabetService.class);
			
			Alphabet alphabet = service.alphabetForLanguage(ES.toString());
			
			assertThat(alphabet.getLetters().size()).isEqualTo(22);
			assertThat(alphabet.getSize()).isEqualTo(22);
			assertThat(alphabet.getLetters()).excludes("X","W");
		});
	}

	@Test
	public void whenObtainingAlphabetForLanguageAndCategoryThenAlphabetMustNotIncludeBannedLetters() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			cleanAlphabetCache();
			
			AlphabetService service = SpringApplicationContext.getBeanNamed("alphabetService", AlphabetService.class);
			AlphabetCache cache = SpringApplicationContext.getBeanNamed("alphabetCache", AlphabetCache.class);
			
			cache.addUnavailableLetter("bands", "N");
			cache.addUnavailableLetter("bands", "P");
			cache.addUnavailableLetter("bands", "A");
			
			Alphabet alphabet = service.alphabetForCategoriesAndLanguage(ES.toString(),singletonList(new Category("bands")));
			
			assertThat(alphabet.getLetters().size()).isEqualTo(19);
			assertThat(alphabet.getSize()).isEqualTo(19);
			assertThat(alphabet.getLetters()).excludes("A","P","N","X","W");
		});
	}
	
}
