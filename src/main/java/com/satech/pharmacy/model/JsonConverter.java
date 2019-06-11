package com.satech.pharmacy.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.postgresql.util.PGobject;

import javax.persistence.Converter;
import java.io.IOException;

@Converter(autoApply = true)
public class JsonConverter implements javax.persistence.AttributeConverter<JSONObject, Object> {

    private static final long serialVersionUID = 1L;
    private static ObjectMapper mapper = new ObjectMapper();

    @Override
    public Object convertToDatabaseColumn(JSONObject objectValue) {
        try {
            PGobject out = new PGobject();
            out.setType("json");
            out.setValue(objectValue.toString());
            return out;
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to serialize to json field ", e);
        }
    }

    @Override
    public JSONObject convertToEntityAttribute(Object dataValue) {
        try {
            if (dataValue instanceof PGobject && ((PGobject) dataValue).getType().equals("json")) {
                return mapper.readerFor(JSONObject.class).readValue(((PGobject) dataValue).getValue());
            }
            return null;
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to deserialize to json field ", e);
        }
    }
}
