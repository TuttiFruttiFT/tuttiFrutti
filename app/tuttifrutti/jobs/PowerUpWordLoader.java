package tuttifrutti.jobs;

import static tuttifrutti.utils.ConfigurationAccessor.i;

import org.springframework.beans.factory.annotation.Autowired;

import tuttifrutti.elastic.ElasticUtil;
import tuttifrutti.models.Category;
import tuttifrutti.models.Letter;

/**
 * @author rfanego
 */
public class PowerUpWordLoader implements Runnable{
	private static final int NUMBER_WORDS_PER_CATEGORY = i("powerUp.loader.count");
	
	@Autowired
	private ElasticUtil elasticUtil;
	
	@Autowired
	private Category categoryService;
	
	@Override
	public void run() {
//		for(Category category : categoryService.categories("ES")){			
//			for(Letter letter : Letter.values()){
//				elasticUtil.searchWords(letter, category.getId(), NUMBER_WORDS_PER_CATEGORY);
//			}
//		}
	}
}
