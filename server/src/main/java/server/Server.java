package server;

import java.util.List;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Router;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.Request;
import org.restlet.Response;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.Key;


public class Server extends Application {


	public Server() {
		super();
	}


	public static void main(String[] args) throws Exception {
		Component component = new Component();
		component.getServers().add(Protocol.HTTP, 8100);
		Server application = new Server();
		component.getDefaultHost().attach(application);
		List<Key<User>> keys = ObjectifyService.ofy().load().type(User.class).keys().list();
		ObjectifyService.ofy().delete().keys(keys).now();
		component.start();
	}


	/**
	 * Creates a root Restlet that will
	 * receive all incoming calls.
	 */
	@Override
	public Restlet createInboundRoot() {
		Router router = new Router(getContext());
		router.attach("/users/{userName}", UserResource.class);
		router.attach("/users", UsersResource.class);

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
}
