public enum EmojiSet{
    WORDS("*Feuerfalle*", "*Gold*", "*Leer*","*Wächterin*","*Abenteurer*"),
    NEAT("\uD83D\uDCB0", "\uD83D\uDD25", "\uD83D\uDCED", "\uD83D\uDC79", "\uD83D\uDD26");

    private String fire;
    private String gold;
    private String empty;
    private String guard;
    private String adventurer;
    EmojiSet(String fire, String gold, String empty, String guard, String adventurer) {
        this.fire = fire;
        this.gold = gold;
        this.empty = empty;
        this.guard = guard;
        this.adventurer = adventurer;
    }
    public String fire() { return fire; }
    public String gold() { return gold; }
    public String empty() { return empty; }
    public String guard() { return guard; }
    public String adventurer() { return adventurer; }

}