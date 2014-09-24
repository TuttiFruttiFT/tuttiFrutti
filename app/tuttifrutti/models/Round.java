package tuttifrutti.models;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tuttifrutti.serializers.LetterDeserializer;
import tuttifrutti.serializers.ObjectIdSerializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * @author rfanego
 */
@Entity
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class Round {
	@Id
	@JsonSerialize(using = ObjectIdSerializer.class)
	private ObjectId id;
	
	@Property("match_id")
	@JsonProperty(value = "match_id")
	private String matchId;
	
	private Integer number;
	
	@Embedded
	@JsonUnwrapped
	@JsonDeserialize(using = LetterDeserializer.class)
	private LetterWrapper letter;
	
	@Property("end_time")
	@JsonProperty(value = "end_time")
	private Integer endTime;
	
	@Embedded(value = "stop_player")
	@JsonProperty(value = "stop_player")
	private Player stopPlayer;
	
	@Embedded
	private List<Turn> turns;
	
	@Transient
	@Autowired
	private Datastore mongoDatastore;

	public void create(Match match) {
		Round round = new Round();
		round.setNumber(getRoundNumber(match));
		round.setLetter(getLetter(match));
		match.setLastRound(round);
		mongoDatastore.save(match);
	}

	private LetterWrapper getLetter(Match match) {
		Round round = match.getLastRound();
		if(round == null){
			return LetterWrapper.random();
		}
		LetterWrapper roundLetter = round.getLetter();
		return roundLetter.next();
	}

	private Integer getRoundNumber(Match match) {
		Round round = match.getLastRound();
		if(round == null){
			return 1;
		}
		return (round.getNumber() + 1);
	}

	public void addTurn(Turn turn) {
		if(turns == null){
			turns = new ArrayList<>();
		}
		turns.add(turn);
	}

	public Round getRound(String matchId, Integer roundNumber) {
		Query<Round> query = mongoDatastore.find(Round.class, "match_id =", matchId);
		query.and(query.criteria("number").equal(roundNumber));
		return query.get();
	}
}