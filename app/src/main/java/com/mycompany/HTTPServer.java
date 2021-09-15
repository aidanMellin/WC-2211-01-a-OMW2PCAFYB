package com.mycompany;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class HTTPServer extends Thread {

    private Socket client = null;
    private BufferedReader inClient = null;
    private DataOutputStream outClient = null;

    public HTTPServer(Socket cl) {
	client = cl;
    }

    public void run() {
	try {
	    System.out.println("The Client " + client.getInetAddress() + ":" + client.getPort() + " is connected");

	    inClient = new BufferedReader(new InputStreamReader(client.getInputStream()));
	    outClient = new DataOutputStream(client.getOutputStream());

	    String requestString = inClient.readLine();
	    String headerLine = requestString;

	    /** sometimes headerLine is null */
	    if (headerLine == null)
		return;

	    StringTokenizer tokenizer = new StringTokenizer(headerLine);
	    String httpMethod = tokenizer.nextToken();
	    String httpQueryString = tokenizer.nextToken();

	    System.out.println("The HTTP request string is ....");
	    while (inClient.ready()) {
		/** Read the HTTP request until the end */
		System.out.println(requestString);
		requestString = inClient.readLine();
	    }

	    if (httpMethod.equals("GET")) {
		if (httpQueryString.equals("/")) {
		    /** return the home page */
		    homePage();
		} else if (httpQueryString.startsWith("/hello")) {
		    /** return the hello page */
		    helloPage(
			    httpQueryString.substring(httpQueryString.lastIndexOf('/') + 1, httpQueryString.length()));
		} else {
		    sendResponse(404, "<b>The Requested resource not found.</b>");
		}
	    } else {
		sendResponse(404, "<b>The Requested resource not found.</b>");
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    /**
     * Method used to compose the response back to the client.
     * 
     * @param statusCode
     * @param responseString
     * @throws Exception
     */
    public void sendResponse(int statusCode, String responseString) throws Exception {

	String HTML_START = "<html><title>HTTP Server in Java</title><body>";
	String HTML_END = "</body></html>";
	String NEW_LINE = "\r\n";

	String statusLine = null;
	String serverdetails = Headers.SERVER + ": Java Server";
	String contentLengthLine = null;
	String contentTypeLine = Headers.CONTENT_TYPE + ": text/html" + NEW_LINE;

	if (statusCode == 200)
	    statusLine = Status.HTTP_200;
	else
	    statusLine = Status.HTTP_404;

	statusLine += NEW_LINE;
	responseString = HTML_START + responseString + HTML_END;
	contentLengthLine = Headers.CONTENT_LENGTH + responseString.length() + NEW_LINE;

	outClient.writeBytes(statusLine);
	outClient.writeBytes(serverdetails);
	outClient.writeBytes(contentTypeLine);
	outClient.writeBytes(contentLengthLine);
	outClient.writeBytes(Headers.CONNECTION + ": close" + NEW_LINE);

	/** adding the new line between header and body */
	outClient.writeBytes(NEW_LINE);

	outClient.writeBytes(responseString);

	outClient.close();
    }

    /**
     * Method used to compose the home page response to the client
     * 
     * @throws Exception
     */
    public void homePage() throws Exception {
	StringBuffer responseBuffer = new StringBuffer();
	responseBuffer.append("<b>HTTPServer Home Page.</b><BR><BR>");
	responseBuffer.append("<b>To see the Hello page use : http://localhost:5000/hello/name</b><BR>");
	sendResponse(200, responseBuffer.toString());
    }

    /**
     * Method used to compose the hello page response to the client
     * 
     * @throws Exception
     */
    public void helloPage(String name) throws Exception {
	StringBuffer responseBuffer = new StringBuffer();
	responseBuffer.append("<b>Hello:").append(name).append("</b><BR>");
	sendResponse(200, responseBuffer.toString());
    }

    /**
     * class used to store headers constants
     *
     */
    private static class Headers {
	public static final String SERVER = "Server";
	public static final String CONNECTION = "Connection";
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String CONTENT_TYPE = "Content-Type";
    }

    /**
     * class used to store status line constants
     *
     */
    private static class Status {
	public static final String HTTP_200 = "HTTP/1.1 200 OK";
	public static final String HTTP_404 = "HTTP/1.1 404 Not Found";
    }

    public static void main(String args[]) throws Exception {

	ServerSocket server = new ServerSocket(5000, 10, InetAddress.getByName("127.0.0.1"));

	System.out.println("HTTPServer started on port 5000");

	/**
	 * loop to keep the application alive - a new HTTPServer object is
	 * created for each client
	 */
	while (true) {
	    Socket connected = server.accept();
	    HTTPServer httpServer = new HTTPServer(connected);
	    httpServer.start();
	}
    }
}