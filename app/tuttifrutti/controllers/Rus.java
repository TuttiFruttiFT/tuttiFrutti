package tuttifrutti.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;

import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Pack;
import tuttifrutti.models.Player;
import tuttifrutti.utils.GoogleUtil;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class Rus extends Controller {
	@Autowired
	private Player playerService;
	
	public Result buy(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String packId = json.get("pack_id").asText();
		
		Pack pack = Pack.pack(packId);
		
		GoogleUtil.buy(playerId,pack);
		
		playerService.buy(playerId,pack);
		
		return ok();
	}

	public Result packs(){
		
		List<Pack> packs = Pack.packs();
		
		//TODO ver como crear un json desde una lista
		
		return ok();
	}
}
