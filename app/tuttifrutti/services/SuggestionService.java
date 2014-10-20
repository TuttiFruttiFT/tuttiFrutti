package tuttifrutti.services;

import static java.util.Collections.singletonList;
import static tuttifrutti.models.Suggestion.BATCH_SIZE;
import static tuttifrutti.models.Suggestion.VOTES_TO_ACCEPT;
import static tuttifrutti.models.Suggestion.VOTES_TO_REJECT;
import static tuttifrutti.models.enums.SuggestionState.ACCEPTED;
import static tuttifrutti.models.enums.SuggestionState.REJECTED;
import static tuttifrutti.models.enums.SuggestionState.SUGGESTED;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tuttifrutti.models.Category;
import tuttifrutti.models.Suggestion;

/**
 * @author rfanego
 */
@Component
public class SuggestionService {
	@Autowired
	private Datastore mongoDatastore;
	
	public Suggestion suggest(Category category, String word, String playerId) {
		word = word.toLowerCase().trim();
		Suggestion suggestion = search(category,word);
		if(suggestion != null){
			int positiveVotes = suggestion.getPositiveVotes() + 1;
			suggestion.setPositiveVotes(positiveVotes);
		}else{
			suggestion = new Suggestion();
			suggestion.setCategory(category);
			suggestion.setWrittenWord(word);
			suggestion.setNegativeVotes(0);
			suggestion.setPositiveVotes(0);
			suggestion.setPlayerIds(singletonList(playerId));
			suggestion.setState(SUGGESTED);
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
}
