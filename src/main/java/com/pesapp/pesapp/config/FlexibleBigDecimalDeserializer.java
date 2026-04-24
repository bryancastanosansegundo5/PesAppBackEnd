package com.pesapp.pesapp.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.math.BigDecimal;

public class FlexibleBigDecimalDeserializer extends JsonDeserializer<BigDecimal> {

    @Override
    public BigDecimal deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.currentToken();
        if (token == JsonToken.VALUE_NULL) {
            return null;
        }

        if (token == JsonToken.VALUE_STRING) {
            String value = parser.getText();
            if (value == null || value.trim().isEmpty()) {
                return null;
            }

            try {
                return new BigDecimal(value.trim());
            } catch (NumberFormatException exception) {
                return (BigDecimal) context.handleWeirdStringValue(
                        BigDecimal.class,
                        value,
                        "El valor debe ser numerico o vacio");
            }
        }

        if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
            return parser.getDecimalValue();
        }

        return (BigDecimal) context.handleUnexpectedToken(BigDecimal.class, parser);
    }
}
