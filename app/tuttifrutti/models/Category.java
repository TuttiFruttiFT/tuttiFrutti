package tuttifrutti.models;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import lombok.Getter;
import lombok.Setter;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author rfanego
 */
@Entity
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class Category {
	public static final int DEFAULT_CATEGORIES_NUMBER = 6;
	public static final int MINIMUM_CATEGORIES_NUMBER = 4;
	public static final int MAXIMUM_CATEGORIES_NUMBER = 12;
	
	@Id 
	private String id;
	
	private String name;
	
	private String location;
	
	private String language;
	
	private String image;
	
	@Transient
	@Autowired
	private Datastore mongoDatastore;

	public List<Category> categories(String language) {
		Query<Category> query = mongoDatastore.find(Category.class, "language =", language);
		return query.asList();
	}

	public List<Category> getPublicMatchCategories(String language) {
		List<Category> categories = this.categories(language);
		Collections.shuffle(categories, new Random());
		return categories.subList(0, DEFAULT_CATEGORIES_NUMBER);
	}
}
