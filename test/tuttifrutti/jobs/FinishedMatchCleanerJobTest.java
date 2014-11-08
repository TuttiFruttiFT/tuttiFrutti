package tuttifrutti.jobs;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static org.joda.time.DateTime.now;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.Letter.A;
import static tuttifrutti.models.enums.MatchMode.N;
import static tuttifrutti.models.enums.MatchState.CLEAN;
import static tuttifrutti.models.enums.MatchState.FINISHED;
import static tuttifrutti.models.enums.MatchType.PUBLIC;
import static tuttifrutti.utils.TestUtils.createMatch;
import static tuttifrutti.utils.TestUtils.createMatchConfig;
import static tuttifrutti.utils.TestUtils.saveCategories;
import static tuttifrutti.utils.TestUtils.testPlayerResult;

import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;

import tuttifrutti.models.LetterWrapper;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.MatchName;
import tuttifrutti.models.PlayerResult;
import tuttifrutti.models.Round;
import tuttifrutti.utils.SpringApplicationContext;

public class FinishedMatchCleanerJobTest {

	@Test
	public void test() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			FinishedMatchCleanerJob job = SpringApplicationContext.getBeanNamed("finishedMatchCleanerJob", FinishedMatchCleanerJob.class);
			String language = "ES";
			saveCategories(dataStore, language);
			
			MatchConfig matchConfig = createMatchConfig(language, N, PUBLIC, 2, false, 25);
			Round round = new Round(1,new LetterWrapper(A));
			List<PlayerResult> playerResultList = asList(testPlayerResult(dataStore));
			MatchName matchName = new MatchName(2);
			Match match = createMatch(dataStore, language, round,playerResultList, matchConfig, matchName);
			Match match2 = createMatch(dataStore, language, round,playerResultList, matchConfig, matchName);
			Match match3 = createMatch(dataStore, language, round,playerResultList, matchConfig, matchName);
			
			match.setState(FINISHED);
			match.setModifiedDate(now().minusDays(1).toDate());
			
			match2.setState(FINISHED);
			match2.setModifiedDate(now().minusDays(2).toDate());
			
			match3.setState(FINISHED);
			match3.setModifiedDate(now().toDate());
			
			dataStore.save(match);
			dataStore.save(match2);
			dataStore.save(match3);
			
			job.run();
			
			Match recoveredMatch = dataStore.get(Match.class, match.getId());
			Match recoveredMatch2 = dataStore.get(Match.class, match2.getId());
			Match recoveredMatch3 = dataStore.get(Match.class, match3.getId());
			
			assertThat(recoveredMatch.getState()).isEqualTo(CLEAN);
			assertThat(recoveredMatch2.getState()).isEqualTo(CLEAN);
			assertThat(recoveredMatch3.getState()).isEqualTo(FINISHED);
		});
	}
}
