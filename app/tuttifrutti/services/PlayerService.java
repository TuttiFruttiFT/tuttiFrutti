package tuttifrutti.services;

import static java.lang.Math.max;
import static java.util.Collections.shuffle;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.compile;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.joda.time.DateTime.now;
import static play.libs.F.Promise.promise;
import static tuttifrutti.models.enums.SocialNetworkType.facebook;
import static tuttifrutti.models.enums.SocialNetworkType.twitter;
import static tuttifrutti.models.enums.SocialNetworkType.valueOf;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tuttifrutti.cache.RusCache;
import tuttifrutti.models.Match;
import tuttifrutti.models.Pack;
import tuttifrutti.models.Player;
import tuttifrutti.models.PlayerConfig;
import tuttifrutti.models.PlayerResult;
import tuttifrutti.models.enums.PowerUpType;

/**
 * @author rfanego
 */
@Component
public class PlayerService {
	private static final String NO_PLAYER_FOUND = "NO_PLAYER_FOUND";
	private static final String INVALID_NICKNAME = "INVALID_NICKNAME";
	private static final String WRONG_PASSWORD = "WRONG_PASSWORD";
	public static final String INVALID_NEW_PASSWORD = "INVALID_NEW_PASSWORD";
	public static final String SHORT_NICKNAME = "SHORT_NICKNAME";
	public static final String MALFORMED_REQUEST = "MALFORMED_REQUEST";
	
	private static final Integer AMOUNT_OF_RUS_FOR_WINNING = 5;
	private static final Integer AMOUNT_OF_RUS_FOR_LOSING = 1;
//	private static final Integer AMOUNT_OF_RUS_FOR_WINNING_ROUND = 1;
	private static final Integer STARTING_RUS = 25;
	private static final int AMOUNT_OF_OTHER_PLAYERS = 20;
	
	@Autowired
	private Datastore mongoDatastore;
	
	@Autowired
	private MatchService matchService;
	
	@Autowired
	RusCache rusCache;

	public Player player(String playerId){
		return mongoDatastore.get(Player.class,new ObjectId(playerId));
	}
	
	public Player player(ObjectId playerId){
		return mongoDatastore.get(Player.class,playerId);
	}
	
	public Boolean validatePassword(String password,String writtenPassword){
		return StringUtils.equals(password, writtenPassword);
	}
	
	public Player validateFacebook(String facebookId){
		//TODO implementar
		return null;
	}
	
	public Player validateTwitter(String twitterId){
		//TODO implementar
		return null;
	}

	public Player registerMail(String mail, String clave) {
		Player player = new Player();
		player.setNickname(nicknameFromMail(mail));
		player.setMail(mail.trim().toLowerCase());
		player.setPassword(clave);
		player.setBalance(STARTING_RUS);
		mongoDatastore.save(player);
		
		return player;
	}

	public String nicknameFromMail(String mail) {
		String[] splittedMail = mail.split("@");
		return (splittedMail.length > 0) ? splittedMail[0].trim() : mail.trim();
	}

	public Player registerFacebook(String facebookId, String nickname) {
		Player player = new Player();
		player.setNickname(nickname);
		player.setFacebookId(facebookId);
		player.setBalance(STARTING_RUS);
		mongoDatastore.save(player);
		
		return player;
	}

	public Player registerTwitter(String twitterId, String nickname) {
		Player player = new Player();
		player.setNickname(nickname);
		player.setTwitterId(twitterId);
		player.setBalance(STARTING_RUS);
		mongoDatastore.save(player);
		
		return player;
	}

	public String editProfile(String playerId, String mail, String nickname, String password, String newPassword) {
		Player player = mongoDatastore.get(Player.class,new ObjectId(playerId));
		if(player != null){
			if(isNotEmpty(password) && isNotEmpty(newPassword)){				
				if(validatePassword(player.getPassword(),password)){
					if(isValidPassword(newPassword)){						
						player.setPassword(newPassword);
					}else{
						return INVALID_NEW_PASSWORD;
					}
				}else{
					return WRONG_PASSWORD;
				}
			}
			
			player.setMail(mail);
			if(isValidNickname(nickname)){				
				player.setNickname(nickname);
			}else{
				return INVALID_NICKNAME;
			}
			promise(() -> {
				matchService.changePlayerOnMatches(player);
				return null;
			});
			mongoDatastore.save(player);
			return null;
		}
		return NO_PLAYER_FOUND;
	}
	
	public void editSettings(String playerId, PlayerConfig config) {
		Player player = mongoDatastore.get(Player.class,new ObjectId(playerId));
		player.setConfig(config);
		mongoDatastore.save(player);
	}

	public boolean isValidPassword(String newPassword) {
		return newPassword.length() >= 6;
	}

	public void addFriend(String playerId, String friendId) {
		Player player = mongoDatastore.get(Player.class,new ObjectId(playerId));
		Player friend = mongoDatastore.get(Player.class,new ObjectId(friendId));
		player.addFriend(friend.reducedPlayer());
		mongoDatastore.save(player);
	}
	
	public void removeFriend(String playerId, String friendId) {
		Player player = mongoDatastore.get(Player.class,new ObjectId(playerId));
		
		player.getFriends().removeIf(friend -> friend.getId().toString().equals(friendId));
		mongoDatastore.save(player);
	}
	
