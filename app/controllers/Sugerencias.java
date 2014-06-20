package controllers;

import models.Sugerencia;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;

public class Sugerencias extends Controller {
	public static Result sugerir() {
		JsonNode sugerencias = request().body().asJson();

		for(JsonNode sugerencia : sugerencias){
			String categoria = sugerencia.get("categoria").asText();
			String palabra = sugerencia.get("palabra").asText();
			
			Sugerencia.agregar(categoria,palabra);
		}
		
        return ok();
    }
	
	public static Result juzgar() {
		JsonNode json = request().body().asJson();
		
		String categoria = json.get("categoria").asText();
		String palabra = json.get("palabra").asText();
		
		Sugerencia.juzgar(categoria,palabra);
		
        return ok();
    }
	
	public static Result palabraAValidar(String idJugador) {		
		Sugerencia sugerencia = Sugerencia.obtenerPalabra(idJugador);
        return ok(Json.toJson(sugerencia));
    }
}
