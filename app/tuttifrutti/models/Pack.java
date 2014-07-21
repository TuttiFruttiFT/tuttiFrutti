package tuttifrutti.models;

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
public class Pack {
	
	@Id 
	private ObjectId id;
	
	private String name;
	
	private String desc;
	
	@Property("cantidad_actual")
	private Integer currentAmount;
	
	@Property("cantidad_default")
	private Integer defaultAmount;
	
	private Double price;

	public static List<Pack> packs() {
		// TODO implementar
		return null;
	}

	public static Pack pack(String idPack) {
		// TODO implementar
		return null;
	}

}
