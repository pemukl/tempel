package application;

import org.telegram.abilitybots.api.sender.SilentSender;

import java.util.LinkedList;

public class Player {
    private static LinkedList<Player> players;
    private Game currentGame;
    private String name;
    private long ID;
    public SilentSender silent;


    Player(long ID, Game game){
        if (players==null){
            players=new LinkedList<>();
        }
        players.add(this);
        this.currentGame = game;
        this.ID=ID;
    }

    public void setSilent(SilentSender silent){
        this.silent = silent;
    }

    public Game getGame(){
        return currentGame;
    }

    public void setName(String name){
        this.name=name;
        currentGame.silent.send("Dein Name wurde festgelegt, " + name, getID());
        currentGame.addPlayer(this);
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
