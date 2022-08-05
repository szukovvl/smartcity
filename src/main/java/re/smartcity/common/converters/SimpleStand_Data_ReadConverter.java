package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.stand.StandControlData;

public class SimpleStand_Data_ReadConverter implements Converter<String, StandControlData> {

    private final Logger logger = LoggerFactory.getLogger(SimpleStand_Data_ReadConverter.class);

    @Override
    public StandControlData convert(String from) {

        StandControlData res;
        try {
            res = new ObjectMapper().readValue(from, StandControlData.class);
        }
        catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        return res;
    }
}
