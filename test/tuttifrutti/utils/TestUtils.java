package tuttifrutti.utils;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.joda.time.DateTime.now;
import static tuttifrutti.models.Letter.A;
import static tuttifrutti.models.enums.LanguageType.ES;
import static tuttifrutti.models.enums.MatchState.TO_BE_APPROVED;
import static tuttifrutti.models.enums.MatchType.PUBLIC;
import static tuttifrutti.utils.CategoryType.animals;
import static tuttifrutti.utils.CategoryType.bands;
import static tuttifrutti.utils.CategoryType.cities;
import static tuttifrutti.utils.CategoryType.clothes;
import static tuttifrutti.utils.CategoryType.colors;
import static tuttifrutti.utils.CategoryType.countries;
import static tuttifrutti.utils.CategoryType.jobs;
import static tuttifrutti.utils.CategoryType.meals;
import static tuttifrutti.utils.CategoryType.musical_instruments;
import static tuttifrutti.utils.CategoryType.musical_styles;
import static tuttifrutti.utils.CategoryType.names;
import static tuttifrutti.utils.CategoryType.sports;
import static tuttifrutti.utils.CategoryType.things;
import static tuttifrutti.utils.CategoryType.verbs;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;

import play.Logger;
import play.cache.Cache;
import tuttifrutti.cache.AlphabetCache;
import tuttifrutti.models.Alphabet;
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
		match.setModifiedDate(now().toDate());
		match.setCategories(categories);
		match.setPlayerResults(playerResults);
		match.setLastRound(lastRound);
		match.setAlphabet(new Alphabet(ES.toString(),asList(Letter.values())));
		dataStore.save(match);
		return match;
	}
	
	public static PlayerResult savePlayerResult(Datastore datastore, Player player, int score) {
		PlayerResult playerResult = new PlayerResult(player,score,0,true,true,now().toDate(),0);
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
		saveCategory(datastore, language,names.toString(),"nombres_img","Nombres");
		saveCategory(datastore, language,colors.toString(),"colores_img","Colores");
		saveCategory(datastore, language,things.toString(),"cosas_img","Cosas / Objectos");
		saveCategory(datastore, language,meals.toString(),"comidas_img","Comidas y Bebidas");
		saveCategory(datastore, language,countries.toString(),"countries_img","Países");
		saveCategory(datastore, language,animals.toString(),"animales_img","Animales");
		saveCategory(datastore, language,sports.toString(),"deportes_img","Deportes");
		saveCategory(datastore, language,cities.toString(),"ciudades_img","Ciudades");
		saveCategory(datastore, language,clothes.toString(),"ropa_img","Ropa");
		saveCategory(datastore, language,musical_styles.toString(),"musical_styles_img","Estilos Musicales");
		saveCategory(datastore, language,musical_instruments.toString(),"musical_instruments_img","Instrumentos Musicales");
		saveCategory(datastore, language,verbs.toString(),"verbos_img","Verbos");
		saveCategory(datastore, language,jobs.toString(),"trabajos_img","Trabajos");
		saveCategory(datastore, language,bands.toString(),"bands_img","Bandas de Música");
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
	
	public static void sleep(long timeInMillis) {
		try {
			Thread.sleep(timeInMillis);
		} catch (Exception e) {
			Logger.error("In Sleep",e);
		}
	}
	
	public static void cleanAlphabetCache() {
		for(CategoryType category : CategoryType.values()){
			Cache.set(AlphabetCache.PREFIX + category.toString(), null);
		}
	}
	
	public static PlayerResult testPlayerResult(Datastore datastore){
		return savePlayerResult(datastore, savePlayer(datastore, "sarasas@sarasa.com"), 0);
	}
	
	public static Match createMatch(Datastore datastore, MatchMode matchMode, Integer numberOfPlayers){
		String language = "ES";
		saveCategories(datastore, language);
		
		MatchConfig matchConfig = createMatchConfig(language, matchMode, PUBLIC, numberOfPlayers, false, 25);
		Round round = new Round(1,new LetterWrapper(A));
		List<PlayerResult> playerResultList = new ArrayList<PlayerResult>();
		for(int i = 0;i < numberOfPlayers;i++){
			playerResultList.add(testPlayerResult(datastore));
		}
		Match match = createMatch(datastore, language, round,playerResultList, matchConfig, new MatchName(numberOfPlayers));
		return match;
	}
	
}
