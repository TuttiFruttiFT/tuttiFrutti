package tuttifrutti.controllers;

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
	
	public Result searchPlayers(String palabraABuscar) {
		Player players = playerService.getPlayers(palabraABuscar);
		
		//TODO ver como crear un json desde una lista
		
        return ok();
    }
	
	public Result searchOthers(String playerId) {
		Player players = playerService.getOthersPlayers(playerId);
		
		//TODO ver como crear un json desde una lista
		
        return ok();
    }
}
