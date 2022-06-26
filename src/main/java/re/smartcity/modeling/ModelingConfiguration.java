package re.smartcity.modeling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
public class ModelingConfiguration {

    @Autowired
    private ModelingData model;

    @PreDestroy
    public void shutdownModeling() {
        model.stopAll();
    }
}
