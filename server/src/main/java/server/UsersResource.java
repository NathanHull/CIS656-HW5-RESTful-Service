package server;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
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
	 * Handle an HTTP GET: represent list of users
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
				JSONArray userArray = new JSONArray();
				for(User o : this.users) {
					userArray.put(o.toJSON());
				}
				result = new JsonRepresentation(userArray);
			} else {
				// create a plain text representation of our list of users
				StringBuffer buf = new StringBuffer("<html><head><title>User Resources</title><head><body><h1>User Resources</h1>");
				buf.append("<form name=\"input\" action=\"/users\" method=\"POST\">");
				buf.append("User Name: ");
				buf.append("<input type=\"text\" name=\"userName\" />");
				buf.append("<br/>Host Address: ");
				buf.append("<input type=\"text\" name=\"host\" />");
				buf.append("<br/>Port: ");
				buf.append("<input type=\"text\" name=\"port\" />");
				buf.append("<br/>Available: ");
				buf.append("<input type=\"checkbox\" name=\"status\" />");
				buf.append("<br/>");
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
	 * Handle a POST Http request: register new user
	 *
	 * @param entity
	 * @throws ResourceException
	 */
	@Post
	public Representation post(Representation entity, Variant variant)
		throws ResourceException
	{
		Representation rep = null;

		try {
			if (entity.getMediaType().equals(MediaType.APPLICATION_WWW_FORM,
					true))
			{
				Form form = new Form(entity);
				User u = new User();
				u.setUserName(form.getFirstValue("userName"));
				System.out.println(u.getUserName());
				u.setHost(form.getFirstValue("host"));
				System.out.println("Host");
				String port = form.getFirstValue("port");
				System.out.println("Got port at leaset:" + port);
				u.setPort(Integer.parseInt(port));
				System.out.println("Port");
				u.setStatus(Boolean.getBoolean(form.getFirstValue("status")));
				System.out.println("before save");
        		ObjectifyService.ofy().save().entity(u).now();

				getResponse().setStatus(Status.SUCCESS_OK);
				rep = new StringRepresentation(u.toHtml(false));
				rep.setMediaType(MediaType.TEXT_HTML);
				System.out.println("Before response");
				getResponse().setEntity(rep);

			} else if (entity.getMediaType().equals(MediaType.APPLICATION_JSON)) {
				JSONObject json = new JSONObject(entity);
				User u = new User();
				u.setUserName(json.getString("userName"));
				u.setHost(json.getString("host"));
				u.setPort(json.getInt("port"));
				u.setStatus(json.getBoolean("status"));
        		ObjectifyService.ofy().save().entity(u).now();

				getResponse().setStatus(Status.SUCCESS_OK);
				rep = new StringRepresentation(u.toJSON().toString());
				rep.setMediaType(MediaType.APPLICATION_JSON);
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
