public enum Card {
    LEER,
    GOLD,
    FEUERFALLE;

    public String getEmoji(){
        switch (this){
            case FEUERFALLE:
                return PlayNowBot.texturePack.fire();
            case GOLD:
                return PlayNowBot.texturePack.gold();
            case LEER:
                return PlayNowBot.texturePack.empty();
        }
        return "no emoji found";
    }

    public String getClosed(){
        return PlayNowBot.texturePack.closed();
    }

    @Override
    public String toString() {
        if (this == FEUERFALLE)
            return "Feuerfalle";
        else if (this == GOLD)
            return "Gold";
        else
            return "Leer";
    }
}