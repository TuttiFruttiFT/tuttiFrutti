package tuttifrutti.services;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static org.joda.time.DateTime.now;
import static org.springframework.util.StringUtils.isEmpty;
import static play.libs.F.Promise.promise;
import static tuttifrutti.models.Turn.TURN_DURATION_IN_MINUTES;
import static tuttifrutti.models.enums.DuplaScore.ZERO_SCORE;
import static tuttifrutti.models.enums.DuplaState.WRONG;
import static tuttifrutti.models.enums.MatchState.FINISHED;
import static tuttifrutti.models.enums.MatchState.PLAYER_TURN;
import static tuttifrutti.models.enums.MatchState.REJECTED;
import static tuttifrutti.models.enums.MatchState.TO_BE_APPROVED;
import static tuttifrutti.models.enums.MatchType.PRIVATE;
import static tuttifrutti.models.enums.MatchType.PUBLIC;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Predicate;

import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import tuttifrutti.elastic.ElasticUtil;
import tuttifrutti.models.Category;
import tuttifrutti.models.Dupla;
import tuttifrutti.models.Match;
import tuttifrutti.models.MatchConfig;
import tuttifrutti.models.MatchName;
import tuttifrutti.models.Player;
import tuttifrutti.models.PlayerResult;
import tuttifrutti.models.Round;
import tuttifrutti.models.Turn;
import tuttifrutti.models.enums.MatchType;
import tuttifrutti.models.views.ActiveMatch;

/**
 * @author rfanego
 *
 */
@Component
public class MatchService {
	@Autowired
	private Datastore mongoDatastore;
	
	@Autowired
	private Category categoryService;
	
	@Autowired
	private Round roundService;
	
	@Autowired
	private PlayerService playerService;
	
	@Autowired
	private ElasticUtil elasticUtil;
	
	@Autowired
	private PushService pushUtil;
	
	private static final int PUBLIC_NUMBER_OF_ROUND = 10;
	
	public List<ActiveMatch> activeMatches(String playerId) {
		List<ActiveMatch> activeMatches = new ArrayList<>();
		Query<Match> query = mongoDatastore.find(Match.class, "playerResults.player.id =", new ObjectId(playerId));
		for(Match match : query.asList()){
			ActiveMatch activeMatch = new ActiveMatch();
			Round round = new Round();
			round.setNumber(match.getLastRound().getNumber());
			round.setLetter(match.getLastRound().getLetter());
			activeMatch.setCurrentRound(round);
			activeMatch.setId(match.getId().toString());
			match.changeMatchDependingOnPlayer(playerId);
			activeMatch.setMatchName(match.getMatchName());
			activeMatch.setState(match.getState().toString());
			activeMatch.setWinner(match.getWinner());
			activeMatch.setConfig(match.getConfig());
			activeMatches.add(activeMatch);
		}
		return activeMatches;
	}
	
	public Match match(String matchId, String playerId) {
		Match match = mongoDatastore.get(Match.class,new ObjectId(matchId));
		if(playerId != null){
			match.changeMatchDependingOnPlayer(playerId);
		}
		return match;
	}
	
	public Match endedMatch(String matchId){
		Query<Match> query = mongoDatastore.find(Match.class,"state =",FINISHED.toString());
		query.and(query.criteria("id").equal(new ObjectId(matchId)));
		return query.get();
	}
	
	public Match findPublicMatch(String playerId, MatchConfig config) {
		return findMatch(playerId, config, PUBLIC);
	}

	public Match findMatch(String playerId, MatchConfig config, MatchType type) {
		Query<Match> query = mongoDatastore.find(Match.class, "config.number_of_players =", config.getNumberOfPlayers());
		query.and(query.criteria("config.language").equal(config.getLanguage()), 
				  query.criteria("config.type").equal(type.toString()),
				  query.criteria("config.mode").equal(config.getMode()),
				  query.criteria("state").equal(TO_BE_APPROVED),
				  query.criteria("playerResults.player.id").notEqual(new ObjectId(playerId)));
		
		return query.get();
	}
	
	public Match createPublic(MatchConfig config) {
		config.setRounds(PUBLIC_NUMBER_OF_ROUND);
		return create(config, PUBLIC);
	}

	public void addPlayer(Match match, String playerId) {
		PlayerResult playerResult = new PlayerResult();
		Player player = playerService.player(playerId);
		if(player == null){
			throw new RuntimeException("Player " + playerId + " does not exist");
		}
		playerResult.setPlayer(player);
		playerResult.setScore(0);
		playerResult.setAccepted(true);
		match.getPlayerResults().add(playerResult);
		match.getMatchName().incrementPlayers();
	}

	public Match createPrivate(String playerId, String name, MatchConfig config, List<String> playerIds, List<String> categoryIds) {
		List<PlayerResult> playerResults = playerService.playerResultsFromIds(playerIds);
		playerResults.stream().filter(playerResult -> playerResult.getPlayer().getId().toString().equals(playerId))
							  .forEach(playerResult -> playerResult.setAccepted(true));
		return create(config, PRIVATE,new MatchName(name,playerIds.size()),playerResults, categoryService.categoriesFromIds(categoryIds));
	}

	public List<Dupla> play(Match match, String playerId, List<Dupla> duplas, int time) {		
		Round round = match.getLastRound();
		
		elasticUtil.validate(duplas,round.getLetter());
		Turn turn = match.createTurn(match,playerId, duplas, time);
		mongoDatastore.save(match);
		calculateResult(match, turn);
		return getWrongDuplas(duplas);
	}
	
