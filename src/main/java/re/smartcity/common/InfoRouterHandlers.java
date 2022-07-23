package re.smartcity.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.data.GameCriteria;
import re.smartcity.common.data.Tariffs;
import re.smartcity.common.data.exchange.GameCriteriaData;
import re.smartcity.common.data.exchange.MainInfoBlock;
import re.smartcity.common.data.exchange.TariffsData;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.SupportedConsumers;
import re.smartcity.energynet.SupportedGenerations;
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.energynet.component.Consumer;
import re.smartcity.energynet.component.GreenGeneration;
import re.smartcity.modeling.ModelingData;
import re.smartcity.modeling.TaskData;
import re.smartcity.stand.StandStatusData;
import re.smartcity.sun.SunStatusData;
import re.smartcity.wind.WindStatusData;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Component
public class InfoRouterHandlers {

    @Autowired
    private WindStatusData windStatus;

    @Autowired
    private SunStatusData sunStatus;

    @Autowired
    private StandStatusData standStatus;

    @Autowired
    private ModelingData modelingData;

    @Autowired
    private CommonStorage commonStorage;

    private Map<String, Integer> getConsumerCounts(Consumer[] items) {
        Map<String, Integer> res = new HashMap<>();
        for (SupportedConsumers d : SupportedConsumers.values()) {
            int c = (int) Arrays.stream(items).filter(e -> e.getData().getConsumertype() == d).count();
            if (c != 0) {
                res.put(d.name(), c);
            }
        }
        return res;
    }

    private Map<String, Integer> getGreenGenerationCounts(GreenGeneration[] items) {
        Map<String, Integer> res = new HashMap<>();
        for (SupportedGenerations d : SupportedGenerations.values()) {
            int c = (int) Arrays.stream(items).filter(e -> e.getData().getGeneration_type() == d).count();
            if (c != 0) {
                res.put(d.name(), c);
            }
        }
        return res;
    }

    public Mono<ServerResponse> commonInfo(ServerRequest ignoredRq) {

        MainInfoBlock res = new MainInfoBlock();
        res.setSunData(sunStatus);
        res.setWindData(windStatus);
        res.setStandStatus(standStatus);

        Map<String, Integer> itemcounts = new HashMap<>();
        TaskData[] tasks = modelingData.getTasks();
        if (tasks != null && tasks.length != 0) {
            itemcounts.put(SupportedTypes.MAINSUBSTATION.name(), modelingData.getTasks().length);
        }
        IComponentIdentification[] all = modelingData.getAllobjects();
        if (all.length != 0) {
            Map<String, Integer> items = getConsumerCounts(
                    Arrays.stream(all)
                            .filter(e -> e.getComponentType() == SupportedTypes.CONSUMER)
                            .toArray(Consumer[]::new));
            if (items.keySet().size() != 0) {
                itemcounts.putAll(items);
            }

            items = getGreenGenerationCounts(
                    Arrays.stream(all)
                            .filter(e -> e.getComponentType() == SupportedTypes.GREEGENERATOR)
                            .toArray(GreenGeneration[]::new));
            if (items.keySet().size() != 0) {
                itemcounts.putAll(items);
            }

            int c = (int) Arrays.stream(all).filter(e -> e.getComponentType() == SupportedTypes.GENERATOR).count();
            if (c != 0) {
                itemcounts.put(SupportedTypes.GENERATOR.name(), c);
            }

            c = (int) Arrays.stream(all).filter(e -> e.getComponentType() == SupportedTypes.STORAGE).count();
            if (c != 0) {
                itemcounts.put(SupportedTypes.STORAGE.name(), c);
            }

            c = (int) Arrays.stream(all).filter(e -> e.getComponentType() == SupportedTypes.DISTRIBUTOR).count();
            if (c != 0) {
                itemcounts.put(SupportedTypes.DISTRIBUTOR.name(), c);
            }
        }
        if (itemcounts.keySet().size() != 0) {
            res.setElements(itemcounts);
        }

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(res), MainInfoBlock.class);
    }

    public Mono<ServerResponse> getTariffs(ServerRequest ignoredRq) {
        return commonStorage.getAndCreate(Tariffs.key, Tariffs.class)
                .flatMap(e -> ServerResponse
                        .ok()
                        .header("Content-Language", "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(e.getData()))
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()));
    }

    public Mono<ServerResponse> putTariffs(ServerRequest rq) {
        return rq.bodyToMono(TariffsData.class)
                .flatMap(e -> commonStorage.putData(Tariffs.key, e, Tariffs.class))
                .flatMap(e -> {
                    if (e != 0) {
                        return ServerResponse
                                .ok()
                                .header("Content-Language", "ru")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(e);
                    }
                    return ServerResponse
                            .status(HttpStatus.NOT_FOUND)
                            .header("Content-Language", "ru")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(e);
                })
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()));
    }

    public Mono<ServerResponse> getCriteria(ServerRequest ignoredRq) {
        return commonStorage.getAndCreate(GameCriteria.key, GameCriteria.class)
                .flatMap(e -> ServerResponse
                        .ok()
                        .header("Content-Language", "ru")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(e.getData()))
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()));
    }

    public Mono<ServerResponse> putCriteria(ServerRequest rq) {
        return rq.bodyToMono(GameCriteriaData.class)
                .flatMap(e -> commonStorage.putData(GameCriteria.key, e, GameCriteria.class))
                .flatMap(e -> {
                    if (e != 0) {
                        return ServerResponse
                                .ok()
                                .header("Content-Language", "ru")
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(e);
                    }
                    return ServerResponse
                            .status(HttpStatus.NOT_FOUND)
                            .header("Content-Language", "ru")
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(e);
                })
                .onErrorResume(t -> ServerResponse
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(t.getMessage()));
    }
}
