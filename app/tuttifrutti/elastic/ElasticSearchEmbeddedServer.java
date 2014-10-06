package tuttifrutti.elastic;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static tuttifrutti.utils.ConfigurationAccessor.s;

import java.io.IOException;
import java.io.InputStream;

import lombok.SneakyThrows;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.node.Node;

import play.Logger;
import play.libs.Json;
import tuttifrutti.models.CategoryElastic;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author rfanego
 */
public class ElasticSearchEmbeddedServer {
	private static final String ES_INDEX = "categories";
	
	private final Node elasticSearchNode;

	public ElasticSearchEmbeddedServer() {
		super();
		this.elasticSearchNode = createESNode();
	}

	public Client getClient() {
		return elasticSearchNode.client();
	}

	public void close() {
		if (elasticSearchNode != null) {
			elasticSearchNode.close();
		}
	}

	private Node createESNode() {
		Settings settings = ImmutableSettings.settingsBuilder().put("http.enabled", false).put("path.logs", "target/elasticsearch/logs").put(
				"path.data", "target/elasticsearch/data").build();

		return nodeBuilder().local(true).settings(settings).node();
	}

	public void cleanIndex(Client client) {
		Boolean hasIndex = client.admin().indices().exists(new IndicesExistsRequest(ES_INDEX)).actionGet().isExists();
		
		if(hasIndex){
			client.admin().indices().delete(new DeleteIndexRequest(ES_INDEX)).actionGet();
		}
	}

	public void createCategoriesIndexIfNonExistent(Client client) {
		Boolean hasIndex = client.admin().indices().exists(new IndicesExistsRequest(ES_INDEX)).actionGet().isExists();

		if (!hasIndex) {
			client.admin().indices().prepareCreate(ES_INDEX).execute().actionGet();
		}
	}

	public void createType(String category, Client client) {
		client.admin().indices().preparePutMapping(ES_INDEX).setType(category).setSource(getTypeMapping(category)).execute().actionGet();
	}

	private XContentBuilder getTypeMapping(String category) {
		XContentBuilder builder = null;

		try {
			builder = XContentFactory.jsonBuilder().startObject().startObject(category).startObject("properties");

			builder.startObject("value").field("type", "string").field("index", "not_analyzed").endObject();
			
//			builder.startObject("letter").field("type", "string").field("index", "not_analyzed").endObject();
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return builder;
	}

	@SneakyThrows(IOException.class)
	public void indexCategoryFromJsonFile(Client client, String jsonFile,String categoryName) {
		InputStream inJson = CategoryElastic.class.getResourceAsStream("/test/elasticsearch/" + jsonFile);
		String json = null;
		try {
			CategoryElastic category = new ObjectMapper().readValue(inJson, CategoryElastic.class);
			json = Json.toJson(category).toString();
		} catch (Exception e) {
			Logger.error("Error when converting from JSON to String", e);
		} finally {
			inJson.close();
		}
		
		client.prepareIndex(s("elasticsearch.index"), categoryName).setSource(json).execute().actionGet();
	}
}
