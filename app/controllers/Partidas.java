package controllers;

import com.fasterxml.jackson.databind.JsonNode;

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
		//TODO Obtener la partida, la última ronda y cargar los powerUps
		Partida partida = Partida.obtenerPartida(idPartida);
		
		PowerUp.generar(partida);
        return ok(Json.toJson(partida));
    }
	
	public static Result nuevaPartidaPublica() {
		JsonNode json = request().body().asJson();
		String idJugador = json.get("id_jugador").asText();
		Integer cantJugadores = json.get("cant_jugadores").asInt();
		String idioma = json.get("idioma").asText();
		
		Partida partida = Partida.buscarPartida(cantJugadores,idioma);
		if(partida == null){
			partida = Partida.crear(cantJugadores,idioma);
		}
		partida.agregarJugador(idJugador);
		PowerUp.generar(partida);
        return ok(Json.toJson(partida));
    }
	
	public static Result nuevaPartidaPrivada() {
		JsonNode json = request().body().asJson();
		String idJugador = json.get("id_jugador").asText();
		JsonNode configuracion = json.get("configuracion");
        return ok();
    }
	
	public static Result jugarTurno() {
		
        return ok();
    }
}
