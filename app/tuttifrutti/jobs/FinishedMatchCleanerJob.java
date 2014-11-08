package tuttifrutti.jobs;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.joda.time.DateTime.now;
import static tuttifrutti.models.enums.MatchState.CLEAN;
import static tuttifrutti.models.enums.MatchState.FINISHED;

import java.util.List;

import org.joda.time.DateTime;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import play.Logger;
import tuttifrutti.models.Match;

/**
 * @author rfanego
 */
@Component
public class FinishedMatchCleanerJob implements Runnable {

	@Autowired
	private Datastore mongoDatastore;
	
	@Override
	public void run() {
		Logger.info("Starting FinishedMatchCleanerJob");
		Query<Match> query = mongoDatastore.find(Match.class, "state =", FINISHED.toString());
		query.and(query.criteria("modifiedDate").lessThanOrEq(now().minusDays(1).toDate()));
		
		List<Match> matches = query.asList();
		
		if(isNotEmpty(matches)){			
			matches.forEach(match -> {
				match.setState(CLEAN);
			});
			
			mongoDatastore.save(matches);
		}
		
		Logger.info("Finishing FinishedMatchCleanerJob");
	}

}
