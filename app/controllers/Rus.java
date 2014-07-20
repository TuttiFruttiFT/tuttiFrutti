package controllers;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import models.Player;
import models.Pack;
import play.mvc.Controller;
import play.mvc.Result;
import utils.GoogleUtil;

/**
 * @author rfanego
 */
public class Rus extends Controller {
	public static Result buy(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String packId = json.get("pack_id").asText();
		
		Pack pack = Pack.pack(packId);
		
		GoogleUtil.buy(playerId,pack);
		
		Player.buy(playerId,pack);
		
		return ok();
	}

	public static Result packs(){
		
		List<Pack> packs = Pack.packs();
		
		//TODO ver como crear un json desde una lista
		
		return ok();
	}
}
