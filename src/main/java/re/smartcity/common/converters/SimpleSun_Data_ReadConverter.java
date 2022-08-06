package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.common.data.exchange.SimpleSunData;

public class SimpleSun_Data_ReadConverter implements Converter<String, SimpleSunData> {

    private final Logger logger = LoggerFactory.getLogger(SimpleSun_Data_ReadConverter.class);

    @Override
    public SimpleSunData convert(String from) {

        SimpleSunData res;
        try {
            res = new ObjectMapper().readValue(from, SimpleSunData.class);
        }
        catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        return res;
    }
}
