package tuttifrutti.spring;

import org.elasticsearch.client.Client;
import org.mongodb.morphia.Datastore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import play.Logger;
import tuttifrutti.elastic.ElasticSearchEmbeddedServer;
import tuttifrutti.mongo.MongoEmbeddedServer;

import com.mongodb.MongoClient;

/**
 * @author rfanego
 */
@Configuration
@Profile({ "DEV", "TEST" })
@ComponentScan({ "tuttifrutti" })
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
	@Bean(name = "mongoDatastore")
	public Datastore mongoDatastore() {
		return super.mongoDatastore();
	}

	@Override
	@Lazy
	@Bean(name = "mongoClient")
	public MongoClient mongoClient() {
		return embeddedMongoServer().getClient();
	}

	@Lazy
	@Bean
	public MongoEmbeddedServer embeddedMongoServer() {
		MongoEmbeddedServer mongoServer = new MongoEmbeddedServer();

		return mongoServer;
	}
}
