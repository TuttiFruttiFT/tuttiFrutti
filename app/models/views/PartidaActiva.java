package models.views;

import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;

/**
 * @author rfanego
 */
@Getter @Setter
public class PartidaActiva {

	private ObjectId id;
	
	private String nombre;
	
	private String letra;
	
	private Integer rondasFaltantes;
	
	private String estado;
	
}
