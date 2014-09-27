package tuttifrutti.models;

import static tuttifrutti.models.DuplaState.CORRECTED;
import static tuttifrutti.models.DuplaState.PERFECT;
import static tuttifrutti.models.DuplaState.WRONG;
import lombok.Getter;
import lombok.Setter;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rfanego
 */
@Embedded
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Dupla implements Comparable<Dupla>{
	private Category category;
	
	@Property("written_word")
	@JsonProperty(value = "written_word")
	private String writtenWord;
	
	@Property("final_word")
	@JsonProperty(value = "final_word")
	private String finalWord;
	
	private Integer time;
	
	private DuplaState state;
	
	private DuplaScore score;
	
	public String getWrittenWord(){
		return writtenWord != null ? writtenWord.toLowerCase() : writtenWord;
	}
	
	public void setFinalWord(String finalWord){
		this.finalWord = finalWord;
		if(finalWord != null){			
			if(finalWord.equals(getWrittenWord())){
				setState(PERFECT);
			}else{
				setState(CORRECTED);
			}
		}
	}

	public void setWrongState() {
		setState(WRONG);
	}

	@Override
	public int compareTo(Dupla dupla) {
		return this.category.getId().compareTo(dupla.getCategory().getId());
	}
}
