package utils;

import java.net.UnknownHostException;

import models.Category;
import models.Dupla;
import models.Match;
import models.MatchConfig;
import models.MatchResult;
import models.Pack;
import models.Player;
import models.PlayerResult;
import models.PowerUp;
import models.ResultModel;
import models.Round;
import models.Suggestion;
import models.Turn;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import play.Logger;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.typesafe.config.ConfigFactory;


public class MongoUtil {
	public static DBCollection getCollection(String collectionName) {
		MongoClient mongoClient = getMongoClient();
		
		DB db = mongoClient.getDB( ConfigFactory.load().getString("mongo.db.name") );
		
		DBCollection userCollection = db.getCollection(collectionName);
		return userCollection;
	}
	
	public static Datastore getDatastore() {
		MongoClient mongoClient = getMongoClient();
		Morphia morphia = new Morphia();
		morphia.map(Category.class).map(Dupla.class).map(Match.class).map(MatchConfig.class)
			   .map(Pack.class).map(Player.class).map(PlayerResult.class).map(PowerUp.class)
			   .map(Round.class).map(Suggestion.class).map(Turn.class).map(MatchResult.class);
		
		Datastore datastore = morphia.createDatastore(mongoClient, ConfigFactory.load().getString("mongo.db.name"));
		return datastore;
	}

	private static MongoClient getMongoClient() {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient( ConfigFactory.load().getString("mongo.host") , ConfigFactory.load().getInt("mongo.port") );
		} catch (UnknownHostException e) {
			Logger.error("Getting Mongo Client", e);
		}
		return mongoClient;
	}
}
