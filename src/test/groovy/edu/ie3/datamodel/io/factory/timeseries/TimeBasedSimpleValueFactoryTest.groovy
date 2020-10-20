/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
 */
package edu.ie3.datamodel.io.factory.timeseries

import edu.ie3.datamodel.exceptions.FactoryException
import edu.ie3.datamodel.models.StandardUnits
import edu.ie3.datamodel.models.input.NodeInput
import edu.ie3.datamodel.models.timeseries.individual.TimeBasedValue
import edu.ie3.datamodel.models.value.EnergyPriceValue
import edu.ie3.datamodel.models.value.HeatAndPValue
import edu.ie3.datamodel.models.value.HeatAndSValue
import edu.ie3.datamodel.models.value.HeatDemandValue
import edu.ie3.datamodel.models.value.IrradiationValue
import edu.ie3.datamodel.models.value.PValue
import edu.ie3.datamodel.models.value.SValue
import edu.ie3.datamodel.models.value.TemperatureValue
import edu.ie3.datamodel.models.value.WindValue
import edu.ie3.util.TimeUtil
import spock.lang.Shared
import spock.lang.Specification
import tech.units.indriya.quantity.Quantities

import java.time.ZoneId
import java.util.stream.Collectors

import static edu.ie3.datamodel.io.factory.timeseries.TimeBasedSimpleValueFactory.*

class TimeBasedSimpleValueFactoryTest extends Specification {
	@Shared
	TimeUtil defaultTimeUtil

	def setupSpec() {
		defaultTimeUtil = new TimeUtil(ZoneId.of("UTC"), Locale.GERMANY, "yyyy-MM-dd'T'HH:mm:ss[.S[S][S]]'Z'")
	}

	def "The simple time based value factory provides correct fields"() {
		given:
		def factory = new TimeBasedSimpleValueFactory(valueClass)
		def data = Mock(SimpleTimeBasedValueData)
		data.targetClass >> valueClass

		expect:
		factory.getFields(data) == expectedFields

		where:
		valueClass       || expectedFields
		EnergyPriceValue || [
			[
				TimeBasedSimpleValueFactory.UUID,
				TIME,
				PRICE] as Set
		]
		SValue           || [
			[
				TimeBasedSimpleValueFactory.UUID,
				TIME,
				ACTIVE_POWER,
				REACTIVE_POWER] as Set
		]
		PValue || [
			[
				TimeBasedSimpleValueFactory.UUID,
				TIME,
				ACTIVE_POWER] as Set
		]
		HeatAndSValue || [
			[
				TimeBasedSimpleValueFactory.UUID,
				TIME,
				ACTIVE_POWER,
				REACTIVE_POWER,
				HEAT_DEMAND] as Set
		]
		HeatAndPValue || [
			[
				TimeBasedSimpleValueFactory.UUID,
				TIME,
				ACTIVE_POWER,
				HEAT_DEMAND] as Set
		]
		HeatDemandValue || [
			[
				TimeBasedSimpleValueFactory.UUID,
				TIME,
				HEAT_DEMAND] as Set
		]
		IrradiationValue || [
			[
				TimeBasedSimpleValueFactory.UUID,
				TIME,
				DIRECT_IRRADIATION,
				DIFFUSE_IRRADIATION] as Set
		]
		TemperatureValue || [
			[
				TimeBasedSimpleValueFactory.UUID,
				TIME,
				TEMPERATURE] as Set
		]
		WindValue || [
			[
				TimeBasedSimpleValueFactory.UUID,
				TIME,
				WIND_DIRECTION,
				WIND_VELOCITY] as Set
		]
	}

	def "The simple time based value factory throws a FactoryException upon request of fields, if a class is not supported"() {
		given:
		def factory = new TimeBasedSimpleValueFactory(EnergyPriceValue)
		def data = Mock(SimpleTimeBasedValueData)
		data.targetClass >> NodeInput

		when:
		factory.getFields(data)

		then:
		def e = thrown(FactoryException)
		e.message == "The given factory cannot handle target class '" + NodeInput + "'."
	}

