package tuttiFrutti.controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.Category.DEFAULT_CATEGORIES_NUMBER;
import static tuttifrutti.models.Match.TO_BE_APPROVED;
import static tuttifrutti.models.MatchConfig.NORMAL_MODE;
import static tuttifrutti.models.MatchConfig.PUBLIC_TYPE;

import java.util.Arrays;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mongodb.morphia.Datastore;

import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import tuttifrutti.models.Category;
import tuttifrutti.models.Letter;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.Player;
import tuttifrutti.models.PlayerResult;
import tuttifrutti.models.Round;
import tuttifrutti.utils.SpringApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
public class MatchesTest {

	@Test
	public void searchPublicMatchReturnsExistingMatch() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			Category categoryService = SpringApplicationContext.getBeanNamed("category", Category.class);
			String language = "ES";
			
			Player player = savePlayer(dataStore, "SARASA", "sarasas@sarasa.com");
			Player player2 = savePlayer(dataStore, "SARASA2", "sarasas2@sarasa.com");

			savePlayerResult(dataStore, player, 10);
			PlayerResult playerResult2 = savePlayerResult(dataStore, player2, 10);
			
			saveCategories(dataStore, language);

			Match match = new Match();
			MatchConfig matchConfig = createMatchConfig(language, NORMAL_MODE, PUBLIC_TYPE, 3, true, 25);
			match.setConfig(matchConfig);
			match.setName(null); // TODO ver qué poner de nombre
			match.setState(TO_BE_APPROVED);
			match.setStartDate(DateTime.now().toDate());
			match.setCategories(categoryService.getPublicMatchCategories(language));
			match.setPlayers(Arrays.asList(playerResult2));
			dataStore.save(match);
			
			String playerId = player.getId().toString();
			WSResponse r = WS.url("http://localhost:9000/match/public").setContentType("application/json")
							 .post("{\"player_id\" : \"" + playerId + "\", \"config\":" 
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
			assertThat(resultMatch.getConfig().getType()).isEqualTo(PUBLIC_TYPE);
			assertThat(resultMatch.getConfig().getMode()).isEqualTo(NORMAL_MODE);
			assertThat(resultMatch.getConfig().getLanguage()).isEqualTo(language);
			assertThat(resultMatch.getConfig().getNumberOfPlayers()).isEqualTo(3);
			assertThat(resultMatch.getCategories().size()).isEqualTo(DEFAULT_CATEGORIES_NUMBER);
			Round round = resultMatch.getLastRound();
			assertThat(round).isNotNull();
			assertThat(round.getNumber()).isEqualTo(1);
			Letter letter = round.getLetter();
			assertThat(letter).isNotNull();
			System.out.println("Letra: " + letter.getLetter());
		});
	}

	@Test
	public void searchPublicMatchReturnsCreatedMatch() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			Player player = savePlayer(dataStore, null, null);
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
			assertThat(resultMatch.getState()).isEqualTo(TO_BE_APPROVED);
			assertThat(resultMatch.getConfig()).isNotNull();
			assertThat(resultMatch.getConfig().getType()).isEqualTo(MatchConfig.PUBLIC_TYPE);
			assertThat(resultMatch.getConfig().getMode()).isEqualTo(MatchConfig.NORMAL_MODE);
			assertThat(resultMatch.getConfig().getLanguage()).isEqualTo(language);
			assertThat(resultMatch.getConfig().getNumberOfPlayers()).isEqualTo(3);
			assertThat(resultMatch.getCategories().size()).isEqualTo(DEFAULT_CATEGORIES_NUMBER);
			Round round = resultMatch.getLastRound();
			assertThat(round).isNotNull();
			assertThat(round.getNumber()).isEqualTo(1);
			Letter letter = round.getLetter();
			assertThat(letter).isNotNull();
			System.out.println("Letra: " + letter.getLetter());
		});
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
	
	private void saveCategories(Datastore datastore, String language) {
		Category categoryNombres = new Category();
		categoryNombres.setImage("nombres_img");
		categoryNombres.setLanguage(language);
		categoryNombres.setName("Nombres");
		datastore.save(categoryNombres);
		
		Category categoryColores = new Category();
		categoryColores.setImage("colores_img");
		categoryColores.setLanguage(language);
		categoryColores.setName("Colores");
		datastore.save(categoryColores);
		
		Category categoryCosas = new Category();
		categoryCosas.setImage("cosas_img");
		categoryCosas.setLanguage(language);
		categoryCosas.setName("Cosas / Objectos");
		datastore.save(categoryCosas);

		Category categoryComidad = new Category();
		categoryComidad.setImage("comidad_img");
		categoryComidad.setLanguage(language);
		categoryComidad.setName("Comidas y Bebidas");
		datastore.save(categoryComidad);
		
		Category categoryPaises = new Category();
		categoryPaises.setImage("nombres_img");
		categoryPaises.setLanguage(language);
		categoryPaises.setName("Paises");
		datastore.save(categoryPaises);
		
		Category categoryAnimales = new Category();
		categoryAnimales.setImage("animales_img");
		categoryAnimales.setLanguage(language);
		categoryAnimales.setName("Animales");
		datastore.save(categoryAnimales);
		
		Category categoryDeportes = new Category();
		categoryDeportes.setImage("deportes_img");
		categoryDeportes.setLanguage(language);
		categoryDeportes.setName("Deportes");
		datastore.save(categoryDeportes);
		
		Category categoryCiudades = new Category();
		categoryCiudades.setImage("ciudades_img");
		categoryCiudades.setLanguage(language);
		categoryCiudades.setName("Ciudades");
		datastore.save(categoryCiudades);
		
		Category categoryRopa = new Category();
		categoryRopa.setImage("ropa_img");
		categoryRopa.setLanguage(language);
		categoryRopa.setName("Ropa");
		datastore.save(categoryRopa);
		
		Category categoryInstrumentos = new Category();
		categoryInstrumentos.setImage("instrumentos_img");
		categoryInstrumentos.setLanguage(language);
		categoryInstrumentos.setName("Instrumentos");
		datastore.save(categoryInstrumentos);
		
		Category categoryVerbos = new Category();
		categoryVerbos.setImage("verbos_img");
		categoryVerbos.setLanguage(language);
		categoryVerbos.setName("Verbos");
		datastore.save(categoryVerbos);
		
		Category categoryTrabajos = new Category();
		categoryTrabajos.setImage("trabajos_img");
		categoryTrabajos.setLanguage(language);
		categoryTrabajos.setName("Trabajos");
		datastore.save(categoryTrabajos);
	}
}
