package tuttifrutti.services;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import org.junit.Test;
import org.mongodb.morphia.Datastore;

import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import tuttifrutti.models.Device;
import tuttifrutti.utils.SpringApplicationContext;

public class PushServiceTest {

	@Test
	public void registerDevice() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			
			WSResponse r = WS.url("http://localhost:9000/player/device").setContentType("application/json")
					   .post("{\"push_token\":\"87feyw08f7yer8\",\"hwid\":\"1122eedd\",\"player_id\":\"541f1b66e4b01fc869ae8af5\"}")
					   .get(5000000L);
			
			assertThat(r.getStatus()).isEqualTo(OK);
			
			Device device = dataStore.get(Device.class,"541f1b66e4b01fc869ae8af5");
			assertThat(device).isNotNull();
			
			assertThat(device.getHardwareId()).isEqualTo("1122eedd");
			assertThat(device.getPlayerId().toString()).isEqualTo("541f1b66e4b01fc869ae8af5");
			assertThat(device.getPushToken()).isEqualTo("87feyw08f7yer8");
		});
	}

	@Test
	public void updateDeviceInfo() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore dataStore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			//TODO implementar
		});
	}
	
}
