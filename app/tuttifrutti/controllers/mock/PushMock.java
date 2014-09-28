package tuttifrutti.controllers.mock;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class PushMock extends Controller {

	public Result createTargetedMessage(){
		return ok(Json.parse("{\"status_code\":200,\"status_message\":\"OK\",\"response\":{\"skipped\":[]}}"));
	}
	
	public Result registerDevice(){
		return ok(Json.parse("{\"status_code\":200,\"status_message\":\"OK\",\"response\":{\"skipped\":[]}}"));
	}
	
	public Result setTags(){
		return ok(Json.parse("{\"status_code\":200,\"status_message\":\"OK\",\"response\":{\"skipped\":[]}}"));
	}
	
	public Result unregisterDevice(){
		return ok(Json.parse("{\"status_code\":200,\"status_message\":\"OK\",\"response\":{\"skipped\":[]}}"));
	}
}
