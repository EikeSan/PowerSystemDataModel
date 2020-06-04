/*
 * © 2020. TU Dortmund University,
 * Institute of Energy Systems, Energy Efficiency and Energy Economics,
 * Research group Distribution grid planning and operation
*/
package edu.ie3.datamodel.utils;

import edu.ie3.datamodel.exceptions.InvalidGridException;
import edu.ie3.datamodel.graph.*;
import edu.ie3.datamodel.models.input.MeasurementUnitInput;
import edu.ie3.datamodel.models.input.NodeInput;
import edu.ie3.datamodel.models.input.connector.*;
import edu.ie3.datamodel.models.input.container.*;
import edu.ie3.datamodel.models.input.graphics.LineGraphicInput;
import edu.ie3.datamodel.models.input.graphics.NodeGraphicInput;
import edu.ie3.datamodel.models.input.system.*;
import edu.ie3.datamodel.models.voltagelevels.VoltageLevel;
import edu.ie3.util.geo.GeoUtils;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jgrapht.graph.DirectedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Offers functionality useful for grouping different models together */
public class ContainerUtils {

  private static final Logger log = LoggerFactory.getLogger(ContainerUtils.class);

  private ContainerUtils() {
    throw new IllegalStateException("Utility classes cannot be instantiated");
  }

  public static Optional<DistanceWeightedGraph> getDistanceTopologyGraph(GridContainer grid) {
    return getDistanceTopologyGraph(grid.getRawGrid());
  }

  public static Optional<DistanceWeightedGraph> getDistanceTopologyGraph(
      RawGridElements rawGridElements) {

    DistanceWeightedGraph graph = new DistanceWeightedGraph();

    try {
      rawGridElements.getNodes().forEach(graph::addVertex);
    } catch (NullPointerException ex) {
      log.error("At least one node entity of provided RawGridElements is null. ", ex);
      return Optional.empty();
    }

    try {
      rawGridElements
          .getLines()
          .forEach(line -> addDistanceGraphEdge(graph, line.getNodeA(), line.getNodeB()));
    } catch (NullPointerException | IllegalArgumentException | UnsupportedOperationException ex) {
      log.error("Error adding line edges to graph: ", ex);
      return Optional.empty();
    }

    try {
      rawGridElements
          .getSwitches()
          .forEach(
              switchInput ->
                  addDistanceGraphEdge(graph, switchInput.getNodeA(), switchInput.getNodeB()));
    } catch (NullPointerException | IllegalArgumentException | UnsupportedOperationException ex) {
      log.error("Error adding switch edges to graph: ", ex);
      return Optional.empty();
    }

    try {
      rawGridElements
          .getTransformer2Ws()
          .forEach(trafo2w -> addDistanceGraphEdge(graph, trafo2w.getNodeA(), trafo2w.getNodeB()));
    } catch (NullPointerException | IllegalArgumentException | UnsupportedOperationException ex) {
      log.error("Error adding 2 winding transformer edges to graph: ", ex);
      return Optional.empty();
    }

    try {
      rawGridElements
          .getTransformer3Ws()
          .forEach(
              trafo3w -> {
                addDistanceGraphEdge(graph, trafo3w.getNodeA(), trafo3w.getNodeB());
                addDistanceGraphEdge(graph, trafo3w.getNodeA(), trafo3w.getNodeC());
              });
    } catch (NullPointerException | IllegalArgumentException | UnsupportedOperationException ex) {
      log.error("Error adding 3 winding transformer edges to graph: ", ex);
      return Optional.empty();
    }

    // if we reached this point, we can safely return a valid graph
    return Optional.of(graph);
  }

  private static void addDistanceGraphEdge(
      DistanceWeightedGraph graph, NodeInput nodeA, NodeInput nodeB) {
    graph.addEdge(nodeA, nodeB);
    graph.setEdgeWeight(
        graph.getEdge(nodeA, nodeB),
        GeoUtils.haversine(
            nodeA.getGeoPosition().getY(),
            nodeA.getGeoPosition().getX(),
            nodeB.getGeoPosition().getY(),
            nodeB.getGeoPosition().getX()));
  }

