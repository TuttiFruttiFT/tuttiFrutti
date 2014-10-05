package tuttifrutti.services;

import static play.libs.F.Promise.promise;
import static play.libs.Json.newObject;
import static tuttifrutti.utils.PushType.MATCH_REJECTED;
import static tuttifrutti.utils.PushType.MATCH_REJECTED_BY_PLAYER;
import static tuttifrutti.utils.PushType.MATCH_RESULT;
import static tuttifrutti.utils.PushType.PRIVATE_MATCH_READY;
import static tuttifrutti.utils.PushType.ROUND_RESULT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import play.Logger;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Http.Status;
import tuttifrutti.models.Device;
import tuttifrutti.models.Match;
import tuttifrutti.models.Player;
import tuttifrutti.utils.ConfigurationAccessor;
import tuttifrutti.utils.PushType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author rfanego
 */
@Component
public class PushService {
	
	public static final String GCM_SEND_URL = ConfigurationAccessor.s("gcm.send.url");
    private static final String API_KEY = "AIzaSyDQw_q4WGGwTswqdtrdyIYMniuxG70d8sA";
    private static final Integer RETRIES_NUMBER = 5;

	public void privateMatchReady(Match match, List<Player> players) {
		promise(() -> {
			sendMessageTo(players, match,PRIVATE_MATCH_READY);
			return null;
		});
	}

	public void rejected(List<Player> players, Match match) {
		promise(() -> {
			sendMessageTo(players, match,MATCH_REJECTED);
			return null;
		});
	}

	public void rejectedByPlayer(Player rejectorPlayer, Match match) {
		promise(() -> {
			ObjectNode json = newObject().put("type", MATCH_REJECTED_BY_PLAYER.toString()).put("match_id", match.getId().toString());
			for(Player player : match.players()){
				JsonNode jsonToSend = json.put("player_id", player.getId().toString()).set("rejector_player", Json.toJson(rejectorPlayer));
				sendGCMMessage(jsonToSend,player.getDevices());
			}
			return null;
		});
	}

	public void roundResult(Match match, Integer roundNumber) {
		promise(() -> {
			ObjectNode json = newObject().put("type", ROUND_RESULT.toString()).put("match_id", match.getId().toString())
											  .put("round_number", roundNumber);
			for(Player player : match.players()){
				sendGCMMessage(json.put("player_id", player.getId().toString()),player.getDevices());
			}
			return null;
		});
	}
	

	public void matchResult(Match match) {
		promise(() -> {
			sendMessageTo(match.players(), match,MATCH_RESULT);
			return null;
		});
	}
	
	private void sendGCMMessage(JsonNode jsonData,List<Device> devices){
		Map<String,JsonNode> attributes = new HashMap<>();
		ArrayNode registrationIds = newObject().arrayNode();
		for(Device device : devices){
			registrationIds.add(device.getRegistrationId());
		}
		attributes.put("registration_ids", registrationIds);
		attributes.put("data", jsonData);
		JsonNode request = Json.newObject().setAll(attributes);
		Logger.info(request.toString());
		for(int i = 0;i < RETRIES_NUMBER;i++){					
			WSResponse r = WS.url(GCM_SEND_URL).setContentType("application/json").setHeader("Authorization", "key=" + API_KEY)
							 .post(request)
							 .get(5000L);
			
			JsonNode response = r.asJson();
			if(r.getStatus() == Status.OK){
				Logger.info(response.toString());
				return;
			}else{
				Logger.warn("Sending message " + jsonData.toString() + " fail with status " + r.getStatus() + ". Retry: " + i);
				Logger.warn(response.toString());
			}
		}
		
		Logger.error("Could not send message " + jsonData.toString() + " with request " + request.toString());
	}

	private void sendMessageTo(List<Player> players, Match match,PushType pushType) {
		ObjectNode json = newObject().put("type", pushType.toString()).put("match_id", match.getId().toString());
		for(Player player : players){
			sendGCMMessage(json.put("player_id", player.getId().toString()),player.getDevices());
		}
	}
}
