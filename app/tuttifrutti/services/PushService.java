package tuttifrutti.services;

import static org.apache.commons.lang3.StringUtils.join;
import static play.libs.F.Promise.promise;
import static play.libs.Json.newObject;
import static tuttifrutti.utils.PushType.MATCH_RESULT;
import static tuttifrutti.utils.PushType.PRIVATE_MATCH_READY;
import static tuttifrutti.utils.PushType.PUBLIC_MATCH_READY;
import static tuttifrutti.utils.PushType.ROUND_RESULT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import play.Logger;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Http.Status;
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
	
	public static final String PUSHWOOSH_SERVICE_BASE_URL = ConfigurationAccessor.s("pushwoosh.url");
    private static final String AUTH_TOKEN = "AIzaSyDQw_q4WGGwTswqdtrdyIYMniuxG70d8sA";
    private static final String APPLICATION_CODE = "D340E-E46E7";
    private static final Integer ANDROID_DEVICE_TYPE = 3;
    private static final Integer RETRIES_NUMBER = 5;

	public void publicMatchReady(List<String> playerIds, Match match) {
		promise(() -> {
			sendMessageTo(playerIds, match,PUBLIC_MATCH_READY);
			return null;
		});
	}

	public void privateMatchReady(List<String> playerIds, Match match) {
		promise(() -> {
			sendMessageTo(playerIds, match,PRIVATE_MATCH_READY);
			return null;
		});
	}
	
	public void rejected(List<Player> players, Match match) {
		// TODO implementar
	}

	public void rejectedByPlayer(List<Player> players, String playerId, Match match) {
		// TODO implementar
	}

	public void roundResult(Match match, Integer roundNumber) {
		promise(() -> {
			ObjectNode json = newObject().put("type", ROUND_RESULT.toString()).put("match_id", match.getId().toString())
											  .put("round_number", roundNumber);
			for(String playerId : match.playerIds()){
				sendPushwooshMessage(json.put("player_id", playerId).toString(), Arrays.asList(playerId));
			}
			return null;
		});
	}
	

	public void matchResult(Match match) {
		promise(() -> {
			sendMessageTo(match.playerIds(), match,MATCH_RESULT);
			return null;
		});
	}
	
	private void sendPushwooshMessage(String jsonData,List<String> playerIds){
		ArrayNode notifications = newObject().arrayNode();
		notifications.add(newObject().put("send_date", "now").put("data", jsonData));
		JsonNode requestBody = newObject().put("auth", AUTH_TOKEN).put("devices_filter",playerPushTags(playerIds))
											   .set("notifications", notifications);
		JsonNode request = newObject().set("request", requestBody);
		for(int i = 0;i < RETRIES_NUMBER;i++){			
			WSResponse r = WS.url(PUSHWOOSH_SERVICE_BASE_URL + "createTargetedMessage").setContentType("application/json")
					.post(request)
					.get(5000L);
			if(r.getStatus() == Status.OK){
				JsonNode response = r.asJson();
				Integer statusCode = response.get("status_code").asInt();
				if(statusCode == Status.OK){
					return;
				}
				
				String statusMessage = response.get("status_message").asText();
				Logger.warn("Argument error trying to send message " + jsonData + ". Status message: " + statusMessage + ". Retry: " + i);
			}
			Logger.error("Sending message " + jsonData + " fail with status " + r.getStatus() + ". Retry: " + i);
		}
		
		Logger.error("Could not send message " + jsonData + " to players " + StringUtils.join(playerIds, ","));
	}

	public void registerDevice(String pushToken,String hardwareId,String language){
		ObjectNode registerJsonBody = newObject();
		registerJsonBody.put("application", APPLICATION_CODE);
		registerJsonBody.put("push_token", pushToken);
		registerJsonBody.put("language", language);
		registerJsonBody.put("hwid", hardwareId);
		registerJsonBody.put("device_type", ANDROID_DEVICE_TYPE);
		JsonNode registerJson = newObject().put("register", registerJsonBody);
		WSResponse r = WS.url(PUSHWOOSH_SERVICE_BASE_URL + "registerDevice").setContentType("application/json")
				 .post(registerJson)
				 .get(5000L);
		
		processPushResponse(r, "registering device with hwid " + hardwareId);
	}
	
	public void setTag(String hardwareId,String playerId){
		ObjectNode tagJson = newObject().put("player_id", playerId);
		ObjectNode jsonBody = newObject();
		jsonBody.put("application", APPLICATION_CODE);
		jsonBody.put("hwid", hardwareId);
		jsonBody.put("tags", tagJson);
		WSResponse r = WS.url(PUSHWOOSH_SERVICE_BASE_URL + "setTags").setContentType("application/json")
				 .post(jsonBody)
				 .get(5000L);
		
		processPushResponse(r, "set tag for player " + playerId);
	}

	public void unRegisterDevice(String hardwareId) {
		ObjectNode jsonBody = newObject();
		jsonBody.put("application", APPLICATION_CODE);
		jsonBody.put("hwid", hardwareId);
		WSResponse r = WS.url(PUSHWOOSH_SERVICE_BASE_URL + "unregisterDevice").setContentType("application/json")
				 .post(jsonBody)
				 .get(5000L);
		
		processPushResponse(r, "unregistering device with hwid " + hardwareId);
	}
	
	private void processPushResponse(WSResponse r, String partialErrorMessage) {
		if(r.getStatus() == Status.OK){
			JsonNode response = r.asJson();
			Integer statusCode = response.get("status_code").asInt();
			if(statusCode == 210){
				String statusMessage = response.get("status_message").asText();
				String errorMessage = "Argument error trying to " + partialErrorMessage + ". Status message: " + statusMessage;
				Logger.error(errorMessage);
				throw new RuntimeException(errorMessage);
			}
			return;
		}
		String errorMessage = partialErrorMessage + " fail with status " + r.getStatus();
		Logger.error(errorMessage);
		throw new RuntimeException(errorMessage);
	}
	
	private String playerPushTags(List<String> playerIds) {
		List<String> tags = new ArrayList<>();
		for(String playerId : playerIds){
			tags.add(String.format("T(\"playerId\", EQ, \"%s\")", playerId));
		}
		return join(tags," + ");
	}

	private void sendMessageTo(List<String> playerIds, Match match,PushType pushType) {
		ObjectNode json = newObject().put("type", pushType.toString()).put("match_id", match.getId().toString());
		for(String playerId : playerIds){
			sendPushwooshMessage(json.put("player_id", playerId).toString(), Arrays.asList(playerId));
		}
	}
}
