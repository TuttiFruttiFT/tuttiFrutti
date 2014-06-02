package models;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * @author rfanego
 */
@Entity
public class PowerUp {
	@Id 
	private ObjectId id;
	
	private String nombre;
}