  /**
   * Filters all raw grid elements for the provided subnet. For each transformer all nodes (and not
   * only the the node of the grid the transformer is located in) are added as well. Two winding
   * transformers are counted, if the low voltage node is in the queried subnet. Three winding
   * transformers are counted, as long as any of the three nodes is in the queried subnet.
   *
   * @param input The model to filter
   * @param subnet The filter criterion
   * @return A {@link RawGridElements} filtered for the subnet
   */
  public static RawGridElements filterForSubnet(RawGridElements input, int subnet) {
    Set<NodeInput> nodes =
        input.getNodes().stream()
            .filter(node -> node.getSubnet() == subnet)
            .collect(Collectors.toSet());

    Set<LineInput> lines =
        input.getLines().stream()
            .filter(line -> line.getNodeB().getSubnet() == subnet)
            .collect(Collectors.toSet());

    Set<Transformer2WInput> transformer2w =
        input.getTransformer2Ws().stream()
            .filter(transformer -> transformer.getNodeB().getSubnet() == subnet)
            .collect(Collectors.toSet());
    /* Add the higher voltage node to the set of nodes */
    nodes.addAll(
        transformer2w.stream().map(Transformer2WInput::getNodeA).collect(Collectors.toSet()));

    Set<Transformer3WInput> transformer3w =
        input.getTransformer3Ws().stream()
            .filter(
                transformer ->
                    transformer.getNodeA().getSubnet() == subnet
                        || transformer.getNodeB().getSubnet() == subnet
                        || transformer.getNodeC().getSubnet() == subnet)
            .collect(Collectors.toSet());
    /* Add all nodes of a three winding transformer node to the set of nodes */
    nodes.addAll(
        transformer3w.stream()
            .flatMap(
                transformer ->
                    Stream.of(
                        transformer.getNodeA(), transformer.getNodeB(), transformer.getNodeC()))
            .collect(Collectors.toSet()));

    Set<SwitchInput> switches =
        input.getSwitches().stream()
            .filter(switcher -> switcher.getNodeB().getSubnet() == subnet)
            .collect(Collectors.toSet());

    Set<MeasurementUnitInput> measurements =
        input.getMeasurementUnits().stream()
            .filter(measurement -> measurement.getNode().getSubnet() == subnet)
            .collect(Collectors.toSet());

    return new RawGridElements(nodes, lines, transformer2w, transformer3w, switches, measurements);
  }

  /**
   * Filters all system participants for the provided subnet.
   *
   * @param input The model to filter
   * @param subnet The filter criterion
   * @return A {@link SystemParticipants} filtered for the subnet
   */
  public static SystemParticipants filterForSubnet(SystemParticipants input, int subnet) {
    Set<BmInput> bmPlants = filterParticipants(input.getBmPlants(), subnet);
    Set<ChpInput> chpPlants = filterParticipants(input.getChpPlants(), subnet);
    /* Electric vehicle charging systems are currently dummy implementations without nodal reverence */
    Set<EvInput> evs = filterParticipants(input.getEvs(), subnet);
    Set<FixedFeedInInput> fixedFeedIns = filterParticipants(input.getFixedFeedIns(), subnet);
    Set<HpInput> heatpumps = filterParticipants(input.getHeatPumps(), subnet);
    Set<LoadInput> loads = filterParticipants(input.getLoads(), subnet);
    Set<PvInput> pvs = filterParticipants(input.getPvPlants(), subnet);
    Set<StorageInput> storages = filterParticipants(input.getStorages(), subnet);
    Set<WecInput> wecPlants = filterParticipants(input.getWecPlants(), subnet);

    return new SystemParticipants(
        bmPlants,
        chpPlants,
        new HashSet<>(),
        evs,
        fixedFeedIns,
        heatpumps,
        loads,
        pvs,
        storages,
        wecPlants);
  }

