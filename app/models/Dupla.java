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
public class Dupla {
	private Categoria categoria;
	
	@Property("palabra_ingresada")
	private String palabraIngresada;
	
	@Property("palabra_final")
	private String palabraFinal;
	
	private Double tiempoRelativo;
	
	private String estado;
	
	private Integer puntaje;
}
