public class Card {
    public enum Content {
        LEER,
        GOLD,
        FEUERFALLE;
    }
    public final Content state;
    private boolean exposed;
    private Game game;

    public Card(Content state,Game game){
        this.state = state;
        this.exposed = false;
        this.game = game;
    }

    public void expose(){
        this.exposed=true;
    }

    public String getEmoji(){
        switch (this.state){
            case FEUERFALLE:
                return game.texture.fire();
            case GOLD:
                return game.texture.gold();
            case LEER:
                return game.texture.empty();
        }
        return "no emoji found";
    }

    public String getHidden(){
        if (exposed){
            return this.getEmoji();
        } else {
            return game.texture.closed();
        }
    }

    public boolean isExposed(){
        return exposed;
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