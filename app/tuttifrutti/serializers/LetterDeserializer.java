/**
 * 
 */
package tuttifrutti.serializers;

import java.io.IOException;

import tuttifrutti.models.Letter;
import tuttifrutti.models.LetterWrapper;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * @author rfanego
 *
 */
public class LetterDeserializer extends JsonDeserializer<LetterWrapper> {

	@Override
	public LetterWrapper deserialize(JsonParser json,DeserializationContext arg1) throws IOException,JsonProcessingException {
		return new LetterWrapper(Letter.valueOf(json.getText()));
	}

}
