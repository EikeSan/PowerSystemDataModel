/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.dataconnection.metrics;

import com.vividsolutions.jts.geom.Point;
import edu.ie3.dataconnection.source.csv.CsvCoordinateSource;
import edu.ie3.models.StandardUnits;
import edu.ie3.models.timeseries.IndividualTimeSeries;
import edu.ie3.models.value.WeatherValues;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import tec.uom.se.quantity.Quantities;

public class WeatherHealthCheck {

  private static final int coordinateCount = 1683;
  private static final int timeseriesLength = 168;
  private static final ZonedDateTime exampleDate =
      ZonedDateTime.of(2013, 4, 12, 9, 0, 0, 0, ZoneId.of("UTC"));
  private static final Point exampleCoordinate = CsvCoordinateSource.getCoordinate(193176);

  public static boolean check(
      Map<Point, IndividualTimeSeries<WeatherValues>> coordinateToTimeSeries) {
    if (coordinateToTimeSeries == null) return false;
    if (coordinateToTimeSeries.keySet().size() != coordinateCount) return false;
    if (coordinateToTimeSeries.values().stream()
        .anyMatch(timeseries -> timeseries.size() != timeseriesLength)) return false;
    WeatherValues value = coordinateToTimeSeries.get(exampleCoordinate).getValue(exampleDate);
    if (!value
        .getIrradiation()
        .getDiffuseIrradiation()
        .equals(Quantities.getQuantity(69.6875, StandardUnits.IRRADIATION))) return false;
    if (!value
        .getTemperature()
        .getTemperature()
        .equals(Quantities.getQuantity(284.660003662109, StandardUnits.TEMPERATURE)))
      return false;
    return value
        .getWind()
        .getVelocity()
        .equals(Quantities.getQuantity(9.81319808959961, StandardUnits.WIND_VELOCITY));
  }
}