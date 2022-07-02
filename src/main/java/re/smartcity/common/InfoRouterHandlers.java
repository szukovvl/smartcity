package re.smartcity.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import re.smartcity.common.data.exchange.MainInfoBlock;
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

    private final Logger logger = LoggerFactory.getLogger(InfoRouterHandlers.class);

    @Autowired
    private WindStatusData windStatus;

    @Autowired
    private SunStatusData sunStatus;

    @Autowired
    private StandStatusData standStatus;

    @Autowired
    private ModelingData modelingData;

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

    public Mono<ServerResponse> commonInfo(ServerRequest rq) {

        MainInfoBlock res = new MainInfoBlock();
        res.setSunData(sunStatus);
        res.setWindData(windStatus);
        res.setStandStatus(standStatus);

        Map<String, Integer> itemcounts = new HashMap<String, Integer>();
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
}
