package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.energynet.component.DataA;
import re.smartcity.energynet.component.DataB;

public class JsonDataBType_ReadConverter implements Converter<String, DataB> {

    @Override
    public DataB convert(String from) {

        DataB res;
        try {
            res = new ObjectMapper().readValue(from, DataB.class);
        }
        catch (JsonProcessingException ex) {
            return null;
        }
        return res;
    }
}
