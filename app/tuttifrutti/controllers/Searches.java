package tuttifrutti.controllers;

import static play.libs.Json.parse;
import static tuttifrutti.utils.JsonUtil.parseListToJson;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Player;
import tuttifrutti.services.PlayerService;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class Searches extends Controller {
	@Autowired
	private PlayerService playerService;
	
	public Result searchPlayers(String playerId,String word) {
		List<Player> players = new ArrayList<>();
		
		for(Player player : playerService.searchPlayers(word)){
			if(!player.getId().toString().equals(playerId)){	
				players.add(player.reducedPlayer());
			}
		}
		
        return ok(parse(parseListToJson(players)));
    }
	
	public Result searchOthers(String playerId) {
		List<Player> players = playerService.searchOthersPlayers(playerId);
		
        return ok(parse(parseListToJson(players)));
    }
}
