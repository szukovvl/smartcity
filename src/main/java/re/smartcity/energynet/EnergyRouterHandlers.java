package re.smartcity.energynet;

import com.sun.source.tree.BreakTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.data.exchange.SmallConsumerSpecification;
import re.smartcity.energynet.component.*;
import re.smartcity.modeling.ModelingData;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class EnergyRouterHandlers {

    private final Logger logger = LoggerFactory.getLogger(EnergyRouterHandlers.class);

    @Autowired
    private ModelingData modelingData;

    public Mono<ServerResponse> find(ServerRequest rq) {

        SupportedTypes bytype;
        try {
            bytype = SupportedTypes.valueOf(rq.pathVariable("type").toUpperCase());
        }
        catch (Exception ex) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("неверное значение типа объекта энергосети."), String.class);
        }

        if (bytype == SupportedTypes.MAINSUBSTATION) {
            return ServerResponse
                    .ok()
                    .header("Content-Language", "ru")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(Arrays.stream(this.modelingData.getTasks())
                            .map(e -> e.getPowerSystem())
                            .toArray()), IComponentIdentification.class);
        }

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(Arrays.stream(this.modelingData.getAllobjects())
                        .filter(e -> e.getComponentType() == bytype)
                        .toArray()), IComponentIdentification.class);
    }

    public Mono<ServerResponse> putData(ServerRequest rq) {

        String key = rq.pathVariable("key");

        logger.info("--> искомый объект: {}", key);

        Optional<IComponentIdentification> felement = Stream.concat(
                Arrays.stream(modelingData.getAllobjects()),
                Arrays.stream(modelingData.getTasks())
                        .map(e -> e.getPowerSystem()))
                .filter(e -> e.getIdenty().equals(key))
                .findFirst();

        if (felement.isEmpty()) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("обновляемый объект не найден."), String.class);
        }

        Optional<IComponentManagement> res;
        try {
            res = felement
                    .map(e -> {
                        var v = switch (e.getComponentType()) {
                            case STORAGE -> ((EnergyStorage) e).getData();
                            case GENERATOR -> ((Generation) e).getData();
                            case DISTRIBUTOR -> ((EnergyDistributor) e).getData();
                            case MAINSUBSTATION -> ((MainSubstationPowerSystem) e).getData();
                            case CONSUMER -> throw new IllegalArgumentException("неверный тип обновляемого объекта1");
                            case GREEGENERATOR -> ((GreenGeneration) e).getData();
                            default -> throw new IllegalArgumentException("неверный тип обновляемого объекта");
                        };
                        return v;
                    });
        } catch (IllegalArgumentException ex) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("неверный запрашиваемый тип объекта."), String.class);
        }

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(res), IComponentIdentification.class);
    }
}
