package tuttifrutti.models;

import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static tuttifrutti.models.enums.DuplaState.WRONG;
import static tuttifrutti.models.enums.PowerUpType.autocomplete;
import static tuttifrutti.models.enums.PowerUpType.buy_time;
import static tuttifrutti.models.enums.PowerUpType.opponent_word;
import static tuttifrutti.models.enums.PowerUpType.suggest;


import java.util.ArrayList;
import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import tuttifrutti.cache.CategoryCache;
import tuttifrutti.cache.RusCache;
import tuttifrutti.models.enums.PowerUpType;

import tuttifrutti.utils.ConfigurationAccessor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author rfanego
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class PowerUp {
	private PowerUpType name;
	
	private String value;
	
	private int cost;
    
    @JsonIgnore
    @Autowired
    private CategoryCache categoryCache;
    
    @JsonIgnore
    @Autowired
	private RusCache rusCache;

    public PowerUp(PowerUpType powerUp,int cost){
    	this.name = powerUp;
    	this.cost = cost;
    }
    
	public PowerUp(PowerUpType powerUp, String value,int cost) {
		this.name = powerUp;
		this.value = value;
		this.cost = cost;
	}
    
	public void generate(Match match, String playerId) {
		if(match.getConfig().isPowerUpsEnabled() && !match.playerHasAlreadyPlayed(playerId)){
			autoCompleteWords(match);
			suggestWords(match);
			opponentWords(match);
			buyTime(match);
		}
	}

	private void buyTime(Match match) {
		match.getCategories().forEach(category -> {
			PowerUp buyTimePowerUp = new PowerUp(buy_time, ConfigurationAccessor.s("powerup.buy_time"),rusCache.rusFor(buy_time));
			category.getPowerUps().add(buyTimePowerUp);
		});
	}

	private void opponentWords(Match match) {
		List<Turn> turns = match.getLastRound().getTurns();
		if(!isEmpty(turns)){
			List<Dupla> allDuplas = new ArrayList<>();
			turns.forEach(turn -> allDuplas.addAll(turn.getDuplas()));
			List<Dupla> validDuplas = allDuplas.stream().filter(dupla -> !dupla.getState().equals(WRONG)).collect(toList());
			for(Category category : match.getCategories()){
				List<Dupla> categoryDuplas = validDuplas.stream().filter(dupla -> dupla.getCategory().getId().equals(category.getId())).collect(toList());
				if(!isEmpty(categoryDuplas)){
					shuffle(categoryDuplas);
					category.getPowerUps().add(new PowerUp(opponent_word,categoryDuplas.get(0).getWrittenWord(),rusCache.rusFor(opponent_word)));
				}else{
					category.getPowerUps().add(new PowerUp(opponent_word,rusCache.rusFor(opponent_word)));
				}
			}
		}else{
			match.getCategories().forEach(category -> {
				category.getPowerUps().add(new PowerUp(opponent_word,rusCache.rusFor(opponent_word)));
			});
		}
	}

	private void suggestWords(Match match) {
		match.getCategories().forEach(category -> {
			Letter letter = match.getLastRound().getLetter().getLetter();
			category.getPowerUps().add(new PowerUp(suggest,categoryCache.retrieveWord(category.getId(), letter),rusCache.rusFor(suggest)));
		});
	}

	private void autoCompleteWords(Match match) {
		match.getCategories().forEach(category -> {
			Letter letter = match.getLastRound().getLetter().getLetter();
			category.getPowerUps().add(new PowerUp(autocomplete,categoryCache.retrieveWord(category.getId(), letter),rusCache.rusFor(autocomplete)));
		});
	}
}
