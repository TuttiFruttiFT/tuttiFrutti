package tuttifrutti.controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import play.libs.ws.WS;
import play.libs.ws.WSResponse;

/**
 * @author rfanego
 *
 */
public class CategoryLoaderTest {

	@Test
	public void test() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {			
			WSResponse r = WS.url("http://localhost:9000/category/loader").put("").get(1,TimeUnit.DAYS);
			assertThat(r.getStatus()).isEqualTo(OK);
		});
	}

}
