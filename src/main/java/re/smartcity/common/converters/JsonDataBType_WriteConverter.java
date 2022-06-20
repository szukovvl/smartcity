package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import re.smartcity.energynet.component.DataB;
import org.springframework.core.convert.converter.Converter;

public class JsonDataBType_WriteConverter implements Converter<DataB, String> {

    @Override
    public String convert(DataB from) {
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
