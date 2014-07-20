package utils;

import java.net.UnknownHostException;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.typesafe.config.ConfigFactory;


public class MongoUtil {
	public static DBCollection getCollection(String collectionName) {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient( ConfigFactory.load().getString("mongo.host") , ConfigFactory.load().getInt("mongo.port") );
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		DB db = mongoClient.getDB( ConfigFactory.load().getString("mongo.db.name") );
		
		DBCollection userCollection = db.getCollection(collectionName);
		return userCollection;
	}
	
	public static Datastore getDatastore() {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient( ConfigFactory.load().getString("mongo.host") , ConfigFactory.load().getInt("mongo.port") );
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		Morphia morphia = new Morphia();
		morphia.mapPackage("models");
		
		Datastore datastore = morphia.createDatastore(mongoClient, ConfigFactory.load().getString("mongo.db.name"));
		return datastore;
	}
}
