package tuttifrutti.controllers;

import static java.util.stream.Collectors.toList;
import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.Category.DEFAULT_CATEGORIES_NUMBER;
import static tuttifrutti.models.DuplaState.CORRECTED;
import static tuttifrutti.models.DuplaState.PERFECT;
import static tuttifrutti.models.DuplaState.WRONG;
import static tuttifrutti.models.Letter.R;
import static tuttifrutti.models.MatchConfig.NORMAL_MODE;
import static tuttifrutti.models.MatchConfig.PUBLIC_TYPE;
import static tuttifrutti.models.MatchState.TO_BE_APPROVED;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mongodb.morphia.Datastore;

import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import tuttifrutti.elastic.ElasticSearchAwareTest;
import tuttifrutti.models.Category;
import tuttifrutti.models.Dupla;
import tuttifrutti.models.DuplaState;
import tuttifrutti.models.Letter;
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

	@Test
	public void searchPublicMatchReturnsExistingMatch() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			String language = "ES";
			
			Round lastRound = new Round();
			lastRound.setNumber(1);
			lastRound.setLetter(Letter.A);
			
			Player player = savePlayer(dataStore, "SARASA", "sarasas@sarasa.com");
			Player player2 = savePlayer(dataStore, "SARASA2", "sarasas2@sarasa.com");

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
			Letter letter = commonMatchAssertions(language, resultMatch);
			for(PlayerResult playerResult : resultMatch.getPlayers()){
				Player playerAux = playerResult.getPlayer();
				if(playerAux.getNickname().equals("SARASA2")){
					assertThat(playerResult.getScore()).isEqualTo(10);
				}
			}
			assertThat(letter).isEqualTo(Letter.A);
		});
	}

	@Test
	public void searchPublicMatchReturnsCreatedMatch() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			Player player = savePlayer(dataStore, "SARASA", "sarasas@sarasa.com");
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
	public void turn() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			Round roundService = SpringApplicationContext.getBeanNamed("round", Round.class);
			
			String language = "ES";
			
			Player player = savePlayer(dataStore, "SARASA", "sarasas@sarasa.com");
			Player player2 = savePlayer(dataStore, "SARASA2", "sarasas2@sarasa.com");

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
			
			Turn turn = new Turn();
			turn.setPlayerId(player2.getId().toString());
			turn.setEndTime(45);
			turn.setScore(0);
			turn.setDuplas(duplas2);
			
			int roundNumber = 1;
			
			Round lastRound = new Round();
			lastRound.setNumber(roundNumber);
			lastRound.setLetter(R);
			lastRound.addTurn(turn);
			
			MatchConfig matchConfig = createMatchConfig(language, NORMAL_MODE, PUBLIC_TYPE, 3, true, 25);
			Match match = createMatch(dataStore, language, lastRound,Arrays.asList(playerResult1,playerResult2), matchConfig,
									  getCategoriesFromDuplas(duplas, language));
			
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
			assertThat(jsonWrongDupla.get("score").asInt()).isEqualTo(0);
			
			Match modifiedMatch = dataStore.get(Match.class, match.getId());
			Round modifiedRound = roundService.getRound(match.getId().toString(), roundNumber);
			Round newRound = modifiedMatch.getLastRound();
			
			assertThat(newRound.getLetter()).isNotEqualTo(modifiedRound.getLetter());
			assertThat(newRound.getLetter().getPreviousLetters()).contains(R.toString());
			
			for(PlayerResult playerResult : modifiedMatch.getPlayers()){
				if(playerResult.getPlayer().getId().toString().equals(player.getId().toString())){
					assertThat(playerResult.getScore()).isEqualTo(85);
				}
				
				if(playerResult.getPlayer().getId().toString().equals(player2.getId().toString())){
					assertThat(playerResult.getScore()).isEqualTo(70);
				}
			}
			
			assertThat(modifiedRound.getEndTime()).isEqualTo(41);
			for(Turn modifiedTurn : modifiedRound.getTurns()){
				if(modifiedTurn.getPlayerId().equals(player.getId().toString())){
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
				
				if(modifiedTurn.getPlayerId().equals(player2.getId().toString())){
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
	
	private List<Category> getCategoriesFromDuplas(List<Dupla> duplas, String language) {
		Category categoryService = SpringApplicationContext.getBeanNamed("category", Category.class);
		List<Category> categories = categoryService.categories(language);
		
		return categories.stream().filter(category -> duplas.stream().anyMatch(dupla -> dupla.getCategory().getId().equals(category.getId())))
						   .collect(toList());
	}

	private Dupla saveDupla(Category categoryBands, List<Dupla> duplas, String writtenWord, Integer time) {
		return saveDupla(categoryBands, duplas, writtenWord, null,time, null);
	}
	
	private Dupla saveDupla(Category categoryBands, List<Dupla> duplas, String writtenWord, String finalWord,Integer time, DuplaState state) {
		Dupla dupla = new Dupla();
		dupla.setCategory(categoryBands);
		dupla.setWrittenWord(writtenWord);
		dupla.setFinalWord(finalWord);
		dupla.setTime(time);
		dupla.setState(state);
		duplas.add(dupla);
		return dupla;
	}

	private Match createMatch(Datastore dataStore, String language,Round lastRound, List<PlayerResult> playerResults, MatchConfig matchConfig) {
		Category categoryService = SpringApplicationContext.getBeanNamed("category", Category.class);
		return createMatch(dataStore, language,lastRound, playerResults, matchConfig,categoryService.getPublicMatchCategories(language));
	}
	
	private Match createMatch(Datastore dataStore, String language,Round lastRound, List<PlayerResult> playerResults, MatchConfig matchConfig,
							  List<Category> categories) {
		Match match = new Match();
		match.setConfig(matchConfig);
		match.setName(null); // TODO ver qué poner de nombre
		match.setState(TO_BE_APPROVED);
		match.setStartDate(DateTime.now().toDate());
		match.setCategories(categories);
		match.setPlayers(playerResults);
		match.setLastRound(lastRound);
		dataStore.save(match);
		return match;
	}
	
	private Letter commonMatchAssertions(String language, Match resultMatch) {
		assertThat(resultMatch.getState()).isEqualTo(TO_BE_APPROVED);
		assertThat(resultMatch.getConfig()).isNotNull();
		assertThat(resultMatch.getConfig().getType()).isEqualTo(PUBLIC_TYPE);
		assertThat(resultMatch.getConfig().getMode()).isEqualTo(NORMAL_MODE);
		assertThat(resultMatch.getConfig().getLanguage()).isEqualTo(language);
		assertThat(resultMatch.getConfig().getNumberOfPlayers()).isEqualTo(3);
		assertThat(resultMatch.getCategories().size()).isEqualTo(DEFAULT_CATEGORIES_NUMBER);
		assertThat(resultMatch.getPlayers()).isNotNull();
		for(PlayerResult playerResult : resultMatch.getPlayers()){
			Player player = playerResult.getPlayer();
			if(player.getNickname().equals("SARASA")){
				assertThat(playerResult.getScore()).isEqualTo(0);
			}
		}
		Round round = resultMatch.getLastRound();
		assertThat(round).isNotNull();
		assertThat(round.getNumber()).isEqualTo(1);
		Letter letter = round.getLetter();
		assertThat(letter).isNotNull();
		return letter;
	}
	
	private PlayerResult savePlayerResult(Datastore datastore, Player player, int score) {
		PlayerResult playerResult = new PlayerResult();
		playerResult.setPlayer(player);
		playerResult.setScore(score);
		datastore.save(playerResult);
		return playerResult;
	}

	private Player savePlayer(Datastore datastore, String nickname, String mail) {
		Player player = new Player();
		player.setNickname(nickname);
		player.setMail(mail);
		datastore.save(player);
		return player;
	}
	
	private MatchConfig createMatchConfig(String language, String mode, String type, int numberOfPlayers, 
										  boolean powerUpsEnabled, int numberOfRounds) {
		MatchConfig matchConfig = new MatchConfig();
		matchConfig.setLanguage(language);
		matchConfig.setMode(mode);
		matchConfig.setType(type);
		matchConfig.setNumberOfPlayers(numberOfPlayers);
		matchConfig.setPowerUpsEnabled(powerUpsEnabled);
		matchConfig.setRounds(numberOfRounds);
		return matchConfig;
	}
	
	private void saveCategory(Datastore datastore, String language,String id,String image,String name){
		Category category = new Category();
		category.setId(id);
		category.setImage(image);
		category.setLanguage(language);
		category.setName(name);
		datastore.save(category);
	}
	
	private void saveCategories(Datastore datastore, String language) {
		saveCategory(datastore, language,"names","nombres_img","Nombres");
		saveCategory(datastore, language,"colors","colores_img","Colores");
		saveCategory(datastore, language,"things","cosas_img","Cosas / Objectos");
		saveCategory(datastore, language,"meals","comidas_img","Comidas y Bebidas");
		saveCategory(datastore, language,"countries","countries_img","Países");
		saveCategory(datastore, language,"animals","animales_img","Animales");
		saveCategory(datastore, language,"sports","deportes_img","Deportes");
		saveCategory(datastore, language,"cities","ciudades_img","Ciudades");
		saveCategory(datastore, language,"clothes","ropa_img","Ropa");
		saveCategory(datastore, language,"instruments","instrumentos_img","Instrumentos Musicales");
		saveCategory(datastore, language,"verbs","verbos_img","Verbos");
		saveCategory(datastore, language,"jobs","trabajos_img","Trabajos");
		saveCategory(datastore, language,"bands","bands_img","Bandas de Música");
	}
}
