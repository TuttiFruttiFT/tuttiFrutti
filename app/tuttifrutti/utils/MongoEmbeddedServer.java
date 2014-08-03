package tuttifrutti.utils;

import java.net.UnknownHostException;

import play.Logger;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

/**
 * @author rfanego
 */
public class MongoEmbeddedServer {
	private MongodExecutable mongodExecutable;

	public MongoEmbeddedServer() {
		startMongoServer();
	}

	public MongoClient getClient() {
		MongoClient mongoClient = null;
		try {
			mongoClient = new MongoClient(ConfigurationAccessor.s("mongo.host"), ConfigurationAccessor.i("mongo.port"));
		}catch (UnknownHostException e) {
			Logger.error("Problem creating embedded Mongo Server", e);
		}
		return mongoClient;
	}

	private void startMongoServer() {
		try{			
			MongodStarter starter = MongodStarter.getDefaultInstance();
			
			IMongodConfig mongodConfig = new MongodConfigBuilder()
											.version(Version.Main.V2_6)
											.net(new Net(ConfigurationAccessor.i("mongo.port"), Network.localhostIsIPv6()))
											.build();
			
			mongodExecutable = starter.prepare(mongodConfig);
			mongodExecutable.start();
		}catch(Exception e){
			Logger.error("Problem creating embedded Mongo Server", e);
		}
	}

	public void stopMongoServer() {
		mongodExecutable.stop();
	}
}
