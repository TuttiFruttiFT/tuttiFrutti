package tuttifrutti.models;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

import org.mongodb.morphia.annotations.Transient;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author rfanego
 */
//@Entity
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class PowerUp {
//	@Id
//	@JsonSerialize(using = ObjectIdSerializer.class)
//	private ObjectId id;
	
	private String name;
	
	@Transient
    private List<Dupla> duplas;

	public static void generate(Match match) {
		if(match.getConfig().isPowerUpsEnabled()){			
			PowerUp.autoCompleteWords(match);
			PowerUp.suggestWords(match);
			PowerUp.opponentWords(match);
		}
	}

	private static void opponentWords(Match partida) {
		// TODO implementar
		
	}

	private static void suggestWords(Match partida) {
		// TODO implementar
		
	}

	private static void autoCompleteWords(Match partida) {
		// TODO implementar
		
	}
}
