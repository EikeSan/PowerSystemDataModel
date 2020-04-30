/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.factory.timeseries;

import edu.ie3.datamodel.io.factory.EntityData;
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue;
import edu.ie3.datamodel.models.value.Value;
import java.util.Map;

public class TimeBasedEntryData<V extends Value> extends EntityData {
  private final Class<V> valueClass;

  /**
   * Creates a new TimeBasedEntryData object
   *
   * @param fieldsToAttributes attribute map: field name -> value
   * @param valueClass class of the value of the TimeBasedValue to be created with this data
   */
  public TimeBasedEntryData(Map<String, String> fieldsToAttributes, Class<V> valueClass) {
    super(fieldsToAttributes, TimeBasedValue.class);
    this.valueClass = valueClass;
  }

  public Class<? extends Value> getValueClass() {
    return valueClass;
  }
}
