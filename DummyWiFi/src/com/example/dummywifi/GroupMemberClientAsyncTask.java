package com.example.dummywifi;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;

import com.example.dummywifi.Messenger.ChatSession;
import com.example.dummywifi.util.Connection;
import com.example.dummywifi.models.ChatMessage;
import com.example.dummywifi.Messenger.MessengerCommands.CommandExecutor;
import com.example.dummywifi.Messenger.MessengerCommands.JoinGroupCommandExecutor;
import com.example.dummywifi.Messenger.MessengerCommands.SetUsernameCommandExecutor;
import com.example.dummywifi.models.Client;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

/**
 * This is the Member thread. This is used to connect to the server and then to other
 * members. This is where all communication takes place after connection has
 * been established.
 *
 * It will either execute commands or add messages to the que to be printed.
 */

public class GroupMemberClientAsyncTask implements Runnable {

    private Client client;
    private ChatSession session;

    public static Hashtable<String, CommandExecutor> commandMap;

    static {
        commandMap = new Hashtable<String, CommandExecutor> ();
        // add commands to the command map as you implement them
        commandMap.put(SetUsernameCommandExecutor.COMMAND_MESSAGE, new SetUsernameCommandExecutor());
        commandMap.put(JoinGroupCommandExecutor.COMMAND_MESSAGE, new JoinGroupCommandExecutor());
    }

	private SocketAddress groupOwnerAddress;
	public static int GMCAT_JOIN_MESSAGE = 100;
	public static int GMCAT_NEW_MESSAGE = 101;
	
	private Activity mainActivity, chatActivity;
	private List<String> messagesToSend;
	private Connection connection;

	public GroupMemberClientAsyncTask(Activity mainActivity, SocketAddress groupOwnerAddress) {
		this.groupOwnerAddress = groupOwnerAddress;
		this.mainActivity = mainActivity;
		this.messagesToSend = new ArrayList<String>();
	}

    public GroupMemberClientAsyncTask(Client client, ChatSession session)
    {
        this.client = client;
        this.session = session;
    }

    public void closeClient() {
        if (connection != null) {
            connection.close();
        }
    }

	public synchronized void queueMessageToSend(String message) { // called from chatactivity when you push send
		messagesToSend.add(message);
	}
	
	@Override
	public void run() {
		Socket socket = new Socket();
		//Connection connection = null;
		connection = null;
		try {
			socket.bind(null);
			socket.connect(groupOwnerAddress, 3000); //this is timeout
			connection = new Connection(socket);
				
			connection.sendCommand("joingroup");
			Message msg = new Message();
			msg.what = GMCAT_JOIN_MESSAGE;
			((MainActivity)mainActivity).handler.sendMessage(msg);
			Thread.sleep(500);
			//socket.getOutputStream().write("!joingroup".getBytes());
			
			// for testing messages
			while (ChatActivity.currentChatActivity == null) {
				Thread.sleep(10);
			}
			((ChatActivity)ChatActivity.currentChatActivity).gmcat = this;
			
			while (connection.isOpen()) {
				if (messagesToSend.size() > 0) {
					for (String message : messagesToSend) {
						connection.sendText(message);
					}
					messagesToSend.clear();
				}
				//connection.sendText("hello");
				//Thread.sleep(750);
				
				ChatMessage newMessages = null;
				if ((newMessages = connection.receiveString()) != null) {
            		Log.d("message", "Client received message: " + newMessages.getText());
            		String[] messages = newMessages.getText().split(ChatSession.messageDelim);
            		Log.d("message", "actual message count: " + messages.length);
            		for (String message : messages) {
						// This block will move to the gmcat. Right now the gmcat is being used for testing sending "hello" over and over
	            		// but that work will be moved to another thread spawned when the send button is pushed            		
	            		Message newChatMessage = new Message();
	            		newChatMessage.what = GroupMemberClientAsyncTask.GMCAT_NEW_MESSAGE;
	            		newChatMessage.obj = message;
	            		
	            		((ChatActivity)ChatActivity.currentChatActivity).handler.sendMessage(newChatMessage);
            		}
            		// -- end block 
				}
			}
			
			//socket.getOutputStream().flush();
			
			
		} catch (IOException Ex){
			Ex.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (connection != null) 
			connection.close();
		
		return;
	}
	
	

}
