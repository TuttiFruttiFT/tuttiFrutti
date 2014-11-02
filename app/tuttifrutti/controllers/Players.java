package tuttifrutti.controllers;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Transient;
import org.springframework.beans.factory.annotation.Autowired;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Device;
import tuttifrutti.models.Player;
import tuttifrutti.models.views.ActiveMatch;
import tuttifrutti.services.MatchService;
import tuttifrutti.services.PlayerService;
import tuttifrutti.services.PushService;
import tuttifrutti.utils.FacebookUtil;
import tuttifrutti.utils.JsonUtil;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class Players extends Controller {
	private static final String SHORT_NICKNAME = "SHORT_NICKNAME";

	private static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";

	@Autowired
	private PlayerService playerService;
	
	@Autowired
	private MatchService matchService;
	
	@Autowired
	private Datastore mongoDatastore;
	
	@Transient
	@Autowired
	private PushService pushUtil;
	
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
			
			if(isNotEmpty(mail) && isNotEmpty(password)){
				player = playerService.search(mail);
				if(player == null){
					if(playerService.isValid(mail)){						
						player = playerService.registerMail(mail,password);
					}else{
						return badRequest(Json.newObject().put("status_code", SHORT_NICKNAME));
					}
				}else{
					if(!playerService.validatePassword(player.getPassword(), password)){
						return unauthorized();
					}
				}
			}else if(isNotEmpty(facebookId)){
				player = playerService.registerFacebook(facebookId);
			}else if(isNotEmpty(twitterId)){
				player = playerService.registerTwitter(twitterId);
			}
			
			if(player != null){
				return ok(Json.toJson(player));
			}

			return badRequest(Json.newObject().put("status_code", MALFORMED_REQUEST));
		}
    }
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result editProfile() {
		JsonNode json = request().body().asJson();
		
		if(playerService.editProfile(json)){
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
	
	public Result activeMatches(String playerId){
		List<ActiveMatch> activeMatches = matchService.activeMatches(playerId);

        return ok(Json.parse(JsonUtil.parseListToJson(activeMatches)));
	}
	
	@BodyParser.Of(BodyParser.Json.class)
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
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result addFriend(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String friendId = json.get("friend_id").asText();
		
		Player friend = playerService.addFriend(playerId,friendId);
		
		return ok(Json.toJson(friend));
	}
	
	public Result powerUp(String idJugador,String idPowerUp){
		playerService.powerUp(idJugador, idPowerUp);
		return ok();
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result registerDevice(){
		JsonNode json = request().body().asJson();
		String registrationId = json.get("registration_id").asText();
		String hardwareId = json.get("hwid").asText();
		String playerId = json.get("player_id").asText();
		
		Player player = playerService.player(playerId);
		
		if(player == null){
			return badRequest("Player " + playerId + " does not exist");
		}else{
			if(player.getDevices() == null){
				List<Device> devices = new ArrayList<>();
				devices.add(new Device(registrationId,hardwareId));
				player.setDevices(devices);
			}else{
				if(player.getDevices().stream().anyMatch(device -> device.getRegistrationId().equals(registrationId))){
					return badRequest("registration id " + registrationId + " for player " + playerId + " already registered");
				}
				player.getDevices().add(new Device(registrationId,hardwareId));
			}
			mongoDatastore.save(player);
		}
		
		return ok();
	}
}
