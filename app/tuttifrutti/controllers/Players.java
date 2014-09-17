package tuttifrutti.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Match;
import tuttifrutti.models.Player;
import tuttifrutti.models.views.ActiveMatch;
import tuttifrutti.utils.FacebookUtil;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class Players extends Controller {
	@Autowired
	private Player playerService;
	
	@Autowired
	private Match matchService;
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result register() {
		JsonNode json = request().body().asJson();
		if(json == null){
			return badRequest("Expecting json data");
		}else{
			String mail = json.get("mail") != null ? json.get("mail").asText() : null;
			String password = json.get("password") != null ? json.get("password").asText(): null;
			String facebookId = json.get("facebook_id") != null ? json.get("facebook_id").asText() : null;
			String twitterId = json.get("twitter_id") != null ? json.get("twitter_id").asText() : null;

			Player player = null;
			
			if(StringUtils.isNotEmpty(mail) && StringUtils.isNotEmpty(password)){
				player = playerService.registerMail(mail,password);
			}else if(StringUtils.isNotEmpty(facebookId)){
				player = playerService.registerFacebook(facebookId);
			}else if(StringUtils.isNotEmpty(twitterId)){
				player = playerService.registerTwitter(twitterId);
			}
			
			if(player != null){
				return ok(Json.toJson(player));
			}

			return badRequest();
		}
    }
	
	public Result validate(String mail,String password,String facebookId,String twitterId) {
		Player player = null;
		if(StringUtils.isNotEmpty(mail) && StringUtils.isNotEmpty(password)){
			player = playerService.validateMail(mail, password);
		}else if(StringUtils.isNotEmpty(facebookId)){
			player = playerService.validateFacebook(facebookId);
		}else if(StringUtils.isNotEmpty(twitterId)){
			player = playerService.validateTwitter(twitterId);
		}else{
			return badRequest();
		}
		
		if(player !=  null){
			return ok(Json.toJson(player));
		}
		
		return notFound();
    }
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result editProfile() {
		JsonNode json = request().body().asJson();
		
		if(playerService.editarPerfil(json)){
			return ok();
		}
		
		return badRequest();
	}
	
	public Result player(String playerId) {
		Player player = playerService.player(playerId);
		
		if(player != null){
			return ok(Json.toJson(player));
		}
		
		return badRequest();
    }
	
	public Result sync(String playerId) {
		/* TODO implementar
		 * Buscar jugador
		 * Buscar partidas con idJugador en estado distinto a PARTIDA_FINALIZADA
		 * Obtener de cada partida, la letra de la última ronda y la cantidad de rondas que faltan
		 * Compaginar respuesta
		 */
		Player jugador = playerService.player(playerId);
		if(jugador == null){
			return badRequest();
		}
		
		List<ActiveMatch> activeMatches = matchService.activeMatches(playerId);

		//TODO ver como crear un json desde una lista
		
        return ok();
    }
	
	public Result activeMatches(String idJugador){
		List<ActiveMatch> activeMatches = matchService.activeMatches(idJugador);

		//TODO ver como crear un json desde una lista
		
        return ok();
	}
	
	public Result invitePlayers(){
		JsonNode json = request().body().asJson();
		String matchId = json.get("player_id").asText();
		List<String> facebookIds = new ArrayList<String>();
		for(JsonNode node : json.get("guests")){
			facebookIds.add(node.asText());
		}
		
		FacebookUtil.sendInvitations(matchId,facebookIds);
		return ok();
	}
	
	public Result addFriend(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String friendId = json.get("friend_id").asText();
		
		playerService.addFriend(playerId,friendId);
		
		return ok();
	}
	
	public Result powerUp(String idJugador,String idPowerUp){
		playerService.powerUp(idJugador, idPowerUp);
		return ok();
	}
}