	def "The simple time based value factory builds correct energy price value"() {
		given:
		def factory = new TimeBasedSimpleValueFactory(EnergyPriceValue.class)
		def time = TimeUtil.withDefaults.toZonedDateTime("2019-01-01 00:00:00")
		def data = new SimpleTimeBasedValueData([
			"uuid": "78ca078a-e6e9-4972-a58d-b2cadbc2df2c",
			"time": defaultTimeUtil.toString(time),
			"price": "52.4"
		], EnergyPriceValue.class)
		def expected = new TimeBasedValue(
				UUID.fromString("78ca078a-e6e9-4972-a58d-b2cadbc2df2c"),
				time,
				new EnergyPriceValue(Quantities.getQuantity(52.4, StandardUnits.ENERGY_PRICE))
				)

		expect:
		factory.buildModel(data) == expected
	}

	def "The simple time based value factory builds correct heat and apparent power value"() {
		given:
		def factory = new TimeBasedSimpleValueFactory(HeatAndSValue.class)
		def time = TimeUtil.withDefaults.toZonedDateTime("2019-01-01 00:00:00")
		def data = new SimpleTimeBasedValueData([
			"uuid": "78ca078a-e6e9-4972-a58d-b2cadbc2df2c",
			"time": defaultTimeUtil.toString(time),
			"p": "500.0",
			"q": "165.0",
			"heatdemand": "8.0"
		], HeatAndSValue.class)
		def expected = new TimeBasedValue(
				UUID.fromString("78ca078a-e6e9-4972-a58d-b2cadbc2df2c"),
				time,
				new HeatAndSValue(Quantities.getQuantity(500.0, StandardUnits.ACTIVE_POWER_IN), Quantities.getQuantity(165.0, StandardUnits.REACTIVE_POWER_IN), Quantities.getQuantity(8.0, StandardUnits.HEAT_DEMAND))
				)

		expect:
		factory.buildModel(data) == expected
	}

	def "The simple time based value factory builds correct heat and active power value"() {
		given:
		def factory = new TimeBasedSimpleValueFactory(HeatAndPValue.class)
		def time = TimeUtil.withDefaults.toZonedDateTime("2019-01-01 00:00:00")
		def data = new SimpleTimeBasedValueData([
			"uuid": "78ca078a-e6e9-4972-a58d-b2cadbc2df2c",
			"time": defaultTimeUtil.toString(time),
			"p": "500.0",
			"heatdemand": "8.0"
		], HeatAndPValue.class)
		def expected = new TimeBasedValue(
				UUID.fromString("78ca078a-e6e9-4972-a58d-b2cadbc2df2c"),
				time,
				new HeatAndPValue(Quantities.getQuantity(500.0, StandardUnits.ACTIVE_POWER_IN), Quantities.getQuantity(8.0, StandardUnits.HEAT_DEMAND))
				)

		expect:
		factory.buildModel(data) == expected
	}

	def "The simple time based value factory builds correct heat demand value"() {
		given:
		def factory = new TimeBasedSimpleValueFactory(HeatDemandValue.class)
		def time = TimeUtil.withDefaults.toZonedDateTime("2019-01-01 00:00:00")
		def data = new SimpleTimeBasedValueData([
			"uuid": "78ca078a-e6e9-4972-a58d-b2cadbc2df2c",
			"time": defaultTimeUtil.toString(time),
			"heatdemand": "8.0"
		], HeatDemandValue.class)
		def expected = new TimeBasedValue(
				UUID.fromString("78ca078a-e6e9-4972-a58d-b2cadbc2df2c"),
				time,
				new HeatDemandValue(Quantities.getQuantity(8.0, StandardUnits.HEAT_DEMAND))
				)

		expect:
		factory.buildModel(data) == expected
	}

	def "The simple time based value factory builds correct apparent power value"() {
		given:
		def factory = new TimeBasedSimpleValueFactory(SValue.class)
		def time = TimeUtil.withDefaults.toZonedDateTime("2019-01-01 00:00:00")
		def data = new SimpleTimeBasedValueData([
			"uuid": "78ca078a-e6e9-4972-a58d-b2cadbc2df2c",
			"time": defaultTimeUtil.toString(time),
			"p": "500.0",
			"q": "165.0"
		], SValue.class)
		def expected = new TimeBasedValue(
				UUID.fromString("78ca078a-e6e9-4972-a58d-b2cadbc2df2c"),
				time,
				new SValue(Quantities.getQuantity(500.0, StandardUnits.ACTIVE_POWER_IN), Quantities.getQuantity(165.0, StandardUnits.REACTIVE_POWER_IN))
				)

		expect:
		factory.buildModel(data) == expected
	}

