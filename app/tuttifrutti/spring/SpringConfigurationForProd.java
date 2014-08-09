package tuttifrutti.spring;

import static org.apache.commons.lang3.StringUtils.contains;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static tuttifrutti.utils.ConfigurationAccessor.i;
import static tuttifrutti.utils.ConfigurationAccessor.s;

import java.net.UnknownHostException;

import lombok.val;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.mongodb.morphia.Datastore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;

import play.Logger;

import com.mongodb.MongoClient;
import com.typesafe.config.ConfigFactory;

/**
 * @author rfanego
 */
@Configuration
@Profile("PROD")
@ComponentScan({ "tuttifrutti" })
public class SpringConfigurationForProd extends SpringConfigurationFor {

	@Override
	@Lazy
	@Bean
	public Client elasticSearchClient() {
		TransportClient transportClient = null;
		try {
			val elasticSearchHost = s("elasticsearch.host");

			if (!contains(elasticSearchHost, "localhost")) {
				Settings settings = settingsBuilder().put("cluster.name", s("elasticsearch.cluster.name")).put("client.transport.sniff", true)
						.build();

				String[] esHosts = elasticSearchHost.split(",");

				transportClient = new TransportClient(settings);

				for (String host : esHosts) {
					transportClient.addTransportAddress(new InetSocketTransportAddress(host, i("elasticsearch.port")));
				}
				Logger.info("ES - Client Started. Hosts: " + esHosts);
			}
		} catch (Exception e) {
			Logger.error("Error getting ES client.", e);
		}
		return transportClient;
	}

	@Override
	@Lazy
	@Bean(name = "mongoClient")
	public MongoClient mongoClient() {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(ConfigFactory.load().getString("mongo.host"), ConfigFactory.load().getInt("mongo.port"));
		}catch (UnknownHostException e) {
			Logger.error("Getting Mongo Client", e);
		}
		return mongoClient;
	}

	@Override
	@Lazy
	@Bean(name = "mongoDatastore")
	public Datastore mongoDatastore() {
		return super.mongoDatastore();
	}
}
