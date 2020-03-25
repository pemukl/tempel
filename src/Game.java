import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

public class Game {

    private int numGold;
    private int numLeer;
    private int numFeuerfallen;

    private boolean running;

    private final long id;
    private String name;
    public SilentSender silent;
    private PlayNowBot playNowBot;

    private int round, movesLeft, exposedGold, exposedLeer, exposedFeuerfallen;
    private Player activePlayer;
    private List<Player> players;


    public Game(long chatId, PlayNowBot playNowBot) {
        this.playNowBot = playNowBot;
        this.id = chatId;
        this.players = new ArrayList<>();
        this.exposedGold = 0;
        this.exposedLeer = 0;
        this.exposedFeuerfallen = 0;
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
        if (players.size() < 3) {
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
        silent.send("Das Spiel kann beginnen. Folgende Spieler spielen mit:\r\n\r\n" + string.toString() + "\r\n" + activePlayer.getName() + " darf als erstes ziehen.", id);
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
        Collections.shuffle(roles);
        for (Player player : players) {
            player.setRole(roles.remove(0));
            System.out.println("Printing out Emoji: "+player.getRole().getEmoji());
            player.say("Du bist " + player.getRole().getEmoji() + " !");
        }
        distributeCards();
        List<Player> selection = new ArrayList<>();
        for (Player player : players) {
            if (!player.getCards().isEmpty() && player != activePlayer)
                selection.add(player);
        }
        activePlayer.letChoose(selection);
    }

    public void nextMove(Player nextPlayer) {
        sendMarkdown("_" + activePlayer.getName() + "_ geht zu _" + nextPlayer.getName() + "_.");
        activePlayer.setHasKey(false);
        activePlayer = nextPlayer;
        activePlayer.setHasKey(true);
        Card card = activePlayer.getCards().draw();
        sendMarkdown("Es wurde aufgedeckt: "+card.getEmoji());
        movesLeft--;
        StringBuilder sb = new StringBuilder();
        sb.append("Du hast nun nur noch folgende Karten:\r\n\r\n");
        sb.append(activePlayer.getCards().toString());
        activePlayer.say( sb.toString() );
        StringBuilder string = new StringBuilder();
        for (Player player : players) {
            if (!player.getCards().isEmpty()) {
                for (int x = 0; x < player.getCards().size(); x++) {
                    string.append(" - ");
                }
                if (player.isHasKey()) {
                    string.append("_").append(player.getName()).append("_ *(X)*\r\n");
                } else {
                    string.append("_").append(player.getName()).append("_\r\n");
                }
                string.append("\r\n");
            }
        }
        string.append("\r\nDiese Karten wurden bereits aufgedeckt:\r\n").append("*Gold: ").append(exposedGold)
                .append("*/").append(numGold).append("\r\n");
        string.append("*Feuerfallen: ").append(exposedFeuerfallen).append("*/").append(numFeuerfallen).append("\r\n");
        string.append("*Leer: ").append(exposedLeer).append("*/").append(numLeer).append("\r\n");
        if (movesLeft == 0)
            string.append("\r\n\r\nEs sind *keine* Züge mehr übrig.");
        else
            string.append("\r\n\r\nEs sind noch *").append(movesLeft).append("* Züge übrig.");
        sendMarkdown("Das sind die Karten der Mitspieler:\r\n\r\n" + string.toString());


        if (!isFinished() && round != 1) {
            if (movesLeft == 0) {
                round--;
                if (round != 1)
                    nextRound();
                else {
                    finished();
                    return;
                }
            }
            List<Player> selection = new ArrayList<>();
            for (Player player : players) {
                if (!player.getCards().isEmpty() && player != activePlayer)
                    selection.add(player);
            }
            activePlayer.letChoose(selection);
        } else
            finished();
    }

    private boolean isFinished() {
        return exposedGold == numGold || exposedFeuerfallen == numFeuerfallen;
    }

    private void finished() {
        List<String> winner = new ArrayList<>();
        List<String> loser = new ArrayList<>();
        if (exposedGold == numGold) {
            for (Player player : players) {
                if (player.getRole() == Role.ABENTEURER)
                    winner.add(player.getName());
                else
                    loser.add(player.getName());
            }
            StringBuilder string = new StringBuilder();
            string.append("Das ganze Gold wurde aufgedeckt!\r\n\r\nDie Gewinner sind:\r\n");
            for (String s : winner) {
                string.append(s).append("\r\n");
            }
            string.append("\r\nHerzlichen Glückwunsch!\r\n\r\n_____________________\r\n\r\nVerloren haben:\r\n");
            for (String s : loser) {
                string.append(s).append("\r\n");
            }
            string.append("\r\nVielleicht beim nächsten Mal. :(");
            silent.send("Die Abenteurer haben gewonnen!\r\n" + string.toString(), id);
        } else {
            for (Player player : players) {
                if (player.getRole() == Role.WAECHTERIN)
                    winner.add(player.getName());
                else
                    loser.add(player.getName());
            }
            if (numFeuerfallen == exposedFeuerfallen) {
                StringBuilder string = new StringBuilder();
                string.append("Alle Feuerfallen wurden aufgedeckt!\r\n\r\nDie Gewinner sind:\r\n");
                appendWinner(winner, loser, string);
            } else {
                StringBuilder string = new StringBuilder();
                string.append("Es wurde nicht das ganze Gold aufgedeckt!\r\n\r\nDie Gewinner sind:\r\n");
                appendWinner(winner, loser, string);
            }
        }
        running = false;
        playNowBot.removeGame(this);
    }

    private void appendWinner(List<String> winner, List<String> loser, StringBuilder string) {
        for (String s : winner) {
            string.append(s).append("\r\n");
        }
        string.append("\r\nHerzlichen Glückwunsch!\r\n\r\n_____________________\r\n\r\nVerloren haben:\r\n");
        for (String s : loser) {
            string.append(s).append("\r\n");
        }
        string.append("\r\nVielleicht beim nächsten Mal. :(");
        silent.send("Die Wächterinnen haben gewonnen!\r\n" + string.toString(), id);
    }

    private void nextRound() {
        silent.send("Eine neue Runde beginnt. Jeder Spieler bekommt neue Karten.", id);
        distributeCards();
        this.movesLeft = players.size();
        for (Player player : players) {
            if (player.isHasKey())
                player.say("Du hast gerade den Schlüssel und darfst die nächste Runde starten.");
        }
    }

    private void distributeCards() {
        int goldLeft = numGold - exposedGold;
        int feuerfallenLeft = numFeuerfallen - exposedFeuerfallen;
        int leerLeft = numLeer - exposedLeer;
        SetOfCards cards = new SetOfCards();
        for (int x = 0; x < goldLeft; x++) {
            cards.add(Card.GOLD);
        }
        for (int x = 0; x < leerLeft; x++) {
            cards.add(Card.LEER);
        }
        for (int x = 0; x < feuerfallenLeft; x++) {
            cards.add(Card.FEUERFALLE);
        }
        cards.shuffle();
        for (Player player : players) {
            SetOfCards cardsForPlayer = new SetOfCards();
            for (int y = 0; y < round; y++) {
                cardsForPlayer.add(cards.draw());
            }
            player.setCards(cardsForPlayer);
            if (round != 5)
                player.say("----------------- Neue Runde -----------------");
            player.say("Das sind deine Karten für diese Runde:\r\n\r\n" + player.getCards().print());
        }
    }


    private void sendMarkdown(String message) {
        SendMessage sendMessagerequest = new SendMessage();

        sendMessagerequest.setChatId(this.getId());
        sendMessagerequest.enableMarkdown(true);
        sendMessagerequest.setText(message);

        silent.execute(sendMessagerequest);
    }
}