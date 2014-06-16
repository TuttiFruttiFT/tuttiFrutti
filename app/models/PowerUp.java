package models;

import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Transient;

/**
 * @author rfanego
 */
@Entity
public class PowerUp {
	@Id 
	private ObjectId id;
	
	private String nombre;
	
	@Transient
    private Map<String,String> categorias;

	public static void generar(Partida partida) {		
		PowerUp.generarAutoCompletarPalabra(partida);
		PowerUp.generarSugerirPalabra(partida);
		PowerUp.generarPalabrasOponente(partida);
	}

	private static void generarPalabrasOponente(Partida partida) {
		// TODO implementar
		
	}

	private static void generarSugerirPalabra(Partida partida) {
		// TODO implementar
		
	}

	private static void generarAutoCompletarPalabra(Partida partida) {
		// TODO implementar
		
	}
	
}
