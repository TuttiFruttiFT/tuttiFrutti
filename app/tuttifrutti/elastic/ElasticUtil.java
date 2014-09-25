package tuttifrutti.elastic;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.elasticsearch.common.unit.Fuzziness.AUTO;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.MultiSearchResponse.Item;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import play.Logger;
import tuttifrutti.models.Dupla;
import tuttifrutti.models.LetterWrapper;
import tuttifrutti.models.ScoreCalculator;

/**
 * @author rfanego
 */
@Component
public class ElasticUtil {
	
	@Autowired
	private Client elasticSearchClient;
	
	@Autowired
	private ScoreCalculator scoreCalculator;

	public void validar(List<Dupla> duplas, LetterWrapper letter) {
		Map<String,Dupla> mapDuplas = new HashMap<>();
		MultiSearchRequest mSearch = new MultiSearchRequest();
		
		for(Dupla dupla : duplas){
			BoolQueryBuilder boolQueryBuilder = boolQuery();
			String categoryId = dupla.getCategory().getId();
			
			String writtenWord = dupla.getWrittenWord();
			
			if(StringUtils.isNotEmpty(writtenWord)){
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

}
