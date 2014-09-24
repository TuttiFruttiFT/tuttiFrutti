package tuttifrutti.controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.Category.DEFAULT_CATEGORIES_NUMBER;
import static tuttifrutti.models.DuplaState.CORRECTED;
import static tuttifrutti.models.DuplaState.PERFECT;
import static tuttifrutti.models.DuplaState.WRONG;
import static tuttifrutti.models.Letter.A;
import static tuttifrutti.models.Letter.R;
import static tuttifrutti.models.Letter.S;
import static tuttifrutti.models.MatchConfig.NORMAL_MODE;
import static tuttifrutti.models.MatchConfig.PUBLIC_TYPE;
import static tuttifrutti.models.MatchState.PLAYER_TURN;
import static tuttifrutti.models.MatchState.TO_BE_APPROVED;
import static tuttifrutti.utils.TestUtils.createMatch;
import static tuttifrutti.utils.TestUtils.createMatchConfig;
import static tuttifrutti.utils.TestUtils.createRound;
import static tuttifrutti.utils.TestUtils.createTurn;
import static tuttifrutti.utils.TestUtils.getCategoriesFromDuplas;
import static tuttifrutti.utils.TestUtils.saveCategories;
import static tuttifrutti.utils.TestUtils.saveDupla;
import static tuttifrutti.utils.TestUtils.savePlayer;
import static tuttifrutti.utils.TestUtils.savePlayerResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;

import play.Logger;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import tuttifrutti.elastic.ElasticSearchAwareTest;
import tuttifrutti.models.Category;
import tuttifrutti.models.Dupla;
import tuttifrutti.models.Letter;
import tuttifrutti.models.LetterWrapper;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.Player;
import tuttifrutti.models.PlayerResult;
import tuttifrutti.models.Round;
import tuttifrutti.models.Turn;
import tuttifrutti.utils.JsonUtil;
import tuttifrutti.utils.SpringApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
public class MatchesTest extends ElasticSearchAwareTest {

	//@Test
	public void searchPublicMatchReturnsExistingMatch() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			String language = "ES";
			
			Round lastRound = new Round();
			lastRound.setNumber(1);
			lastRound.setLetter(new LetterWrapper(Letter.A));
			
			Player player = savePlayer(dataStore, "sarasas@sarasa.com");
			Player player2 = savePlayer(dataStore, "sarasas2@sarasa.com");

			savePlayerResult(dataStore, player, 10);
			PlayerResult playerResult2 = savePlayerResult(dataStore, player2, 10);
			
			saveCategories(dataStore, language);

			MatchConfig matchConfig = createMatchConfig(language, NORMAL_MODE, PUBLIC_TYPE, 3, true, 25);
			Match match = createMatch(dataStore, language, lastRound,Arrays.asList(playerResult2), matchConfig);
			
