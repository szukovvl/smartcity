package re.smartcity.energynet.component;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.data.Forecast;
import re.smartcity.common.resources.Messages;
import re.smartcity.energynet.*;

import java.time.LocalTime;

@Table("component")
public class Consumer implements IComponentIdentification {

    @Id
    private String identy; // уникальный идентификатор
    private SupportedTypes componenttype = SupportedTypes.CONSUMER; // тип компонента

    // общие характеристики
    private Forecast forecast; // прогноз
    private boolean useforecast = false; // задействовать прогноз
    private double energy; // максимальная мощность в МВт
    private boolean isactive = false; // активность

    // частные характеристики
    private SupportedConsumers consumertype; // категория надежности электроснабжения
    private ElectricalSubnet[] energyline; // энерговвод
    private ElectricalSubnet generation; // собственная генерация

    //region динамические характеристики
    //region мгновенные значения (instant values / instantaneous values)
    private LocalTime timestamp; // метка времени примененных значений
    private double currentcost; // действующая стоимость
    private double currentpower; // мгновенное значения мощности потребления в МВт
    private double current_greenenergy; // значение мощности, приходящейся на зеленную энергетику (?)
    private SupportedLoadIndicators loadindicator; // индикатор нагрузки
    private SupportedAppliedZone appliedZone; // действующая зона тарифа при определении стоимости
    //endregion

    //region значения с накоплением для текущей временной метки
    // (* для вычисления используются предыдущие мгновенные значения)
    private double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private double greenenergy_meter; // потребленная электроэнергия с накоплением, приходящая на зеленную энергетику
    private double topay; // стоимость потребленной электроэнергии в руб.
    //endregion
    //endregion

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