	public boolean powerUp(String playerId, PowerUpType powerUp) {
		Player player = mongoDatastore.get(Player.class,new ObjectId(playerId));
		int rus = rusCache.rusFor(powerUp);
		if(rus < player.getBalance()){
			player.decrementBalance(rus);
			mongoDatastore.save(player);
			return true;
		}
		return false;
	}

	public void buy(String playerId, Pack pack) {
		Player player = mongoDatastore.get(Player.class,new ObjectId(playerId));
		player.incrementBalance(pack.getCurrentAmount());
		mongoDatastore.save(player);
	}

	public List<Player> searchPlayers(String word, String playerId) {
		Query<Player> query = mongoDatastore.find(Player.class);
		query.or(query.criteria("nickname").equal(compile(word, CASE_INSENSITIVE)),
				query.criteria("mail").equal(compile(word, CASE_INSENSITIVE)));
		query.and(query.criteria("id").notEqual(new ObjectId(playerId)));
		return query.asList();
	}

	public List<Player> searchOthersPlayers(String playerId) {
		Query<Player> query = mongoDatastore.find(Player.class);
		query.and(query.criteria("id").notEqual(new ObjectId(playerId)));
		List<Player> players = query.asList();
		shuffle(players);
		return players.subList(0, AMOUNT_OF_OTHER_PLAYERS < players.size() ? AMOUNT_OF_OTHER_PLAYERS : players.size());
	}

	public Player search(String mail) {
		Query<Player> query = mongoDatastore.find(Player.class, "mail =", mail.trim().toLowerCase());
		return query.get();
	}

	public Player searchByFacebook(String facebookId) {
		Query<Player> query = mongoDatastore.find(Player.class, "facebookId =", facebookId.trim().toLowerCase());
		return query.get();
	}
	
	public Player searchByTwitter(String twitterId) {
		Query<Player> query = mongoDatastore.find(Player.class, "twitterId =", twitterId.trim().toLowerCase());
		return query.get();
	}
	
	public List<PlayerResult> playerResultsFromIds(List<String> playerIds) {
		List<PlayerResult> playerResults = new ArrayList<>();
		this.playersFromIds(playerIds).stream().forEach(player -> playerResults.add(new PlayerResult(player,0,0,false,true,now().toDate(),0)));
		return playerResults;
	}
	
	public List<Player> playersFromIds(List<String> playerIds) {
		List<ObjectId> playersObjectIds = new ArrayList<>();
		playerIds.forEach(playerId -> playersObjectIds.add(new ObjectId(playerId)));
		return mongoDatastore.find(Player.class).field("_id").in(playersObjectIds).asList();
	}

	public void updateStatistics(Match match) {
		Player winner = match.getWinner();
		List<PlayerResult> losers = match.getPlayerResults().stream()
										 .filter(playerResult -> !playerResult.getPlayer().getId().toString().equals(winner.getId().toString()))
										 .collect(toList());
		
		PlayerResult winnerResult = match.getPlayerResults().stream()
										 .filter(playerResult -> playerResult.getPlayer().getId().toString().equals(winner.getId().toString()))
										 .findFirst().get();
		
		List<Player> playersToUpdate = new ArrayList<>();
		playersToUpdate.add(updateWinnerStatistics(winnerResult));
		
		for(PlayerResult loser : losers){
			playersToUpdate.add(updateLoserStatistics(loser));
		}
		mongoDatastore.save(playersToUpdate);
	}

	private Player updateLoserStatistics(PlayerResult loserResult) {
		Player loser = this.player(loserResult.getPlayer().getId());
		loser.setLost(loser.getLost() + 1);
		loser.setBest(max(loser.getBest(), loserResult.getScore()));
		loser.setBalance(loser.getBalance() + AMOUNT_OF_RUS_FOR_LOSING);
		return loser;
	}
	
	public Player updateLoserStatisticsInExpiredMatch(PlayerResult loserResult) {
		Player loser = this.player(loserResult.getPlayer().getId());
		loser.setLost(loser.getLost() + 1);
		loser.setBest(max(loser.getBest(), loserResult.getScore()));
		return loser;
	}

	private Player updateWinnerStatistics(PlayerResult winnerResult) {
		Player winner = this.player(winnerResult.getPlayer().getId());
		winner.setWon(winner.getWon() + 1);
		winner.setBest(max(winner.getBest(), winnerResult.getScore()));
		winner.setBalance(winner.getBalance() + AMOUNT_OF_RUS_FOR_WINNING);
		return winner;
	}

	public boolean isValidMail(String mail) {
		String nickname = this.nicknameFromMail(mail);
		return isValidNickname(nickname);
	}

	private boolean isValidNickname(String nickname) {
		if(nickname.length() < 3){
			return false;
		}
		return true;
	}

	public void addSocialNetwork(String playerId, String type, String id) {
		Player player = this.player(playerId);
		if(facebook.equals(valueOf(type))){
			player.setFacebookId(id);
		}else if(twitter.equals(valueOf(type))){
			player.setTwitterId(id);
		}else{
			throw new RuntimeException("Wrong social network type");
		}
		
		mongoDatastore.save(player);
	}
}
