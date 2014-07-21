package tuttifrutti.controllers;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Suggestion;

import com.fasterxml.jackson.databind.JsonNode;

public class Suggestions extends Controller {
	public static Result suggest() {
		JsonNode suggestions = request().body().asJson();

		for(JsonNode suggestion : suggestions){
			String category = suggestion.get("category").asText();
			String word = suggestion.get("word").asText();
			
			if(StringUtils.isEmpty(category) || StringUtils.isEmpty(word)){
				return badRequest();
			}
			
			Suggestion.add(category,word);
		}
		
        return ok();
    }
	
	public static Result judge() {
		JsonNode json = request().body().asJson();
		
		String category = json.get("category").asText();
		String word = json.get("word").asText();
		Boolean valid = json.get("valid").asBoolean();
		
		Suggestion.judge(category,word, valid);
		
        return ok();
    }
	
	public static Result getWords(String playerId) {		
		List<Suggestion> suggestions = Suggestion.getSuggestions(playerId);
		
		//TODO ver como crear un json desde una lista
		
        return ok(Json.toJson(suggestions));
    }
}
