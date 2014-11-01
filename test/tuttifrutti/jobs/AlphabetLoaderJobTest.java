package tuttifrutti.jobs;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.enums.LanguageType.ES;
import static tuttifrutti.utils.TestUtils.saveCategories;

import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;

import tuttifrutti.cache.AlphabetCache;
import tuttifrutti.elastic.ElasticSearchAwareTest;
import tuttifrutti.utils.SpringApplicationContext;

public class AlphabetLoaderJobTest extends ElasticSearchAwareTest {

	@Test
	public void test() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			AlphabetLoaderJob job = SpringApplicationContext.getBeanNamed("alphabetLoaderJob", AlphabetLoaderJob.class);
			AlphabetCache cache = SpringApplicationContext.getBeanNamed("alphabetCache", AlphabetCache.class);
			populateElastic(getJsonFilesFotCategories());
			saveCategories(dataStore, ES.toString());
			
			//TODO LIMPIAR CACHE DE ALPHABET POR CATEGORIA
			
			job.run();
			
			List<String> bandLetters = cache.unavailableLetters("bands");
			
			assertThat(bandLetters.size()).isEqualTo(24);
		});
	}

}
