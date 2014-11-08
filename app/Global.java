import static play.libs.Akka.system;
import static scala.concurrent.duration.Duration.Zero;
import static tuttifrutti.utils.FiniteDurationUtils.ONE_DAY;
import static tuttifrutti.utils.FiniteDurationUtils.ONE_HOUR;
import static tuttifrutti.utils.FiniteDurationUtils.nextExecutionInSeconds;

import java.util.ArrayList;
import java.util.List;

import lombok.val;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import tuttifrutti.elastic.ElasticSearchEmbeddedServer;
import tuttifrutti.jobs.AlphabetLoaderJob;
import tuttifrutti.jobs.FinishedMatchCleanerJob;
import tuttifrutti.jobs.PowerUpWordLoader;
import tuttifrutti.jobs.SuggestionIndexerJob;
import tuttifrutti.mongo.MongoEmbeddedServer;
import tuttifrutti.utils.SpringApplicationContext;
import akka.actor.Cancellable;

public class Global extends GlobalSettings {
	private PowerUpWordLoader powerUpWordLoaderJob;
	
	private SuggestionIndexerJob suggestionIndexerJob;
	
	private AlphabetLoaderJob alphabetLoaderJob;
	
	private FinishedMatchCleanerJob finishedMatchCleanerJob;
	
	private List<Cancellable> jobs = new ArrayList<Cancellable>();
	
	@Override
	public void onStart(Application app) {
		super.onStart(app);
		Logger.info("Starting up the application...");
		SpringApplicationContext.initialize();
		if (app.isProd()) {
			Logger.info("Corre los jobs");
			powerUpWordLoaderJob = SpringApplicationContext.getBean(PowerUpWordLoader.class);
			suggestionIndexerJob = SpringApplicationContext.getBean(SuggestionIndexerJob.class);
			alphabetLoaderJob = SpringApplicationContext.getBean(AlphabetLoaderJob.class);
			finishedMatchCleanerJob = SpringApplicationContext.getBean(FinishedMatchCleanerJob.class);
			system().scheduler().scheduleOnce(Zero(), alphabetLoaderJob, system().dispatcher());
			system().scheduler().scheduleOnce(Zero(), powerUpWordLoaderJob, system().dispatcher());
			jobs.add(system().scheduler().schedule(nextExecutionInSeconds(00, 00), ONE_DAY, powerUpWordLoaderJob, system().dispatcher()));
			jobs.add(system().scheduler().schedule(Zero(),ONE_HOUR,suggestionIndexerJob, system().dispatcher()));
			jobs.add(system().scheduler().schedule(Zero(),ONE_HOUR,finishedMatchCleanerJob, system().dispatcher()));
		}
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
		
		jobs.forEach(job -> job.cancel());
		
		SpringApplicationContext.close();
	}

	@Override
	public <C> C getControllerInstance(Class<C> clazz) {
		return SpringApplicationContext.getBean(clazz);
	}
}
