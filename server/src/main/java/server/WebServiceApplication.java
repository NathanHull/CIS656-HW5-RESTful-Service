package server;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Router;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.Request;
import org.restlet.Response;


public class WebServiceApplication extends Application {


	public static void main(String[] args) throws Exception {

		// Create a component
		Component component = new Component();
		component.getServers().add(Protocol.HTTP, 8100);

		WebServiceApplication application = new WebServiceApplication();

		// Attach the application to the component and start it
		component.getDefaultHost().attach(application);
		component.start();
	}

	/**
	 * Constructor to create a WebServiceApplication instance.
	 */
	public WebServiceApplication() {
		super();
	}

  /**
   * Creates a root Restlet that will receive all incoming calls.
   */
  @Override
  public Restlet createInboundRoot() {

		// Have the router, route resource requests to the appropriate resource class based on the URL pattern.
		Router router = new Router(getContext());
		router.attach("/widgets", WidgetsResource.class);
		router.attach("/widgets/{id}", WidgetResource.class);

		// This page is going to show up if somebody access the default page of the web server.
		Restlet mainpage = new Restlet() {
			@Override
			public void handle(Request request, Response response) {
				StringBuilder stringBuilder = new StringBuilder();

				stringBuilder.append("<html>");
				stringBuilder
				.append("<head><title>Webserver Index</title></head>");
				stringBuilder.append("<body bgcolor=white>");
				stringBuilder.append("<h1>Webserver serving RESTful web services</h1>");
				stringBuilder.append("<a href=\"/widgets\">Widgets</a>");
				stringBuilder.append("</body>");
				stringBuilder.append("</html>");

				response.setEntity(new StringRepresentation(stringBuilder
						.toString(), MediaType.TEXT_HTML));

			}
		};
		router.attach("/", mainpage);
		return router;
	}

}
