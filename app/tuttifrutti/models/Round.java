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
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Transient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tuttifrutti.serializers.ObjectIdSerializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
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
	
	@Reference
	private Match match;
	
	private Integer number;
	
	@JsonUnwrapped
	@Embedded
	private Letter letter;
	
	@Property("end_time")
	private Integer endTime;
	
	private String winner;
	
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

	private Letter getLetter(Match match) {
		Round round = match.getLastRound();
		if(round == null){
			return Letter.random();
		}
		return round.getLetter().next();
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
}
