/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.io.processor.result;

import edu.ie3.exceptions.EntityProcessorException;
import edu.ie3.io.factory.result.SystemParticipantResultFactory;
import edu.ie3.io.processor.EntityProcessor;
import edu.ie3.models.StandardUnits;
import edu.ie3.models.result.ResultEntity;
import edu.ie3.models.result.system.SystemParticipantResult;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Optional;
import javax.measure.Quantity;
import javax.measure.quantity.Power;

/**
 * 'De-serializer' for {@link SystemParticipantResult}s into a fieldName -> value representation to
 * allow for an easy processing into a database or file sink e.g. .csv It is important that the
 * units used in this class are equal to the units used {@link SystemParticipantResultFactory} to
 * prevent invalid interpretation of unit prefixes!
 *
 * @version 0.1
 * @since 31.01.20
 */
public class ResultEntityProcessor extends EntityProcessor<ResultEntity> {

  public ResultEntityProcessor(Class<? extends ResultEntity> registeredClass) {
    super(registeredClass);
  }

  @Override
  protected Optional<LinkedHashMap<String, String>> processEntity(ResultEntity entity) {

    Optional<LinkedHashMap<String, String>> resultMapOpt;

    try {
      LinkedHashMap<String, String> resultMap = new LinkedHashMap<>();
      for (String fieldName : headerElements) {
        Method method = fieldNameToMethod.get(fieldName);
        Optional<Object> methodReturnObjectOpt = Optional.ofNullable(method.invoke(entity));

        if (methodReturnObjectOpt.isPresent()) {
          resultMap.put(
              fieldName, processMethodResult(methodReturnObjectOpt.get(), method, fieldName));
        } else {
          resultMap.put(fieldName, "");
        }
      }
      resultMapOpt = Optional.of(resultMap);
    } catch (Exception e) {
      log.error("Error during entity processing in SystemParticipantResultProcessor:", e);
      resultMapOpt = Optional.empty();
    }
    return resultMapOpt;
  }

  @Override
  protected Optional<String> handleModelProcessorSpecificQuantity(
      Quantity<?> quantity, String fieldName) {
    Optional<String> normalizedQuantityValue = Optional.empty();
    switch (fieldName) {
      case "p":
        normalizedQuantityValue =
            quantityValToOptionalString(
                quantity.asType(Power.class).to(StandardUnits.ACTIVE_POWER_OUT));
        break;
      case "q":
        normalizedQuantityValue =
            quantityValToOptionalString(
                quantity.asType(Power.class).to(StandardUnits.REACTIVE_POWER_OUT));
        break;
      default:
        log.error(
            "Cannot process quantity {} for field with name {} in result model processing!",
            quantity,
            fieldName);
        break;
    }
    return normalizedQuantityValue;
  }

  private String processMethodResult(Object methodReturnObject, Method method, String fieldName) {

    StringBuilder resultStringBuilder = new StringBuilder();

    switch (method.getReturnType().getSimpleName()) {
        // primitives (Boolean, Character, Byte, Short, Integer, Long, Float, Double, String,
      case "UUID":
      case "boolean":
      case "int":
        resultStringBuilder.append(methodReturnObject.toString());
        break;
      case "Quantity":
        resultStringBuilder.append(
            handleQuantity((Quantity<?>) methodReturnObject, fieldName)
                .orElseThrow(
                    () ->
                        new EntityProcessorException(
                            "Unable to process quantity value for attribute '"
                                + fieldName
                                + "' in system participant result model "
                                + getRegisteredClass().getSimpleName()
                                + ".class.")));
        break;
      case "ZonedDateTime":
        resultStringBuilder.append(processZonedDateTime((ZonedDateTime) methodReturnObject));
        break;
      default:
        throw new EntityProcessorException(
            "Unable to process value for attribute/field '"
                + fieldName
                + "' and method return type '"
                + method.getReturnType().getSimpleName()
                + "' for method with name '"
                + method.getName()
                + "' in system participant result model "
                + getRegisteredClass().getSimpleName()
                + ".class.");
    }

    return resultStringBuilder.toString();
  }
}
