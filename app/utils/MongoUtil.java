package utils;

import java.net.UnknownHostException;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;


public class MongoUtil {
	public static DBCollection getCollection(String collectionName) {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient( "localhost" , 27017 );
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		DB db = mongoClient.getDB( "blog" );
		
		DBCollection userCollection = db.getCollection(collectionName);
		return userCollection;
	}
	
	public static Datastore getDatastore() {
		Mongo mongo = null;
		try {
			mongo = new Mongo( "localhost", 27017 );
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		Morphia morphia = new Morphia();
		morphia.mapPackage("models");
		
		Datastore datastore = morphia.createDatastore(mongo, "tutti");
		return datastore;
	}
}
