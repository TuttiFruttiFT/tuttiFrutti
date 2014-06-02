package models;

import org.mongodb.morphia.annotations.Entity;

/**
 * @author rfanego
 */
@Entity
public class CategoriaTurno {
	private Categoria categoria;
	
	private String valor;
	
	private Double tiempoRelativo;
	
	private Integer puntaje;
}
