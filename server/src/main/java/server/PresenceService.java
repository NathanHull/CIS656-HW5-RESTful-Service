package server;

import java.util.HashMap;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Router;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.Request;
import org.restlet.Response;


public class PresenceService extends Application {

	static HashMap<String, User> users = null;
	public static void main(String[] args) throws Exception {
		Component component = new Component();
		component.getServers().add(Protocol.HTTP, 8100);
		PresenceService application = new PresenceService();
		component.getDefaultHost().attach(application);
		users = new HashMap<>();
		component.start();
	}


	public PresenceService() {
		super();
	}


	/**
	 * Creates a root Restlet that will
	 * receive all incoming calls.
	 */
	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/users", UsersResource.class);
		router.attach("/users/{userName}", UserResource.class);

		Restlet mainpage = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				StringBuilder stringBuilder = new StringBuilder();

				stringBuilder.append("<html>");
				stringBuilder
				.append("<head><title>Webserver Index</title></head>");
				stringBuilder.append("<body>");
				stringBuilder.append("<h1>Webserver serving RESTful web services</h1>");
				stringBuilder.append("<a href=\"/users\">Users</a>");
				stringBuilder.append("</body>");
				stringBuilder.append("</html>");

				response.setEntity(new StringRepresentation(stringBuilder.toString(), MediaType.TEXT_HTML));

			}
		};
		router.attach("/", mainpage);
		return router;
	}


	/**
     * Register a client with the presence service.
     * @param reg The information that is to be registered about a client.
     */
	void register(User reg) {
		users.put(reg.getUserName(), reg);
	}


    /**
     * Unregister a client from the presence service.  Client must call this
     * method when it terminates execution.
     * @param userName The name of the user to be unregistered.
     */
	void unregister(String userName) {
		users.remove(userName);
	}


    /**
     * Lookup the registration information of another client.
     * @param name The name of the client that is to be located.
     * @return The RegistrationInfo info for the client, or null if
     * no such client was found.
     */
    User lookup(String name) {
		return users.get(name);
	}

    
    /**
     * Sets the user's presence status.
     * @param name The name of the user whose status is to be set.
     * @param status true if user is available, false otherwise.
     */
    void setStatus(String userName, boolean status) {
		users.get(userName).setStatus(status);
	}
	
	
    /**
     * Determine all users who are currently registered in the system.
     * @return An array of RegistrationInfo objects - one for each client
     * present in the system.
     */
    User[] listRegisteredUsers() {
		return (User[]) users.values().toArray();
	}  
}
