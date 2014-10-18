package tuttifrutti.models;

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
	
	private String category;
	
	private String word;
	
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
}
