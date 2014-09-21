package tuttifrutti.controllers;

import static play.libs.Json.parse;
import static tuttifrutti.utils.JsonUtil.parseListToJson;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import play.mvc.Controller;
import play.mvc.Result;
import tuttifrutti.models.Category;

/**
 * @author rfanego
 */
@org.springframework.stereotype.Controller
public class Categories extends Controller {
	@Autowired
	private Category categoryService;
	
	public Result availableCategories(String language) {
		List<Category> categories = categoryService.categories(language);

        return ok(parse(parseListToJson(categories)));
    }
}
