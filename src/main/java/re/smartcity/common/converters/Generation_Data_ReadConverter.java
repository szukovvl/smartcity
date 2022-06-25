package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.energynet.component.data.GenerationSpecification;

public class Generation_Data_ReadConverter implements Converter<String, GenerationSpecification> {

    private final Logger logger = LoggerFactory.getLogger(Generation_Data_ReadConverter.class);

    @Override
    public GenerationSpecification convert(String from) {

        GenerationSpecification res;
        try {
            res = new ObjectMapper().readValue(from, GenerationSpecification.class);
        }
        catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        return res;
    }
}
