/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.models.input.graphics;

import com.vividsolutions.jts.geom.LineString;
import edu.ie3.datamodel.models.input.InputEntity;
import java.util.Objects;
import java.util.UUID;

/** Describes the graphic data belonging to grid assets */
public class GraphicInput extends InputEntity {
  /** Description of the graphic layer, this graphic is located on */
  private final String graphicLayer;
  /** A graphic representation as path */
  private final LineString path;

  /**
   * @param uuid of the input entity
   * @param graphicLayer Description of the graphic layer, this graphic is located on
   * @param path A graphic representation as path
   */
  public GraphicInput(UUID uuid, String graphicLayer, LineString path) {
    super(uuid);
    this.graphicLayer = graphicLayer;
    this.path = path;
  }

  public String getGraphicLayer() {
    return graphicLayer;
  }

  public LineString getPath() {
    return path;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    GraphicInput that = (GraphicInput) o;
    return Objects.equals(graphicLayer, that.graphicLayer) && Objects.equals(path, that.path);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), graphicLayer, path);
  }

  @Override
  public String toString() {
    return "GraphicInput{" + "graphicLayer='" + graphicLayer + '\'' + ", path=" + path + '}';
  }
}