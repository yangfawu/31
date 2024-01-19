import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author Yangfa Wu
 *
 */
public class Player {
	private User ref;
	private int strikes;
	private boolean knocked;
	private Card[] hand;

	/**
	 * Creates a Player object that can be used in games
	 * @param ref	a User reference for the Player object to construct around
	 */
	public Player(User ref) {
		// TODO Auto-generated constructor stub
		this.ref = ref;
		this.hand = new Card[0];
		this.strikes = 0;
		this.knocked = false;
	}
	
	/**
	 * @return the number of strikes the player has
	 */
	public int getStrikes() {
		return strikes;
	}

	/**
	 * @param count increases the number of strikes the player has by the specified amount (3 is max)
	 */
	public void penalize(int count) {
		strikes+= count;
		if (strikes > 3) strikes = 3;
	}
	
	/**
	 * Removes all the strikes from the player
	 */
	public void resetStrikes() {
		this.strikes = 0;
	}
	
	/**
	 * @return whether or not the player is eliminated
	 */
	public boolean isOut() {
		return strikes > 2;
	}

	/**
	 * @return the id of the player
	 */
	public String getId() {
		return ref.getId();
	}
	
	/**
	 * @return the name of the player
	 */
	public String getName() {
		return ref.getName();
	}
	
	/**
	 * @return the level of the player
	 */
	public long getLevel() {
		return ref.getLevel();
	}

	/**
	 * @return whether or not the player has knocked
	 */
	public boolean isKnocked() {
		return knocked;
	}

	/**
	 * Sets the player's knock state
	 * @param knocked	desired knock state
	 */
	public void setKnocked(boolean knocked) {
		this.knocked = knocked;
	}

	/**
	 * @return a List<Card> representation of the player's hand
	 */
	public List<Card> getHand() {
		return Arrays.asList(hand);
	}
	
	/**
	 * @param c	a card
	 * @return	whether or not such a card is in the player's hand
	 */
	public boolean hasCard(Card c) {
		for (Card h: hand)
			if (h.equals(c))
				return true;
		return false;
	}
	
	/**
	 * @param src	a Card array to add cards to
	 * @param val	the card to be added
	 * @return	a new Card array containing all the cards passed through the method
	 */
	public static Card[] addToArr(Card[] src, Card val) {
		Card[] out = new Card[src.length+1];
		for (int i=0; i<src.length; i++)
			out[i] = src[i];
		out[src.length] = val;
		return out;
	}
	
	/**
	 * Attempts to add a card to the player's hand if it's not a duplicate
	 * @param c	a Card
	 */
	public void addCard(Card c) {
		if (hasCard(c)) return;

		hand = addToArr(hand, c);
	}
	
	/**
	 * Attempts to discard the specified card from the player's hand
	 * @param c	a Card
	 */
	public void discardCard(Card c) {
		if (!hasCard(c)) return;
		
		Card[] temp = new Card[hand.length-1];
		int idx = 0;
		for (Card h: hand)
			if (!h.equals(c))
				temp[idx++] = h;
		
		hand = temp;
	}
	
	/**
	 * Removes all the cards in the player's possession
	 */
	public void emptyHand() {
		hand = new Card[0];
	}
	
	/**
	 * @return	whether or not the player has 3 cards of matching rank
	 */
	private boolean hasMatch() {
		for (int i=0; i<hand.length; i++) {
			int count = 1;
			for (int j=0; j<hand.length; j++)
				if (i != j && hand[j].isSameRank(hand[i]))
					count++;
			if (count > 2) return true;
		}
		return false;
	}
	
	/**
	 * @return	all the unique suits in the player's hand
	 */
	private String[] getHandSuits() {	
		String[] out = new String[0];
		for (Card c: hand) {
			String suit = c.getSuit();
			if (!Main.hasItem(out, suit))
				out = Main.addToArr(out, suit);
		}
		
		return out;
	}
	
	/**
	 * @return	the current value of the player's hand
	 */
	public int getValue() {
		if (hasMatch()) return 30;
		
		HashMap<String, Integer> log = new HashMap<>();
		for (String suit: getHandSuits())
			log.put(suit, 0);
		
		for (Card c: hand) {
			String suit = c.getSuit();
			Integer currSuitVal = log.get(suit);
			log.put(suit, currSuitVal + c.getValue());
		}
		
		int max = 0;
		for (Integer val: log.values())
			if (max < val)
				max = val;
		
		return max;
	}
	
	/**
	 * @param p	another Player
	 * @return	an integer that indicates how much great the player's hand is than that of the other player
	 */
	public int compareTo(Player p) {
		return getValue() - p.getValue();
	}

}
