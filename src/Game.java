import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class Game {

    private int numGold;
    private int numLeer;
    private int numFeuerfallen;

    private boolean running;

    private final long id;
    private String name;
    public SilentSender silent;
    public PlayNowBot playNowBot;

    private int round, movesLeft;
    private SetOfCards exposedCards;
    private Player activePlayer;
    private List<Player> players;
    public EmojiSet texture;



    public Game(long chatId, EmojiSet texture, PlayNowBot playNowBot) {
        this.texture = texture;
        this.playNowBot = playNowBot;
        this.id = chatId;
        this.players = new ArrayList<>();
        this.exposedCards = new SetOfCards();
        this.round = 5;
        this.running = false;
    }

    public void setSilent(SilentSender silent) {
        this.silent = silent;
    }

    public long getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public boolean isRunning() {
        return running;
    }

    public Player getActivePlayer() {
        return this.activePlayer;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public Player findPlayer(long id) {
        return players.stream()
                .filter(player -> player.getId() == id)
                .findAny()
                .orElse(null);
    }

    public void play() {
        if (players.size() < 2) {
            silent.send("Es sind leider zu wenig Spieler in der Lobby. Bitte fügt noch weitere Spieler hinzu.", id);
            return;
        }
        if (players.size() > 10) {
            silent.send("Es sind leider zu viele Spieler in der Lobby. Bitte erstellt mehrere Spiele.", id);
            return;
        }
        running = true;
        this.movesLeft = players.size();
        Collections.shuffle(players);
        this.activePlayer = players.get(0);
        this.activePlayer.setHasKey(true);
        StringBuilder string = new StringBuilder();
        for (Player player : players) {
            string.append(player.getName()).append("\r\n");
        }
        int numWaechterinnen;
        int numAbenteurer;
        Distribution distribution = Distribution.getDistribution(players.size());
        numAbenteurer = distribution.getAbenteurer();
        numWaechterinnen = distribution.getWaechterinnen();
        this.numGold = distribution.getGold();
        this.numLeer = distribution.getLeer();
        this.numFeuerfallen = distribution.getFeuerfallen();
        List<Role> roles = new ArrayList<>();
        for (int x = 0; x < numAbenteurer; x++) {
            roles.add(Role.ABENTEURER);
        }
        for (int x = 0; x < numWaechterinnen; x++) {
            roles.add(Role.WAECHTERIN);
        }
        sendMarkdown(texture.adventurer()+":"+numAbenteurer+"   "+texture.guard() +":"+numWaechterinnen);
        Collections.shuffle(roles);
        for (Player player : players) {
            player.setRole(roles.remove(0));
            player.say("Deine Rolle ist " + player.getRole().getEmoji(this) + " !");
            player.sendSticker(player.getRole().getStickerID(this));
        }
        distributeCards();
        printStatsWithKeyboard(null);
    }

    public void nextMove(Player nextPlayer, int cardIndex, Message message) {

        if (cardIndex!=-2) {
            Card card;
            if (cardIndex==-1){
                card = nextPlayer.getCards().openRandom();
            }else{
                card = nextPlayer.getCards().open(cardIndex);
            }
            movesLeft--;
            exposedCards.add(card);
        }

        activePlayer.setHasKey(false);
        activePlayer = nextPlayer;
        activePlayer.setHasKey(true);



        if (isFinished()||(movesLeft==0&&round==2)) {
            MyMessage mb = transformToMyMessage(message);
            addStats(mb);
            mb.send();
            finished();
            return;
        }

        if (movesLeft == 0) {
            printStatsWithKeyboard(message);

            round--;
            this.movesLeft = players.size();
            distributeCards();
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        printStatsWithKeyboard(message);
    }

    public void printStatsWithKeyboard(Message message){
       MyMessage mb = transformToMyMessage(message);
       addStats(mb);
       addKeyboard(mb);
       mb.send();
    }

    private MyMessage transformToMyMessage(Message message){
        MyMessage messageBuilder;
        if(message == null)
            messageBuilder = new MyMessage(id, silent);
        else
            messageBuilder =new MyMessage(message,playNowBot);
        return messageBuilder;
    }

    private MyMessage addStats(MyMessage messageBuilder){

        messageBuilder.append(texture.gold()+"*: "+exposedCards.countGold()+"*/"+numGold+"\r\n");
        messageBuilder.append(texture.fire()+"*: "+exposedCards.countFire()+"*/"+numFeuerfallen+"\r\n\r\n");
        //messageBuilder.append("*Leer: "+exposedCards.countEmpty()+"*/"+numLeer+"\r\n\r\n");
        messageBuilder.append(exposedCards.print(players.size()));

        return messageBuilder;

    }

    private MyMessage addKeyboard(MyMessage messageBuilder){
        messageBuilder.append(""+printMovesLeft());
        activePlayer.addKeyboard(players,messageBuilder);
        return messageBuilder;
    }



    private String printMovesLeft() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < players.size(); i++) {
            System.out.println("movesleft:"+movesLeft+" Players: " + players.size());
            if (i<players.size()-movesLeft) {
                //sb.append("\u26AA"); //uncovered
            }else{
                sb.append("\uD83D\uDD12"); //covered (yet to uncover)
            }
        }
        return sb.toString();
    }

    private boolean isFinished() {
        return exposedCards.countGold() == numGold || exposedCards.countFire() == numFeuerfallen;
    }

    private void finished() {

        StringBuilder string = new StringBuilder();
        Role winnerParty;
        if (exposedCards.countGold() == numGold) {

            string.append("Die *Guten* gewinnen! Alle "+texture.gold()+" wurden gefunden.");
            winnerParty = Role.ABENTEURER;

        } else if(exposedCards.countFire() == numFeuerfallen){

            string.append("Die *Bösen* gewinnen! Alle "+texture.fire()+" wurden aufgedeckt!");
            winnerParty=Role.WAECHTERIN;
        } else {
            string.append("Die *Bösen* gewinnen! Alle Züge sind aufgebraucht!");
            winnerParty=Role.WAECHTERIN;
        }

        List<Player> winners = new ArrayList<>();
        List<Player> losers = new ArrayList<>();
        for (Player player : players) {
            if (player.getRole() == winnerParty)
                winners.add(player);
            else
                losers.add(player);
        }
        string.append("\r\n\r\n");
        string.append("----------\uD83C\uDFC6----------\r\n\r\n");
        for (Player winner :winners ) {
            string.append(winner.getRole().getEmoji(this) +" "+ winner.getName()+"\r\n");
        }
        string.append("\r\n");
        string.append("----------\uD83D\uDC4E----------\r\n\r\n");
        for (Player loser :losers ) {
            string.append(loser.getRole().getEmoji(this) + " " +loser.getName()+"\r\n");
        }
        string.append("\r\n------------------------");

        sendSticker(winnerParty.getStickerID(this));
        sendMarkdown(string.toString());
        running = false;
        playNowBot.removeGame(this);
    }




    private void distributeCards() {
        int goldLeft = numGold - exposedCards.countGold();
        int feuerfallenLeft = numFeuerfallen - exposedCards.countFire();
        int leerLeft = numLeer - exposedCards.countEmpty();
        SetOfCards cards = new SetOfCards();
        for (int x = 0; x < goldLeft; x++) {
            cards.add(new Card(Card.Content.GOLD,this));
        }
        for (int x = 0; x < leerLeft; x++) {
            cards.add(new Card(Card.Content.LEER,this));
        }
        for (int x = 0; x < feuerfallenLeft; x++) {
            cards.add(new Card(Card.Content.FEUERFALLE,this));
        }
        cards.shuffle();
        for (Player player : players) {
            SetOfCards cardsForPlayer = new SetOfCards();
            for (int y = 0; y < round; y++) {
                cardsForPlayer.add(cards.removeRandom());
            }
            player.setCards(cardsForPlayer);
            //player.say("Neue Runde. Das sind Deine Karten:\r\n" + player.getCards().printSort());
            player.knowsHisCards=false;
        }
    }


    public void sendMarkdown(String message) {
        SendMessage sendMessagerequest = new SendMessage();

        sendMessagerequest.setChatId(this.getId());
        sendMessagerequest.enableMarkdown(true);
        sendMessagerequest.setText(message);

        silent.execute(sendMessagerequest);
    }

    public void sendSticker(String stickerId) {
        SendSticker sendSticker = new SendSticker();
        sendSticker.setChatId(getId());
        sendSticker.setSticker(stickerId);
        try {
            playNowBot.execute(sendSticker);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public String printPlayers() {
        StringBuilder sb = new StringBuilder();
        for (Player player:players) {
            sb.append(player.getName()+"\r\n");
        }
        return sb.toString();
    }

    public InlineKeyboardMarkup getJoinKeyboard() {
        List<InlineKeyboardButton> rowInline = new ArrayList<>();
        rowInline.add(new InlineKeyboardButton().setText("Bin dabei!").setCallbackData("joinrequest"));
        int numPlayers = this.players.size();
        if(numPlayers>2 && numPlayers <11)
            rowInline.add(new InlineKeyboardButton().setText("Spiel starten!").setCallbackData("startgame"));

        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(rowInline);

        ReplyKeyboard replyKeyboard = new InlineKeyboardMarkup();
        InlineKeyboardMarkup inlineKeyboardMarkup = ((InlineKeyboardMarkup) replyKeyboard);
        inlineKeyboardMarkup.setKeyboard(rowsInline);
        return inlineKeyboardMarkup;
    }


    public void removeplayer(Integer idToRemove) {
        for (int i = 0; i < players.size(); i++) {
            if(players.get(i).getId() == idToRemove)
                players.remove(players.get(i));
        }
    }

}