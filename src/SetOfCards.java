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

    public String print(){
        StringBuilder sb = new StringBuilder();
        for (Card cardi: cards) {
            sb.append(cardi.getEmoji());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "SetOfCards{" +
                "cards=" + cards +
                '}';
    }
}
