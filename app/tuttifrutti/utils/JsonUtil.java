/**
 * 
 */
package tuttifrutti.utils;

import java.util.List;

import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 *
 */
public class JsonUtil {
	public static <T> StringBuilder parseListToJson(List<T> list) {
		StringBuilder jsonResult = new StringBuilder();
		String comma = "";
		for(T object : list){
			JsonNode jsonCategory = Json.toJson(object);
			jsonResult.append(comma + jsonCategory.toString());
			comma = ",";
		}
		jsonResult.insert(0, "[");
		jsonResult.append("]");
		return jsonResult;
	}
}
