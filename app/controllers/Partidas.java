package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Dupla;
import models.Match;
import models.MatchConfig;
import models.PowerUp;
import models.ResultModel;
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
		Match partida = Match.obtenerPartida(idPartida);
		
		PowerUp.generar(partida);
        return ok(Json.toJson(partida));
    }
	
	public static Result nuevaPartidaPublica(){
		JsonNode json = request().body().asJson();
		String idJugador = json.get("id_jugador").asText();
		Integer cantJugadores = json.get("cant_jugadores").asInt();
		String idioma = json.get("idioma").asText();
		
		Match partida = Match.buscarPartida(cantJugadores,idioma);
		if(partida == null){
			partida = Match.crear(cantJugadores,idioma);
		}
		partida.agregarJugador(idJugador);
		PowerUp.generar(partida);
        return ok(Json.toJson(partida));
    }
	
	public static Result nuevaPartidaPrivada(){
		JsonNode json = request().body().asJson();
		String idJugador = json.get("id_jugador").asText();
		JsonNode jsonConfiguracion = json.get("configuracion");
		JsonNode jsonJugadores = json.get("jugadores");
		
		MatchConfig configuracion = Json.fromJson(jsonConfiguracion, MatchConfig.class);
		List<String> jugadores = new ArrayList<String>();
		
		for(JsonNode jsonJugador : jsonJugadores){
			jugadores.add(jsonJugador.asText());
		}
		
		Match partida = Match.crear(idJugador, configuracion,jugadores);
		
		PushUtil.partida(jugadores,partida);
		
        return ok(Json.toJson(partida));
    }
	
	public static Result jugarTurno(){
		JsonNode json = request().body().asJson();
		String idJugador = json.get("id_jugador").asText();
		String idPartida = json.get("id_partida").asText();
		JsonNode jsonCategoriasTurno = json.get("categorias_turno");
		
		List<Dupla> categoriasTurno = new ArrayList<Dupla>();
		
		for(JsonNode jsonCategoriaTurno : jsonCategoriasTurno){
			categoriasTurno.add(Json.fromJson(jsonCategoriaTurno, Dupla.class));
		}
		
		Match partida = Match.obtenerPartida(idPartida);
		
		ResultModel resultado = partida.jugar(idJugador,categoriasTurno);
		
        return ok(Json.toJson(resultado));
    }
	
	public static Result resultadoTurno(String idPartida,Integer numeroTurno){
		ResultModel resultadoTurno = ResultModel.resultadoTurno(idPartida,numeroTurno);
		
		if(resultadoTurno != null){
			return ok(Json.toJson(resultadoTurno));
		}
		
		return notFound();
	}
	
	public static Result resultadoPartida(String idPartida){
		ResultModel resultadoPartida = ResultModel.resultadoPartida(idPartida);
		
		if(resultadoPartida != null){
			return ok(Json.toJson(resultadoPartida));
		}
		
		return notFound();
	}
}
