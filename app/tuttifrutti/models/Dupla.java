package tuttifrutti.models;

import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author rfanego
 */
@Entity
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dupla {
	@Id 
	private ObjectId id;
	
	private Category category;
	
	@Property("written_word")
	private String writtenWord;
	
	@Property("final_word")
	private String finalWord;
	
	private Double time;
	
	private String state;
	
	private Integer score;
}
