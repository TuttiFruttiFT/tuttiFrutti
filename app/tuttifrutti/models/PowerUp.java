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

import org.springframework.stereotype.Component;

import tuttifrutti.models.enums.PowerUpType;

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
	
    private List<Dupla> duplas;

    public PowerUp(PowerUpType name){
    	this.name = name;
    }
    
	public void generate(Match match, String playerId) {
		if(match.getConfig().isPowerUpsEnabled() && !match.playerHasAlreadyPlayed(playerId)){
			match.setPowerUps(new ArrayList<>());
			autoCompleteWords(match);
			suggestWords(match);
			opponentWords(match);
			buyTime(match);
		}
	}

	private void buyTime(Match match) {
		match.getPowerUps().add(new PowerUp(buy_time,new ArrayList<>()));
	}

	private void opponentWords(Match match) {
		PowerUp powerUp = new PowerUp(opponent_word,new ArrayList<>());
		List<Turn> turns = match.getLastRound().getTurns();
		if(!isEmpty(turns)){
			List<Dupla> allDuplas = new ArrayList<>();
			turns.forEach(turn -> allDuplas.addAll(turn.getDuplas()));
			List<Dupla> validDuplas = allDuplas.stream().filter(dupla -> !dupla.getState().equals(WRONG)).collect(toList());
			for(Category category : match.getCategories()){
				List<Dupla> categoryDuplas = validDuplas.stream().filter(dupla -> dupla.getCategory().getId().equals(category.getId())).collect(toList());
				if(!isEmpty(categoryDuplas)){
					shuffle(categoryDuplas);
					powerUp.getDuplas().add(categoryDuplas.get(0).simplified());
				}else{
					//TODO no devuelvo nada, ver si necesito agregar una dupla solo con categoría
				}
			}
		}
		match.getPowerUps().add(powerUp);
	}

	private void suggestWords(Match match) {
		PowerUp powerUp = new PowerUp(suggest,new ArrayList<>());
		// TODO implementar
		match.getPowerUps().add(powerUp);
	}

	private void autoCompleteWords(Match match) {
		PowerUp powerUp = new PowerUp(autocomplete,new ArrayList<>());
		// TODO implementar
		match.getPowerUps().add(powerUp);
		
	}
}
