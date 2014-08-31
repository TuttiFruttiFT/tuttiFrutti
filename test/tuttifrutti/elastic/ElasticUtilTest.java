package tuttifrutti.elastic;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.DuplaState.CORRECTED;
import static tuttifrutti.models.DuplaState.WRONG;
import static tuttifrutti.models.Letter.R;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import tuttifrutti.models.Category;
import tuttifrutti.models.Dupla;
import tuttifrutti.utils.SpringApplicationContext;

public class ElasticUtilTest extends ElasticSearchAwareTest {

	@Test
	public void test() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			ElasticUtil elasticUtil = SpringApplicationContext.getBeanNamed("elasticUtil", ElasticUtil.class);
			populateElastic(getJsonFilesFotCategories());
			
			List<Dupla> duplas = new ArrayList<>();
			saveDupla(new Category("bands"), duplas, "Rolling Stone", 11);
			saveDupla(new Category("colors"), duplas, "Gris Arena", 19);
			saveDupla(new Category("meals"), duplas, "", 19);
			saveDupla(new Category("countries"), duplas, null, 19);
			
			elasticUtil.validar(duplas, R);
			
			for(Dupla dupla : duplas){
				if(dupla.getCategory().getId().equals("bands")){
					assertThat(dupla.getFinalWord()).isNotNull();
					assertThat(dupla.getFinalWord()).isEqualTo("rolling stones");
					assertThat(dupla.getState()).isEqualTo(CORRECTED);
				}
				
				if(dupla.getCategory().getId().equals("colors") || dupla.getCategory().getId().equals("meals")
				   || dupla.getCategory().getId().equals("countries")){
					assertThat(dupla.getFinalWord()).isNull();
					assertThat(dupla.getState()).isEqualTo(WRONG);
				}
			}
			
			assertThat(duplas.stream().filter(dupla -> dupla.getState().equals(WRONG)).count()).isEqualTo(3);
			assertThat(duplas.stream().filter(dupla -> dupla.getState().equals(CORRECTED)).count()).isEqualTo(1);			
		});
	}

	private void saveDupla(Category categoryBands, List<Dupla> duplas, String writtenWord, Integer time) {
		Dupla duplaBanda = new Dupla();
		duplaBanda.setCategory(categoryBands);
		duplaBanda.setWrittenWord(writtenWord);
		duplaBanda.setTime(time);
		duplas.add(duplaBanda);
	}
}
