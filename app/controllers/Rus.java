package controllers;

import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;

import models.Jugador;
import models.Pack;
import play.mvc.Controller;
import play.mvc.Result;
import utils.GoogleUtil;

/**
 * @author rfanego
 */
public class Rus extends Controller {
	public static Result comprar(){
		JsonNode json = request().body().asJson();
		String idJugador = json.get("id_jugador").asText();
		String idPack = json.get("id_pack").asText();
		
		Pack pack = Pack.pack(idPack);
		
		GoogleUtil.compra(idJugador,pack);
		
		Jugador.compra(idJugador,pack);
		
		return ok();
	}

	public static Result obtenerPacks(){
		
		List<Pack> packs = Pack.packs();
		
		//TODO ver como crear un json desde una lista
		
		return ok();
	}
}
