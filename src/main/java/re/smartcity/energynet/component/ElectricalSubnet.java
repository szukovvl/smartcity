package re.smartcity.energynet.component;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.energynet.*;

import java.time.LocalTime;

@Table("component")
public class ElectricalSubnet implements IComponentIdentification {

    // одинаковые для всех
    @Id
    private String identy; // уникальный идентификатор
    private SupportedTypes componenttype = SupportedTypes.LINE; // тип компонента

    // общие характеристики
    private double energy; // максимальная мощность в МВт (устанавливается при первой инициализации)
    private boolean isactive = false; // активность

    // частные характеристики
    private double lossfactor; // потери в сети (устанавливается при первой инициализации)
    private IComponentIdentification[] components; // компоненты электросети, подключенные к данной подсети
                                 // подключаются объекты только одного типа - либо генерация, либо потребление
    private double highload = 0.8; // значение в процента от мощности, высокая нагрузка (80%)
    private double criticalload = 0.95; // значение в процентах от мощности, критическая нагрузка (95)
    private int blackouttime = 300; // время в секундах, прежде чем произойдет отключение
    private double tariff; // (? котловой) (во время работы - задают администратор)
                           // перенести в раздел настроек администратора

    //region динамические характеристики
    //region мгновенные значения (instant values / instantaneous values)
    private LocalTime timestamp; // метка времени примененных значений
    private double currentcost; // действующая стоимость
    private double in_power; // мгновенное значения мощности на входе
    private double in_greenenergy; // значение мощности на входе, приходящейся на зеленную энергетику (?)
    private double out_power; // мгновенное значения мощности на выходе
    private double out_greenenergy; // значение мощности на выходе, приходящейся на зеленную энергетику (?)
    private double losspower; // мгновенное значение мощности потерь в сети
    private SupportedLoadIndicators loadindicator; // индикатор нагрузки
    //endregion

    //region значения с накоплением для текущей временной метки
    // (* для вычисления используются предыдущие мгновенные значения)
    private double in_powermeter; // мощность в кВт*ч на входе.
    private double in_greenpower_meter; // мощность, приходящая на зеленную энергетику, на входе
    private double out_powermeter; // мощность в кВт*ч, на выходе.
    private double out_greenpower_meter; // мощность, приходящая на зеленную энергетику, на выходе
    private double loss_power_meter; // потери с накоплением
    private double losscosts; // затраты на потери электроэнергии в руб.
    private double topay; // оплата за потребленную энергию
    //endregion
    //endregion

    public ElectricalSubnet() { }

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion

}
