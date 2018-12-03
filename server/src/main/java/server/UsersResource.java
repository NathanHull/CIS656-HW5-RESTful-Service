package server;

import java.util.List;
import org.json.JSONArray;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.resource.Post;
import org.restlet.resource.Get;
import com.googlecode.objectify.ObjectifyService;


public class UsersResource extends ServerResource {

	private List<User> users = null;


	@Override
	public void doInit() {
		this.users = ObjectifyService.ofy().load().type(User.class).list();
		
		// representation types this resource can use to describe
		getVariants().add(new Variant(MediaType.TEXT_HTML));
		getVariants().add(new Variant(MediaType.APPLICATION_JSON));
	}


	/**
	 * Handle an HTTP GET. Represent the user object in the requested format.
	 *
	 * @param variant
	 * @return
	 * @throws ResourceException
	 */
	@Get
	public Representation get(Variant variant) throws ResourceException {
		Representation result = null;
		if (null == this.users) {
			ErrorMessage em = new ErrorMessage();
			return representError(variant, em);
		} else {
			if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
				JSONArray widgetArray = new JSONArray();
				for(User o : this.users) {
					widgetArray.put(o.toJSON());
				}

				result = new JsonRepresentation(widgetArray);
			} else {
				// create a plain text representation of our list of widgets
				StringBuffer buf = new StringBuffer("<html><head><title>User Resources</title><head><body><h1>User Resources</h1>");
				buf.append("<form name=\"input\" action=\"/users\" method=\"POST\">");
				buf.append("User name: ");
				buf.append("<input type=\"text\" userName=\"User Name\" />");
				buf.append("<input type=\"text\" host=\"Host Address\" />");
				buf.append("<input type=\"text\" port=\"Port\" />");
				buf.append("<input type=\"checkbox\" status=\"Available\" />");
				buf.append("<input type=\"submit\" value=\"Create\" />");
				buf.append("</form>");
				buf.append("<br/><h2> There are " + this.users.size() + " users.</h2>");
				for(User o : this.users) {
					buf.append(o.toHtml(true));
				}
				buf.append("</body></html>");
				result = new StringRepresentation(buf.toString());
				result.setMediaType(MediaType.TEXT_HTML);
			}
		}
		return result;
	}


	/**
	 * Handle a POST Http request. Create a new user
	 *
	 * @param entity
	 * @throws ResourceException
	 */
	@Post
	public Representation post(Representation entity, Variant variant)
		throws ResourceException
	{
		Representation rep = null;

		// We handle only a form request in this example. Other types could be
		// JSON or XML.
		try {
			if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM,
					true))
			{
				// Create user
				Form form = new Form(entity);
				User w = new User();
				w.setUserName(form.getFirstValue("userName"));
				w.setHost(form.getFirstValue("host"));
				w.setPort(Integer.parseInt(form.getFirstValue("port")));
				w.setStatus(Boolean.getBoolean(form.getFirstValue("status")));
        		ObjectifyService.ofy().save().entity(w).now();

				getResponse().setStatus(Status.SUCCESS_OK);
				rep = new StringRepresentation(w.toHtml(false));
				rep.setMediaType(MediaType.TEXT_HTML);
				getResponse().setEntity(rep);
			} else {
				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			}
		} catch (Exception e) {
			getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
		}
		return rep;
	}


	/**
	 * Represent an error message in the requested format.
	 *
	 * @param variant
	 * @param em
	 * @return
	 * @throws ResourceException
	 */
	private Representation representError(Variant variant, ErrorMessage em)
	throws ResourceException {
		Representation result = null;
		if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
			result = new JsonRepresentation(em.toJSON());
		} else {
			result = new StringRepresentation(em.toString());
		}
		return result;
	}


	protected Representation representError(MediaType type, ErrorMessage em)
	throws ResourceException {
		Representation result = null;
		if (type.equals(MediaType.APPLICATION_JSON)) {
			result = new JsonRepresentation(em.toJSON());
		} else {
			result = new StringRepresentation(em.toString());
		}
		return result;
	}
}
