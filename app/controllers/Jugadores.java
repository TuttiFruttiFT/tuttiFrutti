package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Player;
import models.Match;
import models.views.PartidaActiva;

import org.apache.commons.lang3.StringUtils;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.FacebookUtil;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
public class Jugadores extends Controller {
	public static Result registrar() {
		JsonNode json = request().body().asJson();
		if(json == null){
			return badRequest();
		}else{
			String mail = json.get("mail").asText();
			String clave = json.get("clave").asText();
			String facebookId = json.get("facebookId").asText();
			String twitterId = json.get("twitterId").asText();

			Player jugador = null;
			
			if(StringUtils.isNotEmpty(mail) && StringUtils.isNotEmpty(clave)){
				jugador = Player.registrarMail(mail,clave);
			}else if(StringUtils.isNotEmpty(facebookId)){
				jugador = Player.registrarFacebook(facebookId);
			}else if(StringUtils.isNotEmpty(twitterId)){
				jugador = Player.registrarTwitter(twitterId);
			}
			
			if(jugador != null){
				return ok(Json.toJson(jugador));
			}

			return badRequest();
		}
    }
	
	public static Result validar(String mail,String clave,String facebookId,String twitterId) {
		Player jugador = null;
		if(StringUtils.isNotEmpty(mail) && StringUtils.isNotEmpty(clave)){
			jugador = Player.validacionMail(mail, clave);
		}else if(StringUtils.isNotEmpty(facebookId)){
			jugador = Player.validacionFacebook(facebookId);
		}else if(StringUtils.isNotEmpty(twitterId)){
			jugador = Player.validacionTwitter(twitterId);
		}else{
			return badRequest();
		}
		
		if(jugador !=  null){
			return ok(Json.toJson(jugador));
		}
		
		return notFound();
    }
	
	public static Result editarPerfil() {
		JsonNode json = request().body().asJson();
		
		if(Player.editarPerfil(json)){
			return ok();
		}
		
		return badRequest();
	}
	
	public static Result obtenerJugador(String idJugador) {
		Player jugador = Player.obtenerJugador(idJugador);
		
		if(jugador != null){
			return ok(Json.toJson(jugador));
		}
		
		return badRequest();
    }
	
	public static Result sincronizar(String idJugador) {
		/* TODO
		 * Buscar jugador
		 * Buscar partidas con idJugador en estado distinto a PARTIDA_FINALIZADA
		 * Obtener de cada partida, la letra de la última ronda y la cantidad de rondas que faltan
		 * Compaginar respuesta
		 */
		Player jugador = Player.obtenerJugador(idJugador);
		if(jugador == null){
			return badRequest();
		}
		
		List<PartidaActiva> partidasActivas = Match.obtenerPartidasActivas(idJugador);

		//TODO ver como crear un json desde una lista
		
        return ok();
    }
	
	
	public static Result obtenerPartidas(String idJugador){
		List<PartidaActiva> partidasActivas = Match.obtenerPartidasActivas(idJugador);

		//TODO ver como crear un json desde una lista
		
        return ok();
	}
	
	public static Result invitarJugadores(){
		JsonNode json = request().body().asJson();
		String idJugador = json.get("id_jugador").asText();
		List<String> facebookIds = new ArrayList<String>();
		for(JsonNode node : json.get("invitados")){
			facebookIds.add(node.asText());
		}
		
		FacebookUtil.enviarInvitaciones(idJugador,facebookIds);
		return ok();
	}
	
	public static Result agregarAmigo(){
		JsonNode json = request().body().asJson();
		String idJugador = json.get("id_jugador").asText();
		String idAmigo = json.get("id_amigo").asText();
		
		Player.agregarAmigo(idJugador,idAmigo);
		
		return ok();
	}
	
	public static Result powerUp(String idJugador,String idPowerUp){
		Player.powerUp(idJugador, idPowerUp);
		return ok();
	}
}
