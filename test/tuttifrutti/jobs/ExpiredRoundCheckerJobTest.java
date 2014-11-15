package tuttifrutti.jobs;

import static org.fest.assertions.Assertions.assertThat;
import static org.joda.time.DateTime.now;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;
import static tuttifrutti.models.enums.MatchMode.N;
import static tuttifrutti.models.enums.MatchMode.Q;
import static tuttifrutti.models.enums.MatchState.EXPIRED;
import static tuttifrutti.models.enums.MatchState.TO_BE_APPROVED;
import static tuttifrutti.utils.TestUtils.createMatch;

import org.junit.Test;
import org.mongodb.morphia.Datastore;

import tuttifrutti.models.Match;
import tuttifrutti.models.Player;
import tuttifrutti.models.PlayerResult;
import tuttifrutti.models.enums.MatchState;
import tuttifrutti.utils.SpringApplicationContext;

/**
 * @author rfanego
 */
public class ExpiredRoundCheckerJobTest {

	@Test
	public void test() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore datastore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			ExpiredRoundCheckerJob job = SpringApplicationContext.getBeanNamed("expiredQuickRoundCheckerJob", ExpiredRoundCheckerJob.class);
			Match match1 = createMatch(datastore, Q, 2);
			Match match2 = createMatch(datastore, Q, 2);
			Match match3 = createMatch(datastore, Q, 2);
			Match match4 = createMatch(datastore, Q, 2);
			
			match1.setModifiedDate(now().minusMinutes(16).toDate());
			match2.setModifiedDate(now().minusMinutes(5).toDate());
			match4.setModifiedDate(now().minusDays(1).toDate());
			
			datastore.save(match1);
			datastore.save(match2);
			datastore.save(match3);
			datastore.save(match4);
			
			job.run();
			
			Match recoveredMatch1 = datastore.get(Match.class, match1.getId());
			Match recoveredMatch2 = datastore.get(Match.class, match2.getId());
			Match recoveredMatch3 = datastore.get(Match.class, match3.getId());
			Match recoveredMatch4 = datastore.get(Match.class, match4.getId());
			
