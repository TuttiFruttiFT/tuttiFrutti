import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import tuttifrutti.spring.SpringConfiguration;


public class Global extends GlobalSettings {
	private ConfigurableApplicationContext ctx;
	
	@Override
	public void onStart(Application app) {
		super.onStart(app);
		Logger.info("Starting up the application...");
		ctx = new AnnotationConfigApplicationContext(SpringConfiguration.class);

		if (ctx == null) {
			throw new IllegalStateException("application context could not be initialized properly");
		}
	}
}
