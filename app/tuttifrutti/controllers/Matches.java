package tuttifrutti.controllers;

import static play.libs.Json.fromJson;
import static play.libs.Json.parse;
import static play.libs.Json.toJson;
import static tuttifrutti.models.enums.MatchState.PLAYER_TURN;
import static tuttifrutti.utils.JsonUtil.parseListToJson;

import java.util.ArrayList;
import java.util.List;

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
import tuttifrutti.models.PowerUp;
import tuttifrutti.models.Round;

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
	
	public Result getMatch(String matchId,String playerId) {
		Match match = matchService.match(matchId, playerId);
		
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
			match.setState(PLAYER_TURN);
			matchService.publicMatchReady(playerId, match);
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
		
		Match match = matchService.createPrivate(playerId, config,playerIds, categoryIds);
		
		mongoDatastore.save(match);
		
		matchService.privateMatchReady(playerIds, match);
		
        return ok(Json.toJson(match));
    }
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result rejectedMatch(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String matchId = json.get("match_id").asText();
		
		Match match = matchService.match(matchId, playerId);
		matchService.playerReject(playerId, match);
		
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
		
		Match match = matchService.match(matchId, playerId);
		
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
			return ok(toJson(round));
		}
		
		return notFound();
	}
	
	public Result matchResult(String matchId){
		Match match = matchService.endedMatch(matchId);
		
		if(match != null){
			Match matchResult = new Match();
			matchResult.setWinnerId(match.getWinnerId());
			matchResult.setPlayerResults(match.getPlayerResults());
			return ok(toJson(matchResult));
		}
		
		return notFound();
	}
}
