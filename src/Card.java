public enum Card implements Comparable<Card>{
    LEER ("Leer", 0),
    GOLD("Gold", 1),
    FEUERFALLE("Feuerfalle", 2);

    private String name;
    private int index;

    Card(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }
}