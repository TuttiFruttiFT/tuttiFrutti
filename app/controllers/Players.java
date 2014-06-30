package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Player;
import models.Match;
import models.views.ActiveMatch;

import org.apache.commons.lang3.StringUtils;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import utils.FacebookUtil;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
public class Players extends Controller {
	public static Result register() {
		JsonNode json = request().body().asJson();
		if(json == null){
			return badRequest();
		}else{
			String mail = json.get("mail").asText();
			String password = json.get("password").asText();
			String facebookId = json.get("facebook_id").asText();
			String twitterId = json.get("twitter_id").asText();

			Player player = null;
			
			if(StringUtils.isNotEmpty(mail) && StringUtils.isNotEmpty(password)){
				player = Player.registerMail(mail,password);
			}else if(StringUtils.isNotEmpty(facebookId)){
				player = Player.registerFacebook(facebookId);
			}else if(StringUtils.isNotEmpty(twitterId)){
				player = Player.registerTwitter(twitterId);
			}
			
			if(player != null){
				return ok(Json.toJson(player));
			}

			return badRequest();
		}
    }
	
	public static Result validate(String mail,String password,String facebookId,String twitterId) {
		Player player = null;
		if(StringUtils.isNotEmpty(mail) && StringUtils.isNotEmpty(password)){
			player = Player.validateMail(mail, password);
		}else if(StringUtils.isNotEmpty(facebookId)){
			player = Player.validateFacebook(facebookId);
		}else if(StringUtils.isNotEmpty(twitterId)){
			player = Player.validateTwitter(twitterId);
		}else{
			return badRequest();
		}
		
		if(player !=  null){
			return ok(Json.toJson(player));
		}
		
		return notFound();
    }
	
	public static Result editProfile() {
		JsonNode json = request().body().asJson();
		
		if(Player.editarPerfil(json)){
			return ok();
		}
		
		return badRequest();
	}
	
	public static Result player(String playerId) {
		Player player = Player.player(playerId);
		
		if(player != null){
			return ok(Json.toJson(player));
		}
		
		return badRequest();
    }
	
	public static Result sync(String playerId) {
		/* TODO
		 * Buscar jugador
		 * Buscar partidas con idJugador en estado distinto a PARTIDA_FINALIZADA
		 * Obtener de cada partida, la letra de la última ronda y la cantidad de rondas que faltan
		 * Compaginar respuesta
		 */
		Player jugador = Player.player(playerId);
		if(jugador == null){
			return badRequest();
		}
		
		List<ActiveMatch> activeMatches = Match.activeMatches(playerId);

		//TODO ver como crear un json desde una lista
		
        return ok();
    }
	
	
	public static Result activeMatches(String idJugador){
		List<ActiveMatch> activeMatches = Match.activeMatches(idJugador);

		//TODO ver como crear un json desde una lista
		
        return ok();
	}
	
	public static Result invitePlayers(){
		JsonNode json = request().body().asJson();
		String matchId = json.get("player_id").asText();
		List<String> facebookIds = new ArrayList<String>();
		for(JsonNode node : json.get("guests")){
			facebookIds.add(node.asText());
		}
		
		FacebookUtil.sendInvitations(matchId,facebookIds);
		return ok();
	}
	
	public static Result addFriend(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String friendId = json.get("friend_id").asText();
		
		Player.addFriend(playerId,friendId);
		
		return ok();
	}
	
	public static Result powerUp(String idJugador,String idPowerUp){
		Player.powerUp(idJugador, idPowerUp);
		return ok();
	}
}
