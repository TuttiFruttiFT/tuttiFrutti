import static tuttifrutti.spring.RuntimeEnvironment.currentRuntimeEnvironment;
import lombok.val;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import tuttifrutti.spring.CommonSpringConfiguration;
import tuttifrutti.utils.ElasticSearchEmbeddedServer;
import tuttifrutti.utils.MongoEmbeddedServer;


public class Global extends GlobalSettings {
	private ConfigurableApplicationContext ctx;
	
	@Override
	public void onStart(Application app) {
		super.onStart(app);
		Logger.info("Starting up the application...");
		ctx = new AnnotationConfigApplicationContext(CommonSpringConfiguration.class);
		ctx.getEnvironment().setActiveProfiles(currentRuntimeEnvironment().name());
		ctx.refresh();
		if (ctx == null) {
			throw new IllegalStateException("application context could not be initialized properly");
		}
	}
	
	@Override
	public void onStop(Application arg0) {
		super.onStop(arg0);
		val esEmbeddedServer = ctx.getBean(ElasticSearchEmbeddedServer.class);
		if (esEmbeddedServer != null) {
			esEmbeddedServer.close();
		}

		val mongoEmbeddedServer = ctx.getBean(MongoEmbeddedServer.class);
		if (mongoEmbeddedServer != null) {
			mongoEmbeddedServer.stopMongoServer();
		}
		ctx.close();
	}

	@Override
	public <C> C getControllerInstance(Class<C> clazz) {
		return ctx.getBean(clazz);
	}
}
