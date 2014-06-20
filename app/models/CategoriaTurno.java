package models;

import lombok.Getter;
import lombok.Setter;

import org.mongodb.morphia.annotations.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author rfanego
 */
@Entity
//@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CategoriaTurno {
	private Categoria categoria;
	
	private String palabra_ingresada;
	
	private String palabra_final;
	
	private Double tiempoRelativo;
	
	private String estado;
	
	private Integer puntaje;
}
