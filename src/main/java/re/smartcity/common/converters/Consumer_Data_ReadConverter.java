package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.energynet.component.data.ConsumerSpecification;

public class Consumer_Data_ReadConverter implements Converter<String, ConsumerSpecification> {

    private final Logger logger = LoggerFactory.getLogger(Consumer_Data_ReadConverter.class);

    @Override
    public ConsumerSpecification convert(String from) {

        ConsumerSpecification res;
        try {
            res = new ObjectMapper().readValue(from, ConsumerSpecification.class);
        }
        catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        return res;
    }
}
