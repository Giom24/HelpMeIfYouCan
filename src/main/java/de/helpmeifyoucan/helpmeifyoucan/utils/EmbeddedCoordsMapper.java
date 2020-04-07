package de.helpmeifyoucan.helpmeifyoucan.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.helpmeifyoucan.helpmeifyoucan.models.Coordinates;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EmbeddedCoordsMapper extends StdSerializer<Coordinates> {


    public EmbeddedCoordsMapper() {
        this(null);
    }

    public EmbeddedCoordsMapper(Class<Coordinates> t) {
        super(t);
    }

    @Override
    public void serialize(
            Coordinates value, JsonGenerator jgen, SerializerProvider provider) throws IOException {

        jgen.writeStartObject();
        jgen.writeStringField("id", value.getId().toString());
        jgen.writeObjectField("helpOffers", value.getHelpOffers());
        jgen.writeObjectField("helpRequests", value.getLongitude());
        jgen.writeObjectField("location", value.getLocation());
        jgen.writeEndObject();
    }


}
