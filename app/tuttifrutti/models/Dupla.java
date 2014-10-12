package tuttifrutti.models;

import static tuttifrutti.models.enums.DuplaState.CORRECTED;
import static tuttifrutti.models.enums.DuplaState.PERFECT;
import static tuttifrutti.models.enums.DuplaState.WRONG;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import tuttifrutti.models.enums.DuplaState;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author rfanego
 */
@Embedded
@Getter @Setter
@NoArgsConstructor
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
	
	private Integer score;
	
	public Dupla(String categoryId){
		this.category = new Category(categoryId);
	}
	
	public Dupla(String categoryId,String writtenWord){
		this.category = new Category(categoryId);
		this.writtenWord = writtenWord;
	}
	
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

	public Dupla simplified() {
		Dupla dupla = new Dupla();
		dupla.setWrittenWord(this.getWrittenWord());
		dupla.setCategory(new Category(this.getCategory().getId()));
		return dupla;
	}
}
