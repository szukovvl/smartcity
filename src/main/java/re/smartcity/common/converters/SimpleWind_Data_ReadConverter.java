package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.common.data.exchange.SimpleWindData;

public class SimpleWind_Data_ReadConverter implements Converter<String, SimpleWindData> {

    private final Logger logger = LoggerFactory.getLogger(SimpleWind_Data_ReadConverter.class);

    @Override
    public SimpleWindData convert(String from) {

        SimpleWindData res;
        try {
            res = new ObjectMapper().readValue(from, SimpleWindData.class);
        }
        catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        return res;
    }
}
