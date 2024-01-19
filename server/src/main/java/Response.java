import java.util.Date;
import java.util.HashMap;

public class Response extends Message {
	public static HashMap<String, Response> log = new HashMap<String, Response>();
	
	/**
	 * @param r	a Response object
	 * @return	whether or not it was successful in adding the response to memory
	 */
	public static boolean add(Response r) {
		String resId = r.getId();
		if (log.containsKey(resId))
			return false;
		else {
			log.put(resId, r);
			return true;
		}
	}
	
	/**
	 * @param resId	a possible response id
	 * @return	whether or not such a response exists in memory
	 */
	public static boolean hasResponse(String resId) {
		return log.containsKey(resId);
	}
	
	/**
	 * @param resId	a possible response id
	 * @return	whether or not it was successful in removing a response such such id from memory
	 */
	public static boolean remove(String resId) {
		if (log.containsKey(resId)) {
			log.remove(resId);
			return true;
		} else 
			return false;
	}
	
	/**
	 * @param resId	a possible response id
	 * @return	the response in memory that that has such id (if none, returns null)
	 */
	public static Response getResponse(String resId) {
		return log.get(resId);
	}

	/**
	 * Creates a response object based on the following fields
	 * @param id	the id of the message
	 * @param uid	the uid of the messenger
	 * @param type	the type of message
	 */
	public Response(String id, String uid, String type) {
		super("server", id, uid, type, new Date().getTime(), new HashMap<String, Object>());
		// TODO Auto-generated constructor stub
		setSuccess(false);
	}
	
	/**
	 * Creates an Response based a request
	 */
	public Response(Request req) {
		this(req.getId(), req.getUid(), req.getType());
	}
	
	/**
	 * Sets the success state of the response
	 * @param b the state of the response's success
	 */
	public void setSuccess(boolean b) {
		write("success", b);
	}
	
	/**
	 * Sets the message of the response
	 * @param msg	message attached as additional info in response
	 */
	public void message(String msg) {
		write("message", msg);
	}
	
	/**
	 *	@return a string representation of the response
	 */
	@Override
	public String toString() {
		return "RES " + super.toString();
	}
	
	/**
	 * Removes the response from the database
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
	
	
	/**
	 * Sends the response into the database to be picked up and confirmed by the client
	 */
	public void send() {
		final Response THIS = this;
		if (add(this)) {
			remove(getId());
			Database.set(getPath(), this, new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					Database.print("SENT", THIS);
					add(THIS);
				}
			});
		} else
			Database.print("DUP", this);
	}

}
