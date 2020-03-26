/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.io.factory.input.participant;

import edu.ie3.datamodel.exceptions.ParsingException;
import edu.ie3.datamodel.models.OperationTime;
import edu.ie3.datamodel.models.StandardLoadProfile;
import edu.ie3.datamodel.models.StandardUnits;
import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.OperatorInput;
import edu.ie3.datamodel.models.input.system.LoadInput;
import javax.measure.Quantity;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoadInputFactory
    extends SystemParticipantInputEntityFactory<LoadInput, SystemParticipantEntityData> {
  private static final Logger logger = LoggerFactory.getLogger(LoadInputFactory.class);

  private static final String SLP = "slp";
  private static final String DSM = "dsm";
  private static final String E_CONS_ANNUAL = "econsannual";
  private static final String S_RATED = "srated";
  private static final String COS_PHI = "cosphi";

  public LoadInputFactory() {
    super(LoadInput.class);
  }

  @Override
  protected String[] getAdditionalFields() {
    return new String[] {SLP, DSM, E_CONS_ANNUAL, S_RATED, COS_PHI};
  }

  @Override
  protected LoadInput buildModel(
      SystemParticipantEntityData data,
      java.util.UUID uuid,
      String id,
      NodeInput node,
      String qCharacteristics,
      OperatorInput operatorInput,
      OperationTime operationTime) {
    StandardLoadProfile slp;
    try {
      slp = StandardLoadProfile.parse(data.getField(SLP));
    } catch (ParsingException e) {
      logger.error(
          "Cannot parse the standard load profile \"{}\" of load \"{}\". Assign no load profile instead.",
          data.getField(SLP),
          id);
      slp = StandardLoadProfile.DefaultLoadProfiles.NO_STANDARD_LOAD_PROFILE;
    }
    final boolean dsm = data.getBoolean(DSM);
    final Quantity<Energy> eConsAnnual = data.getQuantity(E_CONS_ANNUAL, StandardUnits.ENERGY_IN);
    final Quantity<Power> sRated = data.getQuantity(S_RATED, StandardUnits.S_RATED);
    final double cosPhi = data.getDouble(COS_PHI);

    return new LoadInput(
        uuid,
        operationTime,
        operatorInput,
        id,
        node,
        qCharacteristics,
        slp,
        dsm,
        eConsAnnual,
        sRated,
        cosPhi);
  }
}