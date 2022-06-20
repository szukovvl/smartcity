package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.energynet.component.DataA;

public class JsonDataAType_ReadConverter implements Converter<String, DataA> {

    @Override
    public DataA convert(String from) {

        DataA res;
        try {
            res = new ObjectMapper().readValue(from, DataA.class);
        }
        catch (JsonProcessingException ex) {
            return null;
        }
        return res;
    }
}
