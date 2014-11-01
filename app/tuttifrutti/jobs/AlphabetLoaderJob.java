package tuttifrutti.jobs;

import static tuttifrutti.models.enums.LanguageType.ES;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import play.Logger;
import tuttifrutti.cache.AlphabetCache;
import tuttifrutti.elastic.ElasticUtil;
import tuttifrutti.models.Category;
import tuttifrutti.models.Letter;

/**
 * @author rfanego
 */
@Component
public class AlphabetLoaderJob implements Runnable {
	@Autowired
	private Category categoryService;
	
	@Autowired
	private ElasticUtil elasticUtil;
	
	@Autowired
	private AlphabetCache alphabetCache;
	
	@Override
	public void run() {
		Logger.info("Starting AlphabetLoaderJob");
		for(Category category : categoryService.categories(ES.toString())){
			String categoryId = category.getId();
			
			alphabetCache.cleanCache(categoryId);
			Map<String,Boolean> availableLetters = elasticUtil.availableLettersForCategory(categoryId);
			for(Letter letter : Letter.values()){
				if(availableLetters.get(letter.getLetter()) == null){
					alphabetCache.addUnavailableLetter(categoryId,letter.getLetter());
				}
			}
		}
		Logger.info("Finishing AlphabetLoaderJob");
	}

}
