package tuttifrutti.serializers;

import java.io.IOException;
import java.util.Date;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class DateSerializer extends JsonSerializer<Date> {

	@Override
	public void serialize(Date value, JsonGenerator gen, SerializerProvider prov)
			throws IOException, JsonProcessingException {

		String formattedDate = DateDeserializer.FORMATTER_SHORT.print(value.getTime());

		gen.writeString(formattedDate);
	}
}
