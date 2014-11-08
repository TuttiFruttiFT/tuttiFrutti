package tuttifrutti.controllers;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.enums.MatchMode.N;
import static tuttifrutti.models.enums.MatchState.FINISHED;
import static tuttifrutti.models.enums.MatchState.REJECTED;
import static tuttifrutti.models.enums.MatchType.PUBLIC;
import static tuttifrutti.utils.TestUtils.createMatch;
import static tuttifrutti.utils.TestUtils.createMatchConfig;
import static tuttifrutti.utils.TestUtils.saveCategories;
import static tuttifrutti.utils.TestUtils.savePlayer;
import static tuttifrutti.utils.TestUtils.savePlayerResult;

import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;

import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import tuttifrutti.models.Letter;
import tuttifrutti.models.LetterWrapper;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.MatchName;
import tuttifrutti.models.Player;
import tuttifrutti.models.PlayerResult;
import tuttifrutti.models.Round;
import tuttifrutti.utils.SpringApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author rfanego
 *
 */
public class PlayersTest {
	
	@Test
	public void activeMatchesFromPlayer(){
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			String language = "ES";
			
			Round lastRound = new Round();
			lastRound.setNumber(1);
			lastRound.setLetter(new LetterWrapper(Letter.A));
			
			Player player = savePlayer(dataStore, "sarasas@sarasa.com");
			Player player2 = savePlayer(dataStore, "sarasas2@sarasa.com");

			PlayerResult playerResult1 = savePlayerResult(dataStore, player, 10);
			PlayerResult playerResult2 = savePlayerResult(dataStore, player2, 10);
			
			saveCategories(dataStore, language);

			MatchConfig matchConfig = createMatchConfig(language, N, PUBLIC, 2, true, 25);
			
			List<PlayerResult> playersResultList = asList(playerResult1,playerResult2);
			MatchName matchName = new MatchName(2);
			
			createMatch(dataStore, language, lastRound,playersResultList, matchConfig, matchName);
			createMatch(dataStore, language, lastRound,playersResultList, matchConfig, matchName);
			createMatch(dataStore, language, lastRound,playersResultList, matchConfig, matchName);
			Match match4 = createMatch(dataStore, language, lastRound,playersResultList, matchConfig, matchName);
			match4.setState(FINISHED);
			dataStore.save(match4);
			Match match5 = createMatch(dataStore, language, lastRound,playersResultList, matchConfig, matchName);
			match5.setState(REJECTED);
			dataStore.save(match5);
			
			WSResponse r = WS.url("http://localhost:9000/player/matches/" + player.getId().toString()).get().get(5000000L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);
			
			ArrayNode jsonNode = (ArrayNode) r.asJson();
			
			assertThat(jsonNode.size()).isEqualTo(5);
		});
	}
	
	@Test
	public void findExistingPlayer(){
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			
			Player player = savePlayer(dataStore, "sarasas@sarasa.com", "SARASA");
			
			WSResponse r = WS.url("http://localhost:9000/player").setContentType("application/json")
						   .post("{\"mail\":\"sarasas@sarasa.com\",\"password\":\"SARASA\"}").get(5000000L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);
			
			JsonNode jsonNode = r.asJson();
			
			assertThat(jsonNode.get("id").asText()).isEqualTo(player.getId().toString());
		});
	}
	
	@Test
	public void createNewPlayer(){
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			WSResponse r = WS.url("http://localhost:9000/player").setContentType("application/json")
					   .post("{\"mail\":\"sarasas@sarasa.com\",\"password\":\"SARASA\"}").get(5000000L);
		
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);
			
			JsonNode jsonNode = r.asJson();
			
			assertThat(jsonNode.get("mail").asText()).isEqualTo("sarasas@sarasa.com");
			assertThat(jsonNode.get("password")).isNull();
			assertThat(jsonNode.get("nickname").asText()).isEqualTo("sarasas");
		});
	}
	
	@Test
	public void addFriend(){
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			
			Player player = savePlayer(dataStore, "sarasas@sarasa.com", "SARASA");
			Player friend = savePlayer(dataStore, "sarasas2@sarasa.com", "SARASA2");
			
			WSResponse r = WS.url("http://localhost:9000/player/friend").setContentType("application/json")
					   .post(Json.newObject().put("player_id", player.getId().toString())
							   				 .put("friend_id", friend.getId().toString()))
					   .get(5000000L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);
		});
	}
}
