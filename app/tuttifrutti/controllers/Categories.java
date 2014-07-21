package tuttifrutti.controllers;

import java.util.List;

import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Category;

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
