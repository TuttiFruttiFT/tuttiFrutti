package controllers;

import java.net.UnknownHostException;

import models.Jugador;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.Mongo;

import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author rfanego
 */
public class Jugadores extends Controller {
	public static Result registrar() {
		JsonNode json = request().body().asJson();
		if(json == null){
			return badRequest();
		}else{
			String usuario = json.get("mail").asText();
			String clave = json.get("clave").asText();
			String facebookId = json.get("facebookId").asText();
			String twitterId = json.get("twitterId").asText();

			String dbName = new String("tutti");
			Mongo mongo = null;
			try {
				mongo = new Mongo( "localhost", 27017 );
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
			Morphia morphia = new Morphia();
			Datastore datastore = morphia.createDatastore(mongo, dbName); 
			
			morphia.mapPackage("models");
			
			Jugador jugador = new Jugador();
			jugador.setNickname(usuario);
			jugador.setClave(clave);
			
			datastore.save(jugador);

			return ok();
		}
    }
	
	public static Result validar(String mail,String clave,String facebookId,String twitterId) {
		
        return ok();
    }
	
	public static Result sincronizar(String idJugador) {
		
        return ok();
    }
	
	public static Result editarPerfil() {
		
        return ok();
    }
}