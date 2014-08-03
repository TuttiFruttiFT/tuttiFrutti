package tuttifrutti.spring;

import org.elasticsearch.client.Client;
import org.mongodb.morphia.Datastore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import play.Logger;
import tuttifrutti.utils.ElasticSearchEmbeddedServer;
import tuttifrutti.utils.MongoEmbeddedServer;

import com.mongodb.MongoClient;

/**
 * @author rfanego
 */
@Configuration
@Profile({ "DEV", "TEST" })
public class SpringConfigurationForDevAndTest extends SpringConfigurationFor {

	@Override
	@Lazy
	@Bean
	public Client elasticSearchClient() {
		return embeddedElasticSearchServer().getClient();
	}

	@Lazy
	@Bean
	public ElasticSearchEmbeddedServer embeddedElasticSearchServer() {
		ElasticSearchEmbeddedServer esServer = new ElasticSearchEmbeddedServer();
		Client client = null;
		try {
			client = esServer.getClient();
			client.admin().indices().prepareRefresh().execute().actionGet();
		} catch (Exception e) {
			Logger.error("Could not initialize embedded Elastic Search Server for DEV/TEST", e);
			esServer.close();
		} finally {
			client.close();
		}
		return esServer;
	}

	@Override
	@Lazy
	@Bean
	public Datastore mongoDatastore() {
		return super.mongoDatastore();
	}

	@Override
	@Lazy
	@Bean
	public MongoClient mongoClient() {
		return embeddedMongoServer().getClient();
	}

	@Lazy
	@Bean
	private MongoEmbeddedServer embeddedMongoServer() {
		MongoEmbeddedServer mongoServer = new MongoEmbeddedServer();

		return mongoServer;
	}
}
