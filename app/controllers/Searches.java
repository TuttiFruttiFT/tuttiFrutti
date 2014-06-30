package controllers;

import models.Player;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author rfanego
 */
public class Searches extends Controller {
	public static Result searchPlayers(String palabraABuscar) {
		Player players = Player.getPlayers(palabraABuscar);
		
		//TODO ver como crear un json desde una lista
		
        return ok();
    }
	
	public static Result searchOthers(String playerId) {
		Player players = Player.getOthersPlayers(playerId);
		
		//TODO ver como crear un json desde una lista
		
        return ok();
    }
}
