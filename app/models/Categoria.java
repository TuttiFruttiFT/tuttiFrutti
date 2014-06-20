package models;

import java.util.List;

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
//@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Categoria {
	@Id 
	private ObjectId id;
	
	private String nombre;
	
	private String localizacion;
	
	private String idioma;
	
	@Property("id_imagen")
	private String idImagen;

	public static List<Categoria> categorias() {
		// TODO implementar
		return null;
	}
}
