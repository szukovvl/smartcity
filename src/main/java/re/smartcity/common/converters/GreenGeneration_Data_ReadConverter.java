package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.energynet.component.data.GreenGenerationSpecification;

public class GreenGeneration_Data_ReadConverter implements Converter<String, GreenGenerationSpecification> {

    private final Logger logger = LoggerFactory.getLogger(GreenGeneration_Data_ReadConverter.class);

    @Override
    public GreenGenerationSpecification convert(String from) {

        GreenGenerationSpecification res;
        try {
            res = new ObjectMapper().readValue(from, GreenGenerationSpecification.class);
        }
        catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        return res;
    }
}
