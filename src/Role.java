public enum Role {
    ABENTEURER,
    WAECHTERIN;

    public String getEmoji(){
        switch (this){
            case ABENTEURER:
                return PlayNowBot.texturePack.adventurer();
            case WAECHTERIN:
                return PlayNowBot.texturePack.guard();
        }
        return "no emoji found";
    }

    @Override
    public String toString() {
        if (this == ABENTEURER)
            return "Abenteurer";
        else
            return "Wächterin";
    }
}