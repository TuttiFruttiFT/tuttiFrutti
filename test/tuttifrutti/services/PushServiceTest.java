package tuttifrutti.services;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.utils.TestUtils.savePlayer;

import org.junit.Test;
import org.mongodb.morphia.Datastore;

import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import tuttifrutti.models.Device;
import tuttifrutti.models.Player;
import tuttifrutti.utils.SpringApplicationContext;

public class PushServiceTest {

	@Test
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
}
