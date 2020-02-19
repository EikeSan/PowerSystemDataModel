/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.io.factory.input;

import edu.ie3.models.OperationTime;
import edu.ie3.models.input.OperatorInput;
import edu.ie3.models.input.thermal.ThermalBusInput;
import java.util.UUID;

public class ThermalBusInputFactory
    extends AssetInputEntityFactory<ThermalBusInput, AssetInputEntityData> {
  public ThermalBusInputFactory() {
    super(ThermalBusInput.class);
  }

  @Override
  protected String[] getAdditionalFields() {
    return new String[0];
  }

  @Override
  protected ThermalBusInput buildModel(
      AssetInputEntityData data,
      UUID uuid,
      String id,
      OperatorInput operatorInput,
      OperationTime operationTime) {
    return new ThermalBusInput(uuid, operationTime, operatorInput, id);
  }

  @Override
  protected ThermalBusInput buildModel(AssetInputEntityData data, UUID uuid, String id) {
    return new ThermalBusInput(uuid, id);
  }
}
