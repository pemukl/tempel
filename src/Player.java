import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class Player {
    private String name;
    private final long id;

    private Role role;
    private SetOfCards cards;
    private boolean hasKey;
    private String userName;
    public boolean knowsHisCards;

    private int welcomeMessageId;

    private Game currentGame;

    public Player(long id, String userName, Game game) {
        this.currentGame = game;
        this.id = id;
        this.userName = userName;
        if(userName==null)
            //this.userName="Unnamed";
        this.cards = new SetOfCards();
        this.hasKey = false;
        this.welcomeMessageId = welcomeMessageId;
    }

    public void setCards(SetOfCards cards) {
        this.cards = cards;
    }

    public void setHasKey(boolean hasKey) {
        this.hasKey = hasKey;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setName(String name) {
        EditMessageText toEdit = new EditMessageText();
        this.name = name;
    }

    public SetOfCards getCards() {
        return cards;
    }

    public Role getRole() {
        return role;
    }

    public boolean isHasKey() {
        return hasKey;
    }

    public String getName() {
        if (name == null) {
            return userName;
        }
        return this.name;
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
        SendSticker sendSticker = new SendSticker();
        sendSticker.setChatId(getId());
        sendSticker.setSticker(stickerId);
        try {
            currentGame.playNowBot.execute(sendSticker);
        } catch (Exception e) {
            System.out.println("Caught Sticker exeption on user "+getName()+": "+e.getMessage());
        }
    }


    public void letChoose(List<Player> selection, MyMessage myMessage) {
        myMessage.setReplyMarkup(getKeyboard(selection));
        myMessage.send();
    }

    private InlineKeyboardMarkup getKeyboard(List<Player> players) {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Player player : players) {
            List<InlineKeyboardButton> rowInline1 = new ArrayList<>();
            String key = "";
            if (player.hasKey)
                key = currentGame.texture.key();
            rowInline1.add(new InlineKeyboardButton().setText(key + player.getName() + key).setCallbackData(player.getId()+";-1"));

            List<InlineKeyboardButton> rowInline2 = new ArrayList<>();
            String[] hiddencards =player.getCards().getHiddenStrings();
            for (int i = 0; i < hiddencards.length; i++) {
                String str = hiddencards[i];
                rowInline2.add(new InlineKeyboardButton().setText(str).setCallbackData(player.getId()+";"+i));
            }

            rowsInline.add(rowInline1);
            rowsInline.add(rowInline2);
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}