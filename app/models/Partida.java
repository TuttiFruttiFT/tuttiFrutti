package models;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity
public class Partida {
	@Id 
	private ObjectId id;
	
	private String tipo;
	
	@Property("tiempo_ronda")
	private Integer tiempoRonda;
	
	@Property("cant_rondas")
	private Integer cantRondas;
	
	@Property("duracion_maxima")
	private Integer duracionMaxima;
	
	@Property("tiempo_espera_ronda")
	private Integer tiempoEsperaRonda;
	
	@Property("jugador_ganador")
	private Jugador jugadorGanador;
	
	@Property("fecha_inicio")
	private Date fechaInicio;
	
	@Embedded
	private List<Categoria> categorias;
	
	@Embedded
	private List<Jugador> jugadores;
	
	@Embedded
	private List<PowerUp> powerUps;
}
