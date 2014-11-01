package tuttifrutti.controllers;

import static org.springframework.util.StringUtils.hasText;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;

import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.elastic.ElasticUtil;
import tuttifrutti.services.SuggestionService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class CategoryLoader extends Controller {
	
	@Autowired
	private SuggestionService suggestionService;
	
	@Autowired
	private ElasticUtil elasticUtil;
	
	@SneakyThrows
	public Result load94secondsCategories(){
		InputStream inJson = CategoryLoader.class.getResourceAsStream("/categorias/ninetyfour_seconds_es.json");
		InputStream verbsFile = CategoryLoader.class.getResourceAsStream("/categorias/verbs.txt");
		InputStream bandsFile = CategoryLoader.class.getResourceAsStream("/categorias/bandas_def.txt");
		InputStream animalsFile = CategoryLoader.class.getResourceAsStream("/categorias/animales.txt");
		InputStream colorsFile = CategoryLoader.class.getResourceAsStream("/categorias/colors.txt");
		InputStream countriesFile = CategoryLoader.class.getResourceAsStream("/categorias/countries.txt");
		InputStream drinksFile = CategoryLoader.class.getResourceAsStream("/categorias/drinks.txt");
		InputStream jobsFile = CategoryLoader.class.getResourceAsStream("/categorias/jobs.txt");
		InputStream musicalInstrumentsFile = CategoryLoader.class.getResourceAsStream("/categorias/musical_instruments.txt");
		InputStream musicalStylesFile = CategoryLoader.class.getResourceAsStream("/categorias/musical_styles.txt");
		InputStream sportsFile = CategoryLoader.class.getResourceAsStream("/categorias/sports.txt");
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
//				process94Category(171,"animals",categoryNumber,json);
//				process94Category(174,"animals",categoryNumber,json);
				process94Category(173,"countries",categoryNumber,json);
				process94Category(170,"jobs",categoryNumber,json);
//				process94Category(183,"verbs",categoryNumber,json);
			}
			
			processWordFile(verbsFile, "verbs");
			processWordFile(bandsFile, "bands");
			processWordFile(animalsFile, "animals");
			processWordFile(colorsFile, "colors");
			processWordFile(countriesFile, "countries");
			processWordFile(drinksFile, "drinks");
			processWordFile(jobsFile, "jobs");
			processWordFile(musicalInstrumentsFile, "musical_instruments");
			processWordFile(musicalStylesFile, "musical_styles");
			processWordFile(sportsFile, "sports");
			
			suggestionService.consolidatedSuggestions().forEach(suggestion -> {
				String trimmedLine = suggestion.getWrittenWord().trim();
				if(hasText(trimmedLine)){
					elasticUtil.indexWord(suggestion.getCategory().getId(), trimmedLine);
				}
			});
			
			return ok();
		} catch (IOException e) {
			Logger.error("Processing categories json",e);
		}finally{
			inJson.close();
			verbsFile.close();
		}
		
		return internalServerError();
	}

	private void processWordFile(InputStream file, String typeName) throws IOException {
		String line;
		BufferedReader br = new BufferedReader(new InputStreamReader(file, Charset.forName("UTF-8")));
		while ((line = br.readLine()) != null) {
			String trimmedLine = line.trim().replace("\ufeff", "");
			if(hasText(trimmedLine)){
				elasticUtil.indexWord(typeName, trimmedLine);
			}
		}
		br.close();
	}

	private void process94Category(int categoryNumber, String categoryName, int categoryNumberFromJson, JsonNode json) {
		if(categoryNumberFromJson == categoryNumber){
			for(JsonNode word : json.get("w")){
				index94JsonWord(word,categoryName);
			}
		}
	}

	private void index94JsonWord(JsonNode jsonWord, String categoryName) {
		String word = process94Word(Json.stringify(jsonWord.get("m")));
		elasticUtil.indexWord(categoryName, word);
	}

	private String process94Word(String unprocessedWord) {
		return unprocessedWord.substring(1, unprocessedWord.length() - 1);
	}
}
