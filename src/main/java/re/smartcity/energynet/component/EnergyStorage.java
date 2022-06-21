package re.smartcity.energynet.component;

import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.energynet.*;

import java.time.LocalTime;

@Table("component")
public class EnergyStorage implements IComponentIdentification {

    // одинаковые для всех
    private String identy; // уникальный идентификатор
    private SupportedTypes componenttype = SupportedTypes.STORAGE; // тип компонента

    // общие характеристики
    private double energy = 1.1; // емкость в мВт*ч
    private boolean isactive = false; // активность

    // частные характеристики
    private double performance = 0.88; // показатель эффективности системы хранения
    private double peckertexponent = 1.1; // экспонента Пекерта
    private double outpower = 0.5; // граница нормального значения мощности, отдаваемой потребителю, в процентах
    private boolean overload_enabled = false; // разрешение превышения установленного параметра границы нормального значения отдаваемой мощности
    private double maxdischarge = 0.2; // максимальная разрядка хранилища, в процентах
    private double undercharging = 0.7; // недозарядка, когда устройство может быть вновь использовано
    private double criticalload = 0.9; // критическое значение нагрузки на хранилище
    private int blackouttime = 90; // время в секундах, прежде чем произойдет отключение хранилища
    private double tariff; // тариф
    private double carbon = 798.9; // г/кВт*ч
    private GenerationUsageModes mode = GenerationUsageModes.RESERVE; // режим использования
    private EnergyStorage_ChargeBehaviors chargebehavior; // поведение хранилища при восстановлении
    private EnergyStorage_States initstate; // начальное состояние перед началом игрового процесса

    //region динамические характеристики
    //region мгновенные значения (instant values / instantaneous values)
    private LocalTime timestamp; // метка времени примененных значений
    private double currentcost; // действующая стоимость
    private double currentpower; // мгновенное значения мощности потребления в МВт
    private double chargecost; // стоимость энергии для зарядки хранилища
    private double chargepower; // потребление энергии на зарядку
    private double capacity; // остаточная емкость хранилища.
    private SupportedLoadIndicators loadindicator; // индикатор нагрузки
    private EnergyStorage_States state; // текущее состояние хранилища
    //endregion

    //region значения с накоплением для текущей временной метки
    // (* для вычисления используются предыдущие мгновенные значения)
    private double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private double topay; // стоимость потребленной электроэнергии в руб.
    private double chargepowermeter; // потребленная электроэнергия на зарядку с накоплением в кВт*ч.
    private double chargetopay; // стоимость потребленной электроэнергии в руб. на зарядку хранилища
    //endregion
    //endregion

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion

}
