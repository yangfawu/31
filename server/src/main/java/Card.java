
/**
 * @author Yangfa Wu
 *
 */
public class Card {
	
	/**
	 * @param suit	a card suit
	 * @return if the suit is a valid card suit
	 */
	public static boolean isValidSuit(String suit) {
		for (String s: new String[] { "clubs", "spades", "diamonds", "hearts" })
			if (suit.equalsIgnoreCase(s))
				return true;
		return false;
	}
	
	
	/**
	 * @param rank	a card rank
	 * @return if the rank is a valid card rank
	 */
	public static boolean isValidRank(int rank) {
		return rank > 0 && rank < 14;
	}
	
	private int rank;
	private String suit;

	/**
	 * Creates a Card object using integers as rank and suit
	 * @param rank	a card rank
	 * @param suit	a card suit in the form of an integer
	 * Precondition: 0 < rank < 14 && suit needs to be a valid suit
	 */
	public Card(int rank, int suit) {
		// TODO Auto-generated constructor stub
		this.rank = rank;
		this.suit = switch(suit) {
			case 0 -> "clubs";
			case 1 -> "spades";
			case 2 -> "diamonds";
			case 3 -> "hearts";
			default -> "no-suit";
		};
	}
	
	/**
	 * Creates a Card object using the suit name and rank integer
	 * @param rank	a card rank
	 * @param suit	a card suit
	 */
	public Card(int rank, String suit) {
		this.rank = rank;
		this.suit = suit.toLowerCase();
	}
	
	/**
	 * @return a formatted string representing the card 
	 */
	@Override
	public String toString() {
		return "%s of %s".formatted(getRankString(), suit);
	}

	/**
	 * @return the numerical rank of the card
	 */
	public int getRank() {
		return rank;
	}
	
	/**
	 * @return the rank name of the card
	 */
	public String getRankString() {
		return switch(rank) {
		case 1 -> "ace";
		case 11 -> "jack";
		case 12 -> "queen";
		case 13 -> "king";
		default -> "" + rank;
		};
	}
	
	/**
	 * @return the suit name of the card
	 */
	public String getSuit() {
		return suit;
	}
	
	/**
	 * A method that FBase recognizes
	 * @return the same thing as toString()
	 */
	public String getName() {
		// for Database Auto-PUT
		return this.toString();
	}
	
	
	/**
	 * @return the real value of the card
	 */
	public int getValue() {
		return switch(rank) {
		case 1 -> 11;
		case 11 -> 10;
		case 12 -> 10;
		case 13 -> 10;
		default -> rank;
		};
	}
	
	/**
	 * @param c	a Card object
	 * @return if the card has the same rank as the passed Card object
	 */
	public boolean isSameRank(Card c) {
		return rank == c.rank;
	}
	
	/**
	 * @param c	a Card object
	 * @return if the card has the same suit as the passed Card object
	 */
	public boolean isSameSuit(Card c) {
		return suit.equalsIgnoreCase(c.suit);
	}
	
	/**
	 * @param c	a Card object
	 * @return if the card is pretty much the same as the passed Card object
	 */
	public boolean equals(Card c) {
		return isSameRank(c) && isSameSuit(c);
	}

}
