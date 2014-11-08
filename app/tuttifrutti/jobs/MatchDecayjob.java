package tuttifrutti.jobs;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.joda.time.DateTime.now;
import static tuttifrutti.models.enums.MatchState.TO_BE_APPROVED;

import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import play.Logger;
import tuttifrutti.models.Match;import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.Turn;
import tuttifrutti.services.MatchService;


/**
 * @author rfanego
 */
@Component
public class MatchDecayjob implements Runnable {

	@Autowired
	private Datastore mongoDatastore;
	
	@Autowired
	private MatchService matchService;
	
	private int numberOfPlayers;
	
	@Override
	public void run() {
		Logger.info("Starting MatchDecayjob");
		Query<Match> query = mongoDatastore.find(Match.class, "state =", TO_BE_APPROVED.toString());
		query.and(query.criteria("config.numberOfPlayers").equal(numberOfPlayers),
				  query.criteria("config.incorporatedNumberOfPlayers").lessThan(numberOfPlayers),
				  query.criteria("startDate").lessThanOrEq(now().minusHours(1).toDate()));
		
		List<Match> matches = query.asList();
		
		if(isNotEmpty(matches)){
			matches.forEach(match -> {
				MatchConfig config = match.getConfig();
				config.decrementMatchPlayers();
				List<Turn> turns = match.getLastRound().getTurns();
				if(isNotEmpty(turns) && (config.getNumberOfPlayers() == turns.size())){
					matchService.calculateResult(match);
				}
			});			
		}
		
		Logger.info("Finishing MatchDecayjob");
	}

}
