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
import static tuttifrutti.utils.TestUtils.sleep;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import tuttifrutti.elastic.ElasticSearchAwareTest;
import tuttifrutti.models.Category;
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
public class SuggestionsTest extends ElasticSearchAwareTest{

	@Test
	public void suggestWords() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {			
			Datastore dataStore = getBeanNamed("mongoDatastore", Datastore.class);
			SuggestionService suggestionService = getBeanNamed("suggestionService", SuggestionService.class);
			populateElastic(getJsonFilesFotCategories());
			
			Player player = savePlayer(dataStore, "sarasa@sarasa.com");
			String playerId = player.getId().toString();
			
			ArrayNode suggestionsArray = Json.newObject().arrayNode()
											 .add(Json.newObject().put("category", "bands").put("written_word", "  David Bowie  "))
											 .add(Json.newObject().put("category", "colors").put("written_word", "Marrón Sucio   "));
			
			WSResponse r = WS.url("http://localhost:9000/word/suggestion").setContentType("application/json")
					.post(Json.newObject().put("player_id", playerId).set("duplas", suggestionsArray))
							.get(5000000L);
			
			sleep(500L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);
			Query<Suggestion> query = dataStore.find(Suggestion.class, "state =", SUGGESTED.toString());
			List<Suggestion> suggestions = query.asList();
			
			assertThat(suggestionService.getSuggestions(playerId)).isEmpty();
			assertThat(suggestions.size()).isEqualTo(2);
			
			suggestions.forEach(suggestion -> {
				if(suggestion.getCategory().equals("bands")){
					assertThat(suggestion.getWrittenWord()).isEqualTo("david bowie");
					assertThat(suggestion.getPositiveVotes()).isEqualTo(0);
					assertThat(suggestion.getNegativeVotes()).isEqualTo(0);
					assertThat(suggestion.getState()).isEqualTo(SUGGESTED);
				}
				
				if(suggestion.getCategory().equals("colors")){
					assertThat(suggestion.getWrittenWord()).isEqualTo("marrón sucio");
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
			populateElastic(getJsonFilesFotCategories());
			
			Player player = savePlayer(dataStore, "sarasa@sarasa.com");
			String playerId = player.getId().toString();
			List<Suggestion> suggestions = new ArrayList<>();
			Suggestion bandSuggestion = suggestionService.suggest(new Category("bands"), "  David Bowie  ", playerId);
			Suggestion colorSuggestion = suggestionService.suggest(new Category("colors"), "  Marrón Sucio  ", playerId);
			Suggestion animalSuggestion1 = suggestionService.suggest(new Category("animals"), "  Martín Pescador  ", playerId);
			suggestions.add(bandSuggestion);
			suggestions.add(colorSuggestion);
			suggestions.add(animalSuggestion1);

			dataStore.save(suggestions);
			
			suggestions = new ArrayList<>();
			Suggestion animalSuggestion2 = suggestionService.suggest(new Category("animals"), "  Martín Pescador  ", "1234");
			suggestions.add(animalSuggestion2);
			
			dataStore.save(suggestions);
			
			suggestions = new ArrayList<>();
			Suggestion animalSuggestion3 = suggestionService.suggest(new Category("animals"), "  Martín Pescador  ", "2345");
			suggestions.add(animalSuggestion3);
			
			dataStore.save(suggestions);
			
			suggestions = new ArrayList<>();
			Suggestion animalSuggestion4 = suggestionService.suggest(new Category("animals"), "  Martín Pescador  ", "3456");
			suggestions.add(animalSuggestion4);
			
			dataStore.save(suggestions);
			
			suggestions = new ArrayList<>();
			Suggestion animalSuggestion5 = suggestionService.suggest(new Category("animals"), "  Martín Pescador  ", "4567");
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
					assertThat(suggestion.getWrittenWord()).isEqualTo("david bowie");
					assertThat(suggestion.getPositiveVotes()).isEqualTo(1);
					assertThat(suggestion.getNegativeVotes()).isEqualTo(0);
					assertThat(suggestion.getState()).isEqualTo(SUGGESTED);
				}
				
				if(suggestion.getCategory().equals("colors")){
					assertThat(suggestion.getWrittenWord()).isEqualTo("marrón sucio");
					assertThat(suggestion.getPositiveVotes()).isEqualTo(1);
					assertThat(suggestion.getNegativeVotes()).isEqualTo(0);
					assertThat(suggestion.getState()).isEqualTo(SUGGESTED);
				}
			});
			
			Query<Suggestion> queryAccepted = dataStore.find(Suggestion.class, "state =", ACCEPTED.toString());
			Suggestion suggestionsAccepted = queryAccepted.get();
			
			assertThat(suggestionsAccepted.getCategory().getId()).isEqualTo("animals");
			assertThat(suggestionsAccepted.getWrittenWord()).isEqualTo("martín pescador");
			assertThat(suggestionsAccepted.getPositiveVotes()).isEqualTo(5);
			assertThat(suggestionsAccepted.getNegativeVotes()).isEqualTo(0);
			assertThat(suggestionsAccepted.getState()).isEqualTo(ACCEPTED);
		});
	}
	
	@Test
	public void obtainSuggestionsForAParticularPlayer() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {			
			Datastore dataStore = getBeanNamed("mongoDatastore", Datastore.class);
			SuggestionService suggestionService = getBeanNamed("suggestionService", SuggestionService.class);
			populateElastic(getJsonFilesFotCategories());
			
			Player player1 = savePlayer(dataStore, "sarasa@sarasa.com");
			Player player2 = savePlayer(dataStore, "sarasa@sarasa.com");
			String playerId1 = player1.getId().toString();
			String playerId2 = player2.getId().toString();
			
			Suggestion bandSuggestion = suggestionService.suggest(new Category("bands"), "  David Bowie  ", playerId1);
			Suggestion colorSuggestion = suggestionService.suggest(new Category("colors"), "  Marrón Sucio  ", playerId2);
			
			dataStore.save(bandSuggestion);
			dataStore.save(colorSuggestion);
			
			createAcceptedSuggestion(dataStore,playerId2, new Category("animals"), "Ay");
			
			WSResponse r = WS.url("http://localhost:9000/word/" + playerId1).setContentType("application/json").get().get(5000000L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);
			
			JsonNode suggestionJson = r.asJson().get(0);
			
			assertThat(suggestionJson.get("category").get("id").textValue()).isEqualTo(colorSuggestion.getCategory().getId());
			assertThat(suggestionJson.get("written_word").textValue()).isEqualTo(colorSuggestion.getWrittenWord());
			assertThat(suggestionJson.get("id").textValue()).isEqualTo(colorSuggestion.getId().toString());
		});
	}
	
	private void createAcceptedSuggestion(Datastore dataStore,String playerId, Category category, String word){
		Suggestion suggestion = new Suggestion();
		suggestion.setCategory(category);
		suggestion.setWrittenWord(word);
		suggestion.setNegativeVotes(2);
		suggestion.setPositiveVotes(5);
		suggestion.setPlayerIds(singletonList(playerId));
		suggestion.setState(ACCEPTED);
		dataStore.save(suggestion);
	}
}
