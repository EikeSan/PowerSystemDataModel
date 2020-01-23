/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.models.input.thermal;

import java.util.UUID;

/** Common properties to all thermal storage devices */
public abstract class ThermalStorageInput extends ThermalUnitInput {
  /**
   * @param uuid Unique identifier of a certain thermal storage input model
   * @param id Identifier of the thermal unit
   * @param bus Thermal bus, a thermal unit is connected to
   */
  ThermalStorageInput(UUID uuid, String id, ThermalBusInput bus) {
    super(uuid, id, bus);
  }
}
