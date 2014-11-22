package tuttifrutti.models;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static tuttifrutti.models.enums.SuggestionState.REJECTED;
import static tuttifrutti.models.enums.SuggestionState.TO_BE_ACCEPTED;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import tuttifrutti.models.enums.SuggestionState;
import tuttifrutti.serializers.ObjectIdSerializer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author rfanego
 */
@Entity
@Getter @Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Suggestion {
	public static final int VOTES_TO_ACCEPT = 5;
	public static final int VOTES_TO_REJECT = 5;
	public static final int BATCH_SIZE = 20;
	
	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	private ObjectId id;
	
	private Category category;
	
	@JsonProperty("written_word")
	@Property("written_word")
	private String writtenWord;
	
	@JsonIgnore
	private SuggestionState state;
	
	@Property("positive_votes")
	@JsonIgnore
	private Integer positiveVotes;
	
	@Property("negative_votes")
	@JsonIgnore
	private Integer negativeVotes;
	
	@Property("player_ids")
	@JsonIgnore
	private List<String> playerIds;
	
	public Suggestion(Category category, String writtenWord, Integer positiveVotes, Integer negativeVotes, List<String> playerIds,SuggestionState state){
		this.category = category;
		this.writtenWord = writtenWord;
		this.positiveVotes = positiveVotes;
		this.negativeVotes = negativeVotes;
		this.playerIds = playerIds;
		this.state = state;
	}

	public void addPlayer(String playerId) {
		if(isEmpty(playerIds)){
			playerIds = new ArrayList<>();
		}
		playerIds.add(playerId);
	}

	public void addPositiveVote() {
		int positiveVotes = this.getPositiveVotes() + 1;
		if(positiveVotes >= VOTES_TO_ACCEPT){
			this.setState(TO_BE_ACCEPTED);
		}
		this.setPositiveVotes(positiveVotes);
	}

	public void addNegativeVote() {
		int negativeVotes = this.getNegativeVotes() + 1;
		if(negativeVotes >= VOTES_TO_REJECT){
			this.setState(REJECTED);
		}
		this.setNegativeVotes(negativeVotes);
		
	}
}
