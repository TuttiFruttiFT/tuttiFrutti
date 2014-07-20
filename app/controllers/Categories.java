package controllers;

import java.util.List;

import models.Category;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author rfanego
 */
public class Categories extends Controller {
	public static Result availableCategories() {
		List<Category> categories = Category.categories();
		
		//TODO ver como crear un json desde una lista
		
        return ok();
    }
}
