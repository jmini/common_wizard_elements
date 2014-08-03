package xmltransfo;

public enum NodeName {
  G("g"),
  PATH("path"),
  TEXT("text"),
  TSPAN("tspan"),
  METADATA("metadata"),
  DEFS("defs");

  private String name;

  private NodeName(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
