package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.common.data.exchange.GameCriteriaData;

public class GameCriteria_Data_ReadConverter implements Converter<String, GameCriteriaData> {

    private final Logger logger = LoggerFactory.getLogger(GameCriteria_Data_ReadConverter.class);

    @Override
    public GameCriteriaData convert(String from) {

        GameCriteriaData res;
        try {
            res = new ObjectMapper().readValue(from, GameCriteriaData.class);
        }
        catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        return res;
    }
}
