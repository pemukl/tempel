package main;

public enum Role {
    ABENTEURER,
    WAECHTERIN;

    public String getEmoji(Game game){
        switch (this){
            case ABENTEURER:
                return game.texture.adventurer();
            case WAECHTERIN:
                return game.texture.guard();
        }
        return "no emoji found";
    }

    public String getStickerID(Game game){
        switch (this){
            case ABENTEURER:
                return game.texture.stickerAdventurer();
            case WAECHTERIN:
                return game.texture.stickerGuard();
        }
        return null;
    }

    @Override
    public String toString() {
        if (this == ABENTEURER)
            return "Abenteurer";
        else
            return "Wächterin";
    }
}