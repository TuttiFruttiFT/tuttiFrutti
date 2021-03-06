package tuttifrutti.controllers;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static play.libs.Json.parse;
import static tuttifrutti.models.enums.SuggestionState.SUGGESTED;
import static tuttifrutti.utils.JsonUtil.parseListToJson;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import play.Logger;
import play.libs.F.Function;
import play.libs.F.Promise;
import play.libs.Json;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Category;
import tuttifrutti.models.Suggestion;
import tuttifrutti.services.SuggestionService;

import com.fasterxml.jackson.databind.JsonNode;


@org.springframework.stereotype.Controller
public class Suggestions extends Controller {
	
	@Autowired
	private SuggestionService suggestionService;
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result suggest() {
		JsonNode jsonRequest = request().body().asJson();
		String playerId = jsonRequest.get("player_id").asText();
		JsonNode suggestionsJson = jsonRequest.get("duplas");
		
		Promise.promise(() -> {
			List<Suggestion> suggestions = new ArrayList<>();
			suggestionsJson.forEach(suggestionJson -> {
				Category category = Json.fromJson(suggestionJson.get("category"),Category.class);
				String word = suggestionJson.get("written_word").asText();
				
				Logger.info("player_id" + playerId +  " written_word " + word);
				
				if(category != null || !isEmpty(word)){
					Logger.info("Category: " + category.getId());
					Suggestion suggestion = suggestionService.suggest(category,word, playerId);
					if(suggestion != null && suggestion.getState().equals(SUGGESTED)){						
						suggestions.add(suggestion);
					}
				}
			});
			
			if(!isEmpty(suggestions)){				
				suggestionService.save(suggestions);
			}
			return null;
		}).recover(new Function<Throwable, Object>() {
			@Override
			public Object apply(Throwable arg0) throws Throwable {
				Logger.error("recover suggest word",arg0);
				return null;
			}
		});
		
        return ok();
    }
	
	@BodyParser.Of(BodyParser.Json.class)
	public Result judge(){
		JsonNode jsonRequest = request().body().asJson();
		String playerId = jsonRequest.get("player_id").asText();
		JsonNode judgements = jsonRequest.get("duplas");
		
		Promise.promise(() -> {
			List<Suggestion> suggestions = new ArrayList<>();
			judgements.forEach(judgement -> {
				String suggestionId = judgement.get("id").asText();
				Boolean valid = judgement.get("valid").asBoolean();
				suggestions.add(suggestionService.judge(suggestionId,valid, playerId));
			});
			
			if(!isEmpty(suggestions)){				
				suggestionService.save(suggestions);
			}
			return null;
		}).recover(new Function<Throwable, Object>() {
			@Override
			public Object apply(Throwable arg0) throws Throwable {
				Logger.error("recover judge word",arg0);
				return null;
			}
		});
		
        return ok();
    }
	
	public Result getWords(){
		JsonNode jsonRequest = request().body().asJson();
		List<String> ommitedWordIds = new ArrayList<>();
		String playerId = jsonRequest.get("player_id").asText();
		JsonNode jsonOmmitedWords = jsonRequest.get("ommited_words");
		if(jsonOmmitedWords != null && !jsonOmmitedWords.isNull()){			
			for(JsonNode jsonOmmitedWord : jsonOmmitedWords){
				ommitedWordIds.add(jsonOmmitedWord.asText());
			}
		}
		List<Suggestion> suggestions = suggestionService.getSuggestions(playerId, ommitedWordIds);

		if(isNotEmpty(suggestions)){			
			return ok(parse(parseListToJson(suggestions)));
		}
		
		return noContent();
    }
}
