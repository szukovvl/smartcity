package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.stand.StandControlData;

public class SimpleStand_Data_WriteConverter  implements Converter<StandControlData, String> {

    private final Logger logger = LoggerFactory.getLogger(SimpleStand_Data_WriteConverter.class);

    @Override
    public String convert(StandControlData from) {
        String s;
        try {
            s = new ObjectMapper().writeValueAsString(from);
        }
        catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        return s;
    }
}
