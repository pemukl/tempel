package main;

public enum EmojiSet{
    //WORDS("*Feuerfalle*", "*Gold*", "*Leer*", "-", "*Wächterin*","*Abenteurer*", "*(X)*",
    //        "StickerAdventurer",
     //       "StickerGuard"),
    NEAT("\uD83D\uDD25", "\uD83D\uDCB0", "\uD83D\uDCED" , "\uD83D\uDCEB", "\uD83D\uDE08", "\uD83D\uDE07", "\uD83D\uDD11",
            "CAACAgIAAxkBAAIODV57YBlg3HdFI7BDXf1vkXM8n5fqAAIgAAOWn4wOrP1BM_Sqb_kYBA",
            "CAACAgIAAxkBAAINUV57S2xDYExq74na24Sy4u4FeYi_AAKmAAP3AsgPqwzk86kqxlgYBA"),
    OLD("\uD83D\uDCA9", "\uD83D\uDC51", "\uD83D\uDD70" , "\uD83E\uDDF3", "\uD83D\uDC79", "\uD83D\uDD26", "\uD83D\uDDDD",
            "CAACAgIAAxkBAAIeyF6A90NmNHTxdzx-0KkVRATF7y-wAAJdAQACFkJrClEqo1WBBrUdGAQ",
            "CAACAgIAAxkBAAIexl6A9ndjZgnaG9j0QlkbvD-bxnVLAAItAAMNttIZUy3GocSR-3IYBA"),
    CORONA("\uD83E\uDDA0", "\uD83D\uDC8A", "\u23F3", "\u2623", "\uD83D\uDC6F", "\uD83D\uDC6E", "\uD83E\uDDFB",
            "CAACAgIAAxkBAAIZiV58BDxbWS__TuxhQHY84_I4abBYAAIpAAOQ_ZoVDYC2OEIbKcsYBA",
            "CAACAgIAAxkBAAIZh158BCIsjdHil0OBLtvj0SAxySNgAALWAQACVp29CqFJ-1mHfv8RGAQ"),
    CLIMATE("\uD83D\uDEE2",  "\u267B\uFE0F", "\uD83D\uDCA8","\uD83E\uDDF0", "\uD83C\uDFA3", "\uD83D\uDC69\u200D\uD83C\uDF3E","\uD83C\uDF0F",
            "CgACAgQAAxkBAAIdxF6AhbQaGIbAxjvX5W9n-XHnNNv_AAImAgACeBjtUsD-e3D0OR9WGAQ",
            "CgACAgIAAxkBAAIeWV6A1tGDfpwiio2HxeBZaJHDIoJ8AAKqBgACsMAJSKB5v7gQFHCDGAQ");



    private String fire;
    private String gold;
    private String empty;
    private String guard;
    private String adventurer;
    private String closed;
    private String key;
    private String stickerAdventurer;
    private String stickerGuard;



    EmojiSet(String fire, String gold, String empty, String closed, String guard, String adventurer, String key,String stickerAdventurer,String stickerGuard) {
        this.fire = fire;
        this.gold = gold;
        this.closed = closed;
        this.empty = empty;
        this.guard = guard;
        this.adventurer = adventurer;
        this.key = key;
        this.stickerAdventurer = stickerAdventurer;
        this.stickerGuard = stickerGuard;
    }
    public String fire() { return fire; }
    public String gold() { return gold; }
    public String empty() { return empty; }
    public String guard() { return guard; }
    public String adventurer() { return adventurer; }
    public String closed() { return closed; }
    public String key() { return key; }
    public String stickerAdventurer(){
        return stickerAdventurer;
    }
    public String stickerGuard(){
        return stickerGuard;
    }

}