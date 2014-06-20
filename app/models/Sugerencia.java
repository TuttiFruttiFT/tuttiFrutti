package models;

import lombok.Getter;
import lombok.Setter;

import org.mongodb.morphia.annotations.Entity;
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
public class Sugerencia {
	
	public static final String SUGERIDA = "SUGERIDA";
	public static final String APROBADA = "APROBADA";
	public static final String RECHAZADA = "RECHAZADA";
	
	private String categoria;
	
	private String palabra;
	
	private String estado;
	
	@Property("cantidad_juzgada_bien")
	private Integer cantidadJuzgadaBien;
	
	@Property("cantidad_juzgada_mal")
	private Integer cantidadJuzgadaMal;
	
	public static void agregar(String categoria, String palabra) {
		// TODO implementar
	}

	public static void juzgar(String categoria, String palabra) {
		// TODO implementar
		
	}

	public static Sugerencia obtenerPalabra(String idJugador) {
		/* TODO 
		 * implementar, consultar primero en un Cache (Redis o EhCache) si el jugador
		 * ya consultó esa palabra, y guardar en cache cuando la devolvemos
		 */
		
		return null;
	}

}
