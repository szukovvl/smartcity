package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.energynet.component.data.EnergyDistributorSpecification;

public class EnergyDistributor_Data_WriteConverter implements Converter<EnergyDistributorSpecification, String> {

    private final Logger logger = LoggerFactory.getLogger(EnergyDistributor_Data_WriteConverter.class);

    @Override
    public String convert(EnergyDistributorSpecification from) {
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
