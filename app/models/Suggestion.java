package models;

import java.util.List;

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
public class Suggestion {
	
	public static final String SUGGESTED = "SUGGESTED";
	public static final String APPROVED = "APPROVED";
	public static final String REJECTED = "REJECTED";
	
	@Id 
	private ObjectId id;
	
	private String category;
	
	private String word;
	
	private String state;
	
	@Property("positive_votes")
	private Integer positiveVotes;
	
	@Property("negative_votes")
	private Integer negativeVotes;
	
	public static void add(String categoria, String palabra) {
		// TODO implementar
	}

	public static void judge(String categoria, String palabra, boolean valid) {
		// TODO implementar
		
	}

	public static List<Suggestion> getSuggestions(String idJugador) {
		/* TODO implementar
		 * implementar, consultar primero en un Cache (Redis o EhCache) si el jugador
		 * ya consultó esa palabra, y guardar en cache cuando la devolvemos
		 */
		
		return null;
	}

}
