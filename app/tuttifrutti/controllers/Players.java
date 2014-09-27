package tuttifrutti.controllers;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Transient;
import org.springframework.beans.factory.annotation.Autowired;

import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Device;
import tuttifrutti.models.Match;
import tuttifrutti.models.Player;
import tuttifrutti.models.views.ActiveMatch;
import tuttifrutti.utils.FacebookUtil;
import tuttifrutti.utils.JsonUtil;
import tuttifrutti.utils.PushUtil;

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
	
	@Autowired
	private Device deviceService;
	
	@Autowired
	private Datastore mongoDatastore;
	
	@Transient
	@Autowired
	private PushUtil pushUtil;
	
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
					player = playerService.registerMail(mail,password);
				}else{
					if(!playerService.validateMail(mail, password)){
						return unauthorized();
					}
				}
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
		
		playerService.addFriend(playerId,friendId);
		
		return ok();
	}
	
	public Result powerUp(String idJugador,String idPowerUp){
		playerService.powerUp(idJugador, idPowerUp);
		return ok();
	}
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result registerDevice(){
		JsonNode json = request().body().asJson();
		String pushToken = json.get("push_token").asText();
		String hardwareId = json.get("hwid").asText();
		String playerId = json.get("player_id").asText();
		
		Device device = deviceService.device(playerId);
		
		if(device == null){
			pushUtil.registerDevice(pushToken, hardwareId, "ES");
			pushUtil.setTag(hardwareId, playerId);
			device = new Device(playerId,pushToken,hardwareId);
			mongoDatastore.save(device);
			return null;
		}else{
			if(!pushToken.equals(device.getPushToken())){
				pushUtil.unRegisterDevice(hardwareId);
				pushUtil.registerDevice(pushToken, hardwareId, "ES");
				device.setPushToken(pushToken);
				mongoDatastore.save(device);
			}
		}
		
		return ok();
	}
}
