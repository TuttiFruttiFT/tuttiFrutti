package tuttifrutti.services;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static tuttifrutti.models.Suggestion.BATCH_SIZE;
import static tuttifrutti.models.Suggestion.VOTES_TO_ACCEPT;
import static tuttifrutti.models.Suggestion.VOTES_TO_REJECT;
import static tuttifrutti.models.enums.SuggestionState.ACCEPTED;
import static tuttifrutti.models.enums.SuggestionState.CONSOLIDATED;
import static tuttifrutti.models.enums.SuggestionState.REJECTED;
import static tuttifrutti.models.enums.SuggestionState.SUGGESTED;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import play.Logger;
import tuttifrutti.elastic.ElasticUtil;
import tuttifrutti.models.Category;
import tuttifrutti.models.Suggestion;
import tuttifrutti.models.enums.SuggestionState;

/**
 * @author rfanego
 */
@Component
public class SuggestionService {
	@Autowired
	private Datastore mongoDatastore;
	
	@Autowired
	private ElasticUtil elasticUtil;
	
	public Suggestion suggest(Category category, String word, String playerId) {
		word = word.toLowerCase().trim();
		Suggestion suggestion = search(category,word);
		if(suggestion != null){
			if(!suggestion.getPlayerIds().contains(playerId)){				
				int positiveVotes = suggestion.getPositiveVotes() + 1;
				suggestion.setPositiveVotes(positiveVotes);
				suggestion.getPlayerIds().add(playerId);
			}
		}else{
			if(!elasticUtil.existWord(category.getId(), word)){		
				if(isAdminUser(playerId)){
					suggestion = new Suggestion(category,word,VOTES_TO_ACCEPT,0,singletonList(playerId),ACCEPTED);
				}else{
					suggestion = new Suggestion(category,word,0,0,singletonList(playerId),SUGGESTED);
				}
			}else{
				Logger.info(format("Word %s already exists for category %s", word,category.getId()));
			}
		}
		return suggestion;
	}

	public Suggestion judge(String suggestionId, boolean valid, String playerId) {
		Suggestion suggestion = search(suggestionId);
		if(valid){
			int positiveVotes = suggestion.getPositiveVotes() + 1;
			if(positiveVotes >= VOTES_TO_ACCEPT){
				suggestion.setState(ACCEPTED);
			}
			suggestion.setPositiveVotes(positiveVotes);
		}else{
			int negativeVotes = suggestion.getNegativeVotes() + 1;
			if(negativeVotes >= VOTES_TO_REJECT){
				suggestion.setState(REJECTED);
			}
			suggestion.setNegativeVotes(negativeVotes);
		}
		suggestion.getPlayerIds().add(playerId);
		return suggestion;
	}

	public List<Suggestion> getSuggestions(String playerId) {
		Query<Suggestion> query = mongoDatastore.find(Suggestion.class, "state =", SUGGESTED.toString());
		query.and(query.criteria("player_ids").not().contains(playerId));
		return query.batchSize(BATCH_SIZE).asList();
	}
	
	public List<Suggestion> consolidatedSuggestions(){
		return getSuggestionsWithState(CONSOLIDATED);
	}
	
	public List<Suggestion> acceptedSuggestions(){
		return getSuggestionsWithState(ACCEPTED);
	}
	
	public List<Suggestion> rejectedSuggestions(){
		return getSuggestionsWithState(REJECTED);
	}
	
	private List<Suggestion> getSuggestionsWithState(SuggestionState state) {
		Query<Suggestion> query = mongoDatastore.find(Suggestion.class, "state =", state.toString());
		return query.asList();
	}
	
	private Suggestion search(Category category,String word){
		Query<Suggestion> query = mongoDatastore.find(Suggestion.class, "category.id =", category.getId());
		query.and(query.criteria("written_word").equal(word));
		return query.get();
	}

	public void save(List<Suggestion> suggestions) {
		mongoDatastore.save(suggestions);
	}
	
	private Suggestion search(String suggestionId) {
		return mongoDatastore.get(Suggestion.class,new ObjectId(suggestionId));
	}
	
	private boolean isAdminUser(String playerId) {
		return playerId.equals("5435b037e4b04c9acff5e8a7") || playerId.equals("5435c9a1e4b04c9acff5e8a8") || playerId.equals("543d3a91e4b0a5e7d636f678") || 
			   playerId.equals("5439b152e4b0a9cd2f3467ca") || playerId.equals("5439964de4b0a9cd2f3467c9") || playerId.equals("5435a0bbe4b04c9acff5e8a5");
	}
}
