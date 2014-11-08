package tuttifrutti.jobs;

import static tuttifrutti.models.enums.MatchState.EXPIRED;
import static tuttifrutti.models.enums.MatchState.FINISHED;
import static tuttifrutti.models.enums.MatchState.REJECTED;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import play.Logger;
import tuttifrutti.models.Match;
import tuttifrutti.models.enums.MatchMode;

/**
 * @author rfanego
 */
@Component
public class ExpiredRoundCheckerJob implements Runnable {
	private MatchMode mode;
	
	@Autowired
	private Datastore mongoDatastore;
	
	private long normalModeTimeMillis = TimeUnit.DAYS.toMillis(2);
	private long quickModeTimeMillis = TimeUnit.MINUTES.toMillis(15);
	
//	public ExpiredRoundCheckerJob(MatchMode mode) {
//		this.mode = mode;
//	}
	
	@Override
	public void run() {
		Logger.info("Starting ExpiredRoundCheckerJob");
		Query<Match> query = mongoDatastore.find(Match.class, "config.mode =", this.mode.toString());
		query.and(query.criteria("state").not().equal(FINISHED),query.criteria("state").not().equal(EXPIRED),
				  query.criteria("state").not().equal(REJECTED),
				  query.criteria("startDate").greaterThanOrEq(DateTime.now()));
		
		List<Match> matches = query.asList();
		
		matches.forEach(match -> {
			
		});
		
		Logger.info("Finishing ExpiredRoundCheckerJob");		
	}
}
