package com.example.dummywifi.Messenger;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.example.dummywifi.ChatActivity;
import com.example.dummywifi.models.Client;
import com.example.dummywifi.util.Connection;

/**
 * This class contains all of the information about a chat session
 * Create one of these when you establish a connection
 * Like who is in it and what is the message queue
 * Maybe it could contain the chat history if we wanted that
 */

public class ChatSession {

	private List<Client> connectedClients;	
	private List<String> messageQueue;
	
	private int id_counter;
	
	
	public static final int dispatchDelay = 750; // 750ms
	public static final String messageDelim = "_&&_";
	
	public synchronized void queueMessage(String message) { // this is safe to call from any thread		
		messageQueue.add(message);

	}
	
	public synchronized void clearMessages() {
		// clear messageQueue
		messageQueue.clear();
		
	}
	//get rid of delimiter and queuebuffer and return an array of strings
	public int fetchMessages(int lastToken, StringBuffer queueBuffer) {
		if (lastToken < messageQueue.size()) { // retrieve messages until you have the current message
			int i, byteCount = 0;
			
			for (i = lastToken; i < messageQueue.size(); i++) {
				String message = messageQueue.get(i) + messageDelim;
				queueBuffer.append(message);
			}
			
			return i;
		}

		return lastToken;
	}
	
	public List<Client> getConnectedClients() {
		return connectedClients;
	}
		
	// Call this from the worker when you get the !joingroup message
	public void clientJoin(Client c) {
		Log.i("session", "client joined, id = " + c.getId());
		connectedClients.add(c);
	}
	
	// Call this from the worker when the connection closes
	public void clientLeave(Client c) {
		Log.i("session", "client left, id = " + c.getId());
		connectedClients.remove(c);
	}
	
	public int getNextId() {
		id_counter++;
		return id_counter;
	}
	
	public ChatSession(){
		id_counter = 1000;
		connectedClients = new ArrayList<Client>();
		messageQueue = new ArrayList<String>();
	}
	
}
