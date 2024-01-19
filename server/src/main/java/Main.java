import java.util.HashMap;
import java.util.Timer;
import java.util.concurrent.ExecutionException;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;

/**
 * @author Yangfa Wu
 *
 */
public class Main {

	/**
	 * C'mon really
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Timer();
		Cheese.use();
		Database.start();		
		try {
			HashMap<String, Object> dirtyPaths = new HashMap<>();
			dirtyPaths.put("gamecomms", null);
			dirtyPaths.put("games", null);
			dirtyPaths.put("rooms", null);
			dirtyPaths.put("server", null);
			dirtyPaths.put("client", null);	
			FirebaseDatabase.getInstance().getReference()
				.updateChildrenAsync(dirtyPaths).get();
			Database.watch("/client", new Database.CustomCEL() {
				
				public boolean isValidRequest(DataSnapshot snapshot) {
					String[] reqs = { "origin", "id", "uid", "type", "timestamp" };
					for (String attr: reqs)
						if (!snapshot.hasChild(attr))
							return false;
					return true;
				}
				
				@Override
				public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
					// TODO Auto-generated method stub
					if (snapshot.hasChildren())
						for (DataSnapshot ds: snapshot.getChildren())
							if (isValidRequest(ds)) {
								Request req = ds.getValue(Request.class);
								req.add();
								handleRequest(req);
							}			
				}
			});
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param <T> any non-primitive Type
	 * @param arr	an array of Type T to loop through
	 * @param item	an item of Type T to look for in array
	 * @return	whether or not the desired item is in the specified array
	 */
	public static <T> boolean hasItem(T[] arr, T item) {
		for (T i: arr)
			if (i.equals(item))
				return true;
		return false;
	}
	
	/**
	 * @param src a String array that strings will be added to
	 * @param val	the String to be added
	 * @return	a String array containing all the strings passed through the method
	 */
	public static String[] addToArr(String[] src, String val) {
		String[] out = new String[src.length+1];
		for (int i=0; i<src.length; i++)
			out[i] = src[i];
		out[src.length] = val;
		return out;
	}
	
	/**
	 * Handles different client requests
	 * @param req a Request from the client on default /client branch
	 */
	public static void handleRequest(final Request req) {
		final Response res = new Response(req);
		
		switch (req.getType()) {
		case "USER-GOT-RESPONSE": {
			Object prevResId = req.read("prevResId");
			if (prevResId == null)
				res.message("No \"prevResId\" attribute provided in request content.");
			else if (!Response.hasResponse(prevResId.toString()))
				res.message("Cannot find any response with an ID that matched the provided \"prevResId\" value.");
			else {
				Response.getResponse(prevResId.toString()).resolve();
				break;
			}
			res.send();
			break;
		}
		case "MAKE-ACCOUNT": {
			Object name = req.read("name");
			if (name == null) {
				res.message("No user \"name\" attribute provided in request content.");
				res.send();
			} else {
				res.setSuccess(true);
				User newUser = new User(req.getUid(), name.toString());
				Database.set("/users/" + newUser.getId(), newUser, new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						res.send();
					}
				});
			}
			break;
		}
		case "MAKE-ROOM": {
			Object password = req.read("password");
			
			final Room room;
			if (password == null) room = new Room();
			else room = new Room(password.toString());
			
			room.addUser(req.getUid(), new Room.AddUserOnComplete() {
				
				@Override
				public void start() {
					// TODO Auto-generated method stub
					res.setSuccess(true);
					res.write("code", room.getId());
					res.send();
				}
			});
			// user should auto join the room using the code
			break;
		}
		case "JOIN-ROOM": {
			// content::code
			Object code = req.read("code");
			Object password = req.read("password");
			
			if (code == null) res.message("No room \"code\" attribute provided in request content.");
			else if (!Room.hasRoom(code.toString())) res.message("There is no room with this code.");
			else {
				Room room = Room.getRoom(code.toString());
				
				if (room.hasUser(req.getUid())) {
					res.setSuccess(true);
					res.send();
					break;
				} else if (room.isLocked() && password == null)
					res.message("No password provided to a private room. Access denied.");
				else if (room.isLocked() && !room.check(password.toString()))
					res.message("Incorrect password provided to a private room. Access denied.");
				else if (room.getNumUsers() > 3)
					res.message("Room is full. Access denied.");
				else {
					room.addUser(req.getUid(), new Room.AddUserOnComplete() {

						@Override
						public void start() {
							// TODO Auto-generated method stub
							res.setSuccess(true);
							res.send();
						}
					});
					break;
				}
			}
			res.send();
			break;
		}
		default: {
			res.write("message", "Invalid request type provided.");
			res.send();
			break;
		}}
		
		req.resolve();
	}
	
}
