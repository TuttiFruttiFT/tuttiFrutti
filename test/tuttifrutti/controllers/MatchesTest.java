package tuttifrutti.controllers;

import static java.util.Arrays.asList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.Category.DEFAULT_CATEGORIES_NUMBER;
import static tuttifrutti.models.Letter.A;
import static tuttifrutti.models.Letter.R;
import static tuttifrutti.models.Letter.S;
import static tuttifrutti.models.Turn.TURN_DURATION_IN_MINUTES;
import static tuttifrutti.models.enums.DuplaScore.ALONE_SCORE;
import static tuttifrutti.models.enums.DuplaScore.UNIQUE_SCORE;
import static tuttifrutti.models.enums.DuplaScore.ZERO_SCORE;
import static tuttifrutti.models.enums.DuplaState.CORRECTED;
import static tuttifrutti.models.enums.DuplaState.PERFECT;
import static tuttifrutti.models.enums.DuplaState.WRONG;
import static tuttifrutti.models.enums.MatchMode.N;
import static tuttifrutti.models.enums.MatchState.PLAYER_TURN;
import static tuttifrutti.models.enums.MatchState.TO_BE_APPROVED;
import static tuttifrutti.models.enums.MatchType.PRIVATE;
import static tuttifrutti.models.enums.MatchType.PUBLIC;
import static tuttifrutti.models.enums.PowerUpType.autocomplete;
import static tuttifrutti.models.enums.PowerUpType.buy_time;
import static tuttifrutti.models.enums.PowerUpType.opponent_word;
import static tuttifrutti.models.enums.PowerUpType.suggest;
import static tuttifrutti.utils.JsonUtil.parseListToJson;
import static tuttifrutti.utils.TestUtils.cleanAlphabetCache;
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
import tuttifrutti.cache.CategoryCache;
import tuttifrutti.elastic.ElasticSearchAwareTest;
import tuttifrutti.models.Category;
import tuttifrutti.models.Device;
import tuttifrutti.models.Dupla;
import tuttifrutti.models.LetterWrapper;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.MatchName;
import tuttifrutti.models.Player;
import tuttifrutti.models.PlayerResult;
import tuttifrutti.models.PowerUp;
import tuttifrutti.models.Round;
import tuttifrutti.models.Turn;
import tuttifrutti.models.enums.DuplaScore;
import tuttifrutti.models.enums.MatchState;
import tuttifrutti.models.enums.MatchType;
import tuttifrutti.utils.JsonUtil;
import tuttifrutti.utils.SpringApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author rfanego
 */
public class MatchesTest extends ElasticSearchAwareTest {

	@Test
	public void searchPublicMatchReturnsExistingMatch() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			String language = "ES";
			
			Round lastRound = new Round();
			lastRound.setNumber(1);
			lastRound.setLetter(new LetterWrapper(A));
			
			Player player = savePlayer(dataStore, "sarasas@sarasa.com");
			Player player2 = savePlayer(dataStore, "sarasas2@sarasa.com");

			savePlayerResult(dataStore, player, 10);
			PlayerResult playerResult2 = savePlayerResult(dataStore, player2, 0);
			
			saveCategories(dataStore, language);

