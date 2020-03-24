import org.telegram.abilitybots.api.sender.SilentSender;

import java.util.*;

public class Game {
    private int numGold;
    private int numLeer;
    private int numFeuerfallen;

    private long ID;
    private String name;
    public SilentSender silent;

    private int round, movesLeft, exposedGold, exposedLeer, exposedFeuerfallen;
    private Player activePlayer;
    private List<Player> players;

    public Game(long chatID) {
        this.ID = chatID;
        this.players = new ArrayList<>();
        this.exposedGold = 0;
        this.exposedLeer = 0;
        this.exposedFeuerfallen = 0;
        this.round = 5;
    }

    public void setSilent(SilentSender silent) {
        this.silent = silent;
    }

    public long getID() {
        return ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void addPlayer(Player player) {
        players.add(player);
        silent.send("Spieler " + player.getName() + " ist dem Spiel beigetreten.", getID());
    }

    public Player findPlayer(long ID) {
        for (Player player : players) {
            if (player.getID() == ID)
                return player;
        }
        return null;
    }

    public void play() {
        silent.send("Play Method called on Game! Following Players registred:", getID());
        for (Player player : players) {
            silent.send(player.getName(), getID());
        }
        this.movesLeft = players.size();
        Collections.shuffle(players);
        this.activePlayer = players.get(0);
        int numWaechterinnen;
        int numAbenteurer;
        switch (players.size()) {
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
        for (Player player : players) {
            player.setRole(roles.remove(0));
        }
        while (isFinished() && round != 5) {
            if (movesLeft == 0) {
                nextRound();
            } else {
                // TODO nach Spieler fragen
                long id;
                Player nextPlayer;
                do {
                    // TODO Möglicherweise anderen Satz, falls die ID keine Karten mehr hat (also bei Wiederholung)
                    id = 0;
                    nextPlayer = getPlayerById(id);
                } while (nextPlayer == null || getPlayerById(id).getCards().isEmpty());
                nextMove(nextPlayer);
            }
        }
        finished();
    }

    private void nextMove(Player nextPlayer) {
        // TODO Mitteilen wer zu wem gegangen ist.
        activePlayer.setHasKey(false);
        activePlayer = nextPlayer;
        activePlayer.setHasKey(true);
        Card card = activePlayer.getCards().remove(0);
        // TODO Mitteilen welche Karte geöffnet wurde.
        switch (card) {
            case GOLD:
                exposedGold++;
                break;
            case LEER:
                exposedLeer++;
                break;
            case FEUERFALLE:
                exposedFeuerfallen++;
        }
        // TODO Mitteilen wie viele Karten alle Spieler noch haben.
        Map<Player, Integer> playerCards = new HashMap<>();
        for (Player player : players) {
            playerCards.put(player, player.getCards().size());
        }
        // TODO Mitteilen was schon aufgedeckt wurde
        movesLeft--;
    }

    private boolean isFinished() {
        return exposedGold == numGold || exposedFeuerfallen == numFeuerfallen;
    }

    private String finished() {
        List<String> winner = new ArrayList<>();
        if (exposedGold == numGold) {
            // TODO Abenteurer gewinnen
            for (Player player : players) {
                if (player.getRole() == Role.ABENTEURER)
                    winner.add(player.getName());
            }
            // TODO Mitteilen wer in den Teams war und das alle x Gold aufgedeckt wurden
            return "";
        } else {
            // TODO Wächterinnen gewinnen
            for (Player player : players) {
                if (player.getRole() == Role.WAECHTERIN)
                    winner.add(player.getName());
            }
            // TODO Mitteilen wer in den Teams war und das alle x Feuerfallen aufgedeckt wurden oder keine Züge mehr
            return "";
        }
    }

    public void nextRound() {
        distributeCards();
        this.round++;
        for (Player player : players) {
            // TODO Karten den Spielern mitteilen (über ID) (x Leer, x Gold, x Feuerfallen)
            int[] cards = getNumOfCardsFromPlayer(player);
        }
        String player = Objects.requireNonNull(players.stream()
                .filter(Player::isHasKey)
                .findAny()
                .orElse(null)).getName();
        // TODO Mitteilen wer gerade den Schlüssel hat
    }

    // TODO Möglicherweise Parameter der Map ändern und Methode entfernen
    private Player getPlayerById(long id) {
        return players.stream()
                .filter(player -> player.getID() == id)
                .findAny()
                .orElse(null);
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
            for (int y = 0; y < 6 - round; y++) {
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
}
