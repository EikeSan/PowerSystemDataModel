/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.io.factory.input.participant;

import edu.ie3.io.factory.input.AssetInputEntityFactory;
import edu.ie3.models.OperationTime;
import edu.ie3.models.input.NodeInput;
import edu.ie3.models.input.OperatorInput;
import edu.ie3.models.input.system.SystemParticipantInput;
import java.util.*;

/**
 * Abstract factory class for creating {@link SystemParticipantInput} entities with {@link
 * SystemParticipantEntityData} data objects.
 *
 * @param <T> Type of entity that this factory can create. Must be a subclass of {@link
 *     SystemParticipantInput}
 * @param <D> Type of data class that is required for entity creation
 * @version 0.1
 * @since 28.01.20
 */
abstract class SystemParticipantInputEntityFactory<
        T extends SystemParticipantInput, D extends SystemParticipantEntityData>
    extends AssetInputEntityFactory<T, D> {

  private static final String Q_CHARACTERISTICS = "qcharacteristics";

  public SystemParticipantInputEntityFactory(Class<? extends T>... allowedClasses) {
    super(allowedClasses);
  }

  @Override
  protected List<Set<String>> getFields(D data) {
    List<Set<String>> fields = super.getFields(data);
    for (Set<String> set : fields) set.add(Q_CHARACTERISTICS);

    return fields;
  }

  @Override
  protected T buildModel(
      D data, UUID uuid, String id, OperatorInput operatorInput, OperationTime operationTime) {
    NodeInput node = data.getNode();
    String qCharacteristics = data.getField(Q_CHARACTERISTICS);

    return buildModel(data, uuid, id, node, qCharacteristics, operatorInput, operationTime);
  }

  /**
   * Creates SystemParticipantInput entity with given parameters
   *
   * @param data entity data
   * @param uuid UUID of the input entity
   * @param id ID
   * @param node Node that the asset is connected to
   * @param qCharacteristics Description of a reactive power characteristic
   * @param operatorInput Operator of the asset
   * @param operationTime time in which the entity is operated
   * @return newly created asset object
   */
  protected abstract T buildModel(
      D data,
      UUID uuid,
      String id,
      NodeInput node,
      String qCharacteristics,
      OperatorInput operatorInput,
      OperationTime operationTime);
}