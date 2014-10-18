package tuttifrutti.controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
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

import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author rfanego
 *
 */
public class SuggestionsTest {

//	@Test
	public void suggestWords() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {			
			Datastore dataStore = getBeanNamed("mongoDatastore", Datastore.class);
			SuggestionService suggestionService = getBeanNamed("suggestionService", SuggestionService.class);
			
			Player player = savePlayer(dataStore, "sarasa@sarasa.com");
			String playerId = player.getId().toString();
			
			ArrayNode suggestionsArray = Json.newObject().arrayNode()
											 .add(Json.newObject().put("category", "bands").put("word", "  The Rolling Stones  "))
											 .add(Json.newObject().put("category", "colors").put("word", "Marrón Sucio   "));
			
			WSResponse r = WS.url("http://localhost:9000/word/suggest").setContentType("application/json")
					.post(Json.newObject().put("player_id", playerId).set("duplas", suggestionsArray))
							.get(5000000L);
			
			TestUtils.sleep(5000000000L);
			
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
			
			ArrayNode suggestionsArray = Json.newObject().arrayNode()
					 .add(Json.newObject().put("suggestion_id", bandSuggestion.getId().toString()).put("valid", true))
					 .add(Json.newObject().put("suggestion_id", colorSuggestion.getId().toString()).put("valid", true))
					 .add(Json.newObject().put("suggestion_id", animalSuggestion1.getId().toString()).put("valid", true));
			
			WSResponse r = WS.url("http://localhost:9000/word/judgement").setContentType("application/json")
							 .post(Json.newObject().put("player_id", playerId).set("duplas", suggestionsArray)).get(5000000L);
			
			TestUtils.sleep(500L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);
			Query<Suggestion> query = dataStore.find(Suggestion.class, "state =", SUGGESTED.toString());
			List<Suggestion> suggestionsResult = query.asList();
			
			assertThat(suggestionService.getSuggestions(playerId)).isEmpty();
//			assertThat(suggestionsResult.size()).isEqualTo(2);
			
			suggestionsResult.forEach(suggestion -> {
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
				
				if(suggestion.getCategory().equals("animals")){
					assertThat(suggestion.getWord()).isEqualTo("martín pescador");
					assertThat(suggestion.getPositiveVotes()).isEqualTo(1);
					assertThat(suggestion.getNegativeVotes()).isEqualTo(0);
					assertThat(suggestion.getState()).isEqualTo(SUGGESTED);
				}
			});
			
		});
	}
}
