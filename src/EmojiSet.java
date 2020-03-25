public enum EmojiSet{
    WORDS("*Feuerfalle*", "*Gold*", "*Leer*", "-","*Wächterin*","*Abenteurer*", "*(X)*"),
    NEAT("\uD83D\uDD25", "\uD83D\uDCB0", "\uD83D\uDCED", "\uD83D\uDCEB", "\uD83D\uDC79", "\uD83D\uDD26", "\uD83D\uDD11");

    private String fire;
    private String gold;
    private String empty;
    private String guard;
    private String adventurer;
    private String closed;
    private String key;

    EmojiSet(String fire, String gold, String empty, String closed, String guard, String adventurer, String key) {
        this.fire = fire;
        this.gold = gold;
        this.closed = closed;
        this.empty = empty;
        this.guard = guard;
        this.adventurer = adventurer;
        this.key = key;
    }
    public String fire() { return fire; }
    public String gold() { return gold; }
    public String empty() { return empty; }
    public String guard() { return guard; }
    public String adventurer() { return adventurer; }
    public String closed() { return closed; }
    public String key() { return key; }

}