	def "The simple time based value factory builds correct active power value"() {
		given:
		def factory = new TimeBasedSimpleValueFactory(PValue.class)
		def time = TimeUtil.withDefaults.toZonedDateTime("2019-01-01 00:00:00")
		def data = new SimpleTimeBasedValueData([
			"uuid": "78ca078a-e6e9-4972-a58d-b2cadbc2df2c",
			"time": defaultTimeUtil.toString(time),
			"p": "500.0"
		], PValue.class)
		def expected = new TimeBasedValue(
				UUID.fromString("78ca078a-e6e9-4972-a58d-b2cadbc2df2c"),
				time,
				new PValue(Quantities.getQuantity(500.0, StandardUnits.ACTIVE_POWER_IN))
				)

		expect:
		factory.buildModel(data) == expected
	}

	def "The simple time based value factory builds correct irradiation value"() {
		given:
		def factory = new TimeBasedSimpleValueFactory(IrradiationValue.class)
		def time = TimeUtil.withDefaults.toZonedDateTime("2019-01-01 00:00:00")
		def data = new SimpleTimeBasedValueData([
			"uuid": "78ca078a-e6e9-4972-a58d-b2cadbc2df2c",
			"time": defaultTimeUtil.toString(time),
			"diffuseirradiation": "282.671997070312",
			"directirradiation" : "286.872985839844"
		], IrradiationValue.class)
		def expected = new TimeBasedValue(
				UUID.fromString("78ca078a-e6e9-4972-a58d-b2cadbc2df2c"),
				time,
				new IrradiationValue(Quantities.getQuantity(282.671997070312, StandardUnits.IRRADIATION), Quantities.getQuantity(286.872985839844, StandardUnits.IRRADIATION))
				)

		expect:
		factory.buildModel(data) == expected
	}

	def "The simple time based value factory builds correct temperature value"() {
		given:
		def factory = new TimeBasedSimpleValueFactory(TemperatureValue.class)
		def time = TimeUtil.withDefaults.toZonedDateTime("2019-01-01 00:00:00")
		def data = new SimpleTimeBasedValueData([
			"uuid": "78ca078a-e6e9-4972-a58d-b2cadbc2df2c",
			"time": defaultTimeUtil.toString(time),
			"temperature": "278.019012451172"
		], TemperatureValue.class)
		def expected = new TimeBasedValue(
				UUID.fromString("78ca078a-e6e9-4972-a58d-b2cadbc2df2c"),
				time,
				new TemperatureValue(Quantities.getQuantity(278.019012451172, StandardUnits.TEMPERATURE))
				)

		expect:
		factory.buildModel(data) == expected
	}

	def "The simple time based value factory builds correct wind value"() {
		given:
		def factory = new TimeBasedSimpleValueFactory(WindValue.class)
		def time = TimeUtil.withDefaults.toZonedDateTime("2019-01-01 00:00:00")
		def data = new SimpleTimeBasedValueData([
			"uuid": "78ca078a-e6e9-4972-a58d-b2cadbc2df2c",
			"time": defaultTimeUtil.toString(time),
			"winddirection"     : "0",
			"windvelocity"      : "1.66103506088257"
		], WindValue.class)
		def expected = new TimeBasedValue(
				UUID.fromString("78ca078a-e6e9-4972-a58d-b2cadbc2df2c"),
				time,
				new WindValue(Quantities.getQuantity(0, StandardUnits.WIND_DIRECTION), Quantities.getQuantity(1.66103506088257, StandardUnits.WIND_VELOCITY))
				)

		expect:
		factory.buildModel(data) == expected
	}

	def "The simple time based value factory throws a FactoryException upon build request, if a class is not supported"() {
		given:
		def factory = new TimeBasedSimpleValueFactory(EnergyPriceValue)
		def time = TimeUtil.withDefaults.toZonedDateTime("2019-01-01 00:00:00")
		def data = new SimpleTimeBasedValueData([
			"uuid": "78ca078a-e6e9-4972-a58d-b2cadbc2df2c",
			"time": defaultTimeUtil.toString(time)
		], NodeInput.class)

		when:
		factory.buildModel(data)

		then:
		def e = thrown(FactoryException)
		e.message == "The given factory cannot handle target class '" + NodeInput + "'."
	}
}