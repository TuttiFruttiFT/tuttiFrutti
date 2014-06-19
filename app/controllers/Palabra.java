package controllers;

import models.Sugerencia;
import play.mvc.Controller;
import play.mvc.Result;

import com.fasterxml.jackson.databind.JsonNode;

public class Palabra extends Controller {
	public static Result sugerir() {
		JsonNode sugerencias = request().body().asJson();

		for(JsonNode sugerencia : sugerencias){
			String categoria = sugerencia.get("categoria").asText();
			String palabra = sugerencia.get("palabra").asText();
			
			Sugerencia.agregar(categoria,palabra);
		}
		
        return ok();
    }
	
	public static Result validar() {
		
        return ok();
    }
	
	public static Result palabrasAValidar() {
		
        return ok();
    }
}
