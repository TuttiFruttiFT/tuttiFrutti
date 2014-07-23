package tuttifrutti.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author rfanego
 */
@Entity
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Category {
	public static final int DEFAULT_CATEGORIES_NUMBER = 6;
	public static final int MINIMUM_CATEGORIES_NUMBER = 4;
	public static final int MAXIMUM_CATEGORIES_NUMBER = 12;
	
	@Id 
	private ObjectId id;
	
	private String name;
	
	private String location;
	
	private String language;
	
	private String image;

	public static List<Category> categories() {
		// TODO implementar
		return null;
	}

	public static List<Category> getPublicMatchCategories() {
		// TODO implementar
		return null;
	}
}
