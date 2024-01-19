import java.util.HashMap;

public class User {
	private String id, name;
	private long exp;
	private HashMap<String, Object> stats;
	
	/**
	 * Creates a User object with the following fields
	 * @param id	the id of the user assigned by Fbase Auth
	 * @param name	the username of the user used for logging in
	 * @param exp	the exp of the user
	 * @param stats	the stats of the user
	 */
	public User(String id, String name, long exp, HashMap<String, Object> stats) {
		this.id = id;
		this.name = name;
		this.exp = exp;
		this.stats = stats;
	}
	
	/**
	 * Creates a User with just the following fields
	 * @param id	the id of the user assigned by Fbase Auth
	 * @param name	the username of the user used for logging in
	 */
	public User(String id, String name) {
		this(id, name, 0, new HashMap<String, Object>());
		setStat("games-played", 0);
		setStat("games-finished", 0);
		setStat("times-won", 0);
		setStat("times-lost", 0);
	}
	
	/**
	 * Creates an unusable User object that is meant for testing only
	 */
	public User() {
		this(Database.randomId(), "NO-USERNAME");
	}

	/**
	 * @return	the id of the user
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return	the name of the user
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return	the exp of the user
	 */
	public long getExp() {
		return exp;
	}

	/**
	 * @return	the stats of the user
	 */
	public HashMap<String, Object> getStats() {
		return stats;
	}

	/**
	 * @return	the level of the user
	 */
	public long getLevel() {
		return exp/1000+1;
	}
	
	/**
	 * @param key	id of a single stat
	 * @return	a single stat corresponding to the id (returns null if not a valid stat id)
	 */
	public Object getStat(String key) {
		return stats.get(key);
	}
	
	/**
	 * @param key	id of stat
	 * @param value	the stat
	 * @return	the User object itself for chaining
	 */
	public User setStat(String key, Object value) {
		stats.put(key, value);
		return this;
	}
	
	/**
	 * Grants experience points to the user
	 * @param val	the amount of experience to gain
	 */
	public void gainExp(long val) {
		this.exp+= val;
	}
	
	/**
	 * Attempts to increment a single stat by 1
	 * @param name	id of the stat to increment
	 */
	public void incrementStat(String name) {
		Object stat = getStat(name);
		if (stat == null) return;
		
		try {
			int val = Integer.parseInt(stat.toString());
			val++;
			setStat(name, Integer.valueOf(val));
		} catch (NumberFormatException err) {
			return;
		}
	}
	
	/**
	 * Posts all the values of the object onto the database
	 */
	public void update() {
		User THIS = this;
		Database.set("/users/" + this.id, this, new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Database.print("UPDA", "USER", THIS.id);
			}
		});
	}

}
