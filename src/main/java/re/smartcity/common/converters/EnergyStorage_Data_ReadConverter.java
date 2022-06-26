package re.smartcity.common.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import re.smartcity.energynet.component.data.EnergyStorageSpecification;

public class EnergyStorage_Data_ReadConverter implements Converter<String, EnergyStorageSpecification> {

    private final Logger logger = LoggerFactory.getLogger(EnergyStorage_Data_ReadConverter.class);

    @Override
    public EnergyStorageSpecification convert(String from) {

        EnergyStorageSpecification res;
        try {
            res = new ObjectMapper().readValue(from, EnergyStorageSpecification.class);
        }
        catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
            return null;
        }
        return res;
    }
}
