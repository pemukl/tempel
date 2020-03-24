public enum Distribution {

    Spieler2(1, 1, 3, 3, 4),
    Spieler3(2, 2, 8, 5, 2),
    Spieler4(3, 2, 12, 6, 2),
    Spieler5(3, 2, 16, 7, 2),
    Spieler6(4, 2, 20, 8, 2),
    Spieler7(5, 3, 26, 7, 2),
    Spieler8(6, 3, 30, 8, 2),
    Spieler9(6, 3, 34, 9, 2),
    Spieler10(7, 4, 37, 10, 3);

    private final int abenteurer;
    private final int waechterinnen;
    private final int leer;
    private final int gold;
    private final int feuerfallen;

    Distribution(int abenteurer, int waechterinnen, int leer, int gold, int feuerfallen) {
        this.abenteurer = abenteurer;
        this.waechterinnen = waechterinnen;
        this.leer = leer;
        this.gold = gold;
        this.feuerfallen = feuerfallen;
    }

    public static Distribution getDistribution(int numPlayers) {
        switch (numPlayers) {
            case 3:
                return Distribution.Spieler3;
            case 4:
                return Distribution.Spieler4;
            case 5:
                return Distribution.Spieler5;
            case 6:
                return Distribution.Spieler6;
            case 7:
                return Distribution.Spieler7;
            case 8:
                return Distribution.Spieler8;
            case 9:
                return Distribution.Spieler9;
            case 10:
                return Distribution.Spieler10;
            default:
                return Distribution.Spieler2;
        }
    }

    public int getAbenteurer() {
        return abenteurer;
    }

    public int getWaechterinnen() {
        return waechterinnen;
    }

    public int getLeer() {
        return leer;
    }

    public int getGold() {
        return gold;
    }

    public int getFeuerfallen() {
        return feuerfallen;
    }
}