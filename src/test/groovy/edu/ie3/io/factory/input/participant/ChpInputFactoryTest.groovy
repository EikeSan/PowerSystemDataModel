package edu.ie3.io.factory.input.participant

import edu.ie3.models.input.NodeInput
import edu.ie3.models.input.OperatorInput
import edu.ie3.models.input.system.ChpInput
import edu.ie3.models.input.system.type.ChpTypeInput
import edu.ie3.models.input.thermal.ThermalBusInput
import edu.ie3.models.input.thermal.ThermalStorageInput
import edu.ie3.test.helper.FactoryTestHelper
import spock.lang.Specification

import java.time.ZonedDateTime

class ChpInputFactoryTest extends Specification implements FactoryTestHelper {
    def "A ChpInputFactory should contain exactly the expected class for parsing"() {
        given:
        def inputFactory = new ChpInputFactory()
        def expectedClasses = [ChpInput]

        expect:
        inputFactory.classes() == Arrays.asList(expectedClasses.toArray())
    }

    def "A ChpInputFactory should parse a valid ChpInput correctly"() {
        given: "a system participant input type factory and model data"
        def inputFactory = new ChpInputFactory()
        Map<String, String> parameter = [
                "uuid"            : "91ec3bcf-1777-4d38-af67-0bf7c9fa73c7",
                "operatesfrom"    : "2019-01-01T00:00:00+01:00[Europe/Berlin]",
                "operatesuntil"   : "2019-12-31T23:59:00+01:00[Europe/Berlin]",
                "id"              : "TestID",
                "qcharacteristics": "cosphi_fixed:1",
                "marketreaction"  : "true"
        ]
        def inputClass = ChpInput
        def nodeInput = Mock(NodeInput)
        def operatorInput = Mock(OperatorInput)
        def typeInput = Mock(ChpTypeInput)
        def thermalBusInput = Mock(ThermalBusInput)
        def thermalStorageInput = Mock(ThermalStorageInput)

        when:
        Optional<ChpInput> input = inputFactory.getEntity(
                new ChpInputEntityData(parameter, operatorInput, nodeInput, typeInput, thermalBusInput, thermalStorageInput))

        then:
        input.present
        input.get().getClass() == inputClass
        ((ChpInput) input.get()).with {
            assert uuid == UUID.fromString(parameter["uuid"])
            assert operationTime.startDate.present
            assert operationTime.startDate.get() == ZonedDateTime.parse(parameter["operatesfrom"])
            assert operationTime.endDate.present
            assert operationTime.endDate.get() == ZonedDateTime.parse(parameter["operatesuntil"])
            assert operator == operatorInput
            assert id == parameter["id"]
            assert node == nodeInput
            assert qCharacteristics == parameter["qcharacteristics"]
            assert type == typeInput
            assert marketReaction
        }
    }
}
