package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.energynet.component.DataA;

public class JsonDataAType_WriteConverter implements Converter<DataA, String> {

    @Override
    public String convert(DataA from) {
        String s;
        try {
            s = new ObjectMapper().writeValueAsString(from);
        }
        catch (JsonProcessingException ex) {
            return null;
        }
        return s;
    }
}
