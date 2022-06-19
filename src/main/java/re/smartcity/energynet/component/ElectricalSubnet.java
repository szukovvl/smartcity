package re.smartcity.energynet.component;

import re.smartcity.energynet.*;

import java.time.LocalTime;

public class ElectricalSubnet {

    // одинаковые для всех
    private String identy; // уникальный идентификатор
    private SupportedTypes componenttype; // тип компонента

    // общие характеристики
    private Double energy; // проектная мощность в МВт
    private boolean isactive; // активность

    // частные характеристики
    private Double lossfactor; // потери в сети
    private Object[] components; // компоненты электросети, подключенные к данной подсети
    private Integer highload; // значение в процента от мощности, высокая нагрузка
    private Integer criticalload; // значение в процентах от мощности, критическая нагрузка
    private Integer blackouttime; // время в секундах, прежде чем произойдет отключение

    // мгновенные значения (instant values / instantaneous values)
    private LocalTime timestamp; // метка времени примененных значений
    private Double currentcost; // действующая стоимость
    private Double in_power; // мгновенное значения мощности на входе
    private Double in_greenenergy; // значение мощности на входе, приходящейся на зеленную энергетику (?)
    private Double out_power; // мгновенное значения мощности на выходе
    private Double out_greenenergy; // значение мощности на выходе, приходящейся на зеленную энергетику (?)
    private Double losspower; // мгновенное значение мощности потерь в сети
    private SupportedLoadIndicators loadindicator; // индикатор нагрузки
    private SupportedAppliedZone appliedZone; // действующая зона тарифа при определении стоимости

    // значения с накоплением для текущей временной метки
    // (* для вычисления используются предыдущие мгновенные значения)
    private Double in_powermeter; // мощность в кВт*ч на входе.
    private Double in_greenpower_meter; // мощность, приходящая на зеленную энергетику, на входе
    private Double out_powermeter; // мощность в кВт*ч, на выходе.
    private Double out_greenpower_meter; // мощность, приходящая на зеленную энергетику, на выходе
    private Double loss_power_meter; // потери с накоплением
    private Double topay; // стоимость потребленной электроэнергии в руб.

}
