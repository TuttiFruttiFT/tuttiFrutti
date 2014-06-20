package controllers;

import java.util.List;

import models.Categoria;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author rfanego
 */
public class Categorias extends Controller {
	public static Result categoriasDisponibles() {
		List<Categoria> categorias = Categoria.categorias();
		
		//TODO ver como crear un json desde una lista
		
        return ok();
    }
}
