package tuttifrutti.models;

import static java.util.Collections.shuffle;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import lombok.Getter;
import lombok.NoArgsConstructor;
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
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rfanego
 */
@Entity
@Getter @Setter
@NoArgsConstructor
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
	
	private String bgcolor;
	
	@Transient
	@JsonProperty(value = "power_ups")
	private List<PowerUp> powerUps = new ArrayList<>();
	
	@Transient
	@Autowired
	private Datastore mongoDatastore;
	
	public Category(String id){
		this.id = id;
	}
	
	public List<Category> categories(String language) {
		if(isEmpty(language)){
			language = "ES";
		}
		Query<Category> query = mongoDatastore.find(Category.class, "language =", language);
		return query.asList();
	}

	public List<Category> getPublicMatchCategories(String language) {
		List<Category> categories = this.categories(language);
		shuffle(categories, new Random());
		return categories.subList(0, DEFAULT_CATEGORIES_NUMBER);
	}

	public List<Category> categoriesFromIds(List<String> categoryIds) {
		return mongoDatastore.find(Category.class).field("_id").in(categoryIds).asList();
	}
}
