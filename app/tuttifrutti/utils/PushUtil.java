package tuttifrutti.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import play.Logger;
import play.libs.Akka;
import play.libs.Json;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Http.Status;
import scala.concurrent.duration.FiniteDuration;
import tuttifrutti.models.Match;
import tuttifrutti.models.Player;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author rfanego
 */
@Component
public class PushUtil {
	
	public static final String PUSHWOOSH_SERVICE_BASE_URL = ConfigurationAccessor.s("pushwoosh.url");
    private static final String AUTH_TOKEN = "AIzaSyDQw_q4WGGwTswqdtrdyIYMniuxG70d8sA";
    private static final String APPLICATION_CODE = "D340E-E46E7";
    private static final Integer ANDROID_DEVICE_TYPE = 3;
    private static final Integer RETRIES_NUMBER = 5;

	public static void match(List<String> playerIds, Match match) {
		Akka.system().scheduler().scheduleOnce(FiniteDuration.Zero(), () -> {
			sendMessage(Json.toJson(match).toString(), playerIds);
		}, Akka.system().dispatcher());
	}

	public static void rejected(List<Player> players, Match match) {
		// TODO implementar
		
	}

	public static void rejectedByPlayer(List<Player> players, String playerId, Match match) {
		// TODO Auto-generated method stub
		
	}

	public void roundResult(String matchId, Integer roundNumber,List<String> playerIds) {
		Akka.system().scheduler().scheduleOnce(FiniteDuration.Zero(), () -> {
			ObjectNode json = Json.newObject().put("type", PushType.ROUND_RESULT.toString()).put("match_id", matchId)
					.put("round_number", roundNumber);
			for(String playerId : playerIds){
				sendMessage(json.put("playerId", playerId).toString(), Arrays.asList(playerId));
			}			
		}, Akka.system().dispatcher());
	}
	
	private static void sendMessage(String jsonData,List<String> playerIds){
		ArrayNode notifications = Json.newObject().arrayNode();
		notifications.add(Json.newObject().put("send_date", "now").put("data", jsonData));
		JsonNode requestBody = Json.newObject().put("auth", AUTH_TOKEN).put("devices_filter",playerPushTags(playerIds))
											   .set("notifications", notifications);
		JsonNode request = Json.newObject().set("request", requestBody);
		for(int i = 0;i < RETRIES_NUMBER;i++){			
			WSResponse r = WS.url(PUSHWOOSH_SERVICE_BASE_URL + "createTargetedMessage").setContentType("application/json")
					.post(request.toString())
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

	public static void registerDevice(String pushToken,String hardwareId,String language){
		ObjectNode registerJsonBody = Json.newObject();
		registerJsonBody.put("application", APPLICATION_CODE);
		registerJsonBody.put("push_token", pushToken);
		registerJsonBody.put("language", language);
		registerJsonBody.put("hwid", hardwareId);
		registerJsonBody.put("device_type", ANDROID_DEVICE_TYPE);
		JsonNode registerJson = Json.newObject().put("register", registerJsonBody);
		WSResponse r = WS.url(PUSHWOOSH_SERVICE_BASE_URL + "registerDevice").setContentType("application/json")
				 .post(registerJson.asText())
				 .get(5000L);
		
		if(r.getStatus() == Status.OK){
			JsonNode response = r.asJson();
			Integer statusCode = response.get("status_code").asInt();
			if(statusCode == 210){
				String statusMessage = response.get("status_message").asText();
				String errorMessage = "Argument error trying to register device. Status message: " + statusMessage;
				Logger.error(errorMessage);
				throw new RuntimeException(errorMessage);
			}
			return;
		}
		String errorMessage = "Register Device fail with status " + r.getStatus();
		Logger.error(errorMessage);
		throw new RuntimeException(errorMessage);
	}

	private static String playerPushTags(List<String> playerIds) {
		List<String> tags = new ArrayList<>();
		for(String playerId : playerIds){
			tags.add(String.format("T(\"playerId\", EQ, \"%s\")", playerId));
		}
		return StringUtils.join(tags," + ");
	}
}
