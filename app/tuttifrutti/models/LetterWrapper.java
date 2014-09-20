package tuttifrutti.models;

import static java.util.stream.Collectors.toList;
import static tuttifrutti.models.Letter.values;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import lombok.Getter;
import lombok.Setter;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author rfanego
 */
@Embedded
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LetterWrapper {
	private static final int PREVIOUS_LETTERS_SIZE = 5;
	
	private static final List<Letter> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
	private static final int SIZE = VALUES.size();
	private static final Random RANDOM = new Random();
	
	@Getter @Setter
	@Embedded
	private Letter letter;
	
	@Property("previous_letters")
	@JsonIgnore
	@Getter @Setter
	private LinkedList<String> previousLetters;
	
	@SuppressWarnings("unused")
	private LetterWrapper(){
		this.previousLetters = new LinkedList<>();
	}
	
	public LetterWrapper(Letter letter){
		this.letter = letter;
		this.previousLetters = new LinkedList<>();
	}
	
	public static LetterWrapper random() {
		return new LetterWrapper(VALUES.get(RANDOM.nextInt(SIZE)));
	}

	public LetterWrapper next() {
		this.previousLetters.addFirst(this.letter.toString());
		if(this.previousLetters.size() > PREVIOUS_LETTERS_SIZE){
			this.previousLetters.removeLast();
		}
		List<Letter> availableLetters = getAvailableLetters();
		LetterWrapper newLetter = new LetterWrapper(availableLetters.get(RANDOM.nextInt(availableLetters.size())));
		newLetter.previousLetters = this.previousLetters;
		return newLetter;
	}

	private List<Letter> getAvailableLetters() {
		return VALUES.stream().filter(letter -> !this.previousLetters.stream().anyMatch(usedLetter -> usedLetter.equals(letter.getLetter()))).collect(toList());
	}
}
