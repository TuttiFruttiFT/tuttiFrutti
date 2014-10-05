package tuttifrutti.services;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.Letter.A;
import static tuttifrutti.models.enums.MatchMode.N;
import static tuttifrutti.models.enums.MatchType.PUBLIC;
import static tuttifrutti.utils.TestUtils.createMatch;
import static tuttifrutti.utils.TestUtils.createMatchConfig;
import static tuttifrutti.utils.TestUtils.saveCategories;
import static tuttifrutti.utils.TestUtils.savePlayer;
import static tuttifrutti.utils.TestUtils.savePlayerResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.mongodb.morphia.Datastore;

import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import tuttifrutti.models.Device;
import tuttifrutti.models.LetterWrapper;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.Player;
import tuttifrutti.models.PlayerResult;
import tuttifrutti.models.Round;
import tuttifrutti.utils.SpringApplicationContext;

public class PushServiceTest {

//	@Test
	public void registerDevice() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			
			Player player = savePlayer(dataStore, "sarasas@sarasa.com");
			
			WSResponse r = WS.url("http://localhost:9000/player/device").setContentType("application/json")
					   .post("{\"registration_id\":\"87feyw08f7yer8\",\"hwid\":\"1122eedd\",\"player_id\":\"" + player.getId().toString()  + "\"}")
					   .get(5000000L);
			
			assertThat(r.getStatus()).isEqualTo(OK);
			
			Player modifiedPlayer = dataStore.get(Player.class,player.getId());
			assertThat(modifiedPlayer).isNotNull();
			
			Device device = modifiedPlayer.getDevices().get(0);
			
			assertThat(device.getHardwareId()).isEqualTo("1122eedd");
			assertThat(device.getRegistrationId()).isEqualTo("87feyw08f7yer8");
		});
	}
	
	@Test
	public void test() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			PushService pushService = SpringApplicationContext.getBeanNamed("pushService", PushService.class);
			String language = "ES";
			
			Round lastRound = new Round();
			lastRound.setNumber(1);
			lastRound.setLetter(new LetterWrapper(A));
			
			Player player = savePlayer(dataStore, "sarasas@sarasa.com");
			List<Device> devices = new ArrayList<>();
			devices.add(new Device("Aaabbb3425","ooooPPPP98543"));
			player.setDevices(devices);
			PlayerResult playerResult = savePlayerResult(dataStore, player, 0);
			
			saveCategories(dataStore, language);

			MatchConfig matchConfig = createMatchConfig(language, N, PUBLIC, 2, false, 25);
			Match match = createMatch(dataStore, language, lastRound,Arrays.asList(playerResult), matchConfig);

			pushService.roundResult(match, 1);
			System.out.println("SALIO");
			
//			try {
//				Thread.sleep(5000L);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		});
	}
}
