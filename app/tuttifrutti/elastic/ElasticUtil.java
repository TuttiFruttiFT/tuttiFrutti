package tuttifrutti.elastic;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.elasticsearch.action.search.SearchType.QUERY_THEN_FETCH;
import static org.elasticsearch.common.lucene.search.function.CombineFunction.SUM;
import static org.elasticsearch.common.unit.Fuzziness.AUTO;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.functionScoreQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.functionscore.ScoreFunctionBuilders.randomFunction;
import static tuttifrutti.models.enums.LanguageType.ES;
import static tuttifrutti.utils.ConfigurationAccessor.i;
import static tuttifrutti.utils.WordUtil.getLetter;
import static tuttifrutti.utils.WordUtil.processWord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.MultiSearchResponse.Item;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.functionscore.FunctionScoreQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import play.Logger;
import play.libs.Json;
import tuttifrutti.models.Dupla;
import tuttifrutti.models.Letter;
import tuttifrutti.models.LetterWrapper;
import tuttifrutti.models.ScoreCalculator;

/**
 * @author rfanego
 */
@Component
public class ElasticUtil {
	private static final int TIMEOUT_IN_MILLIS = i("elasticsearch.timeout");
	private static final String LETTER_FIELD = "letter";
	private static final String VALUE_FIELD = "value";
	private static final String LANGUAGE_FIELD = "language";
	private static final String CATEGORIES_INDEX = "categories";

	@Autowired
	private Client elasticSearchClient;
	
	@Autowired
	private ScoreCalculator scoreCalculator;

	public void validate(List<Dupla> duplas, LetterWrapper letter) {
		Map<String,Dupla> mapDuplas = new HashMap<>();
		MultiSearchRequest mSearch = new MultiSearchRequest();
		
		for(Dupla dupla : duplas){
			BoolQueryBuilder boolQueryBuilder = boolQuery();
			String categoryId = dupla.getCategory().getId();
			
			String writtenWord = dupla.getWrittenWord() != null ? dupla.getWrittenWord().trim() : null;
			
			if(isNotEmpty(writtenWord)){
				boolQueryBuilder.must(matchQueryForWord(writtenWord));
				boolQueryBuilder.should(matchQueryForLetter(letter.getLetter().toString()));
				boolQueryBuilder.minimumNumberShouldMatch(1);
				
				SearchRequestBuilder searchQuery = elasticSearchClient.prepareSearch(CATEGORIES_INDEX).setTypes(categoryId).setSize(1);
				searchQuery.setQuery(boolQueryBuilder);
				
				mSearch.add(searchQuery);
			}
			mapDuplas.put(categoryId, dupla);
		}
		
		if(!mSearch.requests().isEmpty()){			
			MultiSearchResponse mResponse = elasticSearchClient.multiSearch(mSearch).actionGet();
			Iterator<Item> iterator = mResponse.iterator();
			while(iterator.hasNext()){
				Item item = iterator.next();
				if(item.isFailure()){
					Logger.error("searching word in elastic");
					continue;
				}
				SearchResponse response = item.getResponse();
				SearchHits hits = response.getHits();
				SearchHit[] searchHits = hits.getHits();
				if(searchHits.length > 0){
					SearchHit hit = searchHits[0];
					Dupla dupla = mapDuplas.get(hit.getType());
					Map<String, Object> sourceAsMap = hit.sourceAsMap();
					dupla.setFinalWord(sourceAsMap.get(VALUE_FIELD).toString());
				}			
			}
		}
		
		for(Entry<String, Dupla> entry : mapDuplas.entrySet()){
			Dupla dupla = entry.getValue();
			if(isEmpty(dupla.getFinalWord())){
				dupla.setWrongState();
			}
		}
	}

	private MatchQueryBuilder matchQueryForLetter(String letter) {
		return matchQuery("letter", letter);
	}

	public List<String> searchWords(Letter letter,String category,int numberOfWords){
		List<String> words = new ArrayList<>();
		SearchRequestBuilder searchQuery = elasticSearchClient.prepareSearch(CATEGORIES_INDEX).setSearchType(QUERY_THEN_FETCH)
				.setSize(numberOfWords).setTypes(category);
		QueryBuilder queryBuilder = matchQuery(LETTER_FIELD, letter.getLetter().toString());
		FunctionScoreQueryBuilder functionQueryBuilder = functionScoreQuery(queryBuilder).boostMode(SUM).scoreMode("sum");
		functionQueryBuilder.add(randomFunction(new Random().nextLong()));
		searchQuery.setQuery(functionQueryBuilder);
		
		SearchResponse searchResponse =  searchQuery.execute().actionGet(TIMEOUT_IN_MILLIS, MILLISECONDS);
		SearchHits hits = searchResponse.getHits();
		Iterator<SearchHit> it = hits.iterator();
		while (it.hasNext()){
			SearchHit hit = it.next();
			words.add(hit.sourceAsMap().get("value").toString());
		}
		return words;
	}
	
	public boolean existWord(String category,String word){
		SearchRequestBuilder searchQuery = elasticSearchClient.prepareSearch(CATEGORIES_INDEX).setSearchType(QUERY_THEN_FETCH)
				.setSize(1).setTypes(category);
		
		BoolQueryBuilder boolQueryBuilder = boolQuery();
		boolQueryBuilder.must(matchQueryForWord(word));
		boolQueryBuilder.must(matchQueryForLetter(getLetter(word)));
		
		searchQuery.setQuery(boolQueryBuilder);
		SearchResponse searchResponse =  searchQuery.execute().actionGet(TIMEOUT_IN_MILLIS, MILLISECONDS);
		SearchHits hits = searchResponse.getHits();
		Iterator<SearchHit> it = hits.iterator();
		return it.hasNext();
	}
	
	public void indexWord(String categoryName, String unprocessedWord) {
		String word = processWord(unprocessedWord);
		String json = Json.newObject().put(VALUE_FIELD, word).put(LETTER_FIELD, getLetter(word)).put(LANGUAGE_FIELD, ES.toString()).toString();
		IndexResponse response = elasticSearchClient.prepareIndex(CATEGORIES_INDEX, categoryName).setSource(json).execute().actionGet();
		response.getIndex();
	}
	
	private MatchQueryBuilder matchQueryForWord(String word) {
		MatchQueryBuilder matchQueryBuilder = matchQuery(VALUE_FIELD, word);
		matchQueryBuilder.fuzziness(AUTO);
		matchQueryBuilder.prefixLength(1);
		matchQueryBuilder.maxExpansions(1);
		matchQueryBuilder.minimumShouldMatch("100%");
		return matchQueryBuilder;
	}

	public Map<String, Boolean> availableLettersForCategory(String categoryName) {
		Map<String,Boolean> availableLetters = new HashMap<String, Boolean>(); 
		TermsBuilder termsBuilder = AggregationBuilders.terms("group_by_letter").field(LETTER_FIELD).size(0);
		SearchResponse searchResponse = elasticSearchClient.prepareSearch(CATEGORIES_INDEX).setTypes(categoryName).addAggregation(termsBuilder).setSize(0).execute().actionGet();
		Aggregations aggregations = searchResponse.getAggregations();
		Terms terms = aggregations.get("group_by_letter");
		terms.getBuckets().forEach(bucket -> {
			availableLetters.put(bucket.getKey().toUpperCase(), true);
		});
		return availableLetters;
	}
}
