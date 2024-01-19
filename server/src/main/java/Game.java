import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

/**
 * @author Yangfa Wu
 *
 */
public class Game {
	private static HashMap<String, Game> games = new HashMap<String, Game>();
	
	/**
	 * @param id	a possible room id
	 * @return	if the program has a room with such an id in its memory
	 */
	public static boolean hasGame(String id) {
		return games.containsKey(id);
	}
	
	/**
	 * Attempts to find a room with the provided id in its memory and removes it
	 * @param id	a possible room id
	 */
	public static void removeGame(String id) {
		if (hasGame(id))
			games.remove(id);
	}
	
	/**
	 * @param r	a Game object
	 * @return	whether or not adding the game to memory was successful
	 */
	public static boolean addGame(Game r) {
		if (games.containsKey(r.id))
			return false;
		else {
			games.put(r.id, r);
			return true;
		}
	}
	
	/**
	 * @param id	a possible room id
	 * @return	a room with such the provided id in memory or null if not found
	 */
	public static Game getGame(String id) {
		return games.get(id);
	}
	
	private String id, message;
	private int turn;
	private boolean isOver, statsHidden;
	private String[] order;
	private Room room;
	private HashMap<String, Player> players;
	private Card[] deck;
	private Card[] discard;

	/**
	 * @param room	a Room object that hosts this Game object
	 * @param users	the users that would be participating in this game
	 */
	public Game(Room room, User[] users) {
		this.room = room;
		
		do this.id = Database.randomId().split("-")[0];
		while (hasGame(this.id));
		
		this.players = new HashMap<>();
		this.order = new String[users.length];
		for (int i=0; i<users.length; i++) {
			Player p = new Player(users[i]);
			this.players.put(p.getName(), p);
			this.order[i] = p.getName();
		}
			
		addGame(this);
	}
	
	/**
	 * @param <T> any non-primitive type
	 * @param arr	the array of Type T to be shuffled
	 * @return	a shuffled array
	 */
	public <T> T[] shuffle(T[] arr) {
		Random rand = new Random();
		for (int i=0; i<arr.length; i++) {
			int nextIdx = rand.nextInt(arr.length);
			T temp = arr[nextIdx];
			arr[nextIdx] = arr[i];
			arr[i] = temp;
		}
		
		return arr;
	}
	
	/**
	 * @param <T>	any non-primitive type
	 * @param arr	the array of Type T to be shuffled
	 * @param amount	the number of places to shift the array left or right
	 * @return	a shifted array
	 */
	public <T> T[] shift(T[] arr, int amount) {
		T[] temp = arr.clone();
		for (int i=0; i<arr.length; i++) {
			int newIdx = (i+amount)%arr.length;
			if (newIdx < 0) newIdx+= arr.length;
			temp[newIdx] = arr[i];
		}
		return temp;
	}
	
	/**
	 * Empties all the players' hand and place the cards back into the deck
	 */
	private void resetCards() {
		for (Player p: players.values())
			p.emptyHand();
		
		deck = new Card[52];
		discard = new Card[0];
		
		for (int suit=0; suit<4; suit++)
			for (int rank=0; rank<13; rank++)
				deck[suit*13+rank] = new Card(rank + 1, suit);
		
		deck = shuffle(deck);			
	}
	
	/**
	 *	@return a formatted string that describes the Game object
	 */
	@Override
	public String toString() {
		return "GAME " + id;
	}
	
