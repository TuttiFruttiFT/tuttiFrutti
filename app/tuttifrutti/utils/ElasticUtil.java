package tuttifrutti.utils;

import java.util.List;

import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
			
		}
		
		MultiSearchResponse mResponse = elasticSearchClient.multiSearch(mSearch).actionGet();
		
	}

}
