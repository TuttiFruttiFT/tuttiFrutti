package tuttifrutti.controllers;

import static java.util.Collections.singletonList;
import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.enums.SuggestionState.ACCEPTED;
import static tuttifrutti.models.enums.SuggestionState.SUGGESTED;
import static tuttifrutti.utils.SpringApplicationContext.getBeanNamed;
import static tuttifrutti.utils.TestUtils.savePlayer;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import tuttifrutti.models.Player;
import tuttifrutti.models.Suggestion;
import tuttifrutti.services.SuggestionService;
import tuttifrutti.utils.TestUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author rfanego
 *
 */
public class SuggestionsTest {

	@Test
	public void suggestWords() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {			
			Datastore dataStore = getBeanNamed("mongoDatastore", Datastore.class);
			SuggestionService suggestionService = getBeanNamed("suggestionService", SuggestionService.class);
			
			Player player = savePlayer(dataStore, "sarasa@sarasa.com");
			String playerId = player.getId().toString();
			
			ArrayNode suggestionsArray = Json.newObject().arrayNode()
											 .add(Json.newObject().put("category", "bands").put("word", "  The Rolling Stones  "))
											 .add(Json.newObject().put("category", "colors").put("word", "Marrón Sucio   "));
			
			WSResponse r = WS.url("http://localhost:9000/word/suggestion").setContentType("application/json")
					.post(Json.newObject().put("player_id", playerId).set("duplas", suggestionsArray))
							.get(5000000L);
			
			TestUtils.sleep(500L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);
			Query<Suggestion> query = dataStore.find(Suggestion.class, "state =", SUGGESTED.toString());
			List<Suggestion> suggestions = query.asList();
			
			assertThat(suggestionService.getSuggestions(playerId)).isEmpty();
			assertThat(suggestions.size()).isEqualTo(2);
			
			suggestions.forEach(suggestion -> {
				if(suggestion.getCategory().equals("bands")){
					assertThat(suggestion.getWord()).isEqualTo("the rolling stones");
					assertThat(suggestion.getPositiveVotes()).isEqualTo(0);
					assertThat(suggestion.getNegativeVotes()).isEqualTo(0);
					assertThat(suggestion.getState()).isEqualTo(SUGGESTED);
				}
				
				if(suggestion.getCategory().equals("colors")){
					assertThat(suggestion.getWord()).isEqualTo("marrón sucio");
					assertThat(suggestion.getPositiveVotes()).isEqualTo(0);
					assertThat(suggestion.getNegativeVotes()).isEqualTo(0);
					assertThat(suggestion.getState()).isEqualTo(SUGGESTED);
				}
			});
		});
	}
	
	@Test
	public void judgeWords() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {			
			Datastore dataStore = getBeanNamed("mongoDatastore", Datastore.class);
			SuggestionService suggestionService = getBeanNamed("suggestionService", SuggestionService.class);
			
			Player player = savePlayer(dataStore, "sarasa@sarasa.com");
			String playerId = player.getId().toString();
			List<Suggestion> suggestions = new ArrayList<>();
			Suggestion bandSuggestion = suggestionService.suggest("bands", "  The Rolling Stones  ", playerId);
			Suggestion colorSuggestion = suggestionService.suggest("colors", "  Marrón Sucio  ", playerId);
			Suggestion animalSuggestion1 = suggestionService.suggest("animals", "  Martín Pescador  ", playerId);
			suggestions.add(bandSuggestion);
			suggestions.add(colorSuggestion);
			suggestions.add(animalSuggestion1);

			dataStore.save(suggestions);
			
			suggestions = new ArrayList<>();
			Suggestion animalSuggestion2 = suggestionService.suggest("animals", "  Martín Pescador  ", playerId);
			suggestions.add(animalSuggestion2);
			
			dataStore.save(suggestions);
			
			suggestions = new ArrayList<>();
			Suggestion animalSuggestion3 = suggestionService.suggest("animals", "  Martín Pescador  ", playerId);
			suggestions.add(animalSuggestion3);
			
			dataStore.save(suggestions);
			
			suggestions = new ArrayList<>();
			Suggestion animalSuggestion4 = suggestionService.suggest("animals", "  Martín Pescador  ", playerId);
			suggestions.add(animalSuggestion4);
			
			dataStore.save(suggestions);
			
			suggestions = new ArrayList<>();
			Suggestion animalSuggestion5 = suggestionService.suggest("animals", "  Martín Pescador  ", playerId);
			suggestions.add(animalSuggestion5);
			
			dataStore.save(suggestions);
			
			ArrayNode suggestionsArray = Json.newObject().arrayNode()
					 .add(Json.newObject().put("suggestion_id", bandSuggestion.getId().toString()).put("valid", true))
					 .add(Json.newObject().put("suggestion_id", colorSuggestion.getId().toString()).put("valid", true))
					 .add(Json.newObject().put("suggestion_id", animalSuggestion1.getId().toString()).put("valid", true));
			
			WSResponse r = WS.url("http://localhost:9000/word/judgement").setContentType("application/json")
							 .post(Json.newObject().put("player_id", playerId).set("duplas", suggestionsArray)).get(5000000L);
			
			TestUtils.sleep(500L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);
			
			Query<Suggestion> querySuggested = dataStore.find(Suggestion.class, "state =", SUGGESTED.toString());
			List<Suggestion> suggestionsSuggestedResult = querySuggested.asList();
			
			assertThat(suggestionService.getSuggestions(playerId)).isEmpty();
			assertThat(suggestionsSuggestedResult.size()).isEqualTo(2);
			
			suggestionsSuggestedResult.forEach(suggestion -> {
				if(suggestion.getCategory().equals("bands")){
					assertThat(suggestion.getWord()).isEqualTo("the rolling stones");
					assertThat(suggestion.getPositiveVotes()).isEqualTo(1);
					assertThat(suggestion.getNegativeVotes()).isEqualTo(0);
					assertThat(suggestion.getState()).isEqualTo(SUGGESTED);
				}
				
				if(suggestion.getCategory().equals("colors")){
					assertThat(suggestion.getWord()).isEqualTo("marrón sucio");
					assertThat(suggestion.getPositiveVotes()).isEqualTo(1);
					assertThat(suggestion.getNegativeVotes()).isEqualTo(0);
					assertThat(suggestion.getState()).isEqualTo(SUGGESTED);
				}
			});
			
			Query<Suggestion> queryAccepted = dataStore.find(Suggestion.class, "state =", ACCEPTED.toString());
			Suggestion suggestionsAccepted = queryAccepted.get();
			
			assertThat(suggestionsAccepted.getCategory()).isEqualTo("animals");
			assertThat(suggestionsAccepted.getWord()).isEqualTo("martín pescador");
			assertThat(suggestionsAccepted.getPositiveVotes()).isEqualTo(5);
			assertThat(suggestionsAccepted.getNegativeVotes()).isEqualTo(0);
			assertThat(suggestionsAccepted.getState()).isEqualTo(ACCEPTED);
		});
	}
	
	@Test
	public void test() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {			
			Datastore dataStore = getBeanNamed("mongoDatastore", Datastore.class);
			SuggestionService suggestionService = getBeanNamed("suggestionService", SuggestionService.class);
			
			Player player1 = savePlayer(dataStore, "sarasa@sarasa.com");
			Player player2 = savePlayer(dataStore, "sarasa@sarasa.com");
			String playerId1 = player1.getId().toString();
			String playerId2 = player2.getId().toString();
			
			Suggestion bandSuggestion = suggestionService.suggest("bands", "  The Rolling Stones  ", playerId1);
			Suggestion colorSuggestion = suggestionService.suggest("colors", "  Marrón Sucio  ", playerId2);
			
			dataStore.save(bandSuggestion);
			dataStore.save(colorSuggestion);
			
			createAcceptedSuggestion(dataStore,playerId2, "animals", "Ay");
			
			WSResponse r = WS.url("http://localhost:9000/word/" + playerId1).setContentType("application/json").get().get(5000000L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);
			
			JsonNode suggestionJson = r.asJson().get(0);
			
			assertThat(suggestionJson.get("category").textValue()).isEqualTo(colorSuggestion.getCategory());
			assertThat(suggestionJson.get("word").textValue()).isEqualTo(colorSuggestion.getWord());
			assertThat(suggestionJson.get("id").textValue()).isEqualTo(colorSuggestion.getId().toString());
		});
	}
	
	private void createAcceptedSuggestion(Datastore dataStore,String playerId, String category, String word){
		Suggestion suggestion = new Suggestion();
		suggestion.setCategory(category);
		suggestion.setWord(word);
		suggestion.setNegativeVotes(2);
		suggestion.setPositiveVotes(5);
		suggestion.setPlayerIds(singletonList(playerId));
		suggestion.setState(ACCEPTED);
		dataStore.save(suggestion);
	}
}
