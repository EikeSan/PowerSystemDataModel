package edu.ie3.io.factory.input.participant

import edu.ie3.models.OperationTime
import edu.ie3.models.StandardUnits
import edu.ie3.models.input.NodeInput
import edu.ie3.models.input.system.LoadInput
import edu.ie3.test.helper.FactoryTestHelper
import spock.lang.Specification

class LoadInputFactoryTest extends Specification implements FactoryTestHelper {
    def "A LoadInputFactory should contain exactly the expected class for parsing"() {
        given:
        def inputFactory = new LoadInputFactory()
        def expectedClasses = [LoadInput]

        expect:
        inputFactory.classes() == Arrays.asList(expectedClasses.toArray())
    }

    def "A LoadInputFactory should parse a valid LoadInput correctly"() {
        given: "a system participant input type factory and model data"
        def inputFactory = new LoadInputFactory()
        Map<String, String> parameter = [
                "uuid"            : "91ec3bcf-1777-4d38-af67-0bf7c9fa73c7",
                "id"              : "TestID",
                "qcharacteristics": "cosphi_fixed:1",
                "dsm"             : "true",
                "econsannual"     : "3",
                "srated"          : "4",
                "cosphi"          : "5"
        ]
        def inputClass = LoadInput
        def nodeInput = Mock(NodeInput)

        when:
        Optional<LoadInput> input = inputFactory.getEntity(
                new SystemParticipantEntityData(parameter, inputClass, nodeInput))

        then:
        input.present
        input.get().getClass() == inputClass
        ((LoadInput) input.get()).with {
            assert uuid == UUID.fromString(parameter["uuid"])
            assert operationTime == OperationTime.notLimited()
            assert operator == null
            assert id == parameter["id"]
            assert node == nodeInput
            assert qCharacteristics == parameter["qcharacteristics"]
            assert dsm
            assert eConsAnnual == getQuant(parameter["econsannual"], StandardUnits.ENERGY_IN)
            assert sRated == getQuant(parameter["srated"], StandardUnits.S_RATED)
            assert cosphiRated == Double.parseDouble(parameter["cosphi"])
        }
    }
}