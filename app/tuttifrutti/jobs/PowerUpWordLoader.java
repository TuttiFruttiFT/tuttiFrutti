package tuttifrutti.jobs;

import static tuttifrutti.utils.ConfigurationAccessor.i;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import play.Logger;
import tuttifrutti.cache.CategoryCache;
import tuttifrutti.elastic.ElasticUtil;
import tuttifrutti.models.Category;
import tuttifrutti.models.Letter;

/**
 * @author rfanego
 */
@Component
public class PowerUpWordLoader implements Runnable{
	private static final int NUMBER_WORDS_PER_CATEGORY = i("powerUp.loader.count");
	
	@Autowired
	private CategoryCache categoryCache;
	
	@Autowired
	private ElasticUtil elasticUtil;
	
	@Autowired
	private Category categoryService;
	
	@Override
	public void run() {
		Logger.info("Starting PowerUpWordLoader");
		for(Category category : categoryService.categories("ES")){
			for(Letter letter : Letter.values()){
				List<String> words = elasticUtil.searchWords(letter, category.getId(), NUMBER_WORDS_PER_CATEGORY);
				for(String word : words){
					categoryCache.saveWord(category.getId(),letter,word);
				}
			}
		}
		Logger.info("Finishing PowerUpWordLoader");
	}
}
