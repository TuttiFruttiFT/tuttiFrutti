package tuttifrutti.controllers.mock;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class PushMock extends Controller {

	@BodyParser.Of(BodyParser.Json.class)
	public Result sendGCMMessage(){
		JsonNode json = request().body().asJson();		
		System.out.println("GCM: " + json.toString());
		return ok(Json.parse("{\"multicast_id\":108, \"success\": 1, \"failure\": 0, \"canonical_ids\": 0, \"results\": [{ \"message_id\": \"1:08\"}]}"));
	}
}
