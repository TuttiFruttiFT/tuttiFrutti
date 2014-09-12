package tuttifrutti.controllers;

import static tuttifrutti.utils.ConfigurationAccessor.s;

import java.io.IOException;
import java.io.InputStream;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.CategoryElastic;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author rfanego
 *
 */
@org.springframework.stereotype.Controller
public class CategoryLoader extends Controller {
	
	@Autowired
	private Client elasticSearchClient;
	
	public Result load94secondsCategories(){
		InputStream inJson = CategoryElastic.class.getResourceAsStream("/categorias/ninetyfour_seconds_es.json");
		try {
			JsonNode jsonArray = new ObjectMapper().readTree(inJson);
			
			for(JsonNode json : jsonArray){
				int categoryNumber = json.get("i").asInt();
				processCategory(152,"capitals",categoryNumber,json);
				processCategory(155,"colors",categoryNumber,json);
				processCategory(161,"sports",categoryNumber,json);
				processCategory(164,"musical_styles",categoryNumber,json);
				processCategory(166,"meals",categoryNumber,json);
				processCategory(184,"meals",categoryNumber,json);
				processCategory(295,"meals",categoryNumber,json);
				processCategory(339,"meals",categoryNumber,json);
				processCategory(168,"musical_instruments",categoryNumber,json);
				processCategory(171,"animals",categoryNumber,json);
				processCategory(174,"animals",categoryNumber,json);
				processCategory(173,"countries",categoryNumber,json);
				processCategory(170,"jobs",categoryNumber,json);
				processCategory(183,"verbs",categoryNumber,json);
			}
			return ok();
		} catch (IOException e) {
			Logger.error("Processing categories json");
		}
		
		return internalServerError();
	}

	private void processCategory(int categoryNumber, String categoryName, int categoryNumberFromJson, JsonNode json) {
		if(categoryNumberFromJson == categoryNumber){
			for(JsonNode word : json.get("w")){
				indexWord(word,categoryName);
			}
		}
	}

	private void indexWord(JsonNode word, String categoryName) {
		String value = getValue(word);
		String json = Json.newObject().put("value", value).put("letter", getLetter(value)).put("language", "ES").toString();
		IndexResponse response = elasticSearchClient.prepareIndex(s("elasticsearch.updater.index"), categoryName).setSource(json).execute().actionGet();
		response.getIndex();
	}

	private String getLetter(String value) {
		return value.substring(0, 1);
	}

	private String getValue(JsonNode word) {
		String rawValue = Json.stringify(word.get("m"));
		String value = rawValue.substring(1, rawValue.length() - 1);
		return value.toLowerCase();
	}
}
