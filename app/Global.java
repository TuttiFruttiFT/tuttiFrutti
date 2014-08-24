import lombok.val;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import tuttifrutti.elastic.ElasticSearchEmbeddedServer;
import tuttifrutti.mongo.MongoEmbeddedServer;
import tuttifrutti.utils.SpringApplicationContext;


public class Global extends GlobalSettings {
	@Override
	public void onStart(Application app) {
		super.onStart(app);
		Logger.info("Starting up the application...");
		SpringApplicationContext.initialize();
	}
	
	@Override
	public void onStop(Application arg0) {
		super.onStop(arg0);
		val esEmbeddedServer = SpringApplicationContext.getBean(ElasticSearchEmbeddedServer.class);
		if (esEmbeddedServer != null) {
			esEmbeddedServer.close();
		}

		val mongoEmbeddedServer = SpringApplicationContext.getBean(MongoEmbeddedServer.class);
		if (mongoEmbeddedServer != null) {
			mongoEmbeddedServer.stopMongoServer();
		}
		SpringApplicationContext.close();
	}

	@Override
	public <C> C getControllerInstance(Class<C> clazz) {
		return SpringApplicationContext.getBean(clazz);
	}
}
