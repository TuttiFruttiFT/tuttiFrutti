package tuttiFrutti.controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.Match.TO_BE_APPROVED;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mongodb.morphia.Datastore;

import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import tuttifrutti.models.Category;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.utils.SpringApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
public class Matches {

	@Test
	public void test() {
		running(testServer(9000, fakeApplication()), new Runnable() {
			@Override
			public void run() {
				Datastore datastore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
				Match match = new Match();
				MatchConfig matchConfig = new MatchConfig();
				matchConfig.setLanguage("ES");
				matchConfig.setMatchType("N");
				matchConfig.setNumberOfPlayers(3);
				matchConfig.setPowerUpsEnabled(true);
				matchConfig.setRounds(25);
				match.setConfig(matchConfig);
				match.setName(null); // TODO ver qué poner de nombre
				match.setState(TO_BE_APPROVED);
				match.setStartDate(DateTime.now().toDate()); // TODO guardamos así o parseamos?
				match.setCategories(Category.getPublicMatchCategories());
				datastore.save(match);

				WSResponse r = WS.url("http://localhost:9000/match/public").setContentType("application/json").
post("{}").get(5000L);
				assertThat(r).isNotNull();
				assertThat(r.getStatus()).isEqualTo(OK);

				JsonNode jsonNode = r.asJson();
				Match resultMatch = Json.fromJson(jsonNode, Match.class);

				assertThat(resultMatch).isNotNull();

			}
		});
	}

}
