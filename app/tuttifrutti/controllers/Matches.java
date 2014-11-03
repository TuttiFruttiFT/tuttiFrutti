package tuttifrutti.controllers;

import static play.libs.Json.fromJson;
import static play.libs.Json.parse;
import static play.libs.Json.toJson;
import static tuttifrutti.utils.JsonUtil.parseListToJson;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;

import play.Logger;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Dupla;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.PowerUp;
import tuttifrutti.models.Round;
import tuttifrutti.models.enums.MatchState;
import tuttifrutti.services.MatchService;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class Matches extends Controller {
	@Autowired
	private MatchService matchService;
	
	@Autowired
	private Round roundService;
	
	@Autowired
	private PowerUp powerUpService;
	
	@Autowired
	private Datastore mongoDatastore;
	
	public Result getMatch(String matchId,String playerId) {
		Match match = matchService.match(matchId, playerId);
		
		powerUpService.generate(match, playerId);
		
        return ok(Json.toJson(match));
    }
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result publicMatch(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		JsonNode jsonConfig = json.get("config");

		MatchConfig config = Json.fromJson(jsonConfig, MatchConfig.class);
		Match match = null;
		
		try{
			match = matchService.findPublicMatch(playerId, config);
			if(match == null){
				match = matchService.createPublic(config);
			}
			matchService.addPlayer(match, playerId);
			powerUpService.generate(match, playerId);
			if(match.readyToStart()){
				match.setState(MatchState.PLAYER_TURN);
			}
			mongoDatastore.save(match);
		}catch(Exception e){
			Logger.error("Public Match Service",e);
		}
        return ok(Json.toJson(match));
    }
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result privateMatch(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String matchName = json.get("match_name") != null ? json.get("match_name").asText() : null;
		JsonNode jsonConfig = json.get("config");
		JsonNode jsonPlayers = json.get("players");
		JsonNode jsonCategories = json.get("categories");
		
		MatchConfig config = fromJson(jsonConfig, MatchConfig.class);
		List<String> playerIds = new ArrayList<>();
		List<String> categoryIds = new ArrayList<>();
		
		playerIds.add(playerId);
		for(JsonNode jsonPlayer : jsonPlayers){
			playerIds.add(jsonPlayer.asText());
		}
		
		for(JsonNode jsonCategory : jsonCategories){
			categoryIds.add(jsonCategory.asText());
		}
		
		Match match = matchService.createPrivate(playerId, matchName,config, playerIds, categoryIds);
		
		mongoDatastore.save(match);
		
		matchService.privateMatchReady(match, match.playersExcept(playerId));
		
        return ok(Json.toJson(match));
    }
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result acceptMatch(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String matchId = json.get("match_id").asText();
		
		Match match = matchService.match(matchId, null);
		
		match.getPlayerResults().stream().filter(playerResult -> playerResult.getPlayer().getId().toString().equals(playerId))
										 .forEach(playerResult -> playerResult.setAccepted(true));
		
		mongoDatastore.save(match);
		
		return ok();
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result rejectedMatch(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String matchId = json.get("match_id").asText();
		
		boolean isPlayerRejected = matchService.playerReject(playerId, matchService.match(matchId, null));
		
		if(isPlayerRejected){
			return ok();
		}
		return badRequest(playerId + " is not present in match " + matchId);
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result turn(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String matchId = json.get("match_id").asText();
		int time = json.get("time").asInt();
		JsonNode jsonDuplas = json.get("duplas");
		List<Dupla> duplas = new ArrayList<>();
		
		Match match = matchService.match(matchId, null);

		if(match.playerHasAlreadyPlayed(playerId)){
			return badRequest("Player " + playerId + " has already played.");
		}
		
		for(JsonNode jsonDupla : jsonDuplas){
			duplas.add(Json.fromJson(jsonDupla, Dupla.class));
		}
		
        List<Dupla> wrongDuplas = matchService.play(match,playerId, duplas, time);
        
		return ok(parse(parseListToJson(wrongDuplas)));
    }
	
	public Result roundResult(String matchId,Integer roundNumber,String playerId){
		Round round = roundService.getRound(matchId,roundNumber);
		
		if(round != null){
			round.reorderTurns(playerId);
			round.sortDuplasInDescendingOrderByTime();
			return ok(toJson(round));
		}
		
		return notFound();
	}
	
	public Result matchResult(String matchId){
		Match match = matchService.endedMatch(matchId);
		
		if(match != null){
			Match matchResult = new Match();
			matchResult.setWinner(match.getWinner());
			matchResult.setWinnerId(match.getWinner().getId().toString());
			matchResult.setPlayerResults(match.getPlayerResults());
			return ok(toJson(matchResult));
		}
		
		return notFound();
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result hideMatch(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String matchId = json.get("match_id").asText();
		
		matchService.hideMatch(matchId,playerId);
		
		return ok();
	}
}
