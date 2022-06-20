package re.smartcity.energynet.component;

import re.smartcity.energynet.*;

import java.time.LocalTime;

public class EnergyStorage {

    // одинаковые для всех
    private String identy; // уникальный идентификатор
    private SupportedTypes componenttype; // тип компонента

    // общие характеристики
    private Double energy; // проектная мощность в МВт (или текущаа генерация ?)
    private boolean isactive; // активность
    private Double cost; // тариф (?)

    // частные характеристики
    private Object energyline; // энерговвод (?)
    private Double performance; // показатель эффективности системы хранения
    private Double peckertexponent; // экспонента Пекерта
    private Integer outpower; // граница нормального значения мощности, отдаваемой потребителю, в процентах
    private boolean overload_enabled; // разрешение превышения установленного параметра границы нормального значения отдаваемой мощности
    private Integer maxdischarge; // максимальная разрядка хранилища, в процентах
    private Integer undercharging; // недозарядка, когда устройство может быть вновь использовано
    private Integer criticalload; // критическое значение нагрузки на хранилище
    private Integer blackouttime; // время в секундах, прежде чем произойдет отключение хранилища
    private EnergyStorage_UsageBehaviors usagebehavior; // поведение хранилища при использовании
    private EnergyStorage_ChargeBehaviors chargebehavior; // поведение хранилища при восстановлении
    private EnergyStorage_States initstate; // начальное состояние перед началом игрового процесса

    // ??? отнести к мгновенным значениям ???
    private double currentPower; // текущее значение остатка мощности

    // мгновенные значения (instant values / instantaneous values)
    private LocalTime timestamp; // метка времени примененных значений
    private Double currentcost; // действующая стоимость
    private Double currentpower; // мгновенное значения мощности потребления в МВт
    private SupportedLoadIndicators loadindicator; // индикатор нагрузки
    private EnergyStorage_States state; // текущее состояние хранилища
    private Double chargepower; // потребление энергии на зарядку

    // значения с накоплением для текущей временной метки
    // (* для вычисления используются предыдущие мгновенные значения)
    private Double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private Double topay; // стоимость потребленной электроэнергии в руб.
    private Double chargepowermeter; // потребленная электроэнергия на зарядку с накоплением в кВт*ч.

}
