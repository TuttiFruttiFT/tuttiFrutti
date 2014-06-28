package controllers;

import models.Player;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author rfanego
 */
public class Busquedas extends Controller {
	public static Result buscarJugadores(String palabraABuscar) {
		Player jugadores = Player.obtenerJugadores(palabraABuscar);
		
		//TODO ver como crear un json desde una lista
		
        return ok();
    }
	
	public static Result buscarOtros(String idJugador) {
		Player jugadores = Player.obtenerOtrosJugadores(idJugador);
		
		//TODO ver como crear un json desde una lista
		
        return ok();
    }
}
