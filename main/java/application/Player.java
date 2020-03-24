package application;

import org.telegram.abilitybots.api.sender.SilentSender;

import java.util.*;

public class Player {
    private final String name;
    private final long ID;
    
    private Role role;
    private List<Card> cards;
    private boolean hasKey;
    
    private static LinkedList<Player> players;
    private Game currentGame;
    public SilentSender silent;


    public Player(long ID, Game game, List<Card> cards, boolean hasKey){
        if (players==null){
            players=new LinkedList<>();
        }
        players.add(this);
        this.currentGame = game;
        this.ID=ID;
        this.cards = new ArrayList<>();
        this.hasKey = false;
    }
    
    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setSilent(SilentSender silent){
        this.silent = silent;
    }

    public void setName(String name){
        this.name=name;
        currentGame.silent.send("Dein Name wurde festgelegt, " + name, getID());
        currentGame.addPlayer(this);
    } 
    
    public List<Card> getCards() {
        return cards;
    }
    
    public Role getRole() {
        return role;
    }
    
    public boolean isHasKey() {
        return hasKey;
    }
    
    public Game getGame(){
        return currentGame;
    }

    public String getName(){
        return this.name;
    }

    public long getID(){
        return ID;
    }

    public static Player getPlayer(long ID){
        System.out.println("searching for "+ID+" in " + players);
        for (Player player: players) {
            if (player.getID() == ID)
                return player;
        }
        return null;
    }

}
