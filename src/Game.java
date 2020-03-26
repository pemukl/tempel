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


    public Game(long chatId, PlayNowBot playNowBot) {
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
        silent.send("Spieler " + player.getName() + " ist dem Spiel beigetreten.", id);
    }

    public Player findPlayer(long id) {
        return players.stream()
                .filter(player -> player.getId() == id)
                .findAny()
                .orElse(null);
    }

    public void play() {
        if (players.size() < 2) {
            silent.send("Es sind leider zu wenig Spieler drin. Bitte fügt noch weitere Spieler hinzu.", id);
            return;
        }
        if (players.size() > 10) {
            silent.send("Es sind leider zu viele Spieler drin. Bitte erstellt mehrere Spiele.", id);
            return;
        }
        running = true;
        this.movesLeft = players.size();
        Collections.shuffle(players);
        this.activePlayer = players.get(0);
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
        sendMarkdown(PlayNowBot.texturePack.adventurer()+":"+numAbenteurer+"   "+PlayNowBot.texturePack.guard() +":"+numWaechterinnen);
        Collections.shuffle(roles);
        for (Player player : players) {
            player.setRole(roles.remove(0));
            player.say("Deine Rolle ist " + player.getRole().getEmoji() + " !");
            player.sendSticker(player.getRole().getStickerID());
        }
        distributeCards();
        nextMove(activePlayer,-2,null);
    }

    public void nextMove(Player nextPlayer, int cardIndex, Message message) {
        MyMessage messageBuilder;
        if(message == null)
            messageBuilder = new MyMessage(id, silent);
        else
            messageBuilder =new MyMessage(id,message.getMessageId(),playNowBot);


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

        messageBuilder.append(PlayNowBot.texturePack.gold()+"*: "+exposedCards.countGold()+"*/"+numGold+"\r\n");
        messageBuilder.append(PlayNowBot.texturePack.fire()+"*: "+exposedCards.countFire()+"*/"+numFeuerfallen+"\r\n\r\n");
        //messageBuilder.append("*Leer: "+exposedCards.countEmpty()+"*/"+numLeer+"\r\n\r\n");





        if (isFinished()||(movesLeft==0&&round==2)) {
            messageBuilder.send();
            finished();
            return;
        }

        messageBuilder.append(exposedCards.print(players.size()));
        if (movesLeft == 0) {
            round--;
            this.movesLeft = players.size();
            distributeCards();
        }
        messageBuilder.append(""+printMovesLeft());
        activePlayer.letChoose(players,messageBuilder);
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

            string.append("Die *Guten* gewinnen! Alle "+PlayNowBot.texturePack.gold()+" wurden gefunden.");
            winnerParty = Role.ABENTEURER;

        } else if(exposedCards.countFire() == numFeuerfallen){

            string.append("Die *Bösen* gewinnen! Alle "+PlayNowBot.texturePack.fire()+" wurden aufgedeckt!");
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
            string.append(winner.getRole().getEmoji() +" "+ winner.getName()+"\r\n");
        }
        string.append("\r\n");
        string.append("----------\uD83D\uDC4E----------\r\n\r\n");
        for (Player loser :losers ) {
            string.append(loser.getRole().getEmoji() + " " +loser.getName()+"\r\n");
        }
        string.append("\r\n------------------------");

        sendSticker(winnerParty.getStickerID());
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
            cards.add(new Card(Card.Content.GOLD));
        }
        for (int x = 0; x < leerLeft; x++) {
            cards.add(new Card(Card.Content.LEER));
        }
        for (int x = 0; x < feuerfallenLeft; x++) {
            cards.add(new Card(Card.Content.FEUERFALLE));
        }
        cards.shuffle();
        for (Player player : players) {
            SetOfCards cardsForPlayer = new SetOfCards();
            for (int y = 0; y < round; y++) {
                cardsForPlayer.add(cards.removeRandom());
            }
            player.setCards(cardsForPlayer);
            player.say("Neue Runde. Das sind Deine Karten:\r\n" + player.getCards().printSort());
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


}