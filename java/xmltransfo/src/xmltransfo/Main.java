package xmltransfo;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public class Main {
  private static final String ATTR_ID = "id";
  private static final String ATTR_STYLE = "style";
  private static final String ATTR_CLIP_PATH = "clip-path";
  private static final String ATTR_TRANSFORM = "transform";

  private static final Pattern PATTERN_URL = Pattern.compile("url\\(#([a-zA-Z]+[0-9]+)\\)");
  private static final Pattern PATTERN_MATRIX = Pattern.compile("matrix\\([0-9\\-\\.]+,[0-9\\-\\.]+,[0-9\\-\\.]+,[0-9\\-\\.]+,([0-9\\-\\.]+),([0-9\\-\\.]+)\\)");
  private static final Pattern PATTERN_BACKGROUND = Pattern.compile("fill:url\\(#(linearGradient[0-9]+)\\);fill\\-opacity:0\\.09999;fill-rule:nonzero;stroke:none");

  private static final String BORDER_STYLE = "fill:none;stroke:#000000;stroke-opacity:1;stroke-width:1;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none";
  private static final BigDecimal MX = new BigDecimal(-1.251151325);
  private static final BigDecimal AX = new BigDecimal(0.18248);
  private static final BigDecimal MY = new BigDecimal(1.250271263);
  private static final BigDecimal AY = new BigDecimal(83.520273);

  public static void main(String[] args) throws Exception {
    DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    File folder = new File("../../resources/svg");
    Document iDoc = docBuilder.parse(new File(folder, "common_wizard_elements.svg"));
    File folderWithBoderAndBackground = new File(folder, "with_border_and_background");
    File folderWithBackground = new File(folder, "with_background");
    File folderIconOnly = new File(folder, "icon_only");
    folderWithBoderAndBackground.mkdir();
    folderWithBackground.mkdir();
    folderIconOnly.mkdir();

    Map<String, RectBound> grid = prepareGrid(iDoc);
    for (Entry<String, RectBound> e : grid.entrySet()) {
//      System.out.println(e.getKey() + " => " + e.getValue());
      cropNodes(docBuilder, iDoc, e.getValue(), true, true, folderWithBoderAndBackground, e.getKey());
      cropNodes(docBuilder, iDoc, e.getValue(), false, true, folderWithBackground, e.getKey());
      cropNodes(docBuilder, iDoc, e.getValue(), false, false, folderIconOnly, e.getKey());
    }

//    String name = "access";
//    RectBound rect = RectBound.newBoundUsingCoordinates(new BigDecimal("-69.3901"), new BigDecimal("5.6099"), new BigDecimal("1386.4829"), new BigDecimal("1452.4829"));
//    String name = "datapool";
//    RectBound rect = RectBound.newBoundUsingCoordinates(new BigDecimal("605.5693"), new BigDecimal("680.5693"), new BigDecimal("1173.6592"), new BigDecimal("1239.6592"));
//    cropNodes(docBuilder, iDoc, rect, true, true, folderWithBoderAndBackground, name);
//    cropNodes(docBuilder, iDoc, rect, false, true, folderWithBackground, name);
//    cropNodes(docBuilder, iDoc, rect, false, false, folderIconOnly, name);

//    cropNodes(docBuilder, iDoc, "g298593", "g300913", folder, "access");
//    cropNodes(docBuilder, iDoc, "g393413", "g393831", folder, "datapool");

//    Document doc = docBuilder.parse(new File("../../resources/svg/common_wizard_elements.svg"));
//    toFile(iDoc, new File("../../resources/svg/common_wizard_elements_pretty.svg"));
  }

  private static Map<String, RectBound> prepareGrid(Document iDoc) throws Exception {
    Map<String, RectBound> map = new HashMap<String, RectBound>();

    Node iSvg = iDoc.getChildNodes().item(1);
    Node iG = iSvg.getChildNodes().item(3);

    NodeList childNodes = iG.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      try {
        i = findNodeIndex(iG, NodeName.PATH, ATTR_STYLE, BORDER_STYLE, i);
        Node iBorderNode = childNodes.item(i);
        RectBound borderRect = toRectBound(iBorderNode.getAttributes().getNamedItem("d").getNodeValue());
        i = i + 1;
        Node iNextBorderNode = childNodes.item(i);
        if (!isBorder(iNextBorderNode)) {
          throw new IllegalStateException("Was expecting a second border after " + iBorderNode.getAttributes().getNamedItem("id").getNodeValue());
        }
        NamedNodeMap attributes = iNextBorderNode.getAttributes();
        RectBound textRect = toRectBound(attributes.getNamedItem("d").getNodeValue());

        StringBuilder sb = new StringBuilder();
        boolean hasMore = false;
        List<Node> iTextList = findTextNodeInRect(iG, textRect);
        for (Node iText : iTextList) {
          Node iTspan = iText.getFirstChild();
          if (!nodeEquals(NodeName.TSPAN, iTspan)) {
            throw new IllegalStateException("Was expecting a tspan after text");
          }
          if (hasMore) {
            sb.append(" ");
          }
          sb.append(iTspan.getTextContent());
          hasMore = true;
        }
        String name = sb.toString();

        name = name.toLowerCase().replaceAll(" ", "_").replaceAll("/", "_").replaceAll("\\.\\.\\.", "_").replaceAll("\\.", "").replaceAll(",", "").replaceAll("\\+", "plus").replaceAll("&", "and").replaceAll("__", "_");
        if (name.equals("c__cplusplus")) {
          name = "c_cplusplus";
        }
        else if (name.equals("façade")) {
          name = "facade";
        }
        else if (name.equals("file")) {
          name = "text_document";
        }

        name = ensureUnique(map, name);
        map.put(name, borderRect);
      }
      catch (NotFoundException e) {
        i = childNodes.getLength();
      }
    }

    return map;
  }

  private static String ensureUnique(Map<String, RectBound> map, String name) {
    String suffix = "";
    int i = 1;
    while (map.containsKey(name + suffix)) {
      i++;
      suffix = "_" + i;
    }
    return name + suffix;
  }

  private static boolean isBorder(Node node) {
    NamedNodeMap attributes = node.getAttributes();
    Node attrStyle = attributes.getNamedItem(ATTR_STYLE);
    if (attrStyle != null) {
      return BORDER_STYLE.equals(attrStyle.getNodeValue());
    }
    return false;
  }

  private static boolean isBackground(Node node) {
    NamedNodeMap attributes = node.getAttributes();
    Node attrStyle = attributes.getNamedItem(ATTR_STYLE);
    if (attrStyle != null) {
      String attrStyleValue = attrStyle.getNodeValue();
      if (attrStyleValue != null) {
        Matcher matcher = PATTERN_BACKGROUND.matcher(attrStyleValue);
        return matcher.matches();
      }
    }
    return false;
  }

  private static void cropNodes(DocumentBuilder docBuilder, Document iDoc, RectBound rect, boolean includeBorder, boolean includeBackground, File folder, String name) throws Exception {
    Document oDoc = docBuilder.newDocument();

    Node iComment = iDoc.getChildNodes().item(0);
    Node oCommtent = oDoc.importNode(iComment, false);
    Node iSvg = iDoc.getChildNodes().item(1);
    Node oSvg = oDoc.importNode(iSvg, false);
    oSvg.getAttributes().getNamedItem("height").setNodeValue("85");
    oSvg.getAttributes().getNamedItem("width").setNodeValue("96");

    oDoc.appendChild(oCommtent);
    oDoc.appendChild(oSvg);

    Node oG = null;
    Node iG = null;
    Node oDefs = null;
    Node iDefs = null;
    NodeList childNodes = iSvg.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node iNode = childNodes.item(i);
      if (nodeEquals(NodeName.METADATA, iNode)) {
        Node oNode = oDoc.importNode(iNode, true);
        oSvg.appendChild(oNode);
      }
      else if (nodeEquals(NodeName.DEFS, iNode)) {
        Node oNode = oDoc.importNode(iNode, false);
        oSvg.appendChild(oNode);
        iDefs = iNode;
        oDefs = oNode;
      }
      else if (nodeEquals(NodeName.G, iNode)) {
        Node oNode = oDoc.importNode(iNode, false);
        oSvg.appendChild(oNode);
        iG = iNode;
        oG = oNode;
      }
    }
    if (iG == null) {
      throw new IllegalStateException("iG can not be null");
    }
    if (oG == null) {
      throw new IllegalStateException("oG can not be null");
    }
    if (iDefs == null) {
      throw new IllegalStateException("iDefs can not be null");
    }
    if (oDefs == null) {
      throw new IllegalStateException("oDefs can not be null");
    }
    if (oSvg.getChildNodes().getLength() != 3) {
      throw new IllegalStateException("svg tag in the out doc should have 3 children, have: " + oSvg.getChildNodes().getLength());
    }

    oG.getAttributes().getNamedItem("transform").setNodeValue("matrix(1.25,0,0,-1.25," + calcTx(rect.getX1()) + "," + calcTy(rect.getY1()) + ")");

    List<String> listDefs = new ArrayList<String>();
    NodeList gChildNodes = iG.getChildNodes();
    for (int i = 0; i < gChildNodes.getLength(); i++) {
      Node iNode = gChildNodes.item(i);

      if (nodeEquals(NodeName.G, iNode)) {
        if (isGInRect(iNode, rect)) {
          Node oNode = oDoc.importNode(iNode, true);
          oG.appendChild(oNode);

          checkAttrAndAddToList(listDefs, iNode);
        }
      }
      else if (nodeEquals(NodeName.PATH, iNode)) {
        if (isPathInRect(iNode, rect) && (includeBorder || !isBorder(iNode)) && (includeBackground || !isBackground(iNode))) {
          Node oNode = oDoc.importNode(iNode, true);
          oG.appendChild(oNode);

          checkAttrAndAddToList(listDefs, iNode);
        }
      }
    }

    NodeList defsChildNodes = iDefs.getChildNodes();
    for (int i = 0; i < defsChildNodes.getLength(); i++) {
      Node iNode = defsChildNodes.item(i);
      NamedNodeMap attributes = iNode.getAttributes();
      if (attributes != null) {
        Node idAttr = attributes.getNamedItem(ATTR_ID);
        if (idAttr != null && listDefs.contains(idAttr.getNodeValue())) {
          Node oNode = oDoc.importNode(iNode, true);
          oDefs.appendChild(oNode);
        }
      }
    }

    toFile(oDoc, new File(folder, name + ".svg"));
  }

  /**
   * @param x
   * @return
   */
  static String calcTx(BigDecimal x) {
    return "" + x.multiply(MX).add(AX).intValue();
  }

  /**
   * @param y
   * @return
   */
  static String calcTy(BigDecimal y) {
    return "" + y.multiply(MY).add(AY).intValue();
  }

  private static void cropNodes(DocumentBuilder docBuilder, Document iDoc, String startNodeId, String endNodeId, File folder, String name) throws Exception {
    //startNodeId is included
    //endNodeId is excluded

    Document oDoc = docBuilder.newDocument();

    Node iComment = iDoc.getChildNodes().item(0);
    Node oCommtent = oDoc.importNode(iComment, false);
    Node iSvg = iDoc.getChildNodes().item(1);
    Node oSvg = oDoc.importNode(iSvg, false);
    oDoc.appendChild(oCommtent);
    oDoc.appendChild(oSvg);

    Node oG = null;
    Node iG = null;
    Node oDefs = null;
    Node iDefs = null;
    NodeList childNodes = iSvg.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node iNode = childNodes.item(i);
      if (nodeEquals(NodeName.METADATA, iNode)) {
        Node oNode = oDoc.importNode(iNode, true);
        oSvg.appendChild(oNode);
      }
      else if (nodeEquals(NodeName.DEFS, iNode)) {
        Node oNode = oDoc.importNode(iNode, false);
        oSvg.appendChild(oNode);
        iDefs = iNode;
        oDefs = oNode;
      }
      else if (nodeEquals(NodeName.G, iNode)) {
        Node oNode = oDoc.importNode(iNode, false);
        oSvg.appendChild(oNode);
        iG = iNode;
        oG = oNode;
      }
    }
    if (iG == null) {
      throw new IllegalStateException("iG can not be null");
    }
    if (oG == null) {
      throw new IllegalStateException("oG can not be null");
    }
    if (iDefs == null) {
      throw new IllegalStateException("iDefs can not be null");
    }
    if (oDefs == null) {
      throw new IllegalStateException("oDefs can not be null");
    }
    if (oSvg.getChildNodes().getLength() != 3) {
      throw new IllegalStateException("svg tag in the out doc should have 3 children, have: " + oSvg.getChildNodes().getLength());
    }

    int start = findNodeIndex(iG, NodeName.G, ATTR_ID, startNodeId, 0);
    int end = findNodeIndex(iG, NodeName.G, ATTR_ID, endNodeId, start);

    List<String> listDefs = new ArrayList<String>();
    for (int i = start; i < end; i++) {
      Node iNode = iG.getChildNodes().item(i);
      if (nodeEquals(NodeName.G, iNode)) {
        Node oNode = oDoc.importNode(iNode, true);
        oG.appendChild(oNode);

        checkAttrAndAddToList(listDefs, iNode);
      }
      else if (nodeEquals(NodeName.PATH, iNode)) {
        Node oNode = oDoc.importNode(iNode, true);
        oG.appendChild(oNode);

        checkAttrAndAddToList(listDefs, iNode);
      }
    }

    NodeList defsChildNodes = iDefs.getChildNodes();
    for (int i = 0; i < defsChildNodes.getLength(); i++) {
      Node iNode = defsChildNodes.item(i);
      NamedNodeMap attributes = iNode.getAttributes();
      if (attributes != null) {
        Node idAttr = attributes.getNamedItem(ATTR_ID);
        if (idAttr != null && listDefs.contains(idAttr.getNodeValue())) {
          Node oNode = oDoc.importNode(iNode, true);
          oDefs.appendChild(oNode);
        }
      }
    }

    toFile(oDoc, new File(folder, name + ".svg"));
  }

  /**
   * @param iNode
   * @param rect
   * @return
   */
  private static boolean isGInRect(Node root, RectBound rect) {
    NodeList childNodes = root.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (nodeEquals(NodeName.G, node)) {
        if (!isGInRect(node, rect)) {
          return false;
        }
      }
      else if (nodeEquals(NodeName.PATH, node)) {
        if (!isPathInRect(node, rect)) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * @param iNode
   * @param rect
   * @return
   */
  private static boolean isPathInRect(Node node, RectBound rect) {
    NamedNodeMap attributes = node.getAttributes();
    String dAttr = attributes.getNamedItem("d").getNodeValue();
    if (dAttr.isEmpty()) {
      return true;
    }
    RectBound r = toRectBound(dAttr);
    return containsRect(rect, r);
  }

  /**
   * @param r
   * @param rect
   * @return
   */
  static boolean containsRect(RectBound rootRect, RectBound rect) {
    return containsPoint(rootRect, rect.getX1(), rect.getY1()) && containsPoint(rootRect, rect.getX2(), rect.getY2());
  }

  /**
   * @param nodeValue
   * @return
   */
  static RectBound toRectBound(String dAttr) {
    String[] parts = dAttr.split(" ");
    PathMode mode = null;
    PathPosition pathPosition = null;

    int end;
    if ("z".equals(parts[parts.length - 1])) {
      end = parts.length - 1;
    }
    else {
      end = parts.length;
    }

    BigDecimal xMin = null;
    BigDecimal xMax = null;
    BigDecimal yMin = null;
    BigDecimal yMax = null;
    BigDecimal x = BigDecimal.ZERO;
    BigDecimal y = BigDecimal.ZERO;

    for (int i = 0; i < end; i++) {
      String part = parts[i];
      if (part.matches("[a-zA-Z]")) {
        if (part.matches("[a-z]")) {
          pathPosition = PathPosition.RELATIVE;
        }
        else {
          pathPosition = PathPosition.ABSOLUTE;
        }
        part = part.toLowerCase();
        if ("m".equals(part)) {
          mode = PathMode.MOVETO;
        }
        else if ("l".equals(part)) {
          mode = PathMode.LINETO;
        }
        else if ("c".equals(part)) {
          mode = PathMode.CURVETO;
        }
        else if ("z".equals(part)) {
          mode = PathMode.CLOSEPATH;
          continue;
        }
        i = i + 1;
        part = parts[i];
      }

      if (mode == PathMode.CURVETO) {
        if (i + 2 >= end) {
          throw new IllegalStateException("ArrayIndexOutOfBounds. part is: '" + part + "'. d is: '" + dAttr + "'");
        }
        i = i + 2;
        part = parts[i];
      }

      String[] coords = part.split(",");
      if (coords.length != 2) {
        throw new IllegalStateException("Was expecting length==2. part is: '" + part + "'. d is: " + dAttr);
      }
      if (pathPosition == PathPosition.RELATIVE) {
        x = new BigDecimal(coords[0]).add(x);
        y = new BigDecimal(coords[1]).add(y);
      }
      else {
        x = new BigDecimal(coords[0]);
        y = new BigDecimal(coords[1]);
      }
      if (xMin == null || xMin.compareTo(x) > 0) {
        xMin = x;
      }
      if (xMax == null || xMax.compareTo(x) < 0) {
        xMax = x;
      }
      if (yMin == null || yMin.compareTo(y) > 0) {
        yMin = y;
      }
      if (yMax == null || yMax.compareTo(y) < 0) {
        yMax = y;
      }
    }
    return RectBound.newBoundUsingCoordinates(xMin, xMax, yMin, yMax);
  }

  /**
   * @param listDefs
   * @param node
   */
  private static void checkAttrAndAddToList(List<String> listDefs, Node node) {
    if (nodeEquals(NodeName.G, node)) {
      Node attr = node.getAttributes().getNamedItem(ATTR_CLIP_PATH);
      addAttrToList(listDefs, attr);

      NodeList childNodes = node.getChildNodes();
      for (int i = 0; i < childNodes.getLength(); i++) {
        Node n = childNodes.item(i);
        checkAttrAndAddToList(listDefs, n);
      }
    }
    else if (nodeEquals(NodeName.PATH, node)) {
      Node attr = node.getAttributes().getNamedItem(ATTR_STYLE);
      addAttrToList(listDefs, attr);
    }
  }

  private static void addAttrToList(List<String> list, Node attr) {
    if (attr != null) {
      String nodeValue = attr.getNodeValue();
      if (nodeValue != null) {
        Matcher matcher = PATTERN_URL.matcher(nodeValue);
        if (matcher.find()) {
          list.add(matcher.group(1));
        }
      }
    }
  }

  private static int findNodeIndex(Node root, NodeName nodeName, String attrName, String attrValue, int startAt) throws NotFoundException {
    NodeList childNodes = root.getChildNodes();
    for (int i = startAt; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (nodeEquals(nodeName, node)) {
        NamedNodeMap attributes = node.getAttributes();
        Node attribute = attributes.getNamedItem(attrName);
        if (attribute != null) {
          String id = attribute.getNodeValue();
          if (attrValue.equals(id)) {
            return i;
          }
        }
      }
    }
    throw new NotFoundException("Not found: " + nodeName.getName() + " with attribute " + attrName + "=\"" + attrValue + "\".");
  }

  private static List<Node> findTextNodeInRect(Node root, RectBound rect) throws NotFoundException {
    ArrayList<Node> list = new ArrayList<Node>();
    NodeList childNodes = root.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      if (nodeEquals(NodeName.TEXT, node)) {
        NamedNodeMap attributes = node.getAttributes();
        Node attribute = attributes.getNamedItem(ATTR_TRANSFORM);
        if (attribute != null) {
          String nodeValue = attribute.getNodeValue();
          if (nodeValue != null) {
            Matcher matcher = PATTERN_MATRIX.matcher(nodeValue);
            if (matcher.find()) {
              BigDecimal x = new BigDecimal(matcher.group(1));
              BigDecimal y = new BigDecimal(matcher.group(2));
              if (containsPoint(rect, x, y)) {
                list.add(node);
              }
            }
          }
        }
      }
    }
    if (list.isEmpty()) {
      throw new NotFoundException("Not found: " + NodeName.TEXT.getName() + " with attribute " + ATTR_TRANSFORM + "=\"matrix(..)\".");
    }
    return list;
  }

  /**
   * @param rootRect
   * @param x
   * @param y
   * @return
   */
  static boolean containsPoint(RectBound rootRect, BigDecimal x, BigDecimal y) {
    if (rootRect.getX1().compareTo(x) > 0) {
      return false;
    }
    if (rootRect.getX2().compareTo(x) < 0) {
      return false;
    }
    if (rootRect.getY1().compareTo(y) > 0) {
      return false;
    }
    if (rootRect.getY2().compareTo(y) < 0) {
      return false;
    }
    return true;
  }

  private static boolean nodeEquals(NodeName nodeName, Node node) {
    return nodeName.getName().equals(node.getNodeName());
  }

  private static void toFile(Document doc, File file) throws Exception {
    Source source = new DOMSource(doc);
    Result result = new StreamResult(file);

    // transformer:
    Transformer transfo = TransformerFactory.newInstance().newTransformer();

    // Transformer configuration:
    transfo.setOutputProperty(OutputKeys.METHOD, "xml");
    transfo.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transfo.setOutputProperty(OutputKeys.ENCODING, "utf-8");
    transfo.setOutputProperty(OutputKeys.INDENT, "yes");
    transfo.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

    transfo.transform(source, result);
  }

  private static void printNode(Node node) {
    System.out.print("<" + node.getNodeName() + " ");

    NamedNodeMap attr = node.getAttributes();
    for (int i = 0; i < attr.getLength(); i++) {
      Attr a = (Attr) attr.item(i);
      System.out.print(a.getName() + "=\"" + a.getNodeValue() + "\" ");
    }
    System.out.println(">");

    for (Node n = node.getFirstChild(); n != null; n = n.getNextSibling()) {
      switch (n.getNodeType()) {
        case Node.ELEMENT_NODE:
          printNode((Element) n);
          break;
        case Node.TEXT_NODE:
          String data = ((Text) n).getData();
          System.out.print(data);
          break;
      }
    }
    System.out.println("</" + node.getNodeName() + ">");
  }
}
