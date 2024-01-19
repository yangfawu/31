import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class Room {
	private static final int maxUsers = 3;
	private static HashMap<String, Room> rooms = new HashMap<String, Room>();
	
	/**
	 * @param id	a possible room id
	 * @return	whether or not the memory has a room with such an id
	 */
	public static boolean hasRoom(String id) {
		return rooms.containsKey(id);
	}
	
	/**
	 * Attempts to remove a room with such id from memory
	 * @param id	a possible room id
	 */
	public static void removeRoom(String id) {
		if (hasRoom(id))
			rooms.remove(id);
	}
	
	/**
	 * @param r	a Room object
	 * @return	whether or not it was successful in adding such a room
	 */
	public static boolean addRoom(Room r) {
		if (rooms.containsKey(r.id))
			return false;
		else {
			rooms.put(r.id, r);
			return true;
		}
	}
	
	/**
	 * @param id	a possible room id
	 * @return	the room in memory corresponding to such id (returns null if none)
	 */
	public static Room getRoom(String id) {
		return rooms.get(id);
	}
	
	private String id, password, message;
	private User[] users;
	private Game game;
	private boolean started;
	
	/**
	 * Creates a room with the given password
	 * @param password	the password to the room
	 */
	public Room(String password) {
		do this.id = Database.randomId().split("-")[0];
		while (hasRoom(this.id));
		this.password = password;
		this.message = "Waiting for players...(0/" + maxUsers + ")";
		this.users = new User[0];
		this.game = null;
		this.started = false;
		
		addRoom(this);
	}
	
	/**
	 * Creates a password with no password
	 */
	public Room() {
		this("");
	}
	
	/**
	 * @return	the id of the room
	 */
	public String getId() {
		return this.id;
	}
	
	/**
	 * @return	whether or not a game has been started
	 */
	public boolean getStarted() {
		return started;
	}
	
	/**
	 * @return	the number of users allowed into the room
	 */
	public int getNumUsers() {
		return users.length;
	}
	
	/**
	 * @param pwd	a possible password to the room
	 * @return	whether or not the password is correct
	 */
	public boolean check(String pwd) {
		return password.equals(pwd);
	}
	
	/**
	 * @return	whether or not the room has a password
	 */
	public boolean isLocked() {
		return !check("");
	}
	
	/**
	 * @return	the string representation of all the users allowed into the room
	 */
	public String getUsers() {
		String out = "";
		for (User u: users)
			out+= ":" + u.getId() + ":";
		return out;
	}
	
	/**
	 * @return	the server message for the room
	 */
	public String getMessage() {
		return this.message;
	}
	
	/**
	 * @return	the id of the game being hosted by this room
	 */
	public String getGame() {
		return game == null ? null : game.getId();
	}
	
	/**
	 *	@return the string represenation of this room
	 */
	@Override
	public String toString() {
		return "ROOM " + id;
	}
	
	/**
	 * @author Yangfa Wu
	 *	A custom interface that I can use as a callback
	 */
	public static interface AddUserOnComplete {
		public void start();
	}
	
	/**
	 * @param uid	a id of a user that could be in the room
	 * @return	whether or not this room has a user with such an id
	 */
	public boolean hasUser(String uid) {
		for (User u: users)
			if (u.getId().equals(uid))
				return true;
		return false;
	}
	
	/**
	 * Attempts to add a player by id to the room
	 * @param uid	the id of the user to be added to the room
	 * @param onComplete	the callback function that runs after a successful attempt to add the user to the room
	 */
	public void addUser(String uid, final AddUserOnComplete onComplete) {
		if (this.users.length >= maxUsers) return;
		
		if (hasUser(uid)) {
			onComplete.start();
			return;
		}
				
		
		final Room THIS = this;
		Database.get("/users/" + uid, new ValueEventListener() {
			
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				// TODO Auto-generated method stub
				if (snapshot.getValue() == null) return;
				
				User[] newUsers = new User[THIS.users.length + 1];
				for (int i=0; i<THIS.users.length; i++)
					newUsers[i] = THIS.users[i];
				
				User user = snapshot.getValue(User.class);
				newUsers[THIS.users.length] = user;
				
				THIS.users = newUsers;
				
				if (newUsers.length >= Room.maxUsers) {
					THIS.message = "Game starting soon...";
					THIS.update();
					// GAME STARTS IN A FEW SECONDS
					
					final Timer timeout = new Timer();
					timeout.schedule(new TimerTask() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							THIS.game = new Game(THIS, THIS.users);
							THIS.message = "Game has started.";
							THIS.started = true;
							THIS.update();
							
							THIS.game.reset();
						}
					}, 10000);
				} else {
					THIS.message = "Waiting for players...(" + THIS.users.length + "/" + Room.maxUsers + ")";
					THIS.update();
				}
				
				onComplete.start();
			}
			
			@Override
			public void onCancelled(DatabaseError error) {
				// TODO Auto-generated method stub
				Database.print(error.getMessage());
			}
		});
	}
	
	/**
	 * Attempts to add a user by id to the room
	 * @param uid	the uid of an user
	 */
	public void addUser(String uid) {
		final Room THIS = this;
		addUser(uid, new AddUserOnComplete() {
			
			@Override
			public void start() {
				// TODO Auto-generated method stub
				Database.print("ADDED USER", THIS);
			}
		});
	}
	
	/**
	 * Posts all the current values of the room onto the database on a predetermined path
	 */
	public void update() {
		final Room THIS = this;
		Database.set("/rooms/" + id, this, new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Database.print("UPDA", THIS);
			}
		});
	}
	
	/**
	 * Closes the room (remove self from database)
	 * Postcondition: the room WILL definitely get deleted and clients can't access it
	 */
	public void close() {
		final Room THIS = this;
		Database.set("/rooms/" + id, null, new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Database.print("REMO", THIS);
				removeRoom(id);
			}
		});
	}
	
}
