package tuttiFrutti.controllers;

import static org.fest.assertions.Assertions.assertThat;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import org.junit.Test;

import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import tuttifrutti.models.Match;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
public class Matches {

	@Test
	public void test() {
		running(testServer(9000, fakeApplication()), new Runnable() {
			@Override
			public void run() {
				WSResponse r = WS.url("http://localhost:9000/mclics/ads/MLA/search").get().get(5000L);
				assertThat(r).isNotNull();
				assertThat(r.getStatus()).isEqualTo(OK);
				
				JsonNode jsonNode = r.asJson();
				Match match = Json.fromJson(jsonNode, Match.class);
				
				assertThat(match).isNotNull();

			}
		});
	}

}
