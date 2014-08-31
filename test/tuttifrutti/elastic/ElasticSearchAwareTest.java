/**
 * 
 */
package tuttifrutti.elastic;

import static org.apache.commons.lang3.ArrayUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.client.Client;

import play.Logger;
import tuttifrutti.utils.SpringApplicationContext;

/**
 * @author rfanego
 *
 */
public class ElasticSearchAwareTest {

	@SuppressWarnings("unchecked")
	protected Pair<String, String>[] getJsonFilesFotCategories() {
		List<Pair<String,String>> jsonList = new ArrayList<>();
		jsonList.add(Pair.of("bandCharly.json", "bands"));
		jsonList.add(Pair.of("bandRadiohead.json", "bands"));
		jsonList.add(Pair.of("bandRolling1.json", "bands"));
		jsonList.add(Pair.of("bandRolling2.json", "bands"));
		jsonList.add(Pair.of("colorNegro.json", "colors"));
		jsonList.add(Pair.of("colorMarron.json", "colors"));
		jsonList.add(Pair.of("colorGrisArena.json", "colors"));
		jsonList.add(Pair.of("mealCaramelo.json", "meals"));
		jsonList.add(Pair.of("mealMango.json", "meals"));
		jsonList.add(Pair.of("mealRisotto.json", "meals"));
		jsonList.add(Pair.of("countryColombia.json", "countries"));
		jsonList.add(Pair.of("countryRumania.json", "countries"));
		jsonList.add(Pair.of("countryMarruecos.json", "countries"));
		return jsonList.toArray((Pair<String, String>[])new Pair[jsonList.size()]);
	}

	@SuppressWarnings("unchecked")
	protected void populateElastic(Pair<String, String>... jsonFileAndCategoryPairs) {
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
