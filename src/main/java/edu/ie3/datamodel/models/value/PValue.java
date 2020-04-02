/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.models.value;

import edu.ie3.datamodel.models.StandardUnits;
import tec.uom.se.ComparableQuantity;

import java.util.Objects;
import javax.measure.quantity.Power;

/** Describes a actove power value as active power */
public class PValue implements Value {

  /** Active power */
  private ComparableQuantity<Power> p; // TODO doublecheck

  /** @param p Active power */
  public PValue(ComparableQuantity<Power> p) {
    this.p = p.to(StandardUnits.ACTIVE_POWER_IN);
  } // TODO doublecheck

  public ComparableQuantity<Power> getP() {
    return p;
  } // TODO doublecheck

  public void setP(ComparableQuantity<Power> p) {
    this.p = p.to(StandardUnits.ACTIVE_POWER_IN);
  } // TODO doublecheck

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    PValue that = (PValue) o;
    return p.equals(that.p);
  }

  @Override
  public int hashCode() {
    return Objects.hash(p);
  }

  @Override
  public String toString() {
    return "PValue{" + "p=" + p + '}';
  }
}
