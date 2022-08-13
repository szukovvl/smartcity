package re.smartcity.sun;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.CommonStorage;
import re.smartcity.common.ForecastRouterHandler;
import re.smartcity.common.ForecastStorage;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.data.ForecastTypes;
import re.smartcity.common.data.exchange.SimpleSunData;
import re.smartcity.common.data.exchange.SunConfiguration;
import re.smartcity.config.sockets.CommonEventTypes;
import re.smartcity.config.sockets.CommonSocketHandler;
import re.smartcity.stand.SerialCommand;
import re.smartcity.stand.SerialElementAddresses;
import re.smartcity.stand.SerialPackageTypes;
import re.smartcity.stand.StandService;
import re.smartcity.wind.*;
import reactor.core.publisher.Mono;

@Component
public class SunRouterHandlers {

    // private final Logger logger = LoggerFactory.getLogger(SunRouterHandlers.class);

    @Autowired
    private SunStatusData sunStatusData;

    @Autowired
    private ForecastStorage storage;

    @Autowired
    private ForecastRouterHandler forecastHandler;

    @Autowired
    private StandService standService;

    @Autowired
    private CommonStorage commonStorage;

    private Mono<ServerResponse> internalSaveCfg() {

        return commonStorage.putData(SunConfiguration.key,
                new SimpleSunData(sunStatusData.getPower()),
                SunConfiguration.class)
                .flatMap(count -> ServerResponse
                        .ok()
                        .header("Content-Language", "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(sunStatusData))
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()));
    }

    public Mono<ServerResponse> setSunPower(ServerRequest rq) {
        int v;
        try {
            v = Integer.parseInt(rq.pathVariable("value"));
        }
        catch (NumberFormatException e) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("освещенность: неверный параметр"), String.class);
        }

        sunStatusData.setPower(v);
        standService.pushSerialCommand(new SerialCommand(SerialElementAddresses.SUN_SIMULATOR,
                SerialPackageTypes.SET_BRIGHTNESS_SUN_SIMULATOR, sunStatusData.isOn() ? v : 0));

        return internalSaveCfg();
    }

    public Mono<ServerResponse> sunOff(ServerRequest ignoredRq) {

        sunStatusData.setOn(false);
        standService.pushSerialCommand(new SerialCommand(SerialElementAddresses.SUN_SIMULATOR,
                SerialPackageTypes.SET_BRIGHTNESS_SUN_SIMULATOR, 0));

        return internalSaveCfg();
    }

    public Mono<ServerResponse> sunOn(ServerRequest ignoredRq) {

        sunStatusData.setOn(true);
        standService.pushSerialCommand(new SerialCommand(SerialElementAddresses.SUN_SIMULATOR,
                SerialPackageTypes.SET_BRIGHTNESS_SUN_SIMULATOR, sunStatusData.getPower()));

        return internalSaveCfg();
    }

    public Mono<ServerResponse> getStatus(ServerRequest ignoredRq) {
        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(sunStatusData), WindStatusData.class);
    }

    // работа с прогнозом
    public Mono<ServerResponse> forecastAll(ServerRequest ignoredRq) {
        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(storage.findAll(ForecastTypes.SUN), Forecast.class);
    }

    public Mono<ServerResponse> forecastCreate(ServerRequest rq) {
        return forecastHandler.forecastCreate(rq, ForecastTypes.SUN);
    }
}
