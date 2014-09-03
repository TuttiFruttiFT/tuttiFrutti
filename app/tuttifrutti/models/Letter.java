/**
 * 
 */
package tuttifrutti.models;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author rfanego
 *
 */
@Embedded
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public enum Letter {
	A("A"),B("B"),C("C"),D("D"),E("E"),F("F"),G("G"),H("H"),I("I"),J("J"),K("K"),L("L"),M("M"),
	N("N"),O("O"),P("P"),Q("Q"),R("R"),S("S"),T("T"),U("U"),V("V"),W("W"),X("X"),Y("Y"),Z("Z");
	
	private static final int PREVIOUS_LETTERS_SIZE = 5;
	
	private static final List<Letter> VALUES = Collections.unmodifiableList(Arrays.asList(values()));
	private static final int SIZE = VALUES.size();
	private static final Random RANDOM = new Random();
	
	@Getter @Setter
	private String letter;
	
	@Property("previous_letters")
	@JsonIgnore
	private CircularFifoQueue<String> previousLetters;
	
	Letter(String letter){
		this.letter = letter;
		this.previousLetters = new CircularFifoQueue<>(PREVIOUS_LETTERS_SIZE);
	}
	
	public static Letter random() {
		return VALUES.get(RANDOM.nextInt(SIZE));
	}

	public void next() {
		this.previousLetters.add(this.letter);
		List<Letter> availableLetters = getAvailableLetters();
		this.letter = availableLetters.get(RANDOM.nextInt(availableLetters.size())).getLetter();
	}

	private List<Letter> getAvailableLetters() {
		return VALUES.stream().filter(letter -> !this.previousLetters.stream().anyMatch(usedLetter -> usedLetter.equals(letter.getLetter()))).collect(toList());
	}
}
