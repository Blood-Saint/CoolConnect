package com.example.dummywifi.Messenger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import android.util.Log;

import com.example.dummywifi.ChatActivity;
import com.example.dummywifi.models.ChatMessage;
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
	private List<ChatMessage> messageQueue;
    private List<UUID> messageIDs;

	private int id_counter;

    private Boolean isBackup;


	public static final int dispatchDelay = 750; // 750ms
	public static final String messageDelim = "_&&_";

	public synchronized void queueMessage(ChatMessage message) { // this is safe to call from any thread
		messageQueue.add(message);
        messageIDs.add(message.getId());
	}

	public synchronized void clearMessages() {
		// clear messageQueue
		messageQueue.clear();

	}
    public void setIsBackup(Boolean value) { isBackup = value; }

    public Boolean getIsBackup() { return isBackup; }

	//get rid of delimiter and queuebuffer and return an array of strings
	public ChatMessage fetchMessages(int lastToken) {
		if (lastToken < messageQueue.size()) { // retrieve messages until you have the current message
			return messageQueue.get(lastToken);
		}
		return null;
	}

	public List<Client> getConnectedClients() {
		return connectedClients;
	}

	// Call this from the worker when you get the !joingroup message
	public void clientJoin(Client c) {
		Log.i("session", "client joined, id = " + c.getId());
		connectedClients.add(c);
	}

    public List<UUID> getMessageIDs() { return messageIDs; }

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
		messageQueue = new ArrayList<ChatMessage>();
        messageIDs = new ArrayList<UUID>();
        isBackup = false;
	}
	
}
