package controllers;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author rfanego
 */
public class Partidas extends Controller {
	public static Result obtenerPartida(String idPartida) {

		return ok();
	}

	public static Result nuevaPartidaPublica() {

		return ok();
	}

	public static Result nuevaPartidaPrivada() {

		return ok();
	}

	public static Result jugarTurno() {

		return ok();
	}

	public static Result resultadoTurno(String idPartida,int numeroTurno) {

		return ok();
	}

	public static Result resultadoPartida(String idPartida) {

		return ok();
	}
}
