import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
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

    private Game currentGame;

    public Player(long id, String name, Game game) {
        this.currentGame = game;
        this.id = id;
        this.name = name;
        this.cards = new SetOfCards();
        this.hasKey = false;
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
        currentGame.sendMarkdown("*" + name+ "* ist der neue Name von " + this.name);
        say("Dein Name wurde zu *"+name+"* geändert.");
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
            return "no username";
        }
        return this.name;
    }

    public long getId() {
        return id;
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
        currentGame.silent.execute(sendMessagerequest);
    }

    public void sendSticker(String stickerId) {
        SendSticker sendSticker = new SendSticker();
        sendSticker.setChatId(getId());
        sendSticker.setSticker(stickerId);
        try {
            currentGame.playNowBot.execute(sendSticker);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void letChoose(List<Player> selection) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(currentGame.getId());
        sendMessage.setText("Jetzt ist " + name + " dran. Bei wem willst du eine Türe öffnen?");
        sendMessage.setReplyMarkup(getKeyboard(selection));
        currentGame.silent.execute(sendMessage);
    }

    private InlineKeyboardMarkup getKeyboard(List<Player> players) {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Player player : players) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            String key = "";
            if (player.hasKey)
                key = PlayNowBot.texturePack.key();
            rowInline.add(new InlineKeyboardButton().setText(key + player.getName()).setCallbackData("player:" + player.getId()));
            for (String str: player.getCards().getHidden() ) {
                rowInline.add(new InlineKeyboardButton().setText(str).setCallbackData("player:" + player.getId()));

            }
            rowsInline.add(rowInline);
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }
}