package tuttifrutti.jobs;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.joda.time.DateTime.now;
import static tuttifrutti.models.enums.MatchState.CLEAN;
import static tuttifrutti.models.enums.MatchState.EXPIRED;
import static tuttifrutti.models.enums.MatchState.FINISHED;
import static tuttifrutti.models.enums.MatchState.REJECTED;

import java.util.Date;
import java.util.List;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;

import play.Logger;
import tuttifrutti.models.Match;
import tuttifrutti.models.Player;
import tuttifrutti.models.PlayerResult;
import tuttifrutti.models.enums.MatchMode;
import tuttifrutti.services.MatchService;
import tuttifrutti.services.PlayerService;
import tuttifrutti.services.PushService;

/**
 * @author rfanego
 */
public class ExpiredRoundCheckerJob implements Runnable {
	private MatchMode mode;
	
	@Autowired
	private Datastore mongoDatastore;
	
	@Autowired
	private PlayerService playerService;
	
	@Autowired
	private PushService pushService;
	
	@Autowired
	private MatchService matchService;
	
	public ExpiredRoundCheckerJob(MatchMode mode) {
		this.mode = mode;
	}
	
	@Override
	public void run() {
		Logger.info("Starting ExpiredRoundCheckerJob " + this.mode.toString());
		
		int modeTime = this.mode.time();
		
		expiredMatches(modeTime);
		
		expiredPlayers(modeTime);
		
		Logger.info("Finishing ExpiredRoundCheckerJob "  + this.mode.toString());
	}

	private void expiredPlayers(int modeTime) {
		Date nowMinusModeTime = now().minusMillis(modeTime).toDate();
		
		Query<Match> query = mongoDatastore.find(Match.class, "config.mode =", this.mode.toString());
		query.and(query.criteria("state").notEqual(FINISHED),query.criteria("state").notEqual(EXPIRED),
				  query.criteria("state").notEqual(REJECTED),query.criteria("state").notEqual(CLEAN),
				  query.criteria("playerResults.modifiedDate").lessThanOrEq(nowMinusModeTime));
		
		List<Match> matches = query.asList();
		
		if(isNotEmpty(matches)){	
			matches.forEach(match -> {
				List<PlayerResult> expiredPlayers = match.expiredPlayers(nowMinusModeTime);
				if(match.getPlayerResults().size() - expiredPlayers.size() > 1){
					expiredPlayers.forEach(aPlayer -> {
						Player player = playerService.updateLoserStatisticsInExpiredMatch(aPlayer);
						pushService.expiredForPlayer(player, match);
						match.addExpiredPlayer(aPlayer);
						mongoDatastore.save(player);
					});
					
					if(match.isRoundOver()){
						matchService.calculateResult(match);
					}
				}else{
					match.setState(EXPIRED);
					expiredPlayers.forEach(aPlayer -> {
						Player player = playerService.updateLoserStatisticsInExpiredMatch(aPlayer);
						match.addExpiredPlayer(aPlayer);
						mongoDatastore.save(player);
					});
					match.getPlayerResults().forEach(aPlayerResult -> {
						pushService.expiredForPlayer(aPlayerResult.getPlayer(), match);
					});
				}
				mongoDatastore.save(match);
			});
		}
	}

	private void expiredMatches(int modeTime) {
		Query<Match> query = mongoDatastore.find(Match.class, "config.mode =", this.mode.toString());
		query.and(query.criteria("state").notEqual(FINISHED),query.criteria("state").notEqual(EXPIRED),
				  query.criteria("state").notEqual(REJECTED),query.criteria("state").notEqual(CLEAN),
				  query.criteria("modifiedDate").lessThanOrEq(now().minusMillis(modeTime).toDate()));
		
		List<Match> matches = query.asList();
		
		if(isNotEmpty(matches)){			
			matches.forEach(match -> {
				expireMatch(match);
			});
		}
	}

	private void expireMatch(Match match) {
		match.setState(EXPIRED);
		pushService.expired(match);
		match.getPlayerResults().forEach(aPlayer -> {
			Player player = playerService.updateLoserStatisticsInExpiredMatch(aPlayer);
			mongoDatastore.save(player);
		});
		mongoDatastore.save(match);
	}
}
