package tuttifrutti.controllers;

import static play.libs.Json.parse;
import static tuttifrutti.utils.JsonUtil.parseListToJson;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Player;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class Searches extends Controller {
	@Autowired
	private Player playerService;
	
	public Result searchPlayers(String playerId,String palabraABuscar) {
		List<Player> players = new ArrayList<>();
		
		for(Player player : playerService.searchPlayers(palabraABuscar)){
			Player reducedPlayer = new Player();
			reducedPlayer.setId(player.getId());
			reducedPlayer.setNickname(player.getNickname());
			reducedPlayer.setImage(player.getImage());
			players.add(reducedPlayer);
		}
		
        return ok(parse(parseListToJson(players)));
    }
	
	public Result searchOthers(String playerId) {
		List<Player> players = playerService.searchOthersPlayers(playerId);
		
        return ok(parse(parseListToJson(players)));
    }
}
