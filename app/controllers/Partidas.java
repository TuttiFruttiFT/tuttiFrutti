package controllers;

import models.Partida;
import models.PowerUp;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author rfanego
 */
public class Partidas extends Controller {
	public static Result obtenerPartida(String idPartida) {
		/*
		 * TODO Obtener la partida, la última ronda y cargar los powerUps
		 */
		Partida partida = Partida.obtenerPartida(idPartida);
		
		PowerUp.generar(partida);
        return ok(Json.toJson(partida));
    }
	
	public static Result nuevaPartidaPublica() {
		
        return ok();
    }
	
	public static Result nuevaPartidaPrivada() {
		
        return ok();
    }
	
	public static Result jugarTurno() {
		
        return ok();
    }
}
