package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.energynet.component.data.MainSubstationSpecification;

public class MainSubstation_Data_ReadConverter implements Converter<String, MainSubstationSpecification> {

    private final Logger logger = LoggerFactory.getLogger(MainSubstation_Data_ReadConverter.class);

    @Override
    public MainSubstationSpecification convert(String from) {

        MainSubstationSpecification res;
        try {
            res = new ObjectMapper().readValue(from, MainSubstationSpecification.class);
        }
        catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        return res;
    }
}
