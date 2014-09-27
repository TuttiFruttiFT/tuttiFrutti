package tuttifrutti.mongo;

import java.net.UnknownHostException;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.stereotype.Component;

import play.Logger;
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

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.typesafe.config.ConfigFactory;

@Component
public class MongoUtil {
	public DBCollection getCollection(String collectionName) {
		MongoClient mongoClient = getMongoClient();
		
		DB db = mongoClient.getDB( ConfigFactory.load().getString("mongo.db.name") );
		
		DBCollection userCollection = db.getCollection(collectionName);
		return userCollection;
	}
	
	public Datastore getDatastore() {
		MongoClient mongoClient = getMongoClient();
		Morphia morphia = new Morphia();
		morphia.map(Category.class).map(Dupla.class).map(Match.class).map(MatchConfig.class)
			   .map(Pack.class).map(Player.class).map(PlayerResult.class).map(PowerUp.class)
			   .map(Round.class).map(Suggestion.class).map(Turn.class).map(MatchResult.class);
		
		Datastore datastore = morphia.createDatastore(mongoClient, ConfigFactory.load().getString("mongo.db.name"));
		return datastore;
	}

	private MongoClient getMongoClient() {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient( ConfigFactory.load().getString("mongo.host") , ConfigFactory.load().getInt("mongo.port") );
		} catch (UnknownHostException e) {
			Logger.error("Getting Mongo Client", e);
		}
		return mongoClient;
	}
}
