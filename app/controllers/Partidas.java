package controllers;

import java.util.ArrayList;
import java.util.List;

import models.CategoriaTurno;
import models.ConfiguracionPartida;
import models.Partida;
import models.PowerUp;
import models.Resultado;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.PushUtil;

import com.fasterxml.jackson.databind.JsonNode;

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
		JsonNode jsonConfiguracion = json.get("configuracion");
		JsonNode jsonJugadores = json.get("jugadores");
		
		ConfiguracionPartida configuracion = Json.fromJson(jsonConfiguracion, ConfiguracionPartida.class);
		List<String> jugadores = new ArrayList<String>();
		
		for(JsonNode jsonJugador : jsonJugadores){
			jugadores.add(jsonJugador.asText());
		}
		
		Partida partida = Partida.crear(idJugador, configuracion,jugadores);
		
		PushUtil.partida(jugadores,partida);
		
        return ok(Json.toJson(partida));
    }
	
	public static Result jugarTurno() {
		JsonNode json = request().body().asJson();
		String idJugador = json.get("id_jugador").asText();
		String idPartida = json.get("id_partida").asText();
		JsonNode jsonCategoriasTurno = json.get("categorias_turno");
		
		List<CategoriaTurno> categoriasTurno = new ArrayList<CategoriaTurno>();
		
		for(JsonNode jsonCategoriaTurno : jsonCategoriasTurno){
			categoriasTurno.add(Json.fromJson(jsonCategoriaTurno, CategoriaTurno.class));
		}
		
		Partida partida = Partida.obtenerPartida(idPartida);
		
		Resultado resultado = partida.jugar(idJugador,categoriasTurno);
		
        return ok(Json.toJson(resultado));
    }
}
