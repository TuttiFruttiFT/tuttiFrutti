package tuttifrutti.models;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.Letter.S;
import static tuttifrutti.models.enums.DuplaState.CORRECTED;
import static tuttifrutti.models.enums.DuplaState.PERFECT;
import static tuttifrutti.models.enums.MatchMode.N;
import static tuttifrutti.models.enums.MatchState.PLAYER_TURN;
import static tuttifrutti.models.enums.MatchType.PUBLIC;
import static tuttifrutti.utils.TestUtils.createMatch;
import static tuttifrutti.utils.TestUtils.createMatchConfig;
import static tuttifrutti.utils.TestUtils.createRound;
import static tuttifrutti.utils.TestUtils.createTurn;
import static tuttifrutti.utils.TestUtils.getCategoriesFromDuplas;
import static tuttifrutti.utils.TestUtils.saveDupla;
import static tuttifrutti.utils.TestUtils.savePlayer;
import static tuttifrutti.utils.TestUtils.savePlayerResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;

import tuttifrutti.utils.SpringApplicationContext;

public class MatchTest {

	@Test
	public void test() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			String language = "ES";
			int roundNumber = 1;
			
			Player player1 = savePlayer(dataStore, "sarasas@sarasa.com");
			Player player2 = savePlayer(dataStore, "sarasas2@sarasa.com");
			Player player3 = savePlayer(dataStore, "sarasas3@sarasa.com");

			PlayerResult playerResult1 = savePlayerResult(dataStore, player1, 35);
			PlayerResult playerResult2 = savePlayerResult(dataStore, player2, 40);
			PlayerResult playerResult3 = savePlayerResult(dataStore, player3, 50);
			
			List<Dupla> duplas2 = new ArrayList<>();
			saveDupla(new Category("animals"), duplas2, "surubi", "surubi",11, PERFECT);
			saveDupla(new Category("musical_styles"), duplas2, "salsa", "salsa",17, PERFECT);
			saveDupla(new Category("verbs"), duplas2, "sentir", "servir",8, CORRECTED);
			saveDupla(new Category("sports"), duplas2, "softball", "softból",26, CORRECTED);
			saveDupla(new Category("meals"), duplas2, "sandia", "savia de abedul",35, CORRECTED);
			saveDupla(new Category("colors"), duplas2, "sarlanga", "",40, PERFECT);
			
			Turn turn2 = createTurn(player2.getId().toString(), 45, 0, duplas2);
			
			Round lastRound = createRound(turn2, roundNumber, S);
			
			MatchConfig matchConfig = createMatchConfig(language, N, PUBLIC, 3, true, 25);
			Match match = createMatch(dataStore, language, lastRound,Arrays.asList(playerResult1,playerResult2,playerResult3), matchConfig,
									  getCategoriesFromDuplas(duplas2, language), PLAYER_TURN, new MatchName(3));
			
			List<Player> playersThatHaveNotPlayed = match.playersThatHaveNotPlayedExcept(player2.getId().toString());
			
			assertThat(match.bestBpmbpt(turn2)).isTrue();
			assertThat(playersThatHaveNotPlayed.size()).isEqualTo(2);
			
			Turn turn1 = createTurn(player1.getId().toString(), 40, 0, new ArrayList<>());
			turn1.setBpmbpt(true);
			lastRound.addTurn(turn1);
			
			List<Player> playersThatHaveNotPlayed2 = match.playersThatHaveNotPlayedExcept(player2.getId().toString());
			
			assertThat(playersThatHaveNotPlayed2.size()).isEqualTo(1);
			assertThat(match.bestBpmbpt(turn2)).isFalse();
			assertThat(match.bestBpmbpt(turn1)).isTrue();
			
			turn1.setBpmbpt(false);
			
			assertThat(match.bestBpmbpt(turn2)).isTrue();
		});
	}

}
