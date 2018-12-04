package client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.restlet.Client;
import org.restlet.data.*;
import org.restlet.*;
import org.restlet.representation.Representation;


public class ChatClient
{
	// The base URL for all requests.
	static private String baseURL = "http://localhost:8080";
	static private String usersResourceURL;
	private String userName;
	private String host;
	private int port;
	private int recvPort;
	private boolean status;

	
	public ChatClient() {}

    public static void main(String args[]) {
		if (args.length != 3) {
			System.err.println("Input error: ./exe userName serverPort recvPort");
			System.exit(1);
		}

		ChatClient client = new ChatClient();
		usersResourceURL = baseURL + "/users";
		client.userName = args[0];
		client.port = Integer.parseInt(args[1]);
		client.recvPort = Integer.parseInt(args[2]);

		try {
			client.host = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			System.out.println("Local host is unknown");
		}
		System.out.println("Your receiving address: " + client.host + ":" + client.port);


		// // Invoke the client HTTP connector to send the POST request to the server.
		// Request request;
	    // Response resp = new Client(Protocol.HTTP).handle(request);

	    // // now, let's check what we got in response.
	    // System.out.println(resp.getStatus());
	    // Representation responseData = resp.getEntity();
	    // try {
		// 	System.out.println(responseData.getText());
		// } catch (IOException e) {
		// 	e.printStackTrace();
		// }

		// // EXAMPLE HTTP REQUEST #2
		// // Let's do an HTTP GET of widget 1 and ask for JSON response.
		// widgetsResourceURL = APPLICATION_URI + "/widgets/5066549580791808";
	    // request = new Request(Method.GET,widgetsResourceURL);

	    // // We need to ask specifically for JSON
        // request.getClientInfo().getAcceptedMediaTypes().
        // add(new Preference(MediaType.APPLICATION_JSON));

	    // // Now we do the HTTP GET
	    // System.out.println("Sending an HTTP GET to " + widgetsResourceURL + ".");
		// resp = new Client(Protocol.HTTP).handle(request);

		// // Let's see what we got!
		// if(resp.getStatus().equals(Status.SUCCESS_OK)) {
		// 	responseData = resp.getEntity();
		// 	System.out.println("Status = " + resp.getStatus());
		// 	try {
		// 		String jsonString = responseData.getText().toString();
		// 		System.out.println("result text=" + jsonString);
		// 		JSONObject jObj = new JSONObject(jsonString);
		// 		System.out.println("id=" + jObj.getInt("id") + " name=" + jObj.getString("name"));
		// 	} catch (IOException e) {
		// 		// TODO Auto-generated catch block
		// 		e.printStackTrace();
		// 	} catch (JSONException je) {
		// 		je.printStackTrace();
		// 	}
		// }


		Scanner scanner = new Scanner(System.in);
		ServerSocket receiveSocket = null;
		try {
			receiveSocket = new ServerSocket(client.recvPort);
		} catch (IOException e) {
			System.err.println("Error opening receiving socket");
			System.exit(1);
		}
		Thread acceptThread = new Thread(new ProcessIncomingRequest(receiveSocket));
		acceptThread.start();

		client.register();
		System.out.println();
		

		// Main program loop
		while (true) {
			System.out.println("\n=========\n0: friends\n1: talk\n2: broadcast\n3: busy\n4: available\n5: exit\n");
			int userChoice = 6;
			try {
				userChoice = scanner.nextInt();
			} catch (InputMismatchException e) {
				System.out.println("Invalid input");
				scanner.nextLine();
			}
			
			System.out.println();

			switch (userChoice) {
				case 0:
					System.out.println("Friends:");
					System.out.println("---------");
					ArrayList<JSONObject> userList = client.getUsers();
					if (userList != null) {
						for (JSONObject o : userList) {
							if (!o.getString("userName").equals(client.userName)) {
								System.out.print(o.getString("userName") + " @ " + o.getString("host") + ":" + o.getInt("port") + " - ");
								if (o.getBoolean("status")) {
									System.out.println("Available");
								} else {
									System.out.println("Busy");
								}
							}
						}
					}
					System.out.println();
					break;
				case 1:
					// System.out.println("Talk: to who?");
					// scanner.nextLine();
					// String targetUserName = scanner.nextLine();
					// RegistrationInfo targetRI = service.lookup(targetUserName);

					// if (targetRI == null) {
					// 	System.out.println("User does not exist");
					// } else if (targetRI.getStatus()) {
					// 	String message = scanner.nextLine();

					// 	Socket socket = new Socket(targetRI.getHost(), targetRI.getPort());
					// 	new PrintStream(socket.getOutputStream()).println(message);
					// 	socket.close();
					// } else if (!targetRI.getStatus()) {
					// 	System.out.println("User busy");
					// }
					break;
				case 2:
					// System.out.println("Broadcast: message?");
					// scanner.nextLine();
					// String message = scanner.nextLine();

					// Vector<RegistrationInfo> targetRIs = service.listRegisteredUsers();
					// for (RegistrationInfo RI : targetRIs) {
					// 	if (RI.getStatus() && !RI.getUserName().equals(userName)) {
					// 		Socket socket = new Socket(RI.getHost(), RI.getPort());
					// 		new PrintStream(socket.getOutputStream()).println(message);
					// 		socket.close();
					// 	}
					// }
					break;
				case 3:
					// System.out.println("Setting status as busy");
					// service.updateRegistrationInfo(new RegistrationInfo(userName, host, port, false));
					break;
				case 4:
					// System.out.println("Setting status as available");
					// service.updateRegistrationInfo(new RegistrationInfo(userName, host, port, true));
					break;
				case 5:
					client.unregister();
					try {
						receiveSocket.close();
					} catch (IOException e) {
						System.err.println("Error closing receive socket");
					}
					scanner.close();
					System.exit(0);
				default:
					System.out.println("Invalid input");
					break;
			}
		}
	}
	
	void register() {
		Form form = new Form();
		form.add("userName", userName);
		form.add("host", host);
		form.add("port", ""+port);
		form.add("status", ""+status);

		Request request = new Request(Method.POST, usersResourceURL);
		request.setEntity(form.getWebRepresentation());
		Response resp = new Client(Protocol.HTTP).handle(request);
		System.out.println(resp.getStatus());
	}

	void unregister() {
		Request request = new Request(Method.DELETE,usersResourceURL + "/" + userName);
		Response resp = new Client(Protocol.HTTP).handle(request);
		System.out.println(resp.getStatus());
	}

	ArrayList<JSONObject> getUsers() {
		Request request = new Request(Method.GET, usersResourceURL);
		request.getClientInfo().getAcceptedMediaTypes().add(new Preference(MediaType.APPLICATION_JSON));
		Response resp = new Client(Protocol.HTTP).handle(request);
		JSONArray ja = new JSONArray(resp.getEntityAsText());

		System.out.println(resp.getStatus());
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		if (ja != null) {
			for (int i = 0; i < ja.length(); i++) {
				list.add(ja.getJSONObject(i));
			}
		}
		return list;
	}

	// User getUser() {

	// }
}
