package tuttifrutti.controllers;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static play.libs.Json.parse;
import static tuttifrutti.models.enums.PowerUpType.valueOf;
import static tuttifrutti.services.PlayerService.INVALID_NEW_PASSWORD;
import static tuttifrutti.services.PlayerService.MALFORMED_REQUEST;
import static tuttifrutti.services.PlayerService.SHORT_NICKNAME;
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
import tuttifrutti.models.Device;
import tuttifrutti.models.Player;
import tuttifrutti.models.PlayerConfig;
import tuttifrutti.models.enums.PowerUpType;
import tuttifrutti.models.views.ActiveMatch;
import tuttifrutti.services.MatchService;
import tuttifrutti.services.PlayerService;
import tuttifrutti.services.PushService;
import tuttifrutti.utils.JsonUtil;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class Players extends Controller {
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
			String nickname = json.get("nickname") != null ? json.get("nickname").asText() : null;
			String twitterId = json.get("twitter_id") != null ? json.get("twitter_id").asText() : null;

			Player player = null;
			
			if(isNotEmpty(mail) && isNotEmpty(password)){
				player = playerService.search(mail);
				if(player == null){
					if(playerService.isValidMail(mail)){
						if(playerService.isValidPassword(password)){							
							player = playerService.registerMail(mail,password);
						}else{
							return badRequest(Json.newObject().put("status_code", INVALID_NEW_PASSWORD));
						}
					}else{
						return badRequest(Json.newObject().put("status_code", SHORT_NICKNAME));
					}
				}else{
					if(!playerService.validatePassword(player.getPassword(), password)){
						return unauthorized();
					}
				}
			}else if(isNotEmpty(facebookId) && isNotEmpty(nickname) && isNotEmpty(mail)){
				player = playerService.searchByFacebook(facebookId);
				if(player == null){					
					player = playerService.registerFacebook(facebookId, nickname, mail);
				}
			}else if(isNotEmpty(twitterId) && isNotEmpty(nickname) && isNotEmpty(mail)){
				player = playerService.searchByTwitter(twitterId);
				if(player == null){					
					player = playerService.registerTwitter(twitterId, nickname, mail);
				}
			}
			
			if(player != null){
				return ok(Json.toJson(player));
			}

			return badRequest(Json.newObject().put("status_code", MALFORMED_REQUEST));
		}
    }
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result addSocialNetwork(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id") != null ? json.get("player_id").asText() : null;
		String type = json.get("type") != null ? json.get("type").asText() : null;
		String id = json.get("id") != null ? json.get("id").asText() : null;
		
		if(isEmpty(playerId) || isEmpty(type) || isEmpty(id)){
			return badRequest("Missing parameters");
		}
		
		playerService.addSocialNetwork(playerId,type,id);
		
		return ok();
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result editProfile() {
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id") != null ? json.get("player_id").asText() : null;
		String mail = json.get("mail") != null ? json.get("mail").asText() : null;
		String nickname = json.get("nickname") != null ? json.get("nickname").asText() : null;
		String password = json.get("password") != null ? json.get("password").asText() : null;
		String newPassword = json.get("new_password") != null ? json.get("new_password").asText() : null;
		
		String result = playerService.editProfile(playerId, mail, nickname, password, newPassword);
		if(isEmpty(result)){
			return ok();
		}
		
		return badRequest(result);
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result editSettings() {
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id") != null ? json.get("player_id").asText() : null;
		JsonNode jsonConfig = json.get("config");
		PlayerConfig config = Json.fromJson(jsonConfig, PlayerConfig.class);
		
		playerService.editSettings(playerId, config);
		
		return ok();
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
	public Result addFriend(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String friendId = json.get("friend_id").asText();
		
		playerService.addFriend(playerId,friendId);
		
		return ok();
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result removeFriend(){
		JsonNode json = request().body().asJson();
		String playerId = json.get("player_id").asText();
		String friendId = json.get("friend_id").asText();
		
		playerService.removeFriend(playerId,friendId);
		
		return ok();
	}
	
	public Result friends(String playerId){
		Player player = playerService.player(playerId);
		
		return ok(parse(parseListToJson(player.getFriends())));
	}
	
	public Result powerUp(String playerId,String powerUpId){
		PowerUpType powerUp = valueOf(powerUpId);
		if(powerUp == null){
			return badRequest();
		}
		if(playerService.powerUp(playerId, powerUp)){
			return ok();
		}
		return forbidden();
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
