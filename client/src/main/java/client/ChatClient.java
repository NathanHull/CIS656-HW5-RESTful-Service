package client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.Socket;
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


		// Get args for username and ports
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


		// Client closeables
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

		
		// Stub in case user gets left registered
		// Request request = new Request(Method.DELETE,usersResourceURL + "/brah");
		// Response resp = new Client(Protocol.HTTP).handle(request);
		// System.out.println(resp.getStatus());


		// Server registration
		if (!client.register()) {
			System.out.println("Username already taken");
			System.exit(0);
		}

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
					System.out.println("Talk: to who?");
					scanner.nextLine();
					String targetUserName = scanner.nextLine();
					JSONObject target = client.getUser(targetUserName);

					try {
						target.getString("userName");
					} catch (JSONException e) {
						target = null;
					}

					if (target == null) {
						System.out.println("User does not exist");
					} else if (target.getBoolean("status")) {
						String message = scanner.nextLine();
						Socket socket = null;

						try {
							socket = new Socket (target.getString("host"), target.getInt("port"));
						} catch (UnknownHostException e) {
							System.out.println("Host not found");
							break;
						} catch (IOException e) {
							System.err.println("IO Exception on socket");
							break;
						}

						try {
							new PrintStream(socket.getOutputStream()).println(message);
						} catch (IOException e) {
							System.err.println("IO Error");
						}

						try {
							socket.close();
						} catch (IOException e) {
							System.err.println("Error closing send socket");
						}
					} else if (!target.getBoolean("status")) {
						System.out.println("User busy");
					}
					break;

				case 2:
					System.out.println("Broadcast message?");
					scanner.nextLine();
					String message = scanner.nextLine();

					ArrayList<JSONObject> targets = client.getUsers();
					for (JSONObject t : targets) {
						if (t.getBoolean("status") && !t.getString("userName").equals(client.userName)) {
							Socket socket = null;
							try {
								socket = new Socket(t.getString("host"), t.getInt("port"));
							} catch (UnknownHostException e) {
								System.out.println("Unknown host");
								continue;
							} catch (IOException e) {
								System.err.println("Socket IO exception");
								break;
							}

							try {
								new PrintStream(socket.getOutputStream()).println(message);
							} catch (IOException e) {
								System.err.println("PrintStream error");
							}
							
							try {
								socket.close();
							} catch (IOException e) {
								System.err.println("Error closing socket");
							}
						}
					}
					break;
					
				case 3:
					System.out.println("Setting status as busy");
					client.updateUser(client.userName, client.host, client.port, false);
					break;

				case 4:
				System.out.println("Setting status as available");
				client.updateUser(client.userName, client.host, client.port, true);
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
	
	boolean register() {
		Form form = new Form();
		form.add("userName", userName);
		form.add("host", host);
		form.add("port", ""+port);
		form.add("status", ""+status);

		Request request = new Request(Method.POST, usersResourceURL);
		request.setEntity(form.getWebRepresentation());
		Response resp = new Client(Protocol.HTTP).handle(request);
		System.out.println("\n" + resp.getStatus());

		return resp.getStatus().getCode() == 200;
	}

	void unregister() {
		Request request = new Request(Method.DELETE,usersResourceURL + "/" + userName);
		Response resp = new Client(Protocol.HTTP).handle(request);
		System.out.println("\n" + resp.getStatus());
	}

	ArrayList<JSONObject> getUsers() {
		Request request = new Request(Method.GET, usersResourceURL);
		request.getClientInfo().getAcceptedMediaTypes().add(new Preference(MediaType.APPLICATION_JSON));
		Response resp = new Client(Protocol.HTTP).handle(request);
		JSONArray ja = new JSONArray(resp.getEntityAsText());

		System.out.println("\n" + resp.getStatus());
		ArrayList<JSONObject> list = new ArrayList<JSONObject>();
		if (ja != null) {
			for (int i = 0; i < ja.length(); i++) {
				list.add(ja.getJSONObject(i));
			}
		}
		return list;
	}

	JSONObject getUser(String target) {
		Request request = new Request(Method.GET, usersResourceURL + "/" + target);
		request.getClientInfo().getAcceptedMediaTypes().add(new Preference(MediaType.APPLICATION_JSON));
		Response resp = new Client(Protocol.HTTP).handle(request);
		System.out.println("\n" + resp.getStatus());
		return new JSONObject(resp.getEntityAsText());
	}

	void updateUser(String userName, String host, int port, boolean status) {
		Form form = new Form();
		form.add("userName", userName);
		form.add("host", host);
		form.add("port", ""+port);
		form.add("status", ""+status);

		Request request = new Request(Method.PUT, usersResourceURL + "/" + userName);
		request.setEntity(form.getWebRepresentation());
		Response resp = new Client(Protocol.HTTP).handle(request);
		System.out.println("\n" + resp.getStatus());
	}
}
