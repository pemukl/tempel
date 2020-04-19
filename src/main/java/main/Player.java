package main;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class Player {
    private static Map<String,String> names;

    private final long id;

    private Role role;
    private SetOfCards cards;
    private String userName;
    public boolean knowsHisCards;

    protected Game currentGame;

    public Player(long id, String userName, Game game) {
        initializeNames();
        this.currentGame = game;
        this.id = id;
        this.userName = userName;
        this.cards = new SetOfCards();
    }



    public void setCards(SetOfCards cards) {
        this.cards = cards;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setName(String name) {
        addName(name,id);
    }

    public static void addName(String name, long playerId) {
        initializeNames();
        names.put(String.valueOf(playerId),name);
        Properties properties = new Properties();

        for (Map.Entry<String,String> entry : names.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }

        try {
            properties.store(new FileOutputStream("names.properties"), null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializeNames() {
        if (names==null){
            names = new HashMap<String, String>();
            Properties properties = new Properties();
            File file = new File("names.properties");

            try {
                properties.load(new FileInputStream(file));
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (String key : properties.stringPropertyNames()) {
                names.put(key, properties.get(key).toString());
            }
        }
    }

    public static String getName(long playerId){
        return names.get(String.valueOf(playerId));
    }

    public SetOfCards getCards() {
        return cards;
    }

    public Role getRole() {
        return role;
    }


    public String getName() {
        String name = getName(id);
        if (name == null) {
            name= userName;
        }
        return name;
    }

    public String toString(){
        return getName();
    }

    public String getUserName(){
        return userName;
    }

    public long getId() {
        return id;
    }
    public Game getGame(){
        return currentGame;
    }

    static public List<String> playersToStrings(List<Player> players) {
        List<String> array = new ArrayList<>();
        for (Player player : players
        ) {
            array.add(player.getName());
        }
        return array;
    }

    public void say(String message) {
        SendMessage sendMessagerequest = new SendMessage();
        sendMessagerequest.setChatId(this.getId());
        sendMessagerequest.enableMarkdown(true);
        sendMessagerequest.setText(message);
        try {
            currentGame.silent.execute(sendMessagerequest);
        } catch (Exception e) {
            System.out.println("Caught Message exeption on user "+getName()+": "+e.getMessage());
        }
    }

    public void sendSticker(String stickerId) {
      // currentGame.sendSticker(stickerId,this.getId());
    }


    public MyMessage addKeyboard(List<Player> selection, MyMessage myMessage) {
        myMessage.setReplyMarkup(getKeyboard(selection));
        return myMessage;
    }

    private InlineKeyboardMarkup getKeyboard(List<Player> players) {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Player player : players) {
            List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
            String key = "";
            if (currentGame.getActivePlayer() == player)
                key = currentGame.texture.key();
            rowInline1.add(new InlineKeyboardButton().setText(key + player.getName() + key).setCallbackData(player.getId()+";-1"));

            List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
            String[] hiddencards =player.getCards().getHiddenStrings();
            if(player.knowsHisCards) {
                for (int i = 0; i < hiddencards.length; i++) {
                    String str = hiddencards[i];
                    rowInline2.add(new InlineKeyboardButton().setText(str).setCallbackData(player.getId() + ";" + i));
                }
            } else {
                if(currentGame.getActivePlayer() ==player)
                    for (int i = 0; i < hiddencards.length; i++) {
                        rowInline2.add(new InlineKeyboardButton().setText("\ud83d\udd0d").setCallbackData(player.getId() + ";" + i));
                    }
                else
                    rowInline2.add(new InlineKeyboardButton().setText("\ud83d\udd0d").setCallbackData(player.getId() + ";" + 0));
            }

            rowsInline.add(rowInline1);
            rowsInline.add(rowInline2);
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}