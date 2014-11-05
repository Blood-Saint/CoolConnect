package com.example.dummywifi;

import java.util.Hashtable;
import java.util.Map;

import com.example.dummywifi.Messenger.ChatSession;
import com.example.dummywifi.Messenger.MessengerCommands.CommandExecutor;
import com.example.dummywifi.Messenger.MessengerCommands.JoinGroupCommandExecutor;
import com.example.dummywifi.Messenger.MessengerCommands.SetUsernameCommandExecutor;
import com.example.dummywifi.models.Client;
import com.example.dummywifi.util.Connection;
import com.example.dummywifi.models.ChatMessage;

import android.os.AsyncTask;
import android.os.Message;
import android.util.Log;

/**
 * This is a worker thread, each client their own worker on the server
 * The worker listens to the clients for messages, then will either execute the command
 * (like if the client sent "!connectedusers", the worker will send back a list of all users
 * in the room, otherwise if it is not a command, the server will add the message to the message
 * queue for distribution during the next update tick
 *
 */


public class GroupOwnerWorkerAsyncTask implements Runnable {
		
	private Client client;
	private ChatSession session;
	
	public static Hashtable<String, CommandExecutor> commandMap;
	
	static {
		commandMap = new Hashtable<String, CommandExecutor> ();
		// add commands to the command map as you implement them
		commandMap.put(SetUsernameCommandExecutor.COMMAND_MESSAGE, new SetUsernameCommandExecutor());
		commandMap.put(JoinGroupCommandExecutor.COMMAND_MESSAGE, new JoinGroupCommandExecutor());
	}
	
	public GroupOwnerWorkerAsyncTask(Client client, ChatSession session){ 
		this.client = client;
		this.session = session;
	}
	
	private void runCommand(String command, String[] args) {
		if (commandMap != null) {
			CommandExecutor exec = commandMap.get(command);
			if (exec != null) {
				exec.executeCommand(session, client, command, args);
			}	
		}
	}

	@Override
	public void run() {		
		Log.d("netcode", "worker listening to client messages");
		Connection connection = client.getConnection();
		
		ChatMessage readString = null;
		int lastToken = 0;
		StringBuffer messages = new StringBuffer();
		
		while (connection.isOpen()) {            	
        	//Log.d("message", "trying to receive a message");
			// dispatch messages in message queue, then check for more messages
			//Log.d("gowat", "Loop Iteration");
			// check for more messages
			int result = session.fetchMessages(lastToken, messages);
			//Log.d("gowat", "fetchMessages returned!");
			
			if (result != lastToken) { // there are new messages, send them to the client				
				//Log.d("gowat", "new messages! requested with token: " + lastToken + " and received a new token: " + result);
				lastToken = result;
				// star messages by this client, so it knows what side to display them on
				String userNameStarred = messages.toString().replace(client.getUserName(), "*" + client.getUserName());
				connection.sendNamedText(userNameStarred);
				messages = new StringBuffer();
			} else {
				//Log.d("gowat", "no new messages. token: " + lastToken);
			}
        	
            if ((readString = connection.receiveString()) != null) {
            	//Log.d("message", "message received");
            	
            	//Log.d("message", "received message: " + readString);
            	
            	if (readString.getType() == ChatMessage.Types.COMMAND) {
            		// it's a command
            		String[] args = readString.getText().split("\\s+");
            		runCommand(args[0], args);
            	} else {
            		// it's a message
            		// put it in the message queue
            		session.queueMessage(client.getUserName() + ": " +  readString.getText());
            		Log.d("message", "put '" + readString.getText() + "' into the message queue");            		
            	}
            }
            try {
				Thread.sleep(ChatSession.dispatchDelay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//Log.e("gowat", "exception");
				e.printStackTrace();
			}
        }
		Log.d("netcode", "connection with client has been closed");
		
		return;
	}
	

}
