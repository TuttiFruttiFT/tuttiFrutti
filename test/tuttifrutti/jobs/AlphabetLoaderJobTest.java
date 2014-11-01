package tuttifrutti.jobs;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.enums.LanguageType.ES;
import static tuttifrutti.utils.TestUtils.cleanAlphabetCache;
import static tuttifrutti.utils.TestUtils.saveCategories;

import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;

import tuttifrutti.cache.AlphabetCache;
import tuttifrutti.elastic.ElasticSearchAwareTest;
import tuttifrutti.utils.CategoryType;
import tuttifrutti.utils.SpringApplicationContext;

public class AlphabetLoaderJobTest extends ElasticSearchAwareTest {

	@Test
	public void whenRunAlphabetJobThenAlphabetCacheMustBeProperlyFilled() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			cleanAlphabetCache();
			
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			AlphabetLoaderJob job = SpringApplicationContext.getBeanNamed("alphabetLoaderJob", AlphabetLoaderJob.class);
			AlphabetCache cache = SpringApplicationContext.getBeanNamed("alphabetCache", AlphabetCache.class);
			populateElastic(getJsonFilesFotCategories());
			saveCategories(dataStore, ES.toString());
			
			job.run();
			
			List<String> bandLetters = cache.unavailableLetters(CategoryType.bands.toString());
			List<String> sportLetters = cache.unavailableLetters(CategoryType.sports.toString());
			
			assertThat(bandLetters.size()).isEqualTo(23);
			assertThat(sportLetters.size()).isEqualTo(25);
		});
	}
}
