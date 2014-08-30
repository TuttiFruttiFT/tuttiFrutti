package tuttifrutti.elastic;

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;
import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.DuplaState.CORRECTED;
import static tuttifrutti.models.DuplaState.WRONG;
import static tuttifrutti.models.Letter.R;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.client.Client;
import org.junit.Test;

import play.Logger;
import tuttifrutti.models.Category;
import tuttifrutti.models.Dupla;
import tuttifrutti.models.DuplaState;
import tuttifrutti.utils.SpringApplicationContext;

public class ElasticUtilTest {

	@Test
	public void test() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			ElasticUtil elasticUtil = SpringApplicationContext.getBeanNamed("elasticUtil", ElasticUtil.class);
			populateElastic(getJsonFilesFotCategories());
			
			Category categoryBands = new Category();
			categoryBands.setId("bands");
			
			Category categoryColors = new Category();
			categoryColors.setId("colors");
			
			Category categoryMeals = new Category();
			categoryMeals.setId("meals");
			
			Category categoryCountries = new Category();
			categoryCountries.setId("countries");
			
			List<Dupla> duplas = new ArrayList<>();
			Dupla duplaBanda = new Dupla();
			duplaBanda.setCategory(categoryBands);
			duplaBanda.setWrittenWord("Rolling Stone");
			duplaBanda.setTime(11);
			duplas.add(duplaBanda);
			
			Dupla duplaColor = new Dupla();
			duplaColor.setCategory(categoryColors);
			duplaColor.setWrittenWord("Gris");
			duplaColor.setTime(19);
			duplas.add(duplaColor);
			
			Dupla duplaVacia = new Dupla();
			duplaVacia.setCategory(categoryMeals);
			duplaVacia.setWrittenWord("");
			duplaVacia.setTime(19);
			duplas.add(duplaVacia);
			
			Dupla duplaNull = new Dupla();
			duplaNull.setCategory(categoryCountries);
			duplaNull.setWrittenWord(null);
			duplaNull.setTime(19);
			duplas.add(duplaNull);
			
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

	@SuppressWarnings("unchecked")
	private Pair<String, String>[] getJsonFilesFotCategories() {
		List<Pair<String,String>> jsonList = new ArrayList<>();
		jsonList.add(Pair.of("bandCharly.json", "bands"));
		jsonList.add(Pair.of("bandRadiohead.json", "bands"));
		jsonList.add(Pair.of("bandRolling1.json", "bands"));
		jsonList.add(Pair.of("bandRolling2.json", "bands"));
		jsonList.add(Pair.of("colorNegro.json", "colors"));
		jsonList.add(Pair.of("colorMarron.json", "colors"));
		jsonList.add(Pair.of("colorGrisArena.json", "colors"));
		return jsonList.toArray((Pair<String, String>[])new Pair[jsonList.size()]);
	}

	private void populateElastic(Pair<String, String>... jsonFileAndCategoryPairs) {
		Client client = null;
		ElasticSearchEmbeddedServer esServer = SpringApplicationContext.getBean(ElasticSearchEmbeddedServer.class);
		try {
			client = SpringApplicationContext.getBean(Client.class);
			esServer.cleanIndex(client);
			esServer.createCategoriesIndexIfNonExistent(client);
			if (isNotEmpty(jsonFileAndCategoryPairs) && jsonFileAndCategoryPairs[0] != null) {
				populateElasticSearchServerWithTestData(esServer, jsonFileAndCategoryPairs);
			}
			client.admin().indices().prepareRefresh().execute().actionGet();
		} catch (Exception e) {
			Logger.error("Problem when interacting with the embedded Elastic Search Server for DEV/TEST", e);
			esServer.close();
		} finally {
			client.close();
		}
	}

	private void populateElasticSearchServerWithTestData(ElasticSearchEmbeddedServer esServer,Pair<String, String>[] jsonFileAndCategoryPairs) {
		Client client = esServer.getClient();

		for (Pair<String, String> jsonFileAndCategoryPair : jsonFileAndCategoryPairs) {
			String jsonFile = jsonFileAndCategoryPair.getLeft();
			String category = jsonFileAndCategoryPair.getRight();
			esServer.createType(category, client);
			esServer.indexCategoryFromJsonFile(client, jsonFile, category);
		}
	}

}
