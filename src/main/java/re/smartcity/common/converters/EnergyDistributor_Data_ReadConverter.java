package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.energynet.component.data.EnergyDistributorSpecification;

public class EnergyDistributor_Data_ReadConverter implements Converter<String, EnergyDistributorSpecification> {

    private final Logger logger = LoggerFactory.getLogger(EnergyDistributor_Data_ReadConverter.class);

    @Override
    public EnergyDistributorSpecification convert(String from) {

        EnergyDistributorSpecification res;
        try {
            res = new ObjectMapper().readValue(from, EnergyDistributorSpecification.class);
        }
        catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        return res;
    }
}
