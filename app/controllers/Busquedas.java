package controllers;

import models.Jugador;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author rfanego
 */
public class Busquedas extends Controller {
	public static Result buscarJugadores(String palabraABuscar) {
		Jugador jugadores = Jugador.obtenerJugadores(palabraABuscar);
		
		//TODO ver como crear un json desde una lista
		
        return ok();
    }
	
	public static Result buscarOtros(String idJugador) {
		Jugador jugadores = Jugador.obtenerOtrosJugadores(idJugador);
		
		//TODO ver como crear un json desde una lista
		
        return ok();
    }
}
