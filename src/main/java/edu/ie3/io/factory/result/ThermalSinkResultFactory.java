/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.io.factory.result;

import edu.ie3.io.factory.SimpleEntityData;
import edu.ie3.io.factory.SimpleEntityFactory;
import edu.ie3.models.StandardUnits;
import edu.ie3.models.result.ThermalSinkResult;
import edu.ie3.util.TimeTools;
import edu.ie3.util.quantities.interfaces.HeatCapacity;
import java.time.ZonedDateTime;
import java.util.*;
import javax.measure.Quantity;

public class ThermalSinkResultFactory extends SimpleEntityFactory<ThermalSinkResult> {
  private static final String entityUuid = "uuid";
  private static final String timestamp = "timestamp";
  private static final String inputModel = "inputModel";
  private static final String qDemand = "qDemand";

  public ThermalSinkResultFactory() {
    super(ThermalSinkResult.class);
  }

  @Override
  protected List<Set<String>> getFields(SimpleEntityData simpleEntityData) {
    Set<String> minConstructorParams = newSet(timestamp, inputModel, qDemand);
    Set<String> optionalFields = expandSet(minConstructorParams, entityUuid);

    return Arrays.asList(minConstructorParams, optionalFields);
  }

  @Override
  protected ThermalSinkResult buildModel(SimpleEntityData data) {

    ZonedDateTime zdtTimestamp = TimeTools.toZonedDateTime(data.get(timestamp));
    UUID inputModelUuid = data.getUUID(inputModel);
    Quantity<HeatCapacity> qDemandVal =
        data.get(qDemand, StandardUnits.HEAT_CAPACITY); // todo CK ensure that the unit is correct
    Optional<UUID> uuidOpt =
        data.containsKey(entityUuid) ? Optional.of(data.getUUID(entityUuid)) : Optional.empty();

    return uuidOpt
        .map(uuid -> new ThermalSinkResult(uuid, zdtTimestamp, inputModelUuid, qDemandVal))
        .orElseGet(() -> new ThermalSinkResult(zdtTimestamp, inputModelUuid, qDemandVal));
  }
}
