package models;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import models.views.PartidaActiva;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Transient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@Entity
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Partida {
	public static final String ESPERANDO_APROBACION = "ESPERANDO_APROBACION";
	public static final String TU_TURNO = "TU_TURNO";
	public static final String TURNO_RIVALES = "TURNO_RIVALES";
	public static final String PARTIDA_FINALIZADA = "PARTIDA_FINALIZADA";
	
	@Id 
	private ObjectId id;
	
	private String tipo;
	
	private String nombre;
	
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
	
	@Property("power_ups_activados")
	private Boolean powerUpsActivados;
	
	@Embedded
	private List<Categoria> categorias;
	
	@Embedded
	private List<Jugador> jugadores;
	
	@Transient
	private List<PowerUp> powerUps;
	
	@Transient
	private Ronda ultimaRonda;

	public static List<PartidaActiva> obtenerPartidasActivas(String idJugador) {
		// TODO implementar, partidas de idJugador que no estén en PARTIDA_FINALIZADA
		return null;
	}

	public static Partida obtenerPartida(String idPartida) {
		// TODO implementar
		return null;
	}

	public static Partida buscarPartida(Integer cantJugadores, String idioma) {
		// TODO implementar
		return null;
	}

	public static Partida crear(Integer cantJugadores, String idioma) {
		// TODO implementar
		return null;
	}

	public void agregarJugador(String idJugador) {
		// TODO implementar
		
	}
}