			MatchConfig matchConfig = createMatchConfig(language, N, PUBLIC, 2, false, 25);
			Match match = createMatch(dataStore, language, lastRound,Arrays.asList(playerResult2), matchConfig, new MatchName(2));
			
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
			LetterWrapper letter = commonMatchAssertions(language, resultMatch, PLAYER_TURN, PUBLIC, 2, DEFAULT_CATEGORIES_NUMBER);
			assertThat(letter.getLetter()).isEqualTo(A);
		});
	}
	
	@Test
	public void searchPublicMatchReturnsANewOneBecauseTheyAreAllStarted() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			cleanAlphabetCache();
			
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
			
			MatchConfig matchConfig = createMatchConfig(language, N, PUBLIC, 2, false, 25);
			Match match = createMatch(dataStore, language, lastRound,Arrays.asList(playerResult1,playerResult2), matchConfig,
									  getCategoriesFromDuplas(duplas, language), PLAYER_TURN, new MatchName(2));
			
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

	@Test
	public void searchPublicMatchReturnsCreatedMatch() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			cleanAlphabetCache();
			
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			Player player = savePlayer(dataStore, "sarasas@sarasa.com");
			String language = "ES";

			populateElastic(getJsonFilesFotCategories());
			saveCategories(dataStore, language);
			
			MatchConfig matchConfig = createMatchConfig(language, N, PUBLIC, 2, false, 25);
			
			WSResponse r = WS.url("http://localhost:9000/match/public").setContentType("application/json")
							 .post("{\"player_id\" : \"" + player.getId().toString() + "\", \"config\":" 
								   + Json.toJson(matchConfig).toString()+ "}")
							 .get(500000000L);
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);

			JsonNode jsonNode = r.asJson();
			Match resultMatch = Json.fromJson(jsonNode, Match.class);
			
			assertThat(resultMatch).isNotNull();
			assertThat(resultMatch.getId()).isNotNull();
			commonMatchAssertions(language, resultMatch, TO_BE_APPROVED, PUBLIC, 2, DEFAULT_CATEGORIES_NUMBER);
		});
	}
	
	@Test
	public void createPrivateMatchFor4Players() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			Player player1 = savePlayer(dataStore, "sarasas1@sarasa.com");
			Player player2 = savePlayer(dataStore, "sarasas2@sarasa.com");
			List<Device> devices = new ArrayList<>();
			devices.add(new Device("Aaabbb3425","ooooPPPP98543"));
			player2.setDevices(devices);
			dataStore.save(player2);
			Player player3 = savePlayer(dataStore, "sarasas3@sarasa.com");
			Player player4 = savePlayer(dataStore, "sarasas4@sarasa.com");
			String language = "ES";
			
			saveCategories(dataStore, language);
			
			ArrayNode playersArray = Json.newObject().arrayNode().add(player2.getId().toString())
			.add(player3.getId().toString()).add(player4.getId().toString());
			
			ArrayNode categoriesArray = Json.newObject().arrayNode().add("colors").add("countries").add("animals")
																	.add("sports").add("jobs").add("musical_instruments");
			
			MatchConfig matchConfig = createMatchConfig(language, N, PRIVATE, 4, false, 25);
			WSResponse r = WS.url("http://localhost:9000/match/private").setContentType("application/json")
					 .post("{\"match_name\" : \"" + "La partida de la hostia" + "\","
					 		+ "\"player_id\" : \"" + player1.getId().toString() + "\", \"config\":" 
						   + Json.toJson(matchConfig).toString() + ",\"players\":" + playersArray.toString() 
						   + ",\"categories\":" + categoriesArray.toString() + "}")
					 .get(50000000L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);
		
			JsonNode jsonNode = r.asJson();
			Match resultMatch = Json.fromJson(jsonNode, Match.class);
			
			assertThat(resultMatch).isNotNull();
			assertThat(resultMatch.getId()).isNotNull();
			commonMatchAssertions(language, resultMatch, TO_BE_APPROVED, PRIVATE, 4, 6);
			MatchName matchName = resultMatch.getMatchName();
			assertThat(matchName).isNotNull();
			assertThat(matchName.getValue()).isEqualTo("La partida de la hostia");
			assertThat(matchName.getNumberOfOtherPlayers()).isEqualTo(3);
			assertThat(resultMatch.getPlayerResults().size()).isEqualTo(4);
		});
	}
	
	@Test
	public void turnWithTwoPlayers() {
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
			
			MatchConfig matchConfig = createMatchConfig(language, N, PUBLIC, 2, false, 25);
			Match match = createMatch(dataStore, language, lastRound,asList(playerResult1,playerResult2), matchConfig,
									  getCategoriesFromDuplas(duplas, language), PLAYER_TURN, new MatchName(2));
			
			WSResponse r = WS.url("http://localhost:9000/match/turn").setContentType("application/json")
					 .post("{\"player_id\" : \"" + player.getId().toString() + "\", \"match_id\":\"" + match.getId().toString() 
						   + "\", \"time\": 41" 
						   + ", \"duplas\":" + parseListToJson(duplas)
						   + "}")
					 .get(5000000L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);

			JsonNode jsonNode = r.asJson();
			JsonNode jsonWrongDupla = jsonNode.get(0);
			
			assertThat(jsonNode.size()).isEqualTo(0);
			
//			assertThat(jsonWrongDupla.get("category").get("id").asText()).isEqualTo("colors");
//			assertThat(jsonWrongDupla.get("written_word").asText()).isEqualTo("marron");
//			assertThat(jsonWrongDupla.get("time").asInt()).isEqualTo(24);
//			assertThat(jsonWrongDupla.get("state").asText()).isEqualTo("WRONG");
			
			sleep(500L);
			
			Match modifiedMatch = dataStore.get(Match.class, match.getId());
			Round modifiedRound = roundService.getRound(match.getId().toString(), roundNumber);
			Round newRound = modifiedMatch.getLastRound();
			
			assertThat(newRound.getLetter()).isNotEqualTo(modifiedRound.getLetter());
			assertThat(newRound.getLetter().getPreviousLetters()).contains(R.toString());
			
			assertThat(modifiedMatch.getPlayedLetters().size()).isEqualTo(1);
			assertThat(modifiedMatch.getPlayedLetters().get(0)).isEqualTo(R);
			assertPlayerScores(modifiedMatch, player, player2, 85, 70);
			
			assertThat(modifiedRound.getEndTime()).isEqualTo((int)MINUTES.toSeconds(TURN_DURATION_IN_MINUTES));
			for(Turn modifiedTurn : modifiedRound.getTurns()){
				if(modifiedTurn.getPlayer().getId().toString().equals(player.getId().toString())){
					assertThat(modifiedTurn.getEndTime()).isEqualTo(41);
					assertThat(modifiedTurn.getScore()).isEqualTo(50);
					
					for(Dupla modifiedDupla : modifiedTurn.getDuplas()){
						assertDupla(modifiedDupla, "bands", UNIQUE_SCORE);
						assertDupla(modifiedDupla, "colors", ZERO_SCORE);
						assertDupla(modifiedDupla, "meals", ALONE_SCORE);
						assertDupla(modifiedDupla, "countries", ALONE_SCORE);
					}
				}
				
				if(modifiedTurn.getPlayer().getId().toString().equals(player2.getId().toString())){
					assertThat(modifiedTurn.getEndTime()).isEqualTo(45);
					assertThat(modifiedTurn.getScore()).isEqualTo(30);
					for(Dupla modifiedDupla : modifiedTurn.getDuplas()){
						assertDupla(modifiedDupla, "bands", UNIQUE_SCORE);
						assertDupla(modifiedDupla, "colors", ALONE_SCORE);
						assertDupla(modifiedDupla, "meals", ZERO_SCORE);
						assertDupla(modifiedDupla, "countries", ZERO_SCORE);						
					}
				}
			}
		});
	}

	@Test
	public void turnWithThreePlayersAndPowerUps() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			CategoryCache categoryCache = SpringApplicationContext.getBeanNamed("categoryCache", CategoryCache.class);
			populateElastic(getJsonFilesFotCategories());
			populateCategoryCache(categoryCache);
			
			String language = "ES";
			int roundNumber = 1;
			
			Player player1 = savePlayer(dataStore, "sarasas@sarasa.com");
			Player player2 = savePlayer(dataStore, "sarasas2@sarasa.com");
			Player player3 = savePlayer(dataStore, "sarasas3@sarasa.com");

			PlayerResult playerResult1 = savePlayerResult(dataStore, player1, 35);
			PlayerResult playerResult2 = savePlayerResult(dataStore, player2, 40);
			PlayerResult playerResult3 = savePlayerResult(dataStore, player3, 50);
			
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
			saveDupla(new Category("colors"), duplas2, "sarlanga", "",40, WRONG);
			
			Turn turn = createTurn(player2.getId().toString(), 45, 0, duplas2);
			
			Round lastRound = createRound(turn, roundNumber, S);
			
			MatchConfig matchConfig = createMatchConfig(language, N, PUBLIC, 3, true, 25);
			Match match = createMatch(dataStore, language, lastRound,Arrays.asList(playerResult1,playerResult2,playerResult3), matchConfig,
									  getCategoriesFromDuplas(duplas, language), PLAYER_TURN, new MatchName(3));
			
			WSResponse turnResponse = WS.url("http://localhost:9000/match/turn").setContentType("application/json")
					 .post("{\"player_id\" : \"" + player1.getId().toString() + "\", \"match_id\":\"" + match.getId().toString() 
						   + "\", \"time\": 45" 
						   + ", \"duplas\":" + JsonUtil.parseListToJson(duplas)
						   + "}")
					 .get(5000000L);
			
			assertThat(turnResponse).isNotNull();
			assertThat(turnResponse.getStatus()).isEqualTo(OK);

			JsonNode jsonNode = turnResponse.asJson();
			JsonNode jsonWrongDupla = jsonNode.get(0);
			
			assertThat(jsonWrongDupla).isNull();
			
			sleep(500L);
			
			WSResponse matchResponsePlayer3 = WS.url("http://localhost:9000/match/" + match.getId().toString())
										 .setQueryParameter("player_id", player3.getId().toString()).get().get(50000000L);
			
			assertThat(matchResponsePlayer3).isNotNull();
			assertThat(matchResponsePlayer3.getStatus()).isEqualTo(OK);
			
			Match resultMatchPlayer3 = Json.fromJson(matchResponsePlayer3.asJson(), Match.class);
			
			resultMatchPlayer3.getCategories().forEach(category -> {				
				List<PowerUp> powerUps = category.getPowerUps();
				
				assertThat(powerUps).isNotNull();
				assertThat(powerUps.size()).isEqualTo(4);
				
				powerUps.forEach(powerUp -> {
					if(powerUp.getName().equals(opponent_word)){
						assertThat(powerUp.getValue()).isNotEmpty();
					}
					
					if(powerUp.getName().equals(autocomplete)){
						assertThat(powerUp.getValue()).isNotEmpty();
					}
					
					if(powerUp.getName().equals(suggest)){
						assertThat(powerUp.getValue()).isNotEmpty();
					}
					
					if(powerUp.getName().equals(buy_time)){
						assertThat(powerUp.getValue()).isEqualTo("3000");
					}
				});
			});
			
			WSResponse matchResponsePlayer1 = WS.url("http://localhost:9000/match/" + match.getId().toString())
					 							.setQueryParameter("player_id", player1.getId().toString()).get().get(50000000L);
			
			assertThat(matchResponsePlayer1).isNotNull();
			assertThat(matchResponsePlayer1.getStatus()).isEqualTo(OK);
			
			Match resultMatchPlayer1 = Json.fromJson(matchResponsePlayer1.asJson(), Match.class);
			
			resultMatchPlayer1.getCategories().forEach(category -> {
				List<PowerUp> powerUps = category.getPowerUps();
				
				assertThat(powerUps).isNotNull();
			});
		});
	}
	
	@Test
	public void emptyDuplas() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			populateElastic(getJsonFilesFotCategories());
			
			String language = "ES";
			int roundNumber = 1;
			
			Player player = savePlayer(dataStore, "sarasas@sarasa.com");
			Player player2 = savePlayer(dataStore, "sarasas2@sarasa.com");

			PlayerResult playerResult1 = savePlayerResult(dataStore, player, 35);
			PlayerResult playerResult2 = savePlayerResult(dataStore, player2, 40);
			
			saveCategories(dataStore, language);

			List<Dupla> duplas = new ArrayList<>();
			saveDupla(new Category("bands"), duplas, "", 15);
			saveDupla(new Category("colors"), duplas, "", 24);
			saveDupla(new Category("meals"), duplas, "", 35);
			saveDupla(new Category("countries"), duplas, "", 39);
			
			List<Dupla> duplas2 = new ArrayList<>();
			saveDupla(new Category("bands"), duplas2, "Rolling Stone", "rolling stones",15, CORRECTED);
			saveDupla(new Category("colors"), duplas2, "Rojo", "rojo",24, PERFECT);
			saveDupla(new Category("meals"), duplas2, "", null,35, WRONG);
			saveDupla(new Category("countries"), duplas2, null, null,39, WRONG);
			
			Turn turn = createTurn(player2.getId().toString(), 45, 0, duplas2);
			
			Round lastRound = createRound(turn, roundNumber, R);
			
			MatchConfig matchConfig = createMatchConfig(language, N, PUBLIC, 2, false, 25);
			Match match = createMatch(dataStore, language, lastRound,asList(playerResult1,playerResult2), matchConfig,
									  getCategoriesFromDuplas(duplas, language), PLAYER_TURN, new MatchName(2));
			
			WSResponse r = WS.url("http://localhost:9000/match/turn").setContentType("application/json")
					 .post("{\"player_id\" : \"" + player.getId().toString() + "\", \"match_id\":\"" + match.getId().toString() 
						   + "\", \"time\": 41" 
						   + ", \"duplas\":" + parseListToJson(duplas)
						   + "}")
					 .get(5000000L);
			
			assertThat(r).isNotNull();
			assertThat(r.getStatus()).isEqualTo(OK);
			
			JsonNode jsonNode = r.asJson();
			assertThat(jsonNode.size()).isEqualTo(0);
		});
	}

	@Test
	public void finishedGameResult() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			cleanAlphabetCache();
			
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
			saveDupla(new Category("bands"), duplas, "Radiohead", 15);
			saveDupla(new Category("colors"), duplas, "Rojo", 24);
			saveDupla(new Category("meals"), duplas, "Risotto", 35);
			saveDupla(new Category("countries"), duplas, "Rumania", 39);
			
			List<Dupla> duplas2 = new ArrayList<>();
			saveDupla(new Category("bands"), duplas2, "Rolling Stone", "rolling stones",15, CORRECTED);
			saveDupla(new Category("colors"), duplas2, "Rojo", "rojo",24, PERFECT);
			saveDupla(new Category("meals"), duplas2, "", null,35, WRONG);
			saveDupla(new Category("countries"), duplas2, null, null,39, WRONG);
			
			Turn turn = createTurn(player2.getId().toString(), 45, 0, duplas2);
			
			Round lastRound = createRound(turn, roundNumber, R);
			
			MatchConfig matchConfig = createMatchConfig(language, N, PUBLIC, 2, false, 1);
			Match match = createMatch(dataStore, language, lastRound,Arrays.asList(playerResult1,playerResult2), matchConfig,
									  getCategoriesFromDuplas(duplas, language), PLAYER_TURN, new MatchName(2));
			
			WSResponse turnResult = WS.url("http://localhost:9000/match/turn").setContentType("application/json")
					 .post("{\"player_id\" : \"" + player.getId().toString() + "\", \"match_id\":\"" + match.getId().toString() 
						   + "\", \"time\": 41" 
						   + ", \"duplas\":" + JsonUtil.parseListToJson(duplas)
						   + "}")
					 .get(500000000L);
			
			assertThat(turnResult).isNotNull();
			assertThat(turnResult.getStatus()).isEqualTo(OK);
			
			sleep(500L);
			
			Match modifiedMatch = dataStore.get(Match.class, match.getId());
			
			assertPlayerScores(modifiedMatch, player, player2, 90, 55);
			
			WSResponse matchResult = WS.url("http://localhost:9000/match/result/" + match.getId().toString()).get().get(5000000L);
			
			assertThat(matchResult).isNotNull();
			assertThat(matchResult.getStatus()).isEqualTo(OK);
			
			Round modifiedRound = roundService.getRound(match.getId().toString(), roundNumber);
			assertThat(modifiedRound.getEndTime()).isEqualTo(41);
			
			Match resultMatch = Json.fromJson(matchResult.asJson(), Match.class);
			
			assertThat(resultMatch.getWinner()).isNotNull();
			assertThat(resultMatch.getWinner().getId()).isEqualTo(player.getId());
			assertThat(resultMatch.getWinner().getNickname()).isEqualTo("sarasas");
			assertThat(resultMatch.getPlayerResults()).isNotNull();
			assertThat(resultMatch.getPlayerResults().size()).isEqualTo(2);
			
			resultMatch.getPlayerResults().forEach(playerResult -> {
				if(playerResult.getPlayer().getId().toString().equals(player.getId().toString())){
					assertThat(playerResult.getScore()).isEqualTo(90);
				}else{
					assertThat(playerResult.getScore()).isEqualTo(55);
				}
			});
		});
	}
	
	private void populateCategoryCache(CategoryCache categoryCache) {
		categoryCache.saveWord("animals", S, "sapo");
		categoryCache.saveWord("animals", S, "salame");
		categoryCache.saveWord("animals", S, "sarawosi");
		categoryCache.saveWord("animals", S, "serpiente");
		
		categoryCache.saveWord("musical_styles", S, "samba");
		
		categoryCache.saveWord("verbs", S, "salir");
		categoryCache.saveWord("verbs", S, "sacar");
		categoryCache.saveWord("verbs", S, "sumir");
		
		categoryCache.saveWord("sports", S, "subir escaleras");
		categoryCache.saveWord("sports", S, "softwall aereo");
		
		categoryCache.saveWord("meals", S, "sanguche");
		categoryCache.saveWord("meals", S, "sal");
		categoryCache.saveWord("meals", S, "sillon de ensalada");
		
		categoryCache.saveWord("colors", S, "salmon");
		categoryCache.saveWord("colors", S, "siena");
		categoryCache.saveWord("colors", S, "sorete");
	}

	private void assertPlayerScores(Match modifiedMatch, Player player,Player player2, int scorePlayer1, int scorePlayer2) {
		for(PlayerResult playerResult : modifiedMatch.getPlayerResults()){
			if(playerResult.getPlayer().getId().toString().equals(player.getId().toString())){
				assertThat(playerResult.getScore()).isEqualTo(scorePlayer1);
			}
			
			if(playerResult.getPlayer().getId().toString().equals(player2.getId().toString())){
				assertThat(playerResult.getScore()).isEqualTo(scorePlayer2);
			}
		}
	}
	
	private void assertDupla(Dupla modifiedDupla, String categoryId, DuplaScore score) {
		if(modifiedDupla.getCategory().getId().equals(categoryId)){
			assertThat(modifiedDupla.getScore()).isEqualTo(score.getScore());
		}
	}
	
	private void sleep(long timeInMillis) {
		try {
			Thread.sleep(timeInMillis);
		} catch (Exception e) {
			Logger.error("In Sleep",e);
		}
	}

	private LetterWrapper commonMatchAssertions(String language, Match resultMatch, MatchState matchState, MatchType matchType, int numberOfPlayers, int numberOfCategories) {
		assertThat(resultMatch.getState()).isEqualTo(matchState);
		assertThat(resultMatch.getConfig()).isNotNull();
		assertThat(resultMatch.getConfig().getType()).isEqualTo(matchType);
		assertThat(resultMatch.getConfig().getMode()).isEqualTo(N);
		assertThat(resultMatch.getConfig().getLanguage()).isEqualTo(language);
		assertThat(resultMatch.getConfig().getNumberOfPlayers()).isEqualTo(numberOfPlayers);
		assertThat(resultMatch.getCategories().size()).isEqualTo(numberOfCategories);
		assertThat(resultMatch.getPlayerResults()).isNotNull();
		resultMatch.getPlayerResults().forEach(playerResult -> assertThat(playerResult.getScore()).isEqualTo(0));
		Round round = resultMatch.getLastRound();
		assertThat(round).isNotNull();
		assertThat(round.getNumber()).isEqualTo(1);
		LetterWrapper letter = round.getLetter();
		assertThat(letter).isNotNull();
		return letter;
	}
	
}
