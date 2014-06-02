package utils;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
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
}
