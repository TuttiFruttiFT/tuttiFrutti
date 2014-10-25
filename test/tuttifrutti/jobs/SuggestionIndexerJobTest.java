package tuttifrutti.jobs;

import static java.util.Collections.singletonList;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.enums.SuggestionState.ACCEPTED;
import static tuttifrutti.models.enums.SuggestionState.CONSOLIDATED;
import static tuttifrutti.models.enums.SuggestionState.REJECTED;
import static tuttifrutti.models.enums.SuggestionState.TO_BE_ELIMINATED;
import static tuttifrutti.utils.TestUtils.savePlayer;
import static tuttifrutti.utils.TestUtils.sleep;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import tuttifrutti.elastic.ElasticSearchAwareTest;
import tuttifrutti.elastic.ElasticUtil;
import tuttifrutti.models.Category;
import tuttifrutti.models.Player;
import tuttifrutti.models.Suggestion;
import tuttifrutti.utils.SpringApplicationContext;

/**
 * @author rfanego
 *
 */
public class SuggestionIndexerJobTest extends ElasticSearchAwareTest{

	@Test
	public void test() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {	
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			ElasticUtil elasticUtil = SpringApplicationContext.getBeanNamed("elasticUtil", ElasticUtil.class);
			SuggestionIndexerJob job = SpringApplicationContext.getBeanNamed("suggestionIndexerJob", SuggestionIndexerJob.class);
			populateElastic(getJsonFilesFotCategories());
			
			Player player = savePlayer(dataStore, "sarasas@sarasa.com");
			String playerId = player.getId().toString();
			
			Category category = new Category("animals");
			Suggestion suggestionAccepted1 = new Suggestion(category,"sapo",5,0,singletonList(playerId),ACCEPTED);
			Suggestion suggestionAccepted2 = new Suggestion(category,"saltamonte",5,1,singletonList(playerId),ACCEPTED);
			Suggestion suggestionRejected1 = new Suggestion(category,"saranga",2,5,singletonList(playerId),REJECTED);
			Suggestion suggestionRejected2 = new Suggestion(category,"sarasa",3,5,singletonList(playerId),REJECTED);
			
			List<Suggestion> suggestions = new ArrayList<>();
			suggestions.add(suggestionAccepted1);
			suggestions.add(suggestionAccepted2);
			suggestions.add(suggestionRejected1);
			suggestions.add(suggestionRejected2);
			
			dataStore.save(suggestions);
			
			job.run();
			
			sleep(1000);
			
			Query<Suggestion> query = dataStore.find(Suggestion.class, "state =", CONSOLIDATED.toString());
			List<Suggestion> consolidatedSuggestions = query.asList();
			
			assertThat(consolidatedSuggestions.size()).isEqualTo(2);
			
			consolidatedSuggestions.forEach(suggestion -> {
				assertThat(elasticUtil.existWord(suggestion.getCategory().getId(), suggestion.getWrittenWord())).isTrue();
			});
			
			query = dataStore.find(Suggestion.class, "state =", TO_BE_ELIMINATED.toString());
			List<Suggestion> toBeEliminatedSuggestions = query.asList();
			
			assertThat(toBeEliminatedSuggestions.size()).isEqualTo(2);
		});
	}

}
