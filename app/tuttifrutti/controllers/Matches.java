package tuttifrutti.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Transient;
import org.springframework.beans.factory.annotation.Autowired;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Dupla;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.MatchState;
import tuttifrutti.models.Player;
import tuttifrutti.models.PowerUp;
import tuttifrutti.models.ResultModel;
import tuttifrutti.models.Round;
import tuttifrutti.utils.JsonUtil;
import tuttifrutti.utils.PushUtil;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class Matches extends Controller {
	@Autowired
	private Match matchService;
	
	@Autowired
	private Round roundService;
	
	@Transient
	@Autowired
	private Datastore mongoDatastore;
	
	public Result getMatch(String matchId) {
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
		if(match == null){
			match = matchService.createPublic(config);
		}
		matchService.addPlayer(match, playerId);
		PowerUp.generate(match);
		if(match.readyToStart()){
			match.setState(MatchState.PLAYER_TURN);
			List<String> playerIds = match.getPlayers().stream().map(result -> result.getPlayer().getId().toString())
					.filter(id -> !id.equals(playerId)).collect(Collectors.toList());
			PushUtil.match(playerIds,match);
		}
		mongoDatastore.save(match);
        return ok(Json.toJson(match));
    }
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result privateMatch(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		JsonNode jsonConfig = json.get("config");
		JsonNode jsonPlayers = json.get("players");
		
		MatchConfig config = Json.fromJson(jsonConfig, MatchConfig.class);
		List<String> players = new ArrayList<>();
		
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
		
		List<Player> players = match.getPlayers().stream().map(result -> result.getPlayer()).collect(Collectors.toList());
		
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
		int time = json.get("time").asInt();
		JsonNode jsonDuplas = json.get("duplas");
		
		List<Dupla> duplas = new ArrayList<>();
		
		for(JsonNode jsonDupla : jsonDuplas){
			duplas.add(Json.fromJson(jsonDupla, Dupla.class));
		}
		
		Match match = matchService.match(matchId);
		
        List<Dupla> wrongDuplas = matchService.play(match,playerId, duplas, time);
        
		return ok(Json.parse(JsonUtil.parseListToJson(wrongDuplas)));
    }
	
	public Result roundResult(String matchId,Integer roundNumber){
		Round round = roundService.getRound(matchId,roundNumber);
		
		if(round != null){
			return ok(Json.toJson(round));
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