			String playerId = player.getId().toString();
			WSResponse r = WS.url("http://localhost:9000/match/public").setContentType("application/json")
							 .post("{\"player_id\" : \"" + playerId + "\", \"config\":" 
									+ Json.toJson(matchConfig).toString()+ "}")
							 .get(5000000L);
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);

			JsonNode jsonNode = r.asJson();
			Match resultMatch = Json.fromJson(jsonNode, Match.class);
			
			assertThat(resultMatch).isNotNull();
			assertThat(resultMatch.getId().toString()).isEqualTo(match.getId().toString());
			LetterWrapper letter = commonMatchAssertions(language, resultMatch);
			for(PlayerResult playerResult : resultMatch.getPlayerResults()){
				Player playerAux = playerResult.getPlayer();
				if(playerAux.getNickname().equals("SARASA2")){
					assertThat(playerResult.getScore()).isEqualTo(10);
				}
			}
			assertThat(letter.getLetter()).isEqualTo(A);
		});
	}
	
	//@Test
	public void searchPublicMatchReturnsANewOneBecauseTheyAreAllStarted() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			
			String language = "ES";
			int roundNumber = 1;
			
			Player player = savePlayer(dataStore, "sarasas@sarasa.com");
			Player player2 = savePlayer(dataStore, "sarasas2@sarasa.com");
			Player otherPlayer = savePlayer(dataStore, "sarasas3@sarasa.com");

			PlayerResult playerResult1 = savePlayerResult(dataStore, player, 35);
			PlayerResult playerResult2 = savePlayerResult(dataStore, player2, 40);
			
			saveCategories(dataStore, language);

			List<Dupla> duplas = new ArrayList<>();
			saveDupla(new Category("bands"), duplas, "Radiohead", 15);
			saveDupla(new Category("colors"), duplas, "Marron", 24);
			saveDupla(new Category("meals"), duplas, "Risotto", 35);
			saveDupla(new Category("countries"), duplas, "Rumania", 39);
			
			List<Dupla> duplas2 = new ArrayList<>();
			saveDupla(new Category("bands"), duplas2, "Rolling Stone", "rolling stones",15, CORRECTED);
			saveDupla(new Category("colors"), duplas2, "Rojo", "rojo",24, PERFECT);
			saveDupla(new Category("meals"), duplas2, "", null,35, WRONG);
			saveDupla(new Category("countries"), duplas2, null, null,39, WRONG);
			
			Turn turn = createTurn(player2.getId().toString(), 45, 0, duplas2);
			
			Round lastRound = createRound(turn, roundNumber, R);
			
			MatchConfig matchConfig = createMatchConfig(language, NORMAL_MODE, PUBLIC_TYPE, 3, true, 25);
			Match match = createMatch(dataStore, language, lastRound,Arrays.asList(playerResult1,playerResult2), matchConfig,
									  getCategoriesFromDuplas(duplas, language), PLAYER_TURN);
			
			WSResponse r = WS.url("http://localhost:9000/match/public").setContentType("application/json")
					 .post("{\"player_id\" : \"" + otherPlayer.getId().toString() + "\", \"config\":" 
							+ Json.toJson(matchConfig).toString()+ "}")
					 .get(5000000L);
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);
			
			JsonNode jsonNode = r.asJson();
			
			assertThat(jsonNode.get("id").asText()).isNotEqualTo(match.getId().toString());
		});
	}

	//@Test
	public void searchPublicMatchReturnsCreatedMatch() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			Player player = savePlayer(dataStore, "sarasas@sarasa.com");
			String language = "ES";

			saveCategories(dataStore, language);
			
			MatchConfig matchConfig = createMatchConfig(language, NORMAL_MODE, PUBLIC_TYPE, 3, true, 25);
			
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
			commonMatchAssertions(language, resultMatch);
		});
	}
	
	@Test
	public void turnWithTwoPlayers() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			Round roundService = SpringApplicationContext.getBeanNamed("round", Round.class);
			
			String language = "ES";
			int roundNumber = 1;
			
			Player player = savePlayer(dataStore, "sarasas@sarasa.com");
			Player player2 = savePlayer(dataStore, "sarasas2@sarasa.com");

			PlayerResult playerResult1 = savePlayerResult(dataStore, player, 35);
			PlayerResult playerResult2 = savePlayerResult(dataStore, player2, 40);
			
			saveCategories(dataStore, language);

			List<Dupla> duplas = new ArrayList<>();
			saveDupla(new Category("bands"), duplas, "Radiohead", 15);
			saveDupla(new Category("colors"), duplas, "Marron", 24);
			saveDupla(new Category("meals"), duplas, "Risotto", 35);
			saveDupla(new Category("countries"), duplas, "Rumania", 39);
			
			List<Dupla> duplas2 = new ArrayList<>();
			saveDupla(new Category("bands"), duplas2, "Rolling Stone", "rolling stones",15, CORRECTED);
			saveDupla(new Category("colors"), duplas2, "Rojo", "rojo",24, PERFECT);
			saveDupla(new Category("meals"), duplas2, "", null,35, WRONG);
			saveDupla(new Category("countries"), duplas2, null, null,39, WRONG);
			
			Turn turn = createTurn(player2.getId().toString(), 45, 0, duplas2);
			
			Round lastRound = createRound(turn, roundNumber, R);
			
			MatchConfig matchConfig = createMatchConfig(language, NORMAL_MODE, PUBLIC_TYPE, 3, true, 25);
			Match match = createMatch(dataStore, language, lastRound,Arrays.asList(playerResult1,playerResult2), matchConfig,
									  getCategoriesFromDuplas(duplas, language), PLAYER_TURN);
			
			WSResponse r = WS.url("http://localhost:9000/match/turn").setContentType("application/json")
					 .post("{\"player_id\" : \"" + player.getId().toString() + "\", \"match_id\":\"" + match.getId().toString() 
						   + "\", \"time\": 41" 
						   + ", \"duplas\":" + JsonUtil.parseListToJson(duplas)
						   + "}")
					 .get(5000000L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);

			JsonNode jsonNode = r.asJson();
			JsonNode jsonWrongDupla = jsonNode.get(0);
			
			assertThat(jsonWrongDupla.get("category").get("id").asText()).isEqualTo("colors");
			assertThat(jsonWrongDupla.get("written_word").asText()).isEqualTo("marron");
			assertThat(jsonWrongDupla.get("time").asInt()).isEqualTo(24);
			assertThat(jsonWrongDupla.get("state").asText()).isEqualTo("WRONG");
			
			sleep(500L);
			
			Match modifiedMatch = dataStore.get(Match.class, match.getId());
			Round modifiedRound = roundService.getRound(match.getId().toString(), roundNumber);
			Round newRound = modifiedMatch.getLastRound();
			
			assertThat(newRound.getLetter()).isNotEqualTo(modifiedRound.getLetter());
			assertThat(newRound.getLetter().getPreviousLetters()).contains(Letter.R.toString());
			
			for(PlayerResult playerResult : modifiedMatch.getPlayerResults()){
				if(playerResult.getPlayer().getId().toString().equals(player.getId().toString())){
					assertThat(playerResult.getScore()).isEqualTo(85);
				}
				
				if(playerResult.getPlayer().getId().toString().equals(player2.getId().toString())){
					assertThat(playerResult.getScore()).isEqualTo(70);
				}
			}
			
			assertThat(modifiedRound.getEndTime()).isEqualTo(41);
			for(Turn modifiedTurn : modifiedRound.getTurns()){
				if(modifiedTurn.getPlayer().getId().toString().equals(player.getId().toString())){
					assertThat(modifiedTurn.getEndTime()).isEqualTo(41);
					assertThat(modifiedTurn.getScore()).isEqualTo(50);
					for(Dupla modifiedDupla : modifiedTurn.getDuplas()){
						if(modifiedDupla.getCategory().getId().equals("bands")){
							assertThat(modifiedDupla.getScore()).isEqualTo(10);
						}
						
						if(modifiedDupla.getCategory().getId().equals("colors")){
							assertThat(modifiedDupla.getScore()).isEqualTo(0);
						}
						
						if(modifiedDupla.getCategory().getId().equals("meals")){
							assertThat(modifiedDupla.getScore()).isEqualTo(20);
						}
						
						if(modifiedDupla.getCategory().getId().equals("countries")){
							assertThat(modifiedDupla.getScore()).isEqualTo(20);
						}
					}
				}
				
				if(modifiedTurn.getPlayer().getId().toString().equals(player2.getId().toString())){
					assertThat(modifiedTurn.getEndTime()).isEqualTo(45);
					assertThat(modifiedTurn.getScore()).isEqualTo(30);
					for(Dupla modifiedDupla : modifiedTurn.getDuplas()){
						if(modifiedDupla.getCategory().getId().equals("bands")){
							assertThat(modifiedDupla.getScore()).isEqualTo(10);
						}
						
						if(modifiedDupla.getCategory().getId().equals("colors")){
							assertThat(modifiedDupla.getScore()).isEqualTo(20);
						}
						
						if(modifiedDupla.getCategory().getId().equals("meals")){
							assertThat(modifiedDupla.getScore()).isEqualTo(0);
						}
						
						if(modifiedDupla.getCategory().getId().equals("countries")){
							assertThat(modifiedDupla.getScore()).isEqualTo(0);
						}
					}
				}
			}
		});
	}
	
	//@Test
	public void turnWithThreePlayers() { //TODO
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			Round roundService = SpringApplicationContext.getBeanNamed("round", Round.class);
			populateElastic(getJsonFilesFotCategories());
			
			String language = "ES";
			int roundNumber = 1;
			
			Player player = savePlayer(dataStore, "sarasas@sarasa.com");
			Player player2 = savePlayer(dataStore, "sarasas2@sarasa.com");

			PlayerResult playerResult1 = savePlayerResult(dataStore, player, 35);
			PlayerResult playerResult2 = savePlayerResult(dataStore, player2, 40);
			
			saveCategories(dataStore, language);

			List<Dupla> duplas = new ArrayList<>();
			saveDupla(new Category("animals"), duplas, "Serpiente", 11);
			saveDupla(new Category("musical_styles"), duplas, "Samba", 17);
			saveDupla(new Category("verbs"), duplas, "Salir", 8);
			saveDupla(new Category("sports"), duplas, "Surf", 26);
			saveDupla(new Category("meals"), duplas, "Sandia", 35);
			saveDupla(new Category("colors"), duplas, "Siena", 40);
			
			List<Dupla> duplas2 = new ArrayList<>();
			saveDupla(new Category("animals"), duplas2, "surubi", "surubi",11, PERFECT);
			saveDupla(new Category("musical_styles"), duplas2, "salsa", "salsa",17, PERFECT);
			saveDupla(new Category("verbs"), duplas2, "sentir", "servir",8, CORRECTED);
			saveDupla(new Category("sports"), duplas2, "softball", "softból",26, CORRECTED);
			saveDupla(new Category("meals"), duplas2, "sandia", "savia de abedul",35, CORRECTED);
			saveDupla(new Category("colors"), duplas2, "salmon", "salmón",40, CORRECTED);
			
			Turn turn = createTurn(player2.getId().toString(), 45, 0, duplas2);
			
			Round lastRound = createRound(turn, roundNumber, S);
			
			MatchConfig matchConfig = createMatchConfig(language, NORMAL_MODE, PUBLIC_TYPE, 3, true, 25);
			Match match = createMatch(dataStore, language, lastRound,Arrays.asList(playerResult1,playerResult2), matchConfig,
									  getCategoriesFromDuplas(duplas, language), PLAYER_TURN);
			
			WSResponse r = WS.url("http://localhost:9000/match/turn").setContentType("application/json")
					 .post("{\"player_id\" : \"" + player.getId().toString() + "\", \"match_id\":\"" + match.getId().toString() 
						   + "\", \"time\": 45" 
						   + ", \"duplas\":" + JsonUtil.parseListToJson(duplas)
						   + "}")
					 .get(5000000L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);

			JsonNode jsonNode = r.asJson();
			JsonNode jsonWrongDupla = jsonNode.get(0);
			
			assertThat(jsonWrongDupla).isNotNull();
		
			
			sleep(500L);
			
			Match modifiedMatch = dataStore.get(Match.class, match.getId());
			Round modifiedRound = roundService.getRound(match.getId().toString(), roundNumber);
			Round newRound = modifiedMatch.getLastRound();
			
			assertThat(newRound.getLetter()).isNotEqualTo(modifiedRound.getLetter());
			assertThat(newRound.getLetter().getPreviousLetters()).contains(S.toString());
			
//			for(PlayerResult playerResult : modifiedMatch.getPlayers()){
//				if(playerResult.getPlayer().getId().toString().equals(player.getId().toString())){
//					assertThat(playerResult.getScore()).isEqualTo(85);
//				}
//				
//				if(playerResult.getPlayer().getId().toString().equals(player2.getId().toString())){
//					assertThat(playerResult.getScore()).isEqualTo(70);
//				}
//			}
			
			assertThat(modifiedRound.getEndTime()).isEqualTo(45);
		});
	}
	
	private void sleep(long timeInMillis) {
		try {
			Thread.sleep(timeInMillis);
		} catch (Exception e) {
			Logger.error("In Sleep",e);
		}
	}

	private LetterWrapper commonMatchAssertions(String language, Match resultMatch) {
		assertThat(resultMatch.getState()).isEqualTo(TO_BE_APPROVED);
		assertThat(resultMatch.getConfig()).isNotNull();
		assertThat(resultMatch.getConfig().getType()).isEqualTo(PUBLIC_TYPE);
		assertThat(resultMatch.getConfig().getMode()).isEqualTo(NORMAL_MODE);
		assertThat(resultMatch.getConfig().getLanguage()).isEqualTo(language);
		assertThat(resultMatch.getConfig().getNumberOfPlayers()).isEqualTo(3);
		assertThat(resultMatch.getCategories().size()).isEqualTo(DEFAULT_CATEGORIES_NUMBER);
		assertThat(resultMatch.getPlayerResults()).isNotNull();
		for(PlayerResult playerResult : resultMatch.getPlayerResults()){
			Player player = playerResult.getPlayer();
			if(player.getNickname().equals("SARASA")){
				assertThat(playerResult.getScore()).isEqualTo(0);
			}
		}
		Round round = resultMatch.getLastRound();
		assertThat(round).isNotNull();
		assertThat(round.getNumber()).isEqualTo(1);
		LetterWrapper letter = round.getLetter();
		assertThat(letter).isNotNull();
		return letter;
	}
	
}