  /**
   * Filter sets of system participants for their subnet,
   *
   * @param systemParticipantInputs Set of SystemParticipantInputs
   * @param subnet Filter criterion
   * @param <T> Type parameter of the system participant
   * @return A filtered set
   */
  private static <T extends SystemParticipantInput> Set<T> filterParticipants(
      Set<T> systemParticipantInputs, int subnet) {
    return systemParticipantInputs.stream()
        .filter(entity -> entity.getNode().getSubnet() == subnet)
        .collect(Collectors.toSet());
  }

  /**
   * Filters all graphic elements for the provided subnet.
   *
   * @param input The model to filter
   * @param subnet The filter criterion
   * @return A {@link GraphicElements} filtered for the subnet
   */
  public static GraphicElements filterForSubnet(GraphicElements input, int subnet) {
    Set<NodeGraphicInput> nodeGraphics =
        input.getNodeGraphics().stream()
            .filter(entity -> entity.getNode().getSubnet() == subnet)
            .collect(Collectors.toSet());
    Set<LineGraphicInput> lineGraphics =
        input.getLineGraphics().stream()
            .filter(entity -> entity.getLine().getNodeB().getSubnet() == subnet)
            .collect(Collectors.toSet());

    return new GraphicElements(nodeGraphics, lineGraphics);
  }

