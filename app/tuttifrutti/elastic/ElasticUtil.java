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
import static tuttifrutti.utils.ConfigurationAccessor.i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import play.Logger;
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
				MatchQueryBuilder matchQueryBuilder = matchQuery("value", writtenWord);
				matchQueryBuilder.fuzziness(AUTO);
				matchQueryBuilder.prefixLength(1);
				matchQueryBuilder.maxExpansions(1);
				matchQueryBuilder.minimumShouldMatch("100%");
				
				boolQueryBuilder.must(matchQueryBuilder);
				boolQueryBuilder.should(matchQuery("letter", letter.getLetter().toString()));
				boolQueryBuilder.minimumNumberShouldMatch(1);
				
				SearchRequestBuilder searchQuery = elasticSearchClient.prepareSearch("categories").setTypes(categoryId).setSize(1);
				searchQuery.setQuery(boolQueryBuilder);
				
				mSearch.add(searchQuery);
			}
			mapDuplas.put(categoryId, dupla);
		}
		
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
				dupla.setFinalWord(sourceAsMap.get("value").toString());
			}			
		}
		
		for(Entry<String, Dupla> entry : mapDuplas.entrySet()){
			Dupla dupla = entry.getValue();
			if(isEmpty(dupla.getFinalWord())){
				dupla.setWrongState();
			}
		}
	}
	
	public List<String> searchWords(Letter letter,String category,int numberOfWords){
		List<String> words = new ArrayList<>();
		SearchRequestBuilder searchQuery = elasticSearchClient.prepareSearch("categories").setSearchType(QUERY_THEN_FETCH)
				.setSize(numberOfWords).setTypes(category);
		QueryBuilder queryBuilder = matchQuery("letter", letter.getLetter().toString());
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
}