	/**
	 * Updates all the game's values onto the database at a predetermined path
	 */
	public void update() {
		final Game THIS = this;
		Database.set("/games/" + id, this, new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Database.print("UPDA", THIS);
			}
		});
	}
	
	/**
	 * Closes the game (remove from database) and the room hosting it
	 * Postcondition: the game WILL definitely get deleted and clients can't access it
	 */
	public void close() {
		final Game THIS = this;
		Database.set("/games/" + id, null, new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Database.print("REMO", THIS);
				removeGame(id);
				
				THIS.room.close();
			}
		});
	}
	
	/**
	 * @param pile	the pile of cards to be drawn from
	 * @param count	the number of cards to be drawn
	 * @return	a 2D array (first item are the drawn cards, second item are the remaining cards)
	 */
	private Card[][] drawFromPile(Card[] pile, int count) {
		if (pile.length < count) return new Card[][] { new Card[0], pile };
		
		Card[] out = new Card[count];
		for (int i=0; i<count; i++)
			out[i] = pile[i];
			
		Card[] newDeck = new Card[pile.length-count];
		for (int i=count; i<pile.length; i++)
			newDeck[i-count] = pile[i];
		
		return new Card[][] { out, newDeck };
	}
	
	/**
	 * @param count	the number of cards to be drawn from the deck
	 * @return	an array of drawn cards from the deck
	 */
	private Card[] drawFromDeck(int count) {
		Card[][] result = drawFromPile(deck, count);
		deck = result[1];
		
		return result[0];
	}
	
	/**
	 * @return a single card drawn from the deck
	 */
	private Card drawFromDeck() {
		if (deck.length < 1) return null;
		return drawFromDeck(1)[0];
	}
	
	/**
	 * @param count	the number of cards to be drawn from the discard pile
	 * @return	an array of drawn cards from the discard pile
	 */
	private Card[] drawFromDiscard(int count) {
		Card[][] result = drawFromPile(discard, count);
		discard = result[1];
		
		return result[0];
	}
	
	/**
	 * @return a single card drawn from the discard pile
	 */
	private Card drawFromDiscard() {
		if (discard.length < 1) return null;
		return drawFromDiscard(1)[0];
	}
	
	/**
	 * @param target	the card to look for
	 * @param pile	the pile to look in for the target card
	 * @return	whether or not the target card is in the given pile
	 */
	private boolean pileHasCard(Card target, Card[] pile) {
		for (Card c: pile)
			if (c.equals(target))
				return true;
		return false;
	}
	
	/**
	 * Attempts to add a card to the deck that isn't already in the deck
	 * @param target	the card to be added
	 */
	private void addToDeck(Card target) {		
		if (pileHasCard(target, this.deck)) return;
		this.deck = Player.addToArr(this.deck, target);
	}
	
	/**
	 * Attempts to add a card to the discard pile that isn't already in the discard pile
	 * @param target	the card to be added
	 */
	private void addToDiscard(Card target) {
		if (pileHasCard(target, this.discard)) return;
		
		Card[] temp = new Card[this.discard.length+1];
		for (int i=0; i<this.discard.length; i++)
			temp[i+1] = this.discard[i];
		temp[0] = target;
		
		this.discard = temp;
	}
	
	/**
	 * @return the id of the game
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the server message for the game
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the number of turns that can have occured in the game
	 */
	public int getTurn() {
		return turn;
	}
	
	/**
	 * @return a hashmap representation of all the players
	 */
	public HashMap<String, Player> getPlayers() {
		return players;
	}
	
	/**
	 * @param pile	a pile of cards
	 * @return	an List<Card> with a length at most of 2
	 */
	private List<Card> getPile(Card[] pile) {
		int idx = 2;
		if (pile.length < 2) idx--;
		if (pile.length < 1) idx--;
		
		return Arrays.asList(pile).subList(0, idx);
	}
	
	/**
	 * @return a List<Card> representation of the top 2 cards in the deck
	 */
	public List<Card> getDeck() {
		return getPile(deck);
	}
	
	/**
	 * @returna a List<Card> representation of the top 2 cards in the discard pile
	 */
	public List<Card> getDiscard() {
		return getPile(discard);
	}
	
	/**
	 * @return a List<Card> representation of the current turn order
	 */
	public List<String> getTurnOrder() {
		return Arrays.asList(this.order);
	}
	
	/**
	 * @return whether or not the game is over
	 */
	public boolean isOver() {
		return isOver;
	}

	/**
	 * "Resets" the game so that it can start properly
	 */
	public void reset() {
		this.isOver = false;
		
		resetCards();
		this.order = shuffle(this.order);
		this.turn = 1;
		this.message = "Game starting soon...";

		newRound();
	}
	
	/**
	 * shifts the turn order to the left to simulate turn cycling
	 */
	public void nextTurn() {
		this.turn++;		
		this.order = shift(this.order, -1);
	}
	
	/**
	 * filters out all the eliminated players from the turn order
	 */
	public void cleanOrder() {
		String[] newOrder = new String[0];
		for (String name: this.order)
			if (!players.get(name).isOut())
				newOrder = Main.addToArr(newOrder, name);
		this.order = newOrder;
	}
	
	/**
	 * deal out 3 cards to each playing player
	 */
	public void dealOutCards() {
		for (String name: this.order) {
			Player p = players.get(name);
			for (Card c: drawFromDeck(3))
				p.addCard(c);
		}
	}
	
	/**
	 * @return a string representation of all the participating players in the game
	 */
	public String getUsers() {
		String out = "";
		for (Player p: players.values())
			out+= ":" + p.getId() + ":";
		return out;
	}
	
	/**
	 * @return whether or not players are allowed to see the hand value of other players
	 */
	public boolean getStatsHidden() {
		return this.statsHidden;
	}
	
	/**
	 * @param src a Player array to add a player to
	 * @param val	a player that needs to be added
	 * @return	a Player array that contains all the Players passed through the method
	 */
	private Player[] addToArr(Player[] src, Player val) {
		Player[] out = new Player[src.length+1];
		for (int i=0; i<src.length; i++)
			out[i] = src[i];
		out[src.length] = val;
		return out;
	}
	
	/**
	 * @return	all the players that are not eliminated from the turn order
	 */
	private Player[] getPlayingPlayers() {
		Player[] out = new Player[0];
		for (String name: this.order)
			out = addToArr(out, players.get(name));
			
		return out;
	}
	
	/**
	 * @return all playing players ranked from smallest to highest hand value
	 */
	private Player[] getRankPlayers() {
		Player[] pp = getPlayingPlayers();
		boolean changed;
		do {
			changed = false;
			for (int i=0; i<pp.length-1; i++)
				if (pp[i].compareTo(pp[i+1]) > 0) {
					Player temp = pp[i];
					pp[i] = pp[i+1];
					pp[i+1] = temp;
					changed = true;
				}
		} while (changed);
		
		return pp;
	}
	
	/**
	 * @return the smallest hand value among the playing players
	 */
	private int getSmallestHand() {
		return getRankPlayers()[0].getValue();
	}
	
	/**
	 * @return the biggest hand value among the playing players
	 */
	private int getBiggestHand() {
		Player[] rp = getRankPlayers();
		return rp[rp.length-1].getValue();
	}
	
	/**
	 * @param src	the Player array to look for matches in
	 * @param handVal	the target hand value that passed players needs to have
	 * @return	an array containing all the players that have the target hand value
	 */
	private Player[] getMatchingPlayers(Player[] src, int handVal) {
		Player[] out = new Player[0];
		for (Player p: src)
			if (p.getValue() == handVal)
				out = addToArr(out, p);
		
		return out;
	}
	
	/**
	 * @param src	the Player array to look for matches in
	 * @param handVal	the target hand value that passed players needs to have
	 * @return	an array containing all the players that DOES NOT have the target hand value
	 */
	private Player[] getNotMatchingPlayers(Player[] src, int handVal) {
		String[] mp = playersToNames(getMatchingPlayers(src, handVal));
		Player[] out = new Player[0];
		for (Player p: src) {
			String name = p.getName();
			if (!Main.hasItem(mp, name))
				out = addToArr(out, p);
		}
		
		return out;
	}

	/**
	 * @param in	the Player array to be converted into names
	 * @return	an array of player names
	 */
	private String[] playersToNames(Player[] in) {
		String[] out = new String[in.length];
		for (int i=0; i<out.length; i++)
			out[i] = in[i].getName();
		return out;
	}

	/**
	 * Penalizes all the specified players according to their name
	 * @param names	the names of the players to receive one penalty
	 */
	private void penalizePlayers(String[] names) {
		for (String name: names)
			penalize(name, 1);
	}
	
	/**
	 * Penalizes the specified player according to their name with the specified count
	 * @param name	the name of the player to be penalized
	 * @param count	the number of penalties the player will receive
	 */
	private void penalize(String name, int count) {
		players.get(name).penalize(count);
	}
	
	/**
	 * @author Yangfa Wu
	 *	A custom class that specializes in blocking code without blocking other threads 
	 */
	private abstract class BlockingRunnable implements Runnable {
		
		public abstract void execute() throws ExecutionException, InterruptedException;
		
		public abstract void handleError(ExecutionException error);
		
		public abstract void handleError(InterruptedException error);
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				execute();
			} catch (ExecutionException error) {
				handleError(error);
			} catch (InterruptedException error2) {
				handleError(error2);
			}
			
		}
		
	}
	
	private boolean roundOver;
	
	/**
	 * @return whether or not the current round is over
	 */
	public boolean isRoundOver() {
		return this.roundOver;
	}
	
	/**
	 * The meat and potatoes of the game: an asynchronous management of all the player moves and game events in a game
	 */
	public void newRound() {
		resetCards();
		cleanOrder();
		dealOutCards();
		this.roundOver = false;
		
		this.statsHidden = true;
		
		final Game THIS = this;
		final ExecutorService service = Executors.newSingleThreadExecutor();
		service.submit(new BlockingRunnable() {
			
			public void abort() {
				service.shutdownNow();
			}
			
			public String getClientRef(String uid) {
				return "/gamecomms/%s/client/%s".formatted(THIS.id, uid);
			}
			
			public Request awaitForRequest(String uid, String... acceptedTypes) throws InterruptedException, ExecutionException {
				return Database.getBlockingRequest(getClientRef(uid), acceptedTypes).get();
			}
			
			final class BooleanWrapper {
				private boolean val;
				public BooleanWrapper(boolean val) {
					this.val = val;
				}
				public boolean getVal() {
					return this.val;
				}
				public void setVal(boolean val) {
					this.val = val;
				}
			}
			
			@Override
			public void execute() throws ExecutionException, InterruptedException {
				// TODO Auto-generated method stub
				while (!THIS.roundOver) {
					Player moving = THIS.players.get(THIS.order[0]);
					String movingUid = moving.getId();
					String movingName = moving.getName();
					
					// CHECK IF THE MOVING PLAYER KNOCKED
					if (moving.isKnocked()) {
						THIS.roundOver = true;
						THIS.message = movingName + " knocked earlier and it's now their turn. Tallying hand values...";
						THIS.update();
						
						TimeUnit.SECONDS.sleep(3);
						
						int bigHand = THIS.getBiggestHand();
						int smallHand = THIS.getSmallestHand();
						
						if (bigHand == smallHand)
							THIS.message = "Everyone had the same hand value. No penalties sent out!";
						else {
							Player[] playingPlayers = THIS.getPlayingPlayers();
							Player[] toBePenalized = THIS.getMatchingPlayers(playingPlayers, smallHand);
							
							THIS.penalizePlayers(THIS.playersToNames(toBePenalized));
							String out = "Player(s) with the lowest hand value (" + smallHand + ") got penalized.";
							
							if (Main.hasItem(THIS.playersToNames(toBePenalized), movingName)) {
								// penalize again.
								THIS.penalize(movingName, 1);
								out+= "\nAdditionally, " + movingName + " was one of these player(s), so they got another penalty for knocking.";
							}
							
							THIS.message = out;
						}
						
						THIS.statsHidden = false;
						THIS.update();
						
						TimeUnit.SECONDS.sleep(6);
					} else {
						THIS.message = "Waiting for " + movingName + " to make a move...";
						THIS.update();
						
						final BooleanWrapper satisfied = new BooleanWrapper(false);
						while (!satisfied.getVal()) {
							final Request move = awaitForRequest(movingUid, "KNOCK", "DRAW-DECK", "DRAW-DISCARD");
							final Response res = new Response(move);
							res.setOrigin("/gamecomms/" + THIS.id + "/server");
							
							switch (move.getType()) {
							case "KNOCK": {
								boolean newKnock = true;
								for (Player p: THIS.getPlayingPlayers())
									if (p.isKnocked())
										newKnock = false;
								if (newKnock) {
									moving.setKnocked(true);
									
									THIS.message = movingName + " decided to knock! Prepare yourselves!";
									THIS.update();
									
									satisfied.setVal(true);
									
									res.setSuccess(true);
									res.send();
								} else {
									res.message("Somebody else has already knocked.");
									res.send();
									move.resolve();
									continue;
								}
								break;
							}
							case "DRAW-DECK": {
								Card card = THIS.drawFromDeck();
								if (card != null) {
									moving.addCard(card);
									
									THIS.message = movingName + " drew a card from the deck. Waiting for " + movingName + " to discard a card.";
									THIS.update();
									
									res.setSuccess(true);
									res.send();
								} else {
									res.message("The deck is empty. Draw from somewhere else.");
									res.send();
									move.resolve();
									continue;
								}
								break;
							}
							case "DRAW-DISCARD": {
								Card card = THIS.drawFromDiscard();
								if (card != null) {
									moving.addCard(card);
									
									THIS.message = movingName + " drew the " + card.getName() + " from the discard pile. Waiting for " + movingName + " to discard a card.";
									THIS.update();
									
									res.setSuccess(true);
									res.send();
								} else {
									res.message("The discard pile is empty. Draw from somewhere else.");
									res.send();
									move.resolve();
									continue;
								}
								break;
							}
							default: {
								move.resolve();
								throw new ExecutionException(
										"Blocking GET request has a not-accepted type but still got through.", 
										new Throwable()
									);
							}}
							
							move.resolve();						
							
							if (!move.getType().equals("KNOCK")) {
								// the moving player has already drew a card
								
								
								final BooleanWrapper satisfied2 = new BooleanWrapper(false);
								while (!satisfied2.getVal()) {
									final Request move2 = awaitForRequest(movingUid, "DISCARD");
									final Response res2 = new Response(move2);
									res2.setOrigin("/gamecomms/" + THIS.id + "/server");
									
									if (move2.getType().equalsIgnoreCase("DISCARD")) {
										Object rank = move2.read("rank");
										if (rank == null) {
											res2.message("No card \"rank\" attribute provided in request content.");
											res2.send();
											move2.resolve();
											continue;
										} else {
											try {
												int val = Integer.parseInt(rank.toString());
												if (!Card.isValidRank(val)) {
													res2.message("The provided card \"rank\" attribute nneeds to be an integer between 1 and 13, inclusive.");
													res2.send();
													move2.resolve();
													continue;
												}
											} catch (NumberFormatException error) {
												res2.message("The provided card \"rank\" attribute needs to be an integer.");
												res2.send();
												move2.resolve();
												continue;
											}
										}
										
										Object suit = move2.read("suit");
										if (suit == null) {
											res2.message("No card \"suit\" attribute provided in request content.");
											res2.send();
											move2.resolve();
											continue;
										} else {
											String suitString = suit.toString();
											if (!Card.isValidSuit(suitString)) {
												res2.message("The provided card \"suit\" attribute is not a valid suit.");
												res2.send();
												move2.resolve();
												continue;
											}
										}
										
										Card target = new Card(Integer.parseInt(rank.toString()), suit.toString());
										if (moving.hasCard(target)) {
											moving.discardCard(target);
											THIS.addToDiscard(target);
											
											THIS.message = movingName + " discards a " + target.getName() + ".";
											THIS.update();
											
											satisfied.setVal(true);
											satisfied2.setVal(true);
											
											res2.setSuccess(true);
											res2.send();
										} else {
											res2.message("You do not have such a card in your hand.");
											res2.send();
											move2.resolve();
											continue;
										}
									} else {
										throw new ExecutionException(
												"Blocking GET request has a not-accepted type but still got through.", 
												new Throwable()
											);
									}
									
									move2.resolve();
									
								}
								TimeUnit.SECONDS.sleep(2);
							} else {
								TimeUnit.SECONDS.sleep(2);
							}
						}
					}
					// outside if statement checking knock
					nextTurn();
				}
				// round is over
				Player[] playingPlayers = THIS.getPlayingPlayers();
				int numLeft = 0;
				for (Player p: playingPlayers) {
					p.setKnocked(false);
					if (!p.isOut())
						numLeft++;
				}
				
				if (numLeft > 1) {
					// new round
					THIS.message = "A new round with players who are not out will start momentarily...";
					THIS.update();
					
					TimeUnit.SECONDS.sleep(4);
					THIS.newRound();
				} else {
					Player winner = null;
					for (Player p: playingPlayers)
						if (!p.isOut()) {
							winner = p;
							break;
						}
					
					if (winner == null) throw new ExecutionException(
							"Game has winner, but could not identiy the winner.", 
							new Throwable()
						);
					THIS.message = winner.getName() + " is the only one left without 3 strikes! " + winner.getName() + " wins!";
					THIS.isOver = true;
					THIS.update();
					
					final String WINNERID = winner.getId();
					for (Player p: THIS.players.values()) {
						Database.get("/users/" + p.getId(), new ValueEventListener() {
							
							@Override
							public void onDataChange(DataSnapshot snapshot) {
								// TODO Auto-generated method stub
								if (snapshot.getValue() == null) return;
								
								User user = snapshot.getValue(User.class);
								user.incrementStat("games-finished");
								user.incrementStat("games-played");
								
								if (WINNERID.equals(user.getId())) {
									user.gainExp(900);
									user.incrementStat("times-won");
								} else {
									user.gainExp(450);
									user.incrementStat("times-lost");
								}
								
								user.update();
							}
							
							@Override
							public void onCancelled(DatabaseError error) {
								// TODO Auto-generated method stub
								error.toException().printStackTrace();
							}
						});
					}
					
					TimeUnit.SECONDS.sleep(20);
					
					THIS.message = "Game will close soon, please exit...";
					THIS.update();
					
					TimeUnit.SECONDS.sleep(10);
					THIS.message = "Game is killed. Room is killed.";
					THIS.update();
					
					TimeUnit.SECONDS.sleep(3);
					THIS.close();
				}
				
			}
			
			@Override
			public void handleError(ExecutionException error) {
				// TODO Auto-generated method stub
				error.printStackTrace();
				abort();
			}

			@Override
			public void handleError(InterruptedException error) {
				// TODO Auto-generated method stub
				error.printStackTrace();
				abort();
			}
		});
	}
	
}
