import org.telegram.abilitybots.api.sender.SilentSender;

import java.util.*;

public class Game {
    private int numGold;
    private int numLeer;
    private int numFeuerfallen;

    private final long id;
    private String name;
    public SilentSender silent;

    private int round, movesLeft, exposedGold, exposedLeer, exposedFeuerfallen;
    private Player activePlayer;
    private List<Player> players;

    public Game(long chatId) {
        this.id = chatId;
        this.players = new ArrayList<>();
        this.exposedGold = 0;
        this.exposedLeer = 0;
        this.exposedFeuerfallen = 0;
        this.round = 5;
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

    public void addPlayer(Player player) {
        players.add(player);
        silent.send("Spieler " + player.getName() + " ist dem Spiel beigetreten.", getId());
    }

    public Player findPlayer(long id) {
        return players.stream()
                .filter(player -> player.getId() == id)
                .findAny()
                .orElse(null);
    }

    public void play() {
        silent.send("Play Method called on Game! Following Players registred:", getId());
        for (Player player : players) {
            silent.send(player.getName(), getId());
        }
        this.movesLeft = players.size();
        Collections.shuffle(players);
        this.activePlayer = players.get(0);
        int numWaechterinnen;
        int numAbenteurer;
        switch (players.size()) {
            case 2:
                numAbenteurer = 1;
                numWaechterinnen = 1;
                this.numGold = 3;
                this.numLeer = 3;
                this.numFeuerfallen = 4;
                break;
            case 3:
                numAbenteurer = 2;
                numWaechterinnen = 2;
                this.numGold = 5;
                this.numLeer = 8;
                this.numFeuerfallen = 2;
                break;
            case 4:
                numAbenteurer = 3;
                numWaechterinnen = 2;
                this.numGold = 6;
                this.numLeer = 12;
                this.numFeuerfallen = 2;
                break;
            case 5:
                numAbenteurer = 3;
                numWaechterinnen = 2;
                this.numGold = 7;
                this.numLeer = 16;
                this.numFeuerfallen = 2;
                break;
            case 6:
                numAbenteurer = 4;
                numWaechterinnen = 2;
                this.numGold = 8;
                this.numLeer = 20;
                this.numFeuerfallen = 2;
                break;
            case 7:
                numAbenteurer = 5;
                numWaechterinnen = 3;
                this.numGold = 7;
                this.numLeer = 26;
                this.numFeuerfallen = 2;
                break;
            case 8:
                numAbenteurer = 6;
                numWaechterinnen = 3;
                this.numGold = 8;
                this.numLeer = 30;
                this.numFeuerfallen = 2;
                break;
            case 9:
                numAbenteurer = 6;
                numWaechterinnen = 3;
                this.numGold = 9;
                this.numLeer = 34;
                this.numFeuerfallen = 2;
                break;
            case 10:
                numAbenteurer = 7;
                numWaechterinnen = 4;
                this.numGold = 10;
                this.numLeer = 37;
                this.numFeuerfallen = 3;
                break;
            default:
                numAbenteurer = 0;
                numWaechterinnen = 0;
                this.numGold = 0;
                this.numLeer = 0;
                this.numFeuerfallen = 0;
        }
        List<Role> roles = new ArrayList<>();
        for (int x = 0; x < numAbenteurer; x++) {
            roles.add(Role.ABENTEURER);
        }
        for (int x = 0; x < numWaechterinnen; x++) {
            roles.add(Role.WAECHTERIN);
        }
        Collections.shuffle(roles);
        distributeCards();
        for (Player player : players) {
            Role role = roles.remove(0);
            player.setRole(role);
            player.say("Du bist " + role);
            player.say("Du hast folgende Karten bekommen " + player.getCards());
        }

        while (!isFinished() && round != 1) {
            if (movesLeft == 0) {
                nextRound();
            } else {
                List<Player> selection = new ArrayList<>();
                for (Player player : players) {
                    if (!player.getCards().isEmpty() && player != activePlayer)
                        selection.add(player);
                }
                System.out.println("letting Choose " + activePlayer.getName() + " from " + Player.playersToStrings(selection));
                nextMove(activePlayer.letChoose(selection));
            }
        }
        finished();
    }

    private void nextMove(Player nextPlayer) {
        // TODO Mitteilen wer zu wem gegangen ist.
        silent.send(activePlayer.getName() + " geht zu " + nextPlayer.getName() + ".", id);
        activePlayer.setHasKey(false);
        activePlayer = nextPlayer;
        activePlayer.setHasKey(true);
        Card card = activePlayer.getCards().remove(0);
        // TODO Mitteilen welche Karte geöffnet wurde.
        switch (card) {
            case GOLD:
                silent.send("Ein Gold wurde aufgedeckt!", id);
                exposedGold++;
                break;
            case LEER:
                silent.send("Eine leer Karte wurde aufgedeckt!", id);
                exposedLeer++;
                break;
            case FEUERFALLE:
                silent.send("Eine Feuerfalle wurde aufgedeckt!", id);
                exposedFeuerfallen++;
        }
        movesLeft--;
        // TODO Mitteilen wie viele Karten alle Spieler noch haben.
        StringBuilder string = new StringBuilder();
        for (Player player : players) {
            if (!player.getCards().isEmpty()) {
                if (player.isHasKey()) {
                    string.append(player.getName()).append(" (X)\r\n");
                } else {
                    string.append(player.getName()).append("\r\n");
                }
                for (int x = 0; x < player.getCards().size(); x++) {
                    string.append("-");
                }
                string.append("\r\n");
            }
        }
        string.append("\r\nDiese Karten wurden bereits aufgedeckt:\r\n").append("Gold ").append(exposedGold)
                .append("/").append(numGold).append("\r\n");
        string.append("Feuerfallen ").append(exposedFeuerfallen).append("/").append(numFeuerfallen).append("\r\n");
        string.append("Leer ").append(exposedLeer).append("/").append(numLeer).append("\r\n");
        string.append("\r\n\r\nEs sind noch ").append(movesLeft).append(" Züge übrig.");
        silent.send("Hier sind die Karten:\r\n\r\n" + string.toString(), id);
    }

    private boolean isFinished() {
        return exposedGold == numGold || exposedFeuerfallen == numFeuerfallen;
    }

    private void finished() {
        List<String> winner = new ArrayList<>();
        List<String> loser = new ArrayList<>();
        if (exposedGold == numGold) {
            // TODO Abenteurer gewinnen
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
            string.append("\r\nHerzlichen Glückwunsch!\r\n\r\nVerloren haben:");
            for (String s : loser) {
                string.append(s).append("\r\n");
            }
            string.append("\r\nVielleicht beim nächsten Mal. :(");
            silent.send("Die Abenteurer haben gewonnen!\r\n" + string.toString(), id);
            // TODO Mitteilen wer in den Teams war und das alle x Gold aufgedeckt wurden
        } else {
            for (Player player : players) {
                if (player.getRole() == Role.WAECHTERIN)
                    winner.add(player.getName());
                else
                    loser.add(player.getName());
            }
            if (numFeuerfallen == exposedFeuerfallen) {
                // TODO Wächterinnen gewinnen
                StringBuilder string = new StringBuilder();
                string.append("Alle Feuerfallen wurden aufgedeckt!\r\n\r\nDie Gewinner sind:\r\n");
                appendWinner(winner, loser, string);
                // TODO Mitteilen wer in den Teams war und das alle x Feuerfallen aufgedeckt wurden oder keine Züge mehr
            } else {
                // TODO Wächterinnen gewinnen
                StringBuilder string = new StringBuilder();
                string.append("Es wurde nicht das ganze Gold aufgedeckt!\r\n\r\nDie Gewinner sind:\r\n");
                appendWinner(winner, loser, string);
                // TODO Mitteilen wer in den Teams war und das alle x Feuerfallen aufgedeckt wurden oder keine Züge mehr
            }
        }
    }

    private void appendWinner(List<String> winner, List<String> loser, StringBuilder string) {
        for (String s : winner) {
            string.append(s).append("\r\n");
        }
        string.append("\r\nHerzlichen Glückwunsch!\r\n\r\nVerloren haben:");
        for (String s : loser) {
            string.append(s).append("\r\n");
        }
        string.append("\r\nVielleicht beim nächsten Mal. :(");
        silent.send("Die Wächterinnen haben gewonnen!\r\n" + string.toString(), id);
    }

    public void nextRound() {
        silent.send("Eine neue Runde beginnt. Jeder Spieler bekommt neue Karten.", id);
        distributeCards();
        this.round--;
        this.movesLeft = round;
        for (Player player : players) {
            // TODO Karten den Spielern mitteilen (über ID) (x Leer, x Gold, x Feuerfallen)
            int[] cards = getNumOfCardsFromPlayer(player);
            String string = "Leer: " + cards[0] + "\r\n" + "Gold: " + cards[1] + "\r\n" + "Feuerfallen: " + cards[2];
            if (player.isHasKey())
                string += "\r\n\r\nDu hast gerade den Schlüssel und darfst die nächste Runde starten.";
            silent.send("Das sind deine Karten für diese Runde:\r\n\r\n" + string, player.getId());
        }
    }

    private void distributeCards() {
        int goldLeft = numGold - exposedGold;
        int feuerfallenLeft = numFeuerfallen - exposedFeuerfallen;
        int leerLeft = numLeer - exposedLeer;
        List<Card> cards = new ArrayList<>();
        for (int x = 0; x < goldLeft; x++) {
            cards.add(Card.GOLD);
        }
        for (int x = 0; x < leerLeft; x++) {
            cards.add(Card.LEER);
        }
        for (int x = 0; x < feuerfallenLeft; x++) {
            cards.add(Card.FEUERFALLE);
        }
        Collections.shuffle(cards);
        for (Player player : players) {
            List<Card> cardsForPlayer = new ArrayList<>();
            for (int y = 0; y < round; y++) {
                cardsForPlayer.add(cards.remove(0));
            }
            player.setCards(cardsForPlayer);
        }
    }

    private int[] getNumOfCardsFromPlayer(Player player) {
        int[] cards = new int[3];
        List<Card> playerCards = player.getCards();
        cards[0] = (int) playerCards.stream()
                .filter(card -> card == Card.LEER)
                .count();
        cards[1] = (int) playerCards.stream()
                .filter(card -> card == Card.GOLD)
                .count();
        cards[2] = (int) playerCards.stream()
                .filter(card -> card == Card.FEUERFALLE)
                .count();
        return cards;
    }

    public Player getActivePlayer(){
        return activePlayer;
    }
}
