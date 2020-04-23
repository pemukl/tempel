package main;

import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;

public class Game {

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
    private Distribution distribution;
    public int numberofBots;



    public Game(long chatId, EmojiSet texture, PlayNowBot playNowBot) {
        this.texture = texture;
        this.playNowBot = playNowBot;
        this.id = chatId;
        this.players = new ArrayList<>();
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

    public void setTexture(EmojiSet texture){
        this.texture = texture;
    }

    public String getName() {
        return this.name;
    }

    public boolean isRunning() {
        return running;
    }

    public void interrupt(){
        this.running = false;
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
            silent.send("Es sind leider zu wenig Spieler registriert. Bitte fügt noch weitere Spieler hinzu.", id);
            return;
        }
        if (players.size() > 10) {
            silent.send("Es sind leider zu viele Spieler registriert. Bitte erstellt mehrere Spiele.", id);
            return;
        }
        playNowBot.notify("Starting Game: "+players.toString()+"\r\n");

        running = true;
        this.round = 5;
        this.exposedCards = new SetOfCards();
        this.movesLeft = players.size();


        if (this.activePlayer==null)
            this.activePlayer = players.get(0);
        distribution = Distribution.getDistribution(players.size());
        List<Role> roles = new ArrayList<>();
        for (int x = 0; x < distribution.getAbenteurer(); x++) {
            roles.add(Role.ABENTEURER);
        }
        for (int x = 0; x < distribution.getWaechterinnen(); x++) {
            roles.add(Role.WAECHTERIN);
        }
        Collections.shuffle(roles);
        for (Player player : players) {
            player.setRole(roles.remove(0));
            if(player.getId() != playNowBot.creatorId())
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

        activePlayer = nextPlayer;



        printStatsWithKeyboard(message);

        if (movesLeft == 0||isFinished()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (round==2||isFinished()){
                MyMessage mb = transformToMyMessage(message);
                addStats(mb);
                mb.send();
                finished();
            }else{
                round--;
                this.movesLeft = players.size();
                distributeCards();
                printStatsWithKeyboard(message);
            }
        }

    }

    private boolean isFinished() {
        return exposedCards.countGold() == distribution.getGold() || exposedCards.countFire() == distribution.getFeuerfallen();
    }

    private void finished() {

        StringBuilder string = new StringBuilder();
        Role winnerParty;
        if (exposedCards.countGold() == distribution.getGold()) {

            string.append("Die *Guten* gewinnen! Alle "+texture.gold()+" wurden gefunden.");
            winnerParty = Role.ABENTEURER;

        } else if(exposedCards.countFire() == distribution.getFeuerfallen()){

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
        string.append("----------\uD83D\uDCA9----------\r\n\r\n");
        for (Player loser :losers ) {
            string.append(loser.getRole().getEmoji(this) + " " +loser.getName()+"\r\n");
        }
        string.append("\r\n------------------------");

        //sendSticker(winnerParty.getStickerID(this));
        sendMarkdown(string.toString());
        running = false;

        playNowBot.updateLobby(new MyMessage(id,silent),this);
    }




    private void distributeCards() {
        int goldLeft = distribution.getGold() - exposedCards.countGold();
        int feuerfallenLeft = distribution.getFeuerfallen() - exposedCards.countFire();
        int leerLeft = distribution.getLeer() - exposedCards.countEmpty();
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
            messageBuilder =new MyMessage(message);
        return messageBuilder;
    }

    private MyMessage addStats(MyMessage messageBuilder){
        String minusOne = "";
        if (distribution.getWaechterinnen()+distribution.getAbenteurer() > players.size())
            minusOne = "(-1)";
        messageBuilder.append(texture.adventurer()+":"+distribution.getAbenteurer()+minusOne
                +"    "+texture.guard() +":"+distribution.getWaechterinnen()+ minusOne + "\r\n");


        messageBuilder.append(texture.gold()+"*: "+exposedCards.countGold()+"*/"+distribution.getGold()+"\r\n");
        messageBuilder.append(texture.fire()+"*: "+exposedCards.countFire()+"*/"+distribution.getFeuerfallen()+"\r\n");
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
        int toGo;
        if (players.size()==0)
            toGo = 3;
        else
            toGo = players.size();

        for (int i = 0; i < toGo; i++) {
            if (i<players.size()-movesLeft) {
                //sb.append("\u26AA"); //uncovered
            }else{
                sb.append("\uD83D\uDD12"); //covered (yet to uncover)
            }
        }
        return sb.toString();
    }




    public void sendMarkdown(String message) {
        SendMessage sendMessagerequest = new SendMessage();

        sendMessagerequest.setChatId(this.getId());
        sendMessagerequest.enableMarkdown(true);
        sendMessagerequest.setText(message);

        silent.execute(sendMessagerequest);
    }

    public void sendSticker(String id) {
        sendSticker(id,this.getId());
    }


        public void sendSticker(String id,long chatId) {
        SendSticker sticker = new SendSticker();
        sticker.setChatId(chatId);
        sticker.setSticker(id);
        SendAnimation animation = new SendAnimation();
        animation.setChatId(chatId);
        animation.setAnimation(id);
        try {
            playNowBot.execute(sticker);
        } catch (TelegramApiException e) {
            try {
                playNowBot.execute(animation);
            } catch (TelegramApiException er) {
            }
        }


    }


    public String printPlayers() {
        StringBuilder sb = new StringBuilder();
        for (Player player:players) {
            if(player.getUserName()==null)
                sb.append("*"+player.getName()+"*\r\n");
            else
                sb.append("*"+player.getName()+"* (" + player.getUserName() + ")\r\n");
        }
        return sb.toString();
    }

    public int numPlayers(){
        return players.size();
    }


    public void removeplayer(Integer idToRemove) {
        for (int i = 0; i < players.size(); i++) {
            if(players.get(i).getId() == idToRemove)
                players.remove(players.get(i));
        }
    }

}