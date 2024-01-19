import java.util.Date;
import java.util.HashMap;

public class Request extends Message {
	public static HashMap<String, Request> log = new HashMap<String, Request>();
	
	/**
	 * @param r	a Request object
	 * @return	whether or not it was successful in adding the request to memory
	 */
	public static boolean add(Request r) {
		String reqId = r.getId();
		if (log.containsKey(reqId))
			return false;
		else {
			log.put(reqId, r);
			return true;
		}
	}
	
	/**
	 * @param reqId	a possible request id
	 * @return	whether or not it was successful in removing a request such such id from memory
	 */
	public static boolean remove(String reqId) {
		if (log.containsKey(reqId)) {
			log.remove(reqId);
			return true;
		} else 
			return false;
	}
	
	/**
	 * @param reqId	a possible request id
	 * @return	the request in memory that that has such id (if none, returns null)
	 */
	public static Request getRequest(String reqId) {
		return log.get(reqId);
	}

	/**
	 * Creates a request with the required fields
	 * @param origin	where the message is found, excluding id and uid
	 * @param id	the id of the message
	 * @param uid	the uid of the messenger
	 * @param type	the type of message
	 * @param timestamp	the time in milliseconds when this message was made
	 * @param content	any additional info needed to complete the message
	 */
	public Request(String origin, String id, String uid, String type, long timestamp, HashMap<String, Object> content) {
		super(origin, id, uid, type, timestamp, content);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Creates an unusable Request that is meant for testing purposes only
	 */
	public Request() {
		this("client", Database.randomId(), Database.randomId(), "NO-TYPE", new Date().getTime(), new HashMap<String, Object>());
	}
	
	/**
	 * Attempts to add self to memory (IN console if successful, DUP if not)
	 */
	public void add() {
		if (add(this))
			Database.print("IN", this);
		else
			Database.print("DUP", this);
	}
	
	/**
	 *	@return a string representation of the request
	 */
	@Override
	public String toString() {
		return "REQ " + super.toString();
	}
	
	/**
	 * Removes the request from the database
	 */
	public void resolve() {
		super.resolve(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				remove(getId());
			}
		});
	}

}
