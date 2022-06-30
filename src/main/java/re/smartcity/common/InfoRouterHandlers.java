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
import re.smartcity.energynet.SupportedTypes;
import re.smartcity.modeling.ModelingData;
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

    public Mono<ServerResponse> commonInfo(ServerRequest rq) {
        logger.info("--> информирование: общее состояние");

        MainInfoBlock res = new MainInfoBlock();
        res.setSunData(sunStatus);
        res.setWindData(windStatus);
        res.setStandStatus(standStatus);

        IComponentIdentification[] all = modelingData.getAllobjects();
        if (all.length != 0) {
            Map<SupportedTypes, Integer> itemcounts = new HashMap<SupportedTypes, Integer>();
            for (SupportedTypes d : SupportedTypes.values()) {
                int c = (int) Arrays.stream(all).filter(e -> e.getComponentType() == d).count();
                if (c != 0) {
                    itemcounts.put(d, c);
                }
            }
            if (itemcounts.keySet().size() != 0) {
                res.setElements(itemcounts);
            }
        }

        return ServerResponse
                .ok()
                .header("Content-Language", "ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(res), MainInfoBlock.class);
    }
}