			assertThat(recoveredMatch1.getState()).isEqualTo(EXPIRED);
			recoveredMatch1.players().forEach(aPlayer -> {
				Player player = datastore.get(Player.class,aPlayer.getId());
				assertThat(player.getLost()).isEqualTo(1);
			});
			assertThat(recoveredMatch2.getState()).isEqualTo(TO_BE_APPROVED);
			assertThat(recoveredMatch3.getState()).isEqualTo(TO_BE_APPROVED);
			assertThat(recoveredMatch4.getState()).isEqualTo(EXPIRED);
			recoveredMatch4.players().forEach(aPlayer -> {
				Player player = datastore.get(Player.class,aPlayer.getId());
				assertThat(player.getLost()).isEqualTo(1);
			});
		});
	}

	@Test
	public void test2() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore datastore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			ExpiredRoundCheckerJob job = SpringApplicationContext.getBeanNamed("expiredNormalRoundCheckerJob", ExpiredRoundCheckerJob.class);
			Match match1 = createMatch(datastore, N, 2);
			Match match2 = createMatch(datastore, N, 2);
			Match match3 = createMatch(datastore, N, 2);
			Match match4 = createMatch(datastore, N, 2);
			
			match1.setModifiedDate(now().minusDays(2).minusMinutes(1).toDate());
			match2.setModifiedDate(now().minusMinutes(50).toDate());
			match4.setModifiedDate(now().minusDays(3).toDate());
			
			datastore.save(match1);
			datastore.save(match2);
			datastore.save(match3);
			datastore.save(match4);
			
			job.run();
			
			Match recoveredMatch1 = datastore.get(Match.class, match1.getId());
			Match recoveredMatch2 = datastore.get(Match.class, match2.getId());
			Match recoveredMatch3 = datastore.get(Match.class, match3.getId());
			Match recoveredMatch4 = datastore.get(Match.class, match4.getId());
			
			assertThat(recoveredMatch1.getState()).isEqualTo(EXPIRED);
			recoveredMatch1.players().forEach(aPlayer -> {
				Player player = datastore.get(Player.class,aPlayer.getId());
				assertThat(player.getLost()).isEqualTo(1);
			});
			assertThat(recoveredMatch2.getState()).isEqualTo(TO_BE_APPROVED);
			assertThat(recoveredMatch3.getState()).isEqualTo(TO_BE_APPROVED);
			assertThat(recoveredMatch4.getState()).isEqualTo(EXPIRED);
			recoveredMatch4.players().forEach(aPlayer -> {
				Player player = datastore.get(Player.class,aPlayer.getId());
				assertThat(player.getLost()).isEqualTo(1);
			});
		});
	}
	
	@Test
	public void test3() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore datastore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			ExpiredRoundCheckerJob job = SpringApplicationContext.getBeanNamed("expiredQuickRoundCheckerJob", ExpiredRoundCheckerJob.class);
			Match match1 = createMatch(datastore, Q, 2);
			
			PlayerResult expiredPlayerResult = match1.getPlayerResults().get(0);
			PlayerResult otherPlayerResult = match1.getPlayerResults().get(1);
			expiredPlayerResult.setModifiedDate(now().minusMinutes(16).toDate());
			
			datastore.save(match1);
			
			job.run();
			
			Match recoveredMatch1 = datastore.get(Match.class, match1.getId());
			Player playerExpired = datastore.get(Player.class,expiredPlayerResult.getPlayer().getId());
			Player playerNotExpired = datastore.get(Player.class,otherPlayerResult.getPlayer().getId());
			
			assertThat(recoveredMatch1.getState()).isEqualTo(EXPIRED);
			assertThat(playerExpired.getLost()).isEqualTo(1);
			assertThat(playerNotExpired.getLost()).isEqualTo(0);
			assertThat(recoveredMatch1.getPlayerResults().size()).isEqualTo(1);
			assertThat(recoveredMatch1.getExpiredPlayers().size()).isEqualTo(1);
		});
	}
	
	@Test
	public void test4() {
		running(testServer(9000, fakeApplication()), (Runnable) () -> {
			Datastore datastore = SpringApplicationContext.getBeanNamed("mongoDatastore", Datastore.class);
			ExpiredRoundCheckerJob job = SpringApplicationContext.getBeanNamed("expiredQuickRoundCheckerJob", ExpiredRoundCheckerJob.class);
			Match match1 = createMatch(datastore, Q, 3);
			
			PlayerResult expiredPlayerResult = match1.getPlayerResults().get(0);
			PlayerResult otherPlayerResult1 = match1.getPlayerResults().get(1);
			PlayerResult otherPlayerResult2 = match1.getPlayerResults().get(2);
			expiredPlayerResult.setModifiedDate(now().minusMinutes(16).toDate());
			
			datastore.save(match1);
			
			job.run();
			
			Match recoveredMatch1 = datastore.get(Match.class, match1.getId());
			Player playerExpired = datastore.get(Player.class,expiredPlayerResult.getPlayer().getId());
			Player playerNotExpired1 = datastore.get(Player.class,otherPlayerResult1.getPlayer().getId());
			Player playerNotExpired2 = datastore.get(Player.class,otherPlayerResult2.getPlayer().getId());
			
			assertThat(recoveredMatch1.getState()).isEqualTo(TO_BE_APPROVED);
			assertThat(playerExpired.getLost()).isEqualTo(1);
			assertThat(playerNotExpired1.getLost()).isEqualTo(0);
			assertThat(playerNotExpired2.getLost()).isEqualTo(0);
			assertThat(recoveredMatch1.getPlayerResults().size()).isEqualTo(2);
			assertThat(recoveredMatch1.getExpiredPlayers().size()).isEqualTo(1);
		});
	}
}
