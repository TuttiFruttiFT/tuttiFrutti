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
public class Ronda {
	@Id 
	private ObjectId id;
	
	private Key<Partida> partida;
	
	@Property("numero_ronda")
	private Integer numeroRonda;
	
	private String letra;
	
	@Property("tiempo_fin_ronda")
	private Integer tiempoFinRonda;
	
	@Embedded
	private List<Turno> turnos;
}
