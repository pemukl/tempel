package application;

import org.telegram.abilitybots.api.sender.SilentSender;

import java.util.ArrayList;

public class Game {
    private long ID;
    private String name;
    private ArrayList<Player> players = new ArrayList();
    public SilentSender silent;

    Game(long chatID){
        this.ID = chatID;
    }

    public void setSilent(SilentSender silent){
        this.silent = silent;
    }

    public long getID() {
        return ID;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void addPlayer(Player player){
        players.add(player);
        silent.send("Spieler " + player.getName() + " ist dem Spiel beigetreten.",getID());
    }

    public Player findPlayer(long ID){
        for (Player player: players) {
            if(player.getID() == ID)
                return player;
        }
        return null;
    }

    public void play(){
        silent.send("Play Method called on Game! Following Players registred:",getID());
        for (Player player: players ) {
            silent.send(player.getName(),getID());
        }
    }

}
