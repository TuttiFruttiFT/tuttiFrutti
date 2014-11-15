package tuttifrutti.controllers;

import static play.libs.Json.parse;
import static tuttifrutti.utils.JsonUtil.parseListToJson;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Pack;
import tuttifrutti.services.PlayerService;
import tuttifrutti.utils.GoogleUtil;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class Rus extends Controller {
	@Autowired
	private PlayerService playerService;
	
	public Result buy(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		Pack pack = Json.fromJson(json.get("pack"), Pack.class);
		
		GoogleUtil.buy(playerId,pack);
		
		playerService.buy(playerId,pack);
		
		return ok();
	}

	public Result packs(){
		List<Pack> packs = Pack.packs();
		
		return ok(parse(parseListToJson(packs)));
	}
}
