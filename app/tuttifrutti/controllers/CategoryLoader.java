package tuttifrutti.controllers;

import static org.springframework.util.StringUtils.hasText;
import static tuttifrutti.utils.ConfigurationAccessor.s;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import lombok.SneakyThrows;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

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
	
	@SneakyThrows
	public Result load94secondsCategories(){
		InputStream inJson = CategoryLoader.class.getResourceAsStream("/categorias/ninetyfour_seconds_es.json");
		InputStream verbsFile = CategoryLoader.class.getResourceAsStream("/categorias/verbs.txt");
		BufferedReader br = null;
		try {
			JsonNode jsonArray = new ObjectMapper().readTree(inJson);
			
			for(JsonNode json : jsonArray){
				int categoryNumber = json.get("i").asInt();
				process94Category(152,"capitals",categoryNumber,json);
				process94Category(155,"colors",categoryNumber,json);
				process94Category(161,"sports",categoryNumber,json);
				process94Category(164,"musical_styles",categoryNumber,json);
				process94Category(166,"fruits",categoryNumber,json);
				process94Category(184,"fruits",categoryNumber,json);
				process94Category(339,"drinks",categoryNumber,json);
				process94Category(168,"musical_instruments",categoryNumber,json);
				process94Category(171,"animals",categoryNumber,json);
				process94Category(174,"animals",categoryNumber,json);
				process94Category(173,"countries",categoryNumber,json);
				process94Category(170,"jobs",categoryNumber,json);
//				process94Category(183,"verbs",categoryNumber,json);
			}
			
			br = new BufferedReader(new InputStreamReader(verbsFile, Charset.forName("UTF-8")));
			String line;
			while ((line = br.readLine()) != null) {
				String trimmedLine = line.trim();
				if(hasText(trimmedLine)){					
					indexWord("verbs", trimmedLine);
				}
			}
			return ok();
		} catch (IOException e) {
			Logger.error("Processing categories json",e);
		}finally{
			br.close();
			inJson.close();
			verbsFile.close();
		}
		
		System.out.println("ANTES DE INTERNAL");
		return internalServerError();
	}

	private void process94Category(int categoryNumber, String categoryName, int categoryNumberFromJson, JsonNode json) {
		if(categoryNumberFromJson == categoryNumber){
			for(JsonNode word : json.get("w")){
				index94JsonWord(word,categoryName);
			}
		}
	}

	private void index94JsonWord(JsonNode jsonWord, String categoryName) {
		String word = processWord(Json.stringify(jsonWord.get("m")));
		indexWord(categoryName, word);
	}

	private void indexWord(String categoryName, String word) {
		String json = Json.newObject().put("value", word).put("letter", getLetter(word)).put("language", "ES").toString();
		IndexResponse response = elasticSearchClient.prepareIndex(s("elasticsearch.updater.index"), categoryName).setSource(json).execute().actionGet();
		response.getIndex();
	}

	private String getLetter(String word) {
		return word.substring(0, 1);
	}

	private String processWord(String unprocessedWord) {
		String word = unprocessedWord.substring(1, unprocessedWord.length() - 1);
		return word.toLowerCase();
	}
}