  /**
   * Determining the predominant voltage level in this grid by counting the occurrences of the
   * different voltage levels
   *
   * @param rawGrid Raw grid elements of the specified sub grid
   * @param subnet Subnet number of the subnet
   * @return The predominant voltage level in this grid
   * @throws InvalidGridException If not a single, predominant voltage level can be determined
   */
  public static VoltageLevel determinePredominantVoltLvl(RawGridElements rawGrid, int subnet) {
    /* Exclude all nodes, that are at the high voltage side of the transformer */
    Set<NodeInput> gridNodes = new HashSet<>(rawGrid.getNodes());
    gridNodes.removeAll(
        rawGrid.getTransformer2Ws().stream()
            .map(ConnectorInput::getNodeA)
            .collect(Collectors.toSet()));
    gridNodes.removeAll(
        rawGrid.getTransformer3Ws().stream()
            .flatMap(
                transformer -> {
                  if (transformer.getNodeA().getSubnet() == subnet)
                    return Stream.of(transformer.getNodeB(), transformer.getNodeC());
                  else if (transformer.getNodeB().getSubnet() == subnet)
                    return Stream.of(
                        transformer.getNodeA(),
                        transformer.getNodeC(),
                        transformer.getNodeInternal());
                  else
                    return Stream.of(
                        transformer.getNodeA(),
                        transformer.getNodeB(),
                        transformer.getNodeInternal());
                })
            .collect(Collectors.toSet()));

    /* Build a mapping, which voltage level appears how often */
    Map<VoltageLevel, Long> voltageLevelCount =
        gridNodes.stream()
            .map(NodeInput::getVoltLvl)
            .collect(Collectors.groupingBy(voltLvl -> voltLvl, Collectors.counting()));

    /* At this point only one voltage level should be apparent */
    int amountOfVoltLvl = voltageLevelCount.size();
    if (amountOfVoltLvl > 1)
      throw new InvalidGridException(
          "There are "
              + amountOfVoltLvl
              + " voltage levels apparent, although only one is expected. Following voltage levels are present: "
              + voltageLevelCount.keySet().stream()
                  .sorted(Comparator.comparing(VoltageLevel::getNominalVoltage))
                  .map(VoltageLevel::toString)
                  .collect(Collectors.joining(", ")));

    return voltageLevelCount.entrySet().stream()
        .max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey)
        .orElseThrow(
            () ->
                new InvalidGridException(
                    "Cannot determine the predominant voltage level. Following voltage levels are present: "
                        + voltageLevelCount.keySet().stream()
                            .sorted(Comparator.comparing(VoltageLevel::getNominalVoltage))
                            .map(VoltageLevel::toString)
                            .collect(Collectors.joining(", "))));
  }

  /**
   * Disassembles this grid model into sub grid models and returns a topology of the sub grids as a
   * directed, immutable graph. The direction points from higher to lower voltage level.
   *
   * @param gridName Name of the grid
   * @param rawGrid Container model of raw grid elements
   * @param systemParticipants Container model of system participants
   * @param graphics Container element of graphic elements
   * @return An immutable, directed graph of sub grid topologies.
   */
  public static SubGridTopologyGraph buildSubGridTopologyGraph(
      String gridName,
      RawGridElements rawGrid,
      SystemParticipants systemParticipants,
      GraphicElements graphics) {
    /* Collect the different sub nets. Through the validation of lines, it is ensured, that no galvanically connected
     * grid has more than one subnet number assigned */
    SortedSet<Integer> subnetNumbers = determineSubnetNumbers(rawGrid.getNodes());

    /* Build the single sub grid models */
    HashMap<Integer, SubGridContainer> subgrids =
        buildSubGridContainers(gridName, subnetNumbers, rawGrid, systemParticipants, graphics);

    /* Build the graph structure denoting the topology of the grid */
    return buildSubGridTopologyGraph(
        subgrids, rawGrid.getTransformer2Ws(), rawGrid.getTransformer3Ws());
  }

  /**
   * Determine a distinct set of apparent subnet numbers
   *
   * @param nodes Set of nodes
   * @return A sorted set of subnet numbers
   */
  private static SortedSet<Integer> determineSubnetNumbers(Set<NodeInput> nodes) {
    return nodes.stream().map(NodeInput::getSubnet).collect(Collectors.toCollection(TreeSet::new));
  }

  /**
   * Build a mapping from sub net number to actual {@link SubGridContainer}
   *
   * @param gridName Name of the grid
   * @param subnetNumbers Set of available subne numbers
   * @param rawGrid Container model with all raw grid elements
   * @param systemParticipants Container model with all system participant inputs
   * @param graphics Container model with all graphic elements
   * @return A mapping from subnet number to container model with sub grid elements
   */
  private static HashMap<Integer, SubGridContainer> buildSubGridContainers(
      String gridName,
      SortedSet<Integer> subnetNumbers,
      RawGridElements rawGrid,
      SystemParticipants systemParticipants,
      GraphicElements graphics) {
    HashMap<Integer, SubGridContainer> subGrids = new HashMap<>(subnetNumbers.size());
    for (int subnetNumber : subnetNumbers) {
      RawGridElements rawGridElements = ContainerUtils.filterForSubnet(rawGrid, subnetNumber);
      SystemParticipants systemParticipantElements =
          ContainerUtils.filterForSubnet(systemParticipants, subnetNumber);
      GraphicElements graphicElements = ContainerUtils.filterForSubnet(graphics, subnetNumber);

      subGrids.put(
          subnetNumber,
          new SubGridContainer(
              gridName, subnetNumber, rawGridElements, systemParticipantElements, graphicElements));
    }
    return subGrids;
  }

  /**
   * Build an immutable graph of the galvanically separated sub grid topology
   *
   * @param subgrids Mapping from sub net number to container model
   * @param transformer2ws Set of two winding transformers
   * @param transformer3ws Set of three winding transformers
   * @return An immutable graph of the sub grid topology
   */
  private static SubGridTopologyGraph buildSubGridTopologyGraph(
      Map<Integer, SubGridContainer> subgrids,
      Set<Transformer2WInput> transformer2ws,
      Set<Transformer3WInput> transformer3ws) {
    /* Building a mutable graph, that is boxed as immutable later */
    DirectedMultigraph<SubGridContainer, SubGridGate> mutableGraph =
        new DirectedMultigraph<>(SubGridGate.class);

    /* Add all edges */
    subgrids.values().forEach(mutableGraph::addVertex);

    /* Add connections of two winding transformers */
    for (Transformer2WInput transformer : transformer2ws) {
      SubGridContainer from = getSubGridContainer(transformer, ConnectorPort.A, subgrids);
      SubGridContainer to = getSubGridContainer(transformer, ConnectorPort.B, subgrids);
      mutableGraph.addEdge(from, to, new SubGridGate(transformer));
    }

    /* Add connections of three winding transformers */
    for (Transformer3WInput transformer : transformer3ws) {
      SubGridContainer from = getSubGridContainer(transformer, ConnectorPort.A, subgrids);
      SubGridContainer toB = getSubGridContainer(transformer, ConnectorPort.B, subgrids);
      SubGridContainer toC = getSubGridContainer(transformer, ConnectorPort.C, subgrids);
      mutableGraph.addEdge(from, toB, new SubGridGate(transformer, ConnectorPort.B));
      mutableGraph.addEdge(from, toC, new SubGridGate(transformer, ConnectorPort.C));
    }

    return new SubGridTopologyGraph(mutableGraph);
  }

  /**
   * Extracts the {@link SubGridContainer} of the map from sub grid number to sub grid model and
   * checks for its availability.
   *
   * @param connector The connector to use
   * @param port The port of the connector, that is referred to
   * @param subGrids A mapping from sub grid number to sub grid model
   * @return The queried sub grid container
   */
  private static SubGridContainer getSubGridContainer(
      ConnectorInput connector, ConnectorPort port, Map<Integer, SubGridContainer> subGrids) {
    int subGrid;
    switch (port) {
      case A:
        subGrid = connector.getNodeA().getSubnet();
        break;
      case B:
        subGrid = connector.getNodeB().getSubnet();
        break;
      case C:
        if (connector instanceof Transformer3WInput)
          subGrid = ((Transformer3WInput) connector).getNodeC().getSubnet();
        else
          throw new IllegalArgumentException(
              "The connector " + connector + " has no port " + port + ".");
        break;
      default:
        throw new IllegalArgumentException(
            "Cannot determine the sub grid number of connector "
                + connector
                + " at port "
                + port
                + ".");
    }

    SubGridContainer container = subGrids.get(subGrid);
    if (container == null)
      throw new InvalidGridException(
          "Transformer "
              + connector
              + " connects two sub grids, but the sub grid model "
              + subGrid
              + " cannot be found");
    else return container;
  }

  /**
   * Combines a given collection of sub grid containers to a joint model. If the single models do
   * not fit together, exceptions are thrown.
   *
   * @param subGridContainers Collections of already existing sub grid models
   * @return A joint model
   */
  public static JointGridContainer combineToJointGrid(
      Collection<SubGridContainer> subGridContainers) {
    if (subGridContainers.stream().map(SubGridContainer::getGridName).distinct().count() > 1)
      throw new InvalidGridException(
          "You are trying to combine sub grids of different grid models");

    String gridName =
        subGridContainers.stream()
            .map(SubGridContainer::getGridName)
            .findFirst()
            .orElseThrow(
                () ->
                    new InvalidGridException(
                        "Cannot determine a joint name of the provided sub grid models."));

    RawGridElements rawGrid =
        new RawGridElements(
            subGridContainers.stream().map(GridContainer::getRawGrid).collect(Collectors.toSet()));
    SystemParticipants systemParticipants =
        new SystemParticipants(
            subGridContainers.stream()
                .map(GridContainer::getSystemParticipants)
                .collect(Collectors.toSet()));
    GraphicElements graphicElements =
        new GraphicElements(
            subGridContainers.stream().map(GridContainer::getGraphics).collect(Collectors.toSet()));

    Map<Integer, SubGridContainer> subGridMapping =
        subGridContainers.stream()
            .collect(Collectors.toMap(SubGridContainer::getSubnet, Function.identity()));

    SubGridTopologyGraph subGridTopologyGraph =
        buildSubGridTopologyGraph(
            subGridMapping, rawGrid.getTransformer2Ws(), rawGrid.getTransformer3Ws());

    return new JointGridContainer(
        gridName, rawGrid, systemParticipants, graphicElements, subGridTopologyGraph);
  }

  /**
   * Returns a copy {@link SubGridContainer} based on the provided subgrid with a certain set of
   * nodes marked as slack nodes. In general, the grid is modified in a way that slack nodes are
   * added at transformer nodes based on assumptions about the grid, as well as all other affect
   * entities of the grid are accordingly.
   *
   * <p>This step is necessary for power flow calculations, as by default, when the container is
   * derived from {@link JointGridContainer}, only the original slack nodes are incorporated in the
   * different sub containers. Thereby, most of the standard power flow calculations cannot be
   * carried out right away.
   *
   * <p>The following modifications are made:
   *
   * <ul>
   *   <li>2 winding transformer handling
   *       <ul>
   *         <li>high voltage nodes are marked as slack nodes
   *         <li>high voltage nodes in the {@link RawGridElements#getNodes()} set are replaced with
   *             the new slack marked high voltage nodes
   *         <li>high voltage nodes as part of {@link GraphicElements#getNodeGraphics()} are
   *             replaced with the new slack marked high voltage nodes
   *       </ul>
   *   <li>3 winding transformer handling
   *       <ul>
   *         <li>if node a is located in this subgrid, no changes on 3 winding transformer nodes are
   *             made
   *         <li>if node b or c is located in this grid, the transformers internal node is marked as
   *             slack node and if node a is marked as slack node, this node is unmarked as slack
   *             node
   *         <li>if node a got unmarked as slack, the {@link RawGridElements#getNodes()} gets
   *             adapted accordingly
   *         <li>in any case the internal node of the transformer is added to the {@link
   *             RawGridElements#getNodes()} set
   *       </ul>
   * </ul>
   *
   * @param subGridContainer the subgrid container to be altered
   * @return a copy of the given {@link SubGridContainer} with transformer nodes marked as slack
   */
  public static SubGridContainer withTrafoNodeAsSlack(final SubGridContainer subGridContainer) {

    // transformer 3w
    Map<NodeInput, NodeInput> oldToNewTrafo3WANodes = new HashMap<>();
    Map<Transformer3WInput, NodeInput> newTrafos3wToInternalNode =
        subGridContainer.getRawGrid().getTransformer3Ws().stream()
            .map(
                oldTrafo3w -> {
                  AbstractMap.SimpleEntry<Transformer3WInput, NodeInput> resTrafo3wToInternal;
                  if (oldTrafo3w.getNodeA().getSubnet() == subGridContainer.getSubnet()) {
                    // node A is part of this subgrid -> no slack needed
                    // internal node is needed for node admittance matrix and will be added

                    // add the same transformer again to the new transformer set as nothing has
                    // changed for this transformer
                    resTrafo3wToInternal =
                        new AbstractMap.SimpleEntry<>(oldTrafo3w, oldTrafo3w.getNodeInternal());

                  } else {
                    // node B or C is part of this subgrid -> internal node becomes the new slack

                    // if node A is marked as slack, unmark it as slack
                    NodeInput newNodeA =
                        oldTrafo3w.getNodeA().isSlack()
                            ? copyNode(oldTrafo3w.getNodeA(), false)
                            : oldTrafo3w.getNodeA();

                    // we need to take care for this node in our node sets afterwards
                    // (needs to be replaced by the new nodeA which might have been a slack before)
                    oldToNewTrafo3WANodes.put(oldTrafo3w.getNodeA(), newNodeA);

                    // create an update version of this transformer with internal node as slack and
                    // add it to the newTrafos3wToInternalNode set
                    Transformer3WInput newTrafo3w =
                        copyTransformer3WInputWithInternalSlack(oldTrafo3w, newNodeA);

                    // add the slack
                    resTrafo3wToInternal =
                        new AbstractMap.SimpleEntry<>(newTrafo3w, newTrafo3w.getNodeInternal());
                  }
                  return resTrafo3wToInternal;
                })
            .collect(
                Collectors.toMap(
                    AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

    // get old transformer2w high voltage nodes (nodeA)
    Map<NodeInput, NodeInput> oldToNewTrafo2WANodes =
        subGridContainer.getRawGrid().getTransformer2Ws().stream()
            .map(
                oldTrafo2w -> {
                  NodeInput oldNodeA = oldTrafo2w.getNodeA();
                  NodeInput newNodeA = copyNode(oldNodeA, true);

                  return new AbstractMap.SimpleEntry<>(oldNodeA, newNodeA);
                })
            .distinct()
            .collect(
                Collectors.toMap(
                    AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

    // build the updated 2w transformer with nodeA as slack and add it to our new set
    Set<Transformer2WInput> newTrafos2w =
        subGridContainer.getRawGrid().getTransformer2Ws().stream()
            .map(
                oldTrafo2w ->
                    copyTrafo2WUpdateNodeA(
                        oldTrafo2w, oldToNewTrafo2WANodes.get(oldTrafo2w.getNodeA())))
            .collect(Collectors.toSet());

    // update node input graphics (2 winding transformers and 3 winding transformers)
    /// map old to new
    Map<NodeGraphicInput, NodeGraphicInput> oldToNewNodeGraphics =
        subGridContainer.getGraphics().getNodeGraphics().stream()
            .filter(nodeGraphic -> oldToNewTrafo2WANodes.containsKey(nodeGraphic.getNode()))
            .filter(nodeGraphic -> oldToNewTrafo3WANodes.containsKey(nodeGraphic.getNode()))
            .map(
                oldNodeGraphic -> {
                  NodeInput newNode =
                      oldToNewTrafo2WANodes.containsKey(oldNodeGraphic.getNode())
                          ? oldToNewTrafo2WANodes.get(oldNodeGraphic.getNode())
                          : oldToNewTrafo3WANodes.get(oldNodeGraphic.getNode());

                  return new AbstractMap.SimpleEntry<>(
                      oldNodeGraphic, copyNodeGraphic(oldNodeGraphic, newNode));
                })
            .collect(
                Collectors.toMap(
                    AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));

    /// remove old node graphics, add new ones
    Set<NodeGraphicInput> newNodeGraphics =
        Stream.concat(
                // filter old ones
                subGridContainer.getGraphics().getNodeGraphics().stream()
                    .filter(nodeGraphic -> !oldToNewNodeGraphics.containsKey(nodeGraphic)),
                // add the new trafo2w ones
                oldToNewNodeGraphics.values().stream())
            .collect(Collectors.toSet());

    // update nodes in raw grid by removing the old transformer nodes and add all new ones
    Set<NodeInput> newNodes =
        Stream.concat(
                // filter the old ones (trafo2w, trafo3wToBeRemoved)
                subGridContainer.getRawGrid().getNodes().stream()
                    .filter(node -> !oldToNewTrafo2WANodes.containsKey(node))
                    .filter(node -> !oldToNewTrafo3WANodes.containsKey(node)),
                // add the new ones (trafo2w, trafo3w internal and updated trafo3w nodeA (previous
                // slacks))
                Stream.concat(
                    oldToNewTrafo2WANodes.values().stream(),
                    Stream.concat(
                        newTrafos3wToInternalNode.values().stream(),
                        oldToNewTrafo3WANodes.values().stream())))
            .collect(Collectors.toSet());

    return new SubGridContainer(
        subGridContainer.getGridName(),
        subGridContainer.getSubnet(),
        rawGridCopy4Slacks(
            subGridContainer.getRawGrid(),
            newNodes,
            newTrafos2w,
            newTrafos3wToInternalNode.keySet()),
        subGridContainer.getSystemParticipants(),
        graphicsCopy4Slacks(subGridContainer.getGraphics(), newNodeGraphics));
  }

  /**
   * Make a copy of the provided {@link Transformer3WInput} with an altered nodeA and the internal
   * node marked as slack
   *
   * @param oldTrafo3w the transformer that should be altered
   * @param newNodeA node that should be new node a of the altered transformer
   * @return copy of the provided transformer with the provided nodeA and the internal node marked
   *     as slack
   */
  private static Transformer3WInput copyTransformer3WInputWithInternalSlack(
      Transformer3WInput oldTrafo3w, NodeInput newNodeA) {

    return new Transformer3WInput(
        oldTrafo3w.getUuid(),
        oldTrafo3w.getId(),
        oldTrafo3w.getOperator(),
        oldTrafo3w.getOperationTime(),
        newNodeA,
        oldTrafo3w.getNodeB(),
        oldTrafo3w.getNodeC(),
        oldTrafo3w.getParallelDevices(),
        oldTrafo3w.getType(),
        oldTrafo3w.getTapPos(),
        oldTrafo3w.isAutoTap(),
        true);
  }

  /**
   * Make a copy of the provided {@link NodeGraphicInput} with altered node
   *
   * @param oldNodeGraphic node graphic that should be altered
   * @param newNodeInput new node of the altered node graphic
   * @return copy of the provided node graphic with the provided node as field value
   */
  private static NodeGraphicInput copyNodeGraphic(
      NodeGraphicInput oldNodeGraphic, NodeInput newNodeInput) {
    return new NodeGraphicInput(
        oldNodeGraphic.getUuid(),
        oldNodeGraphic.getGraphicLayer(),
        oldNodeGraphic.getPath(),
        newNodeInput,
        oldNodeGraphic.getPoint());
  }

  /**
   * Make a copy of the provided {@link RawGridElements} with altered nodes, 2w and 3w-transformers
   *
   * @param rawGridElements the raw grid elements that should be altered
   * @param newNodes the exhaustive new set of nodes
   * @param newTrafos2w the exhaustive new set of 2 winding transformers
   * @param newTrafos3w the exhaustive new set of 3 winding transoformers
   * @return copy of the provided rawGridElements with altered nodes, 2w- and 3w-transformers
   */
  private static RawGridElements rawGridCopy4Slacks(
      RawGridElements rawGridElements,
      Set<NodeInput> newNodes,
      Set<Transformer2WInput> newTrafos2w,
      Set<Transformer3WInput> newTrafos3w) {

    return new RawGridElements(
        newNodes,
        rawGridElements.getLines(),
        newTrafos2w,
        newTrafos3w,
        rawGridElements.getSwitches(),
        rawGridElements.getMeasurementUnits());
  }

  /**
   * Make a copy of the provided {@link GraphicElements} with altered node graphics
   *
   * @param graphicElements the graphic elements that should be altered
   * @param newNodeGraphics the exhaustive set of new node graphics
   * @return copy of the provided graphic elements with altered nodes
   */
  private static GraphicElements graphicsCopy4Slacks(
      GraphicElements graphicElements, Set<NodeGraphicInput> newNodeGraphics) {
    return new GraphicElements(newNodeGraphics, graphicElements.getLineGraphics());
  }

  /**
   * Make a copy of the provided {@link Transformer2WInput} with altered nodeA
   *
   * @param trafo2wInput the 2w transformer that should be altered
   * @param newNodeA the node that should become the transformers new nodeA
   * @return copy of the provided transformer with altered nodeA
   */
  private static Transformer2WInput copyTrafo2WUpdateNodeA(
      Transformer2WInput trafo2wInput, NodeInput newNodeA) {
    return new Transformer2WInput(
        trafo2wInput.getUuid(),
        trafo2wInput.getId(),
        trafo2wInput.getOperator(),
        trafo2wInput.getOperationTime(),
        newNodeA,
        trafo2wInput.getNodeB(),
        trafo2wInput.getParallelDevices(),
        trafo2wInput.getType(),
        trafo2wInput.getTapPos(),
        trafo2wInput.isAutoTap());
  }

  /**
   * Make a copy of the provided {@link NodeInput} and mark or unmark it as slack
   *
   * @param nodeInput the node that should be marked as slack
   * @param isSlack whether the new node should be marked as slack or not
   * @return copy of the provided node either marked as slack or not
   */
  private static NodeInput copyNode(NodeInput nodeInput, boolean isSlack) {
    return new NodeInput(
        nodeInput.getUuid(),
        nodeInput.getId(),
        nodeInput.getOperator(),
        nodeInput.getOperationTime(),
        nodeInput.getvTarget(),
        isSlack,
        nodeInput.getGeoPosition(),
        nodeInput.getVoltLvl(),
        nodeInput.getSubnet());
  }
}
