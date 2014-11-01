package tuttifrutti.services;

import static java.lang.Math.max;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

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
	private static final Integer AMOUNT_OF_RUS_FOR_WINNING = 20;
	private static final Integer AMOUNT_OF_RUS_FOR_LOSING = 0;
	
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
		String[] splittedMail = mail.split("@");
		player.setNickname((splittedMail.length > 0) ? splittedMail[0] : mail);
		player.setMail(mail);
		player.setPassword(clave);
		mongoDatastore.save(player);
		
		return player;
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

	public void addFriend(String idJugador, String idAmigo) {
		// TODO implementar
		
	}
	
	public void powerUp(String idJugador, String idPowerUp) {
		//TODO implementar
	}

	public void buy(String idJugador, Pack pack) {
		// TODO implementar
		
	}

	public List<Player> searchPlayers(String palabraABuscar) {
		return mongoDatastore.find(Player.class).asList();
	}

	public List<Player> searchOthersPlayers(String idJugador) {
		// TODO implementar
		return null;
	}

	public Player search(String mail) {
		Query<Player> query = mongoDatastore.find(Player.class, "mail =", mail);
		return query.get();
	}

	public List<PlayerResult> playerResultsFromIds(List<String> playerIds) {
		List<PlayerResult> playerResults = new ArrayList<>();
		this.playersFromIds(playerIds).stream().forEach(player -> playerResults.add(new PlayerResult(player,0,0,false)));
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
}
