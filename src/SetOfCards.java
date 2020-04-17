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

    public Card openRandom(){
        Random rand = new Random();
        SetOfCards hidCards = getHidden();
        Card toreturn = hidCards.open(rand.nextInt(hidCards.size()));

        return toreturn;
    }

    public boolean isExposed(int index){
        if (index==-1)
            return false;
        return cards.get(index).isExposed();
    }

    public Card open(int index){
        Card card = cards.get(index);
        card.expose();
        return card;
    }
    
    public SetOfCards getHidden(){
        ArrayList hiddenCards = new ArrayList();
        for (Card cardi:this.cards) {
            if (!cardi.isExposed())
                hiddenCards.add(cardi);
        }
        return new SetOfCards(hiddenCards);
    }
    
    public Card removeRandom(){
        Random rand = new Random();
        Card card = cards.remove(rand.nextInt(cards.size()));
        return card;
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
        return (int) cards.stream().filter(card -> card.state == Card.Content.GOLD).count();
    }
    public int countEmpty(){
        return (int) cards.stream().filter(card -> card.state == Card.Content.LEER).count();
    }
    public int countFire(){
        return (int) cards.stream().filter(card -> card.state == Card.Content.FEUERFALLE).count();
    }

    public String print(){
        StringBuilder sb = new StringBuilder();
        for (Card cardi: cards) {
            sb.append(cardi.getEmoji());
        }
        return sb.toString();
    }

    public String print(int entrysPerline){
        StringBuilder sb = new StringBuilder();
        int n =1;
        int i =0;
        sb.append("Runde "+n+": ");
        for (Card cardi: cards) {
            i++;
            sb.append(cardi.getEmoji());
            if(i==entrysPerline) {
                i=0;
                n++;
                if(n<5)
                    sb.append("\r\n"+"Runde "+n+": ");
            }
        }
        return sb.toString();
    }


    public String printSort(){
        StringBuilder sb = new StringBuilder();
        sb.append(printType(Card.Content.GOLD));
        sb.append(printType(Card.Content.LEER));
        sb.append(printType(Card.Content.FEUERFALLE));
        return sb.toString();
    }

    private String printType(Card.Content cont){
        StringBuilder sb = new StringBuilder();
        for (Card card : cards) {
            if (card.state == cont)
                sb.append(card.getEmoji());
        }
        return sb.toString();
    }



    public String printHidden(){
        StringBuilder sb = new StringBuilder();
        for (Card cardi: cards) {
            sb.append(cardi.getHidden());
        }
        return sb.toString();
    }
    public String[] getHiddenStrings(){
        String[] array = new String[cards.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = cards.get(i).getHidden();
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
