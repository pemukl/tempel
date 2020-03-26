public class Card {
    public enum Content {
        LEER,
        GOLD,
        FEUERFALLE;
    }
    public final Content state;
    public boolean exposed;

    public Card(Content state){
        this.state = state;
        this.exposed = false;
    }

    public void expose(){
        this.exposed=true;
    }

    public String getEmoji(){
        switch (this.state){
            case FEUERFALLE:
                return PlayNowBot.texturePack.fire();
            case GOLD:
                return PlayNowBot.texturePack.gold();
            case LEER:
                return PlayNowBot.texturePack.empty();
        }
        return "no emoji found";
    }

    public String getHidden(){
        if (exposed){
            return this.getEmoji();
        } else {
            return PlayNowBot.texturePack.closed();
        }
    }

    @Override
    public String toString() {
        if (this.state == Content.FEUERFALLE)
            return "Feuerfalle";
        else if (this.state == Content.GOLD)
            return "Gold";
        else
            return "Leer";
    }
}