	private void calculateResult(Match match, Turn turn) {
		if(match.isRoundOver()){
			promise(() -> {				
				Round round = match.getLastRound();
				List<Turn> turns = round.getTurns();
				Integer minTime = getMinimumTime(turns);
				List<Dupla> allDuplas = flatDuplasFromTurns(turns);
				List<Dupla> validDuplas = processInvalidDuplas(minTime,allDuplas);
				
				match.processValidDuplas(validDuplas);
				calculateTurnScores(turns, match);
				saveOldRound(match, round, minTime);
				
				if(match.isFinished(round)){
					match.calculateWinner();
					match.setState(FINISHED);
					pushUtil.matchResult(match);
				}else{				
					match.setState(PLAYER_TURN);
					roundService.create(match);
					pushUtil.roundResult(match,round.getNumber());
				}
				mongoDatastore.save(match);
				return null;
			});
		}else{
			if(turn.isBpmbpt()){
				pushUtil.bpmbpt(match, turn.getPlayer().getId().toString());
			}
		}
	}
	
	private void calculateTurnScores(List<Turn> turns, Match match) {
		for(Turn turn : turns){
			int turnScore = turn.getDuplas().stream().mapToInt(dupla -> dupla.getScore()).sum();
			turn.setScore(turnScore);
			PlayerResult playerResult = match.getPlayerResults().stream().filter(player -> player.getPlayer().getId().toString().equals(turn.getPlayer().getId().toString())).findFirst().get();
			playerResult.setScore(playerResult.getScore() + turnScore);
		}
	}
	
	public List<Dupla> processInvalidDuplas(Integer minTime,List<Dupla> allDuplas) {
		scoreEmptyWrongAndOutOfTimeDuplas(allDuplas, minTime);
		List<Dupla> filteredDuplas = filterEmptyWrongAndOutOfTimeDuplas(allDuplas, minTime);
		return filteredDuplas;
	}
	
	private void scoreEmptyWrongAndOutOfTimeDuplas(List<Dupla> categoryDuplas, Integer minTime) {
		categoryDuplas.stream().filter(emptyWrongAndOutOfTime(minTime)).forEach(dupla -> dupla.setScore(ZERO_SCORE.getScore()));
	}
	
	private List<Dupla> filterEmptyWrongAndOutOfTimeDuplas(List<Dupla> allDuplas,Integer minTime) {
		return allDuplas.stream().filter(emptyWrongAndOutOfTime(minTime).negate()).collect(toList());
	}
	
	private Predicate<? super Dupla> emptyWrongAndOutOfTime(Integer minTime) {
		return dupla -> (dupla.getState().equals(WRONG) || isEmpty(dupla.getWrittenWord()) || dupla.getTime() > minTime);
	}
	
	private Integer getMinimumTime(List<Turn> turns) {
		OptionalInt optionalInt = turns.stream().filter(turn -> turn.isBpmbpt()).mapToInt(turn -> turn.getEndTime()).min();
		if(!optionalInt.isPresent()){
			return (int) MINUTES.toSeconds(TURN_DURATION_IN_MINUTES);
		}
		return optionalInt.getAsInt();
	}

	private List<Dupla> flatDuplasFromTurns(List<Turn> turns) {
		List<Dupla> allDuplas = new ArrayList<>();
		turns.stream().forEach(turn -> allDuplas.addAll(turn.getDuplas()));
		return allDuplas;
	}
	
	private void saveOldRound(Match match, Round round,Integer minTime) {
		round.setEndTime(minTime);
		round.setStopPlayer(match.getStopPlayer(round.getTurns(),minTime));
		round.setMatchId(match.getId().toString());
		mongoDatastore.save(round);
	}
	
	private Match create(MatchConfig config, MatchType type,MatchName matchName,List<PlayerResult> playerResults, List<Category> categories) {
		Match match = new Match();
		config.setType(type);
		config.setCurrentTotalNumberOfPlayers(config.getNumberOfPlayers());
		match.setConfig(config);
		match.setMatchName(matchName);
		match.setName(matchName.getValue());
		match.setState(TO_BE_APPROVED);
		match.setStartDate(now().toDate());
		match.setCategories(categories);
		match.setPlayerResults(playerResults);
		roundService.create(match);
		return match;
	}

	private Match create(MatchConfig config, MatchType type) {
		return create(config, type, new MatchName(1), new ArrayList<PlayerResult>(), categoryService.getPublicMatchCategories(config.getLanguage()));
	}
	
	private List<Dupla> getWrongDuplas(List<Dupla> duplas) {
		return duplas.stream().filter(dupla -> dupla.getState().equals(WRONG)).collect(toList());
	}
	
	public boolean playerReject(String playerId, Match match) {
		Iterator<PlayerResult> it = match.getPlayerResults().iterator();
		Player rejectorPlayer = null;
		
		while(it.hasNext() && rejectorPlayer == null){
			PlayerResult playerResult = it.next();
			
			Player player = playerResult.getPlayer();
			if(player.getId().toString().equals(playerId)){
				rejectorPlayer = new Player(player.getId(),player.getNickname());
				it.remove();
			}
		}
		
		if(rejectorPlayer != null){			
			List<Player> playerIds = match.players();
			
			if(playerIds.size() == 1){
				match.setState(REJECTED);
				pushUtil.rejected(playerIds,match);
			}else{
				match.getConfig().setCurrentTotalNumberOfPlayers(match.getConfig().getCurrentTotalNumberOfPlayers() - 1);
				match.getMatchName().decrementPlayers();
				if(match.getLastRound().getTurns().size() == match.getConfig().getCurrentTotalNumberOfPlayers()){
					this.calculateResult(match, null);
				}
				pushUtil.rejectedByPlayer(rejectorPlayer,match);
			}
			mongoDatastore.save(match);
			return true;
		}
		return false;		
	}
	
	public void privateMatchReady(Match match, List<Player> players) {
		pushUtil.privateMatchReady(match, players);
	}
}
