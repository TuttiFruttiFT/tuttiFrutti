package tuttifrutti.utils;

import static java.util.stream.Collectors.toList;
import static org.joda.time.DateTime.now;
import static tuttifrutti.models.enums.MatchState.TO_BE_APPROVED;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import tuttifrutti.models.Category;
import tuttifrutti.models.Dupla;
import tuttifrutti.models.Letter;
import tuttifrutti.models.LetterWrapper;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.MatchName;
import tuttifrutti.models.Player;
import tuttifrutti.models.PlayerResult;
import tuttifrutti.models.Round;
import tuttifrutti.models.Turn;
import tuttifrutti.models.enums.DuplaState;
import tuttifrutti.models.enums.MatchMode;
import tuttifrutti.models.enums.MatchState;
import tuttifrutti.models.enums.MatchType;

public class TestUtils {
	public static List<Category> getCategoriesFromDuplas(List<Dupla> duplas, String language) {
		Category categoryService = SpringApplicationContext.getBeanNamed("category", Category.class);
		List<Category> categories = categoryService.categories(language);
		
		return categories.stream().filter(category -> duplas.stream().anyMatch(dupla -> dupla.getCategory().getId().equals(category.getId())))
						   .collect(toList());
	}

	public static Dupla saveDupla(Category categoryBands, List<Dupla> duplas, String writtenWord, Integer time) {
		return saveDupla(categoryBands, duplas, writtenWord, null,time, null);
	}
	
	public static Dupla saveDupla(Category categoryBands, List<Dupla> duplas, String writtenWord, String finalWord,Integer time, DuplaState state) {
		Dupla dupla = new Dupla();
		dupla.setCategory(categoryBands);
		dupla.setWrittenWord(writtenWord);
		dupla.setFinalWord(finalWord);
		dupla.setTime(time);
		dupla.setState(state);
		duplas.add(dupla);
		return dupla;
	}

	public static Match createMatch(Datastore dataStore, String language,Round lastRound, List<PlayerResult> playerResults, MatchConfig matchConfig, MatchName matchName) {
		Category categoryService = SpringApplicationContext.getBeanNamed("category", Category.class);
		return createMatch(dataStore, language,lastRound, playerResults, matchConfig,categoryService.getPublicMatchCategories(language), TO_BE_APPROVED, matchName);
	}
	
	public static Match createMatch(Datastore dataStore, String language,Round lastRound, List<PlayerResult> playerResults, MatchConfig matchConfig,
							  List<Category> categories, MatchState matchState, MatchName matchName) {
		Match match = new Match();
		match.setConfig(matchConfig);
		match.setMatchName(matchName);
		match.setName(matchName.getValue());
		match.setState(matchState);
		match.setStartDate(now().toDate());
		match.setCategories(categories);
		match.setPlayerResults(playerResults);
		match.setLastRound(lastRound);
		dataStore.save(match);
		return match;
	}
	
	public static PlayerResult savePlayerResult(Datastore datastore, Player player, int score) {
		PlayerResult playerResult = new PlayerResult();
		playerResult.setPlayer(player);
		playerResult.setScore(score);
		datastore.save(playerResult);
		return playerResult;
	}

	public static Player savePlayer(Datastore datastore, String mail) {
		return savePlayer(datastore, mail, mail);
	}
	
	public static Player savePlayer(Datastore datastore, String mail, String password) {
		Player player = new Player();
		String[] splittedMail = mail.split("@");
		player.setNickname((splittedMail.length > 0) ? splittedMail[0] : mail);
		player.setMail(mail);
		player.setPassword(password);
		datastore.save(player);
		return player;
	}
	
	public static MatchConfig createMatchConfig(String language, MatchMode mode, MatchType type, int numberOfPlayers, boolean powerUpsEnabled, int numberOfRounds) {
		MatchConfig matchConfig = new MatchConfig();
		matchConfig.setLanguage(language);
		matchConfig.setMode(mode);
		matchConfig.setType(type);
		matchConfig.setNumberOfPlayers(numberOfPlayers);
		matchConfig.setPowerUpsEnabled(powerUpsEnabled);
		matchConfig.setRounds(numberOfRounds);
		matchConfig.setCurrentTotalNumberOfPlayers(numberOfPlayers);
		return matchConfig;
	}
	
	public static void saveCategory(Datastore datastore, String language,String id,String image,String name){
		Category category = new Category();
		category.setId(id);
		category.setImage(image);
		category.setLanguage(language);
		category.setName(name);
		datastore.save(category);
	}
	
	public static void saveCategories(Datastore datastore, String language) {
		saveCategory(datastore, language,"names","nombres_img","Nombres");
		saveCategory(datastore, language,"colors","colores_img","Colores");
		saveCategory(datastore, language,"things","cosas_img","Cosas / Objectos");
		saveCategory(datastore, language,"meals","comidas_img","Comidas y Bebidas");
		saveCategory(datastore, language,"countries","countries_img","Países");
		saveCategory(datastore, language,"animals","animales_img","Animales");
		saveCategory(datastore, language,"sports","deportes_img","Deportes");
		saveCategory(datastore, language,"cities","ciudades_img","Ciudades");
		saveCategory(datastore, language,"clothes","ropa_img","Ropa");
		saveCategory(datastore, language,"musical_styles","musical_styles_img","Estilos Musicales");
		saveCategory(datastore, language,"musical_instruments","musical_instruments_img","Instrumentos Musicales");
		saveCategory(datastore, language,"verbs","verbos_img","Verbos");
		saveCategory(datastore, language,"jobs","trabajos_img","Trabajos");
		saveCategory(datastore, language,"bands","bands_img","Bandas de Música");
	}

	public static Round createRound(Turn turn, int roundNumber, Letter letter) {
		Round lastRound = new Round();
		lastRound.setNumber(roundNumber);
		lastRound.setLetter(new LetterWrapper(letter));
		lastRound.addTurn(turn);
		return lastRound;
	}

	public static Turn createTurn(String playerId, Integer endTime, Integer score, List<Dupla> duplas2) {
		Turn turn = new Turn();
		Player player = new Player();
		player.setId(new ObjectId(playerId));
		turn.setPlayer(player);
		turn.setEndTime(endTime);
		turn.setScore(score);
		turn.setDuplas(duplas2);
		return turn;
	}
}
