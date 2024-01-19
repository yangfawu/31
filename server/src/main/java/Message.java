import java.util.HashMap;

/**
 * @author Yangfa Wu
 *
 */
public abstract class Message {
	private String origin, id, uid, type;
	private long timestamp;
	private HashMap<String, Object> content;
	
	/**
	 * Creates a message with the required fields
	 * @param origin	where the message is found, excluding id and uid
	 * @param id	the id of the message
	 * @param uid	the uid of the messenger
	 * @param type	the type of message
	 * @param timestamp	the time in milliseconds when this message was made
	 * @param content	any additional info needed to complete the message
	 */
	public Message(String origin, String id, String uid, String type, long timestamp, HashMap<String, Object> content) {
		this.origin = format(origin);
		this.id = format(id);
		this.uid = format(uid);
		this.type = type.toUpperCase();
		this.timestamp = timestamp;
		this.content = content;
	}
	
	/**
	 * A helper method to format paths
	 * @param pathElement	any badly formatted path string
	 * @return	a properly formatted path string
	 */
	private String format(String pathElement) {
		while (pathElement.startsWith("/"))
			pathElement = pathElement.substring(1);
		while (pathElement.endsWith("/"))
			pathElement = pathElement.substring(0, pathElement.length()-1);
		return pathElement;
	}

	/**
	 * @return where the message is found, excluding id and uid
	 */
	public String getOrigin() {
		return origin;
	}

	/**
	 * Modifies the origin of the message
	 * @param origin	where the message is found, excluding id and uid
	 */
	public void setOrigin(String origin) {
		this.origin = format(origin);
	}

	/**
	 * @return the id of the message
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the uid of the messenger
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * @return the type of message
	 */
	public String getType() {
		return type.toUpperCase();
	}

	/**
	 * @return when the message was made
	 */
	public long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return additional info in the message
	 */
	public HashMap<String, Object> getContent() {
		return content;
	}
	
	/**
	 * @return path of the message where the message was originally found on the database
	 */
	public String getPath() {
		return "/%s/%s/%s".formatted(origin, uid, id);
	}
	
	/**
	 * @param key	a id of the additional info
	 * @param val	the additional info
	 * @return	the Message object itself for chaining
	 */
	public Message write(String key, Object val) {
		content.put(key, val);
		return this;
	}
	
	/**
	 * @param key the id of the additional info
	 * @return	the additional info corresponding to the id
	 */
	public Object read(String key) {
		return content.get(key);
	}
	
	/**
	 * Removes the message from the database and run the onComplete callback on completion
	 * @param onComplete
	 */
	public void resolve(Runnable onComplete) {
		Database.set(getPath(), null, onComplete);
	}
	
	/**
	 *	@return a string representation of the message
	 */
	@Override
	public String toString() {
		return "%s::%s %s".formatted(id, uid, getType());
	}
	
}
