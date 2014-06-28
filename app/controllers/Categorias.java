package controllers;

import java.util.List;

import models.Category;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author rfanego
 */
public class Categorias extends Controller {
	public static Result categoriasDisponibles() {
		List<Category> categorias = Category.categorias();
		
		//TODO ver como crear un json desde una lista
		
        return ok();
    }
}
