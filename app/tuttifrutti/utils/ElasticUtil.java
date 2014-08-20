package tuttifrutti.utils;

import java.util.Iterator;
import java.util.List;

import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.MultiSearchResponse.Item;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import play.Logger;
import tuttifrutti.models.Dupla;

/**
 * @author rfanego
 */
@Component
public class ElasticUtil {
	
	@Autowired
	private Client elasticSearchClient;

	public void validar(List<Dupla> duplas) {
		MultiSearchRequest mSearch = new MultiSearchRequest();
		
		for(Dupla dupla : duplas){
			MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("value", dupla.getWrittenWord());
			matchQueryBuilder.fuzziness(Fuzziness.AUTO);
			matchQueryBuilder.prefixLength(1);
			matchQueryBuilder.maxExpansions(50);
			
			SearchRequestBuilder searchQuery = elasticSearchClient.prepareSearch("words").setSearchType(dupla.getCategory().getId()).setSize(1);
			searchQuery.setQuery(matchQueryBuilder);
			
			mSearch.add(searchQuery);
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
		}
	}

}
