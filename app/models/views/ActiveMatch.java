package models.views;

import lombok.Getter;
import lombok.Setter;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * @author rfanego
 */
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ActiveMatch {

	private ObjectId id;
	
	private String name;
	
	private String letter;
	
	private Integer rondasFaltantes; //TODO check name in apiary
	
	private String state;
}
