package de.jade.ecs.map;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.CharBuffer;

public class Snippet {

	public static void main(String[] args) {

		String temp;
		String displayBytes;
		try {
			// create input stream
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			// create client socket, connect to server
			Socket clientSocket = new Socket("139.13.30.142", 22240);
			// create output stream attached to socket
			 DataOutputStream outToServer = new
			 DataOutputStream(clientSocket.getOutputStream());
            System.out.print("Command : ");

			// create input stream attached to socket
//			DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());
			
			InputStream input = clientSocket.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(input),1024);
//			byte[] array = new byte[1024];
//			CharBuffer array3 = new CharBuffe;
//			String line = reader.read(array3 ) ;   // reads a line of text
			
			temp = reader.readLine();

			System.out.println(temp);
			// send line to server
			// outToServer.writeBytes(temp);
			// outToServer.writeUTF(temp);
			// outToServer.flush();

			// read line from server
//			displayBytes = inFromServer.readLine();
//
//			while ((displayBytes = inFromServer.readUTF()) != null) {
//				System.out.print(displayBytes);
//			}
			// clientSocket.close();
		} catch (Exception ex) {

		}
	}
}
