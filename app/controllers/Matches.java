package controllers;

import java.util.ArrayList;
import java.util.List;

import models.Dupla;
import models.Match;
import models.MatchConfig;
import models.Player;
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
public class Matches extends Controller {
	public static Result getMatch(String matchId) {
		//TODO Obtener la partida, la última ronda y cargar los powerUps
		Match match = Match.match(matchId);
		
		PowerUp.generate(match);
        return ok(Json.toJson(match));
    }
	
	public static Result publicMatch(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		Integer players = json.get("players").asInt();
		String language = json.get("language").asText();
		
		Match partida = Match.findMatch(players,language);
		if(partida == null){
			partida = Match.create(players,language);
		}
		partida.agregarJugador(playerId);
		PowerUp.generate(partida);
        return ok(Json.toJson(partida));
    }
	
	public static Result privateMatch(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		JsonNode jsonConfig = json.get("config");
		JsonNode jsonPlayers = json.get("players");
		
		MatchConfig config = Json.fromJson(jsonConfig, MatchConfig.class);
		List<String> players = new ArrayList<String>();
		
		for(JsonNode jsonPlayer : jsonPlayers){
			players.add(jsonPlayer.asText());
		}
		
		Match match = Match.create(playerId, config,players);
		
		PushUtil.match(players,match);
		
        return ok(Json.toJson(match));
    }
	
	public static Result rejectedMatch(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String matchId = json.get("match_id").asText();
		
		Match match = Match.match(matchId);
		match.playerReject(playerId);
		
		List<Player> players = match.getPlayers();
		
		if(players.size() == 1){
			match.rejected();
			PushUtil.rejected(players,match);
		}else{
			PushUtil.rejectedByPlayer(players,playerId,match);
		}
		
		return ok();
	}
	
	public static Result turn(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String matchId = json.get("match_id").asText();
		JsonNode jsonDuplas = json.get("duplas");
		
		List<Dupla> duplas = new ArrayList<Dupla>();
		
		for(JsonNode jsonDupla : jsonDuplas){
			duplas.add(Json.fromJson(jsonDupla, Dupla.class));
		}
		
		Match match = Match.match(matchId);
		
		ResultModel result = match.play(playerId,duplas);
		
        return ok(Json.toJson(result));
    }
	
	public static Result roundResult(String matchId,Integer roundNumber){
		ResultModel resultadoTurno = ResultModel.resultadoTurno(matchId,roundNumber);
		
		if(resultadoTurno != null){
			return ok(Json.toJson(resultadoTurno));
		}
		
		return notFound();
	}
	
	public static Result matchResult(String matchId){
		ResultModel matchResult = ResultModel.matchResult(matchId);
		
		if(matchResult != null){
			return ok(Json.toJson(matchResult));
		}
		
		return notFound();
	}
}
