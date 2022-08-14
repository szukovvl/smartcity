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
import re.smartcity.common.utils.Helpers;
import re.smartcity.common.utils.Interpolation;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.SupportedGenerations;
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.energynet.component.GreenGeneration;
import re.smartcity.modeling.ModelingData;
import re.smartcity.stand.SerialCommand;
import re.smartcity.stand.SerialElementAddresses;
import re.smartcity.stand.SerialPackageTypes;
import re.smartcity.stand.StandService;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.concurrent.Executors;

import static re.smartcity.common.resources.AppConstant.CALIBRATION_DELAY_LINE;

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

    @Autowired
    private ModelingData modelingData;

    private Mono<ServerResponse> internalSaveCfg() {

        return commonStorage.putData(SunConfiguration.key,
                new SimpleSunData(
                        sunStatusData.getPower(),
                        sunStatusData.getForecast(),
                        sunStatusData.isUseforecast()),
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

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sunStatusData);
    }

    public Mono<ServerResponse> sunOn(ServerRequest ignoredRq) {

        sunStatusData.setOn(true);
        standService.pushSerialCommand(new SerialCommand(SerialElementAddresses.SUN_SIMULATOR,
                SerialPackageTypes.SET_BRIGHTNESS_SUN_SIMULATOR, sunStatusData.getPower()));

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sunStatusData);
    }

    public Mono<ServerResponse> getStatus(ServerRequest ignoredRq) {
        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sunStatusData);
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

    public Mono<ServerResponse> forecastUpdate(ServerRequest rq) {

        return rq.bodyToMono(SimpleSunData.class)
                .flatMap(body -> {
                    try {
                        SimpleSunData.validate(body);
                        if (body.getForecast() != null) {
                            body.getForecast().setData(Helpers.checkForecastBounds(body.getForecast().getData()));
                        }
                    }
                    catch (Exception ex) {
                        ServerResponse
                                .status(HttpStatus.NOT_IMPLEMENTED)
                                .header("Content-Language", "ru")
                                .contentType(MediaType.TEXT_PLAIN)
                                .body(Mono.just(ex.getMessage()), String.class);
                    }
                    sunStatusData.setForecast(body.getForecast());
                    sunStatusData.setUseforecast(body.isUseforecast());

                    return internalSaveCfg();
                });
    }

    public Mono<ServerResponse> interpolate(ServerRequest ignoredRq) {

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Interpolation.interpolate(sunStatusData.getForecast().getData(), 100.0));
    }

    public Mono<ServerResponse> calibrateAll(ServerRequest ignoredRq) {

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean save_ison = sunStatusData.isOn();

            // 1. выключить осветитель
            sunStatusData.setOn(false);
            standService.pushSerialCommand(new SerialCommand(SerialElementAddresses.SUN_SIMULATOR,
                    SerialPackageTypes.SET_BRIGHTNESS_SUN_SIMULATOR, 0));
            try {
                Thread.sleep(CALIBRATION_DELAY_LINE);
            }
            catch (InterruptedException ex) {
                return;
            }

            // 2. команда калибровки в широком вещании
            standService.pushSerialCommand(new SerialCommand(SerialPackageTypes.SOLAR_CELL_CALIBRATION));
            try {
                Thread.sleep(CALIBRATION_DELAY_LINE);
            }
            catch (InterruptedException ex) {
                return;
            }

            // 3. Вернуть обратно осветитель
            if (save_ison) {
                sunStatusData.setOn(true);
                standService.pushSerialCommand(new SerialCommand(SerialElementAddresses.SUN_SIMULATOR,
                        SerialPackageTypes.SET_BRIGHTNESS_SUN_SIMULATOR, sunStatusData.getPower()));
            }
        });

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sunStatusData);
    }

    public Mono<ServerResponse> calibrate(ServerRequest rq) {

        byte v;
        try {
            v = Byte.parseByte(rq.pathVariable("addr"));
            IComponentIdentification item = Arrays.stream(modelingData.getAllobjects())
                    .filter(e -> e.getDevaddr() == v)
                    .findFirst()
                    .get();
            if (item.getComponentType() != SupportedTypes.GREEGENERATOR ||
                    ((GreenGeneration) item).getData().getGeneration_type() != SupportedGenerations.SOLAR)
            {
                throw new IllegalArgumentException("неверный тип элемента");
            }
        }
        catch (IllegalArgumentException e) {
            return ServerResponse
                    .status(HttpStatus.NOT_IMPLEMENTED)
                    .header("Content-Language", "ru")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(Mono.just("калибровка: неверный неверный адрес элемента"), String.class);
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            boolean save_ison = sunStatusData.isOn();

            // 1. выключить осветитель
            sunStatusData.setOn(false);
            standService.pushSerialCommand(new SerialCommand(SerialElementAddresses.SUN_SIMULATOR,
                    SerialPackageTypes.SET_BRIGHTNESS_SUN_SIMULATOR, 0));
            try {
                Thread.sleep(CALIBRATION_DELAY_LINE);
            }
            catch (InterruptedException ex) {
                return;
            }

            // 2. команда калибровки элемента
            standService.pushSerialCommand(new SerialCommand(v, SerialPackageTypes.SOLAR_CELL_CALIBRATION));
            try {
                Thread.sleep(CALIBRATION_DELAY_LINE);
            }
            catch (InterruptedException ex) {
                return;
            }

            // 3. Вернуть обратно осветитель
            if (save_ison) {
                sunStatusData.setOn(true);
                standService.pushSerialCommand(new SerialCommand(SerialElementAddresses.SUN_SIMULATOR,
                        SerialPackageTypes.SET_BRIGHTNESS_SUN_SIMULATOR, sunStatusData.getPower()));
            }
        });

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(sunStatusData);
    }
}
