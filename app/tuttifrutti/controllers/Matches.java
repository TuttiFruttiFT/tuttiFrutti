package tuttifrutti.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Dupla;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.Player;
import tuttifrutti.models.PowerUp;
import tuttifrutti.models.ResultModel;
import tuttifrutti.utils.PushUtil;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class Matches extends Controller {
	@Autowired
	private Match matchService;
	
	public Result getMatch(String matchId) {
		//TODO Obtener la partida, la última ronda y cargar los powerUps
		Match match = matchService.match(matchId);
		
		PowerUp.generate(match);
        return ok(Json.toJson(match));
    }
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result publicMatch(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		JsonNode jsonConfig = json.get("config");

		MatchConfig config = Json.fromJson(jsonConfig, MatchConfig.class);

		Match match = matchService.findPublicMatch(playerId, config);
		System.out.println("MATCH: " + (match == null));
		if(match == null){
			match = matchService.createPublic(config);
		}
		match.addPlayer(playerId);
		PowerUp.generate(match);
		//TODO hay que setear el round desde Round
        return ok(Json.toJson(match));
    }
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result privateMatch(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		JsonNode jsonConfig = json.get("config");
		JsonNode jsonPlayers = json.get("players");
		
		MatchConfig config = Json.fromJson(jsonConfig, MatchConfig.class);
		List<String> players = new ArrayList<String>();
		
		for(JsonNode jsonPlayer : jsonPlayers){
			players.add(jsonPlayer.asText());
		}
		
		Match match = matchService.create(playerId, config,players);
		
		PushUtil.match(players,match);
		
        return ok(Json.toJson(match));
    }
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result rejectedMatch(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String matchId = json.get("match_id").asText();
		
		Match match = matchService.match(matchId);
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
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result turn(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String matchId = json.get("match_id").asText();
		JsonNode jsonDuplas = json.get("duplas");
		
		List<Dupla> duplas = new ArrayList<Dupla>();
		
		for(JsonNode jsonDupla : jsonDuplas){
			duplas.add(Json.fromJson(jsonDupla, Dupla.class));
		}
		
		Match match = matchService.match(matchId);
		
		ResultModel result = match.play(playerId,duplas);
		
        return ok(Json.toJson(result));
    }
	
	public Result roundResult(String matchId,Integer roundNumber){
		ResultModel resultadoTurno = ResultModel.resultadoTurno(matchId,roundNumber);
		
		if(resultadoTurno != null){
			return ok(Json.toJson(resultadoTurno));
		}
		
		return notFound();
	}
	
	public Result matchResult(String matchId){
		ResultModel matchResult = ResultModel.matchResult(matchId);
		
		if(matchResult != null){
			return ok(Json.toJson(matchResult));
		}
		
		return notFound();
	}
}
