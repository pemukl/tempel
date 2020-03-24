import java.util.ArrayList;
import java.util.List;

public class Player {

    private final long id;
    private final String name;
    private Role role;
    private List<Card> cards;
    private boolean hasKey;

    public Player(long id, String name, List<Card> cards, boolean hasKey) {
        this.id = id;
        this.name = name;
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

    public List<Card> getCards() {
        return cards;
    }

    public long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    public boolean isHasKey() {
        return hasKey;
    }
}
