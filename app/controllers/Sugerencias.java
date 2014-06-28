package controllers;

import java.util.List;

import models.Suggestion;
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
			
			Suggestion.agregar(categoria,palabra);
		}
		
        return ok();
    }
	
	public static Result juzgar() {
		JsonNode json = request().body().asJson();
		
		String categoria = json.get("categoria").asText();
		String palabra = json.get("palabra").asText();
		Boolean valid = json.get("valid").asBoolean();
		
		Suggestion.juzgar(categoria,palabra, valid);
		
        return ok();
    }
	
	public static Result palabraAValidar(String idJugador) {		
		List<Suggestion> sugerencias = Suggestion.obtenerPalabra(idJugador);
		
		//TODO ver como crear un json desde una lista
		
        return ok(Json.toJson(sugerencias));
    }
}
