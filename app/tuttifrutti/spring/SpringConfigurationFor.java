package tuttifrutti.spring;

import org.elasticsearch.client.Client;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import tuttifrutti.models.Category;
import tuttifrutti.models.Dupla;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.MatchResult;
import tuttifrutti.models.Pack;
import tuttifrutti.models.Player;
import tuttifrutti.models.PlayerResult;
import tuttifrutti.models.PowerUp;
import tuttifrutti.models.Round;
import tuttifrutti.models.Suggestion;
import tuttifrutti.models.Turn;

import com.mongodb.MongoClient;
import com.typesafe.config.ConfigFactory;

/**
 * @author rfanego
 */
public abstract class SpringConfigurationFor {
	public abstract Client elasticSearchClient();

	public abstract MongoClient mongoClient();

	public Datastore mongoDatastore() {
		MongoClient mongoClient = mongoClient();
		Morphia morphia = new Morphia();
		morphia.map(Category.class).map(Dupla.class).map(Match.class).map(MatchConfig.class).map(Pack.class).map(Player.class)
				.map(PlayerResult.class).map(PowerUp.class).map(Round.class).map(Suggestion.class).map(Turn.class).map(MatchResult.class);

		Datastore datastore = morphia.createDatastore(mongoClient, ConfigFactory.load().getString("mongo.db.name"));
		return datastore;
	}
}
