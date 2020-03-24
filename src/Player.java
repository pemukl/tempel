import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;
import java.util.stream.Collectors;

public class Player {
    private String name;
    private final long id;

    private Role role;
    private List<Card> cards;
    private boolean hasKey;

    private static LinkedList<Player> players;
    private Game currentGame;
    public SilentSender silent;


    public Player(long id, Game game) {
        if (players == null) {
            players = new LinkedList<>();
        }
        players.add(this);
        this.currentGame = game;
        this.id = id;
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

    public void setSilent(SilentSender silent) {
        this.silent = silent;
    }

    public void setName(String name) {
        this.name = name;
        currentGame.silent.send("Dein Name wurde festgelegt, " + name, getId());
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

    public Game getGame() {
        return currentGame;
    }

    public String getName() {
        return this.name;
    }

    public long getId() {
        return id;
    }

    public static Player getPlayer(long id) {
        System.out.println("searching for " + id + " in " + playersToStrings(players));
        for (Player player : players) {
            if (player.getId() == id)
                return player;
        }
        return null;
    }

    static public List<String> playersToStrings(List<Player> players){
        return players.stream().map(player -> player.getName()).collect(Collectors.toList());
    }

    public void say(String message){
        silent.send(message,id);
    }

    public Player letChoose(List<Player> selection) {
        System.out.println("Player: " + selection);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(currentGame.getId());
        sendMessage.setText("Bei wem willst Du eine Türe öffnen?");
        sendMessage.setReplyMarkup(getKeyboard(selection));
        currentGame.silent.execute(sendMessage);
        return selection.get(0);
    }

    private InlineKeyboardMarkup getKeyboard(List<Player> players) {

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        for (Player player : players) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            rowInline.add(new InlineKeyboardButton().setText(player.getName()).setCallbackData("player:" + player.getId()));
            rowsInline.add(rowInline);
        }
        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

}
