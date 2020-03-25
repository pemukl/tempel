import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SetOfCards {
    private List<Card> cards;

    SetOfCards(){
        cards = new ArrayList<>();
    }
    SetOfCards(List<Card> cards){
        this.cards = cards;
    }

    public void add(Card card){
        cards.add(card);
    }

    public Card draw(){
        Random rand = new Random();
        return cards.remove(rand.nextInt(cards.size()));
    }

    public boolean isEmpty(){
        return cards.isEmpty();
    }

    public int size(){
        return cards.size();
    }

    public void shuffle(){
        Collections.shuffle(cards);
    }

    public int countGold(){
        return (int) cards.stream().filter(card -> card == Card.GOLD).count();
    }
    public int countEmpty(){
        return (int) cards.stream().filter(card -> card == Card.LEER).count();
    }
    public int countFire(){
        return (int) cards.stream().filter(card -> card == Card.FEUERFALLE).count();
    }

    public String print(){
        StringBuilder sb = new StringBuilder();
        for (Card cardi: cards) {
            sb.append(cardi.getEmoji());
        }
        return sb.toString();
    }

    public String printHidden(){
        StringBuilder sb = new StringBuilder();
        for (Card cardi: cards) {
            sb.append(cardi.getClosed());
        }
        return sb.toString();
    }
    public String[] getHidden(){
        String[] array = new String[cards.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = cards.get(i).getClosed();
        }
        return array;
    }

    @Override
    public String toString() {
        return "SetOfCards{" +
                "cards=" + cards +
                '}';
    }
}
