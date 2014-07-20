package models;

import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Property;

/**
 * @author rfanego
 */
@Entity
public class CategoriaTurno {
	private Key<Categoria> categoria;
	
	private String valor;
	
	@Property("tiempo_relativo")
	private Double tiempoRelativo;
	
	private Integer puntaje;
}
