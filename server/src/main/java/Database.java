import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.function.Supplier;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * @author Yangfa Wu
 *
 */
public class Database {

	/**
	 * Authorizes the Admin SDK so we can use FBase services
	 */
	public static void start() {
		try {
			FileInputStream serviceAccount = new FileInputStream(
					"./src/main/resources/serviceAccountKey/the-fisherman-68f32-firebase-adminsdk-hboox-4f85ed5d02.json");

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(GoogleCredentials.fromStream(serviceAccount))
					.setServiceAccountId("firebase-adminsdk-hboox@the-fisherman-68f32.iam.gserviceaccount.com")
					.setDatabaseUrl("https://the-fisherman-68f32-default-rtdb.firebaseio.com").build();

			FirebaseApp.initializeApp(options);
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	/**
	 * @return a randomly generated ID
	 */
	public static String randomId() {
		return UUID.randomUUID().toString();
	}
	
	/**
	 * A modified printing method that timestamps lines and take in an infinite number of objects
	 * @param objs	Objects to be printed in the specified order
	 */
	public static void print(Object... objs) {
		String out = "";
		if (objs.length > 0)
			for (Object obj: objs)
				out+= obj.toString() + " ";
		System.out.println("[" + new Date().toString() + "] " + out);
	}
	
	/**
	 * @param path	a file path on the database
	 * @return a reference to the path on the database
	 */
	private static DatabaseReference ref(String path) {
		return FirebaseDatabase.getInstance().getReference(path);
	}
	
	/**
	 * Reads the value at the specified path on the database and uses the onComplete callback to work with the value
	 * @param path	a file path on the database
	 * @param onComplete	a callback function that works with the acquired value
	 */
	public static void get(String path, ValueEventListener onComplete) {
		ref(path).addListenerForSingleValueEvent(onComplete);
	}
	
	/**
	 * Sets the specified path on the database to the passed value and runs on the onComplete callback on completion
	 * @param <T>	any type of data that can be uploaded onto the database
	 * @param path	a file path on the database
	 * @param data	the data of Class T to be uploaded
	 * @param onComplete	a callback function that runs after a successful upload
	 */
	public static <T> void set(final String path, T data, Runnable onComplete) {
		ref(path).setValueAsync(data)
			.addListener(onComplete, new Executor() {
				
				@Override
				public void execute(Runnable command) {
					// TODO Auto-generated method stub
					command.run();
				}
			});
	}
	
	/**
	 * Waits for a value at the specified path on the database and then ambushes it so the provided onComplete callback function can work with it
	 * @param path	a file path on the database
	 * @param onComplete	a callback function that runs after a successful upload
	 */
	public static void ambush(String path, final ValueEventListener onComplete) {
		final DatabaseReference ref = ref(path);
		ref.addValueEventListener(new ValueEventListener() {
			
			@Override
			public void onDataChange(DataSnapshot snapshot) {
				// TODO Auto-generated method stub
				onComplete.onDataChange(snapshot);
				ref.removeEventListener(onComplete);
			}
			
			@Override
			public void onCancelled(DatabaseError error) {
				// TODO Auto-generated method stub
				onComplete.onCancelled(error);
			}
		});
	}
	
	/**
	 * @author Yangfa Wu
	 * A custom event listener that overrides methods I don't need so I can type less
	 */
	public static abstract class CustomCEL implements ChildEventListener {
		
		@Override
		public void onChildRemoved(DataSnapshot snapshot) {
			// TODO Auto-generated method stub
			// left body blank on purpose
		}
		
		@Override
		public void onChildMoved(DataSnapshot snapshot, String previousChildName) {
			// TODO Auto-generated method stub
			// left body blank on purpose
		}
		
		@Override
		public void onChildChanged(DataSnapshot snapshot, String previousChildName) {
			// TODO Auto-generated method stub
			// left body blank on purpose
		}
		
		@Override
		public void onCancelled(DatabaseError error) {
			// TODO Auto-generated method stub
			print(error.getMessage());
		}
		
	}
	
	/**
	 * @param path	a file path on the database
	 * @param listener	a child-event-listener that handles data added as children to the specified path on the database
	 * @return a database reference that can be used to cancel the listener
	 */
	public static DatabaseReference watch(String path, CustomCEL listener) {
		DatabaseReference ref = ref(path);
		ref.addChildEventListener(listener);
		return ref;
	}
	
	/**
	 * @param path	a file path on the database
	 * @param acceptedTypes	types of requests that the listener will only listen for at the specified path
	 * @return	a completable future that blocks the thread it's ran in until it catches a valid request
	 */
	public static CompletableFuture<Request> getBlockingRequest(String path, final String... acceptedTypes) {
		return CompletableFuture.supplyAsync(new Supplier<Request>() {	
			
			final class RequestWrapper {
				private Request req;
				public RequestWrapper(Request req) {
					this.req = req;
				}
				public void setReq(Request req) {
					this.req = req;
				}
				public Request getReq() {
					return this.req;
				}
			}

			@Override
			public Request get() {
				// TODO Auto-generated method stub
				final RequestWrapper reqOut = new RequestWrapper(null);			
				final DatabaseReference ref = FirebaseDatabase.getInstance()
						.getReference(path);		
				final Semaphore semaphore = new Semaphore(0);
				final CustomCEL listener = new CustomCEL() {
					
					public boolean isValidRequest(DataSnapshot snapshot) {
						String[] reqs = { "origin", "id", "uid", "type", "timestamp" };
						for (String attr: reqs)
							if (!snapshot.hasChild(attr))
								return false;
						return true;
					}
					
					public boolean requestHasAcceptedType(Request req) {
						String reqType = req.getType();
						for (String goodType: acceptedTypes)
							if (reqType.equalsIgnoreCase(goodType))
								return true;
						return false;
					}
					
					public void handleReq(Request req) {
						req.add();
						reqOut.setReq(req);
						ref.removeEventListener(this);
						semaphore.release();
					}
					
					@Override
					public void onChildAdded(DataSnapshot snapshot, String previousChildName) {
						// TODO Auto-generated method stub
						if (isValidRequest(snapshot)) {
							Request req = snapshot.getValue(Request.class);
							if (!requestHasAcceptedType(req)) return;
							
							handleReq(req);
						} else if (snapshot.hasChildren())
							for (DataSnapshot ds: snapshot.getChildren()) {
								if (isValidRequest(ds)) {
									Request req = ds.getValue(Request.class);
									if (!requestHasAcceptedType(req)) continue;
									
									handleReq(req);
									break;
								}
							}	
					}
				};
				
				ref.addChildEventListener(listener);

				try {
					semaphore.acquire();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				return reqOut.getReq();
			}
		});
	}

}
