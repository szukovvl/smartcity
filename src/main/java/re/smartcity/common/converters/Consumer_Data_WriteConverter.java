package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.energynet.component.data.ConsumerSpecification;

public class Consumer_Data_WriteConverter implements Converter<ConsumerSpecification, String> {

    private final Logger logger = LoggerFactory.getLogger(Consumer_Data_WriteConverter.class);

    @Override
    public String convert(ConsumerSpecification from) {
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
