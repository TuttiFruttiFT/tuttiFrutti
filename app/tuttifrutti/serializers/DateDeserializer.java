package tuttifrutti.serializers;

import java.io.IOException;
import java.util.Date;

import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class DateDeserializer extends JsonDeserializer<Date> {
	
	public static final DateTimeFormatter FORMATTER_SHORT = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss");
	
	@Override
	public Date deserialize(JsonParser json, DeserializationContext ctx) throws IOException, JsonProcessingException {
		
		long longDate = NumberUtils.toLong(json.getText());
		
		if (longDate > 0) {
			//If a long is sent, we get the date value from it
			return new Date(longDate);
		} else if (json.getText().matches("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}")) {
			return FORMATTER_SHORT.parseDateTime(json.getText()).toDate();
		} else {
			String aux = json.getText().substring(0, 19);
			return FORMATTER_SHORT.parseDateTime(aux).toDate();
		}
	}
}
