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
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfiguracionPartida {
	
	@Property("tiempo_ronda")
	private Integer tiempoRonda;
	
	@Property("cant_rondas")
	private Integer cantRondas;
	
	@Property("duracion_maxima")
	private Integer duracionMaxima;
	
	@Property("tiempo_espera_ronda")
	private Integer tiempoEsperaRonda;
}
