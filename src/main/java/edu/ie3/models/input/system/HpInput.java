/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.models.input.system;

import edu.ie3.models.input.NodeInput;
import edu.ie3.models.input.OperatorInput;
import edu.ie3.models.input.system.type.HpTypeInput;
import edu.ie3.models.input.thermal.ThermalBusInput;
import edu.ie3.models.input.thermal.ThermalUnitInput;
import edu.ie3.util.interval.ClosedInterval;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Describes a heat pump */
public class HpInput extends SystemParticipantInput {
  /** Type of this heat pump, containing default values for heat pump of this kind */
  private HpTypeInput type;
  /** The thermal bus, this model is connected to */
  private ThermalBusInput thermalBus;

  /**
   * @param uuid of the input entity
   * @param operationInterval Empty for a non-operated asset, Interval of operation period else
   * @param operator of the asset
   * @param id of the asset
   * @param node the asset is connected to
   * @param thermalBus The thermal bus, this model is connected to
   * @param qCharacteristics Description of a reactive power characteristic
   * @param cosphi Power factor
   * @param type of HP
   */
  public HpInput(
      UUID uuid,
      Optional<ClosedInterval<ZonedDateTime>> operationInterval,
      OperatorInput operator,
      String id,
      NodeInput node,
      ThermalBusInput thermalBus,
      String qCharacteristics,
      Double cosphi,
      HpTypeInput type,
      ThermalUnitInput heatSink) {
    super(uuid, operationInterval, operator, id, node, qCharacteristics, cosphi);
    this.thermalBus = thermalBus;
    this.type = type;
  }

  /**
   * If both operatesFrom and operatesUntil are Empty, it is assumed that the asset is non-operated.
   *
   * @param uuid of the input entity
   * @param operatesFrom start of operation period, will be replaced by LocalDateTime.MIN if Empty
   * @param operatesUntil end of operation period, will be replaced by LocalDateTime.MAX if Empty
   * @param operator of the asset
   * @param id of the asset
   * @param node the asset is connected to
   * @param thermalBus The thermal bus, this model is connected to
   * @param qCharacteristics Description of a reactive power characteristic
   * @param cosphi Power factor
   * @param type of HP
   */
  public HpInput(
      UUID uuid,
      Optional<ZonedDateTime> operatesFrom,
      Optional<ZonedDateTime> operatesUntil,
      OperatorInput operator,
      String id,
      NodeInput node,
      ThermalBusInput thermalBus,
      String qCharacteristics,
      Double cosphi,
      HpTypeInput type,
      ThermalUnitInput heatSink) {
    super(uuid, operatesFrom, operatesUntil, operator, id, node, qCharacteristics, cosphi);
    this.thermalBus = thermalBus;
    this.type = type;
  }

  /**
   * Constructor for a non-operated asset
   *
   * @param uuid of the input entity
   * @param id of the asset
   * @param node the asset is connected to
   * @param thermalBus The thermal bus, this model is connected to
   * @param qCharacteristics Description of a reactive power characteristic
   * @param cosphi Power factor
   * @param type of HP
   */
  public HpInput(
      UUID uuid,
      String id,
      NodeInput node,
      ThermalBusInput thermalBus,
      String qCharacteristics,
      Double cosphi,
      HpTypeInput type,
      ThermalUnitInput heatSink) {
    super(uuid, id, node, qCharacteristics, cosphi);
    this.thermalBus = thermalBus;
    this.type = type;
  }

  public HpTypeInput getType() {
    return type;
  }

  public void setType(HpTypeInput type) {
    this.type = type;
  }

  public ThermalBusInput getThermalBus() {
    return thermalBus;
  }

  public void setThermalBus(ThermalBusInput thermalBus) {
    this.thermalBus = thermalBus;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    HpInput hpInput = (HpInput) o;
    return type.equals(hpInput.type) && thermalBus.equals(hpInput.thermalBus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), type, thermalBus);
  }
}
