package tuttiFrutti.controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.Match.TO_BE_APPROVED;

import java.util.Arrays;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mongodb.morphia.Datastore;

import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import tuttifrutti.models.Category;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.Player;
import tuttifrutti.utils.SpringApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
public class MatchesTest {

	@Test
	public void searchPublicMatchReturnsExistingMatch() {
		running(testServer(9000, fakeApplication()), new Runnable() {
			@Override
			public void run() {
				Datastore datastore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
				Player player = new Player();
				player.setNickname("SARASA");
				player.setMail("sarasas@sarasa.com");
				datastore.save(player);

				Player player2 = new Player();
				player2.setNickname("SARASA2");
				player2.setMail("sarasas2@sarasa.com");
				datastore.save(player2);

				Match match = new Match();
				MatchConfig matchConfig = new MatchConfig();
				matchConfig.setLanguage("ES");
				matchConfig.setMode(MatchConfig.NORMAL_MODE);
				matchConfig.setType(MatchConfig.PUBLIC_TYPE);
				matchConfig.setNumberOfPlayers(3);
				matchConfig.setPowerUpsEnabled(true);
				matchConfig.setRounds(25);
				match.setConfig(matchConfig);
				match.setName(null); // TODO ver qué poner de nombre
				match.setState(TO_BE_APPROVED);
				match.setStartDate(DateTime.now().toDate()); // TODO guardamos así o parseamos?
				match.setCategories(Category.getPublicMatchCategories());
				match.setPlayers(Arrays.asList(player2));
				datastore.save(match);

				WSResponse r = WS.url("http://localhost:9000/match/public").setContentType("application/json")
								 .post("{\"player_id\" : \"" + player.getId().toString() + "\", \"config\":" 
										+ Json.toJson(matchConfig).toString()+ "}")
								 .get(5000L);
				assertThat(r).isNotNull();
				assertThat(r.getStatus()).isEqualTo(OK);

				JsonNode jsonNode = r.asJson();
				Match resultMatch = Json.fromJson(jsonNode, Match.class);

				assertThat(resultMatch).isNotNull();
				assertThat(resultMatch.getId().toString()).isEqualTo(match.getId().toString());
				assertThat(resultMatch.getState()).isEqualTo(TO_BE_APPROVED);
				assertThat(resultMatch.getConfig()).isNotNull();
				assertThat(resultMatch.getConfig().getType()).isEqualTo(MatchConfig.PUBLIC_TYPE);
				assertThat(resultMatch.getConfig().getMode()).isEqualTo(MatchConfig.NORMAL_MODE);
				assertThat(resultMatch.getConfig().getLanguage()).isEqualTo("ES");
				assertThat(resultMatch.getConfig().getNumberOfPlayers()).isEqualTo(3);
			}
		});
	}

	@Test
	public void searchPublicMatchReturnsCreatedMatch() {
		running(testServer(9000, fakeApplication()), new Runnable() {
			@Override
			public void run() {
				Datastore datastore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
				Player player = new Player();
				player.setNickname("SARASA");
				player.setMail("sarasas@sarasa.com");
				datastore.save(player);

				MatchConfig matchConfig = new MatchConfig();
				matchConfig.setLanguage("ES");
				matchConfig.setMode(MatchConfig.NORMAL_MODE);
				matchConfig.setType(MatchConfig.PUBLIC_TYPE);
				matchConfig.setNumberOfPlayers(3);
				matchConfig.setPowerUpsEnabled(true);
				matchConfig.setRounds(25);
				
				WSResponse r = WS.url("http://localhost:9000/match/public").setContentType("application/json")
        						 .post("{\"player_id\" : \"" + player.getId().toString() + "\", \"config\":" 
        							   + Json.toJson(matchConfig).toString()+ "}")
        						 .get(5000L);
				assertThat(r).isNotNull();
				assertThat(r.getStatus()).isEqualTo(OK);

				JsonNode jsonNode = r.asJson();
				Match resultMatch = Json.fromJson(jsonNode, Match.class);

				assertThat(resultMatch).isNotNull();
				assertThat(resultMatch.getId()).isNotNull();
				assertThat(resultMatch.getState()).isEqualTo(TO_BE_APPROVED);
				assertThat(resultMatch.getConfig()).isNotNull();
				assertThat(resultMatch.getConfig().getType()).isEqualTo(MatchConfig.PUBLIC_TYPE);
				assertThat(resultMatch.getConfig().getMode()).isEqualTo(MatchConfig.NORMAL_MODE);
				assertThat(resultMatch.getConfig().getLanguage()).isEqualTo("ES");
				assertThat(resultMatch.getConfig().getNumberOfPlayers()).isEqualTo(3);
			}
		});
	}

	// @Test
	// public void test() {
	// running(testServer(9000, fakeApplication()), new Runnable() {
	// @Override
	// public void run() {
	//
	// }
	// });
	// }
}
