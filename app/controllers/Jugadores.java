package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Jugador;
import models.Partida;
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

			Jugador jugador = null;
			
			if(StringUtils.isNotEmpty(mail) && StringUtils.isNotEmpty(clave)){
				jugador = Jugador.registrarMail(mail,clave);
			}else if(StringUtils.isNotEmpty(facebookId)){
				jugador = Jugador.registrarFacebook(facebookId);
			}else if(StringUtils.isNotEmpty(twitterId)){
				jugador = Jugador.registrarTwitter(twitterId);
			}
			
			if(jugador != null){
				return ok(Json.toJson(jugador));
			}

			return badRequest();
		}
    }
	
	public static Result validar(String mail,String clave,String facebookId,String twitterId) {
		Jugador jugador = null;
		if(StringUtils.isNotEmpty(mail) && StringUtils.isNotEmpty(clave)){
			jugador = Jugador.validacionMail(mail, clave);
		}else if(StringUtils.isNotEmpty(facebookId)){
			jugador = Jugador.validacionFacebook(facebookId);
		}else if(StringUtils.isNotEmpty(twitterId)){
			jugador = Jugador.validacionTwitter(twitterId);
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
		
		if(Jugador.editarPerfil(json)){
			return ok();
		}
		
		return badRequest();
	}
	
	public static Result obtenerJugador(String idJugador) {
		Jugador jugador = Jugador.obtenerJugador(idJugador);
		
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
		Jugador jugador = Jugador.obtenerJugador(idJugador);
		if(jugador == null){
			return badRequest();
		}
		
		List<PartidaActiva> partidasActivas = Partida.obtenerPartidasActivas(idJugador);

		//TODO ver como crear un json desde una lista
		
        return ok();
    }
	
	
	public static Result obtenerPartidas(String idJugador){
		List<PartidaActiva> partidasActivas = Partida.obtenerPartidasActivas(idJugador);

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
		
		Jugador.agregarAmigo(idJugador,idAmigo);
		
		return ok();
	}
	
	public static Result powerUp(String idJugador,String idPowerUp){
		Jugador.powerUp(idJugador, idPowerUp);
		return ok();
	}
}
