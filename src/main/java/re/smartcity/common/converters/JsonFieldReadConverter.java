package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.common.data.ForecastPoint;

public class JsonFieldReadConverter implements Converter<String, ForecastPoint[]> {

    private final Logger logger = LoggerFactory.getLogger(JsonFieldReadConverter.class);

    @Override
    public ForecastPoint[] convert(String from) {

        ForecastPoint[] res;
        try {
            res = new ObjectMapper().readValue(from, ForecastPoint[].class);
        }
        catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        return res;
    }

}
