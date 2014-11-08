package tuttifrutti.services;

import static java.lang.Math.max;
import static java.util.Collections.shuffle;
import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.stream.Collectors.toList;
import static org.joda.time.DateTime.now;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tuttifrutti.models.Match;
import tuttifrutti.models.Pack;
import tuttifrutti.models.Player;
import tuttifrutti.models.PlayerResult;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author rfanego
 */
@Component
public class PlayerService {
	private static final Integer AMOUNT_OF_RUS_FOR_WINNING = 5;
	private static final Integer AMOUNT_OF_RUS_FOR_LOSING = 1;
	private static final Integer AMOUNT_OF_RUS_FOR_WINNING_ROUND = 1;
	private static final Integer STARTING_RUS = 25;
	private static final int AMOUNT_OF_OTHER_PLAYERS = 20;
	
	@Autowired
	private Datastore mongoDatastore;

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

	public Player registerFacebook(String facebookId) {
		// TODO implementar
		return null;		
	}

	public Player registerTwitter(String twitterId) {
		// TODO implementar
		return null;
	}

	public boolean editProfile(JsonNode json) {
		// TODO implementar
		return false;
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
	
	public void powerUp(String playerId, String powerUpId) {
		//TODO implementar
	}

	public void buy(String idJugador, Pack pack) {
		// TODO implementar
		
	}

	public List<Player> searchPlayers(String word, String playerId) {
		Query<Player> query = mongoDatastore.find(Player.class);
		query.or(query.criteria("nickname").equal(Pattern.compile(word, CASE_INSENSITIVE)),
				query.criteria("mail").equal(Pattern.compile(word, CASE_INSENSITIVE)));
		query.and(query.criteria("id").notEqual(new ObjectId(playerId)));
		return query.asList();
	}

	public List<Player> searchOthersPlayers(String playerId) {
		List<Player> players = mongoDatastore.find(Player.class).asList();
		shuffle(players);
		return players.subList(0, AMOUNT_OF_OTHER_PLAYERS < players.size() ? players.size() : AMOUNT_OF_OTHER_PLAYERS);
	}

	public Player search(String mail) {
		Query<Player> query = mongoDatastore.find(Player.class, "mail =", mail.trim().toLowerCase());
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

	private Player updateWinnerStatistics(PlayerResult winnerResult) {
		Player winner = this.player(winnerResult.getPlayer().getId());
		winner.setWon(winner.getWon() + 1);
		winner.setBest(max(winner.getBest(), winnerResult.getScore()));
		winner.setBalance(winner.getBalance() + AMOUNT_OF_RUS_FOR_WINNING);
		return winner;
	}

	public boolean isValid(String mail) {
		String nickname = this.nicknameFromMail(mail);
		if(nickname.length() < 3){
			return false;
		}
		return true;
	}
}
