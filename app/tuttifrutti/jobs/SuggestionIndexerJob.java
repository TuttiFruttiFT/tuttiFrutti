package tuttifrutti.jobs;

import static tuttifrutti.models.enums.SuggestionState.CONSOLIDATED;
import static tuttifrutti.models.enums.SuggestionState.TO_BE_ELIMINATED;

import java.util.List;

import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import play.Logger;
import tuttifrutti.elastic.ElasticUtil;
import tuttifrutti.models.Suggestion;
import tuttifrutti.services.SuggestionService;

/**
 * @author rfanego
 */
@Component
public class SuggestionIndexerJob implements Runnable {

	@Autowired
	private SuggestionService suggestionService;
	
	@Autowired
	private ElasticUtil elasticUtil;
	
	@Autowired
	private Datastore mongoDatastore;
	
	@Override
	public void run() {
		Logger.info("Starting SuggestionIndexerJob");
		try{
			List<Suggestion> acceptedSuggestions = suggestionService.acceptedSuggestions();
			acceptedSuggestions.forEach(suggestion -> {
				elasticUtil.indexWord(suggestion.getCategory().getId(), suggestion.getWrittenWord());
				suggestion.setState(CONSOLIDATED);
				Logger.info(String.format("Indexed %s in category %s", suggestion.getWrittenWord(),suggestion.getCategory().getId()));
			});
			
			mongoDatastore.save(acceptedSuggestions);
			
			List<Suggestion> rejectedSuggestions = suggestionService.rejectedSuggestions();
			rejectedSuggestions.forEach(suggestion -> {
				suggestion.setState(TO_BE_ELIMINATED);
			});
			
			mongoDatastore.save(rejectedSuggestions);
		}catch(Exception e){
			Logger.error("job SuggestionIndexerJob",e);
		}
		Logger.info("Finishing SuggestionIndexerJob");
	}

}
