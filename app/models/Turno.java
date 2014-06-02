package models;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

/**
 * @author rfanego
 */
@Entity
public class Turno {
	@Id 
	private ObjectId id;
	
	private Key<Jugador> jugador;
	
	@Property("tiempo_fin_turno")
	private Integer tiempoFinTurno;
	
	private Integer puntaje;
	
	@Embedded
	private List<CategoriaTurno> categoriasTurno;
}
