package re.smartcity.energynet.component;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import re.smartcity.energynet.IComponentIdentification;
import re.smartcity.energynet.SupportedTypes;

import java.time.LocalTime;

@Table("component")
public class MainSubstationPowerSystem implements IComponentIdentification {

    // одинаковые для всех
    @Id
    private String identy; // уникальный идентификатор
    private SupportedTypes componenttype = SupportedTypes.MAINSUBSTATION; // тип компонента

    // общие характеристики
    private boolean isactive = false; // активность

    // частные характеристики
    private ElectricalSubnet[] inputs; // подсети ввода энергии только генерация
    private ElectricalSubnet[] outputs; // подсети потребителей только потребление
    private double external_energy; // внешний стабильный источник энергии
                                    // допустим от внешней энергосети сети
    private double tariff; // тариф на внешний источник энергии (ВИЭ)
    private double carbon = 684.75; // г/кВт*ч только для внешней энергии

    //region динамические характеристики
    //region мгновенные значения (instant values / instantaneous values)
    private LocalTime timestamp; // метка времени примененных значений
    private double currentpower; // мгновенное значения мощности потребления в МВт
    private double current_greenenergy; // значение мощности, приходящейся на зеленную энергетику (?)
    private double losspower; // потери в сетях
    private double extpower; // потребление из ВИЭ
    private double currentcarbon; // мгновенное значения загрязнения
    //endregion

    //region значения с накоплением для текущей временной метки
    // (* для вычисления используются предыдущие мгновенные значения)
    private double electricitymeter; // потребленная электроэнергия с накоплением в кВт*ч.
    private double greenenergy_meter; // потребленная электроэнергия с накоплением, приходящая на зеленную энергетику
    private double loss_power_meter; // потери с накоплением
    private double losscosts; // затраты на потери электроэнергии в руб.
    private double topay; // общая стоимость энергии
    private double extpower_meter; // потребленная электроэнергия с ВИЭ
    private double ext_topay; // оплата за ВИЭ
    private double totalcarbon; // итоговое значения загрязнения
    //endregion
    //endregion

    public MainSubstationPowerSystem() { }

    //region IComponentIdentification
    @Override
    public String getIdenty() { return this.identy; }

    @Override
    public SupportedTypes getComponentType() { return this.componenttype; }
    //endregion

}
