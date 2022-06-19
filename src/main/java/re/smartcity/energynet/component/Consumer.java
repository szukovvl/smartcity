package re.smartcity.energynet.component;

import re.smartcity.common.data.Forecast;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;

import java.time.LocalTime;

public class Consumer implements IComponentIdentification {

    private String identy; // уникальный идентификатор
    private SupportedTypes componenttype = SupportedTypes.CONSUMER; // тип компонента

    // общие характеристики
    private Forecast forecast; // прогноз
    private boolean useforecast; // задействовать прогноз
    private Double energy; // проектная мощность в МВт
    private boolean isactive; // активность
    private Double cost; // тариф (?)

    // частные характеристики
    private SupportedConsumers consumertype; // тип потребителя
    private Object[] energyline; // энерговвод
    private Object generation; // собственная генерация
    private SupportedZoneTariffication tariffication; // примененная тарификация

    // мгновенные значения (instant values / instantaneous values)
    private LocalTime timestamp; // метка времени примененных значений
    private Double currentcost; // действующая стоимость
    private Double currentpower; // мгновенное значения мощности потребления в МВт
    private Double current_greenenergy; // значение мощности, приходящейся на зеленную энергетику (?)
    private SupportedLoadIndicators loadindicator; // индикатор нагрузки
    private SupportedAppliedZone appliedZone; // действующая зона тарифа при определении стоимости

    // значения с накоплением для текущей временной метки
    // (* для вычисления используются предыдущие мгновенные значения)
    private Double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private Double greenenergy_meter; // потребленная электроэнергия с накоплением, приходящая на зеленную энергетику
    private Double topay; // стоимость потребленной электроэнергии в руб.

    public Consumer() { }

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion

    public static Consumer create(String identy) {
        if (identy == null || identy.trim().isEmpty()) {
            throw new IllegalArgumentException(Messages.ER_0);
        }
        Consumer res = new Consumer();
        res.identy = identy;
        return res;
    }
}
