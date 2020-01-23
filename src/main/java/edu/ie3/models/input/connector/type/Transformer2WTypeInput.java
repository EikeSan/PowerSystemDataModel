/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.models.input.connector.type;

import edu.ie3.models.StandardUnits;
import edu.ie3.models.input.AssetTypeInput;
import java.util.Objects;
import java.util.UUID;
import javax.measure.Quantity;
import javax.measure.quantity.*;

/** Describes the type of a {@link edu.ie3.models.input.connector.Transformer2WInput} */
public class Transformer2WTypeInput extends AssetTypeInput {
  /** Short circuit resistance (typically in Ohm) */
  Quantity<ElectricResistance> rSc;
  /** Short circuit reactance (typically in Ohm) */
  Quantity<ElectricResistance> xSc;
  /** Rated apparent power (typically in MVA) */
  Quantity<Power> sRated;
  /** Rated voltage of the high voltage winding (typically in kV) */
  Quantity<ElectricPotential> vRatedA;
  /** Rated voltage of the low voltage winding (typically in kV) */
  Quantity<ElectricPotential> vRatedB;
  /** Phase-to-ground conductance (typically in nS) */
  Quantity<ElectricConductance> gM;
  /** Phase-to-ground susceptance (typically in nS) */
  Quantity<ElectricConductance> bM;
  /** Voltage magnitude deviation per tap position (typically in %) */
  Quantity<Dimensionless> dV;
  /** Voltage angle deviation per tap position (typically in °) */
  Quantity<Angle> dPhi;
  /** Selection of winding, where the tap changer is installed. Low voltage, if true */
  Boolean tapSide;
  /** Neutral tap position */
  Integer tapNeutr;
  /** Minimum available tap position */
  Integer tapMin;
  /** Maximum available tap position */
  Integer tapMax;

  /**
   * @param uuid of the input entity
   * @param id of the type
   * @param rSc Short circuit resistance
   * @param xSc Short circuit reactance
   * @param sRated Rated apparent power (typically in MVA)
   * @param vRatedA Rated voltage of the high voltage winding
   * @param vRatedB Rated voltage of the low voltage winding
   * @param gM Phase-to-ground conductance
   * @param bM Phase-to-ground susceptance
   * @param dV Voltage magnitude deviation per tap position
   * @param dPhi Voltage angle deviation per tap position
   * @param tapSide Selection of winding, where the tap changer is installed. Low voltage, if true
   * @param tapNeutr Neutral tap position
   * @param tapMin Minimum available tap position
   * @param tapMax Maximum available tap position
   */
  public Transformer2WTypeInput(
      UUID uuid,
      String id,
      Quantity<ElectricResistance> rSc,
      Quantity<ElectricResistance> xSc,
      Quantity<Power> sRated,
      Quantity<ElectricPotential> vRatedA,
      Quantity<ElectricPotential> vRatedB,
      Quantity<ElectricConductance> gM,
      Quantity<ElectricConductance> bM,
      Quantity<Dimensionless> dV,
      Quantity<Angle> dPhi,
      Boolean tapSide,
      Integer tapNeutr,
      Integer tapMin,
      Integer tapMax) {
    super(uuid, id);
    this.rSc = rSc.to(StandardUnits.IMPEDANCE);
    this.xSc = xSc.to(StandardUnits.IMPEDANCE);
    this.sRated = sRated.to(StandardUnits.S_RATED);
    this.vRatedA = vRatedA.to(StandardUnits.V_RATED);
    this.vRatedB = vRatedB.to(StandardUnits.V_RATED);
    this.gM = gM.to(StandardUnits.ADMITTANCE);
    this.bM = bM.to(StandardUnits.ADMITTANCE);
    this.dV = dV.to(StandardUnits.DV_TAP);
    this.dPhi = dPhi.to(StandardUnits.DPHI_TAP);
    this.tapSide = tapSide;
    this.tapNeutr = tapNeutr;
    this.tapMin = tapMin;
    this.tapMax = tapMax;
  }

  public Quantity<ElectricResistance> getRSc() {
    return rSc;
  }

  public void setRSc(Quantity<ElectricResistance> rSc) {
    this.rSc = rSc.to(StandardUnits.IMPEDANCE);
  }

  public Quantity<ElectricResistance> getXSc() {
    return xSc;
  }

  public void setXSc(Quantity<ElectricResistance> xSc) {
    this.xSc = xSc.to(StandardUnits.IMPEDANCE);
  }

  public Quantity<Power> getSRated() {
    return sRated;
  }

  public void setSRated(Quantity<Power> sRated) {
    this.sRated = sRated.to(StandardUnits.S_RATED);
  }

  public Quantity<ElectricPotential> getVRatedA() {
    return vRatedA;
  }

  public void setVRatedA(Quantity<ElectricPotential> vRatedA) {
    this.vRatedA = vRatedA.to(StandardUnits.V_RATED);
  }

  public Quantity<ElectricPotential> getVRatedB() {
    return vRatedB;
  }

  public void setVRatedB(Quantity<ElectricPotential> vRatedB) {
    this.vRatedB = vRatedB.to(StandardUnits.V_RATED);
  }

  public Quantity<ElectricConductance> getGM() {
    return gM;
  }

  public void setGM(Quantity<ElectricConductance> gM) {
    this.gM = gM.to(StandardUnits.ADMITTANCE);
  }

  public Quantity<ElectricConductance> getBM() {
    return bM;
  }

  public void setBM(Quantity<ElectricConductance> bM) {
    this.bM = bM.to(StandardUnits.ADMITTANCE);
  }

  public Quantity<Dimensionless> getDV() {
    return dV;
  }

  public void setDV(Quantity<Dimensionless> dV) {
    this.dV = dV.to(StandardUnits.DV_TAP);
  }

  public Quantity<Angle> getDPhi() {
    return dPhi;
  }

  public void setDPhi(Quantity<Angle> dPhi) {
    this.dPhi = dPhi.to(StandardUnits.DPHI_TAP);
  }

  public Boolean getTapSide() {
    return tapSide;
  }

  public void setTapSide(Boolean tapSide) {
    this.tapSide = tapSide;
  }

  public Integer getTapNeutr() {
    return tapNeutr;
  }

  public void setTapNeutr(Integer tapNeutr) {
    this.tapNeutr = tapNeutr;
  }

  public Integer getTapMin() {
    return tapMin;
  }

  public void setTapMin(Integer tapMin) {
    this.tapMin = tapMin;
  }

  public Integer getTapMax() {
    return tapMax;
  }

  public void setTapMax(Integer tapMax) {
    this.tapMax = tapMax;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    Transformer2WTypeInput that = (Transformer2WTypeInput) o;
    return rSc.equals(that.rSc)
        && xSc.equals(that.xSc)
        && sRated.equals(that.sRated)
        && vRatedA.equals(that.vRatedA)
        && vRatedB.equals(that.vRatedB)
        && gM.equals(that.gM)
        && bM.equals(that.bM)
        && dV.equals(that.dV)
        && dPhi.equals(that.dPhi)
        && tapSide.equals(that.tapSide)
        && tapNeutr.equals(that.tapNeutr)
        && tapMin.equals(that.tapMin)
        && tapMax.equals(that.tapMax);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        rSc,
        xSc,
        sRated,
        vRatedA,
        vRatedB,
        gM,
        bM,
        dV,
        dPhi,
        tapSide,
        tapNeutr,
        tapMin,
        tapMax);
  }
}
