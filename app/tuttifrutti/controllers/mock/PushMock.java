package tuttifrutti.controllers.mock;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class PushMock extends Controller {

	public Result sendGCMMessage(){
		return ok(Json.parse("{ \"multicast_id\": 108, \"success\": 1, \"failure\": 0, \"canonical_ids\": 0, \"results\": [{ \"message_id\": \"1:08\" } ] }"));
	}
}
