package re.smartcity.energynet.component;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.common.data.Forecast;
import re.smartcity.energynet.*;

import java.time.LocalTime;

@Table("component")
public class EnergyDistributor implements IComponentIdentification {

    // одинаковые для всех
    @Id
    private String identy; // уникальный идентификатор
    private SupportedTypes componenttype = SupportedTypes.DISTRIBUTOR; // тип компонента

    // общие характеристики
    private boolean isactive = false; // активность

    // частные характеристики
    private ElectricalSubnet energyinput; // подсеть ввода энергии
    private ElectricalSubnet[] outputs; // подсети потребителей
    private ElectricalSubnet generation; // собственная генерация

    //region динамические характеристики
    //region мгновенные значения (instant values / instantaneous values)
    private LocalTime timestamp; // метка времени примененных значений
    private double currentpower; // мгновенное значения мощности потребления в МВт
    private double current_greenenergy; // значение мощности, приходящейся на зеленную энергетику (?)
    private double losspower; // потери в сетях
    //endregion

    //region значения с накоплением для текущей временной метки
    // (* для вычисления используются предыдущие мгновенные значения)
    private double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private double greenenergy_meter; // потребленная электроэнергия с накоплением, приходящая на зеленную энергетику
    private double loss_power_meter; // потери с накоплением
    private double losscosts; // затраты на потери электроэнергии в руб.
    private double topay; // общая стоимость энергии
    //endregion
    //endregion

    public EnergyDistributor() { }

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion

}
