package com.example.dummywifi;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Hashtable;
import java.util.Random;

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

    private void runCommand(String command, String[] args) {
        if (commandMap != null) {
            CommandExecutor exec = commandMap.get(command);
            if (exec != null) {
                exec.executeCommand(session, client, command, args);
            }
        }
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
        ChatMessage readString = null;
        int lastToken = 0;
        StringBuffer messages = new StringBuffer();

		try {
            if (client == null) {
                Socket socket = new Socket();
                //Connection connection = null;
                connection = null;
                // above line originally out of try
                socket.bind(null);
                socket.connect(groupOwnerAddress, 3000); //this is timeout
                connection = new Connection(socket);

                connection.sendCommand("joingroup");
                Message msg = new Message();
                msg.what = GMCAT_JOIN_MESSAGE;
                ((MainActivity) mainActivity).handler.sendMessage(msg);
                Client client = new Client(connection, session.getNextId());
                client.setUserName("user" + new Random().nextInt(100));

                Thread.sleep(500);
                //socket.getOutputStream().write("!joingroup".getBytes());

                // for testing messages
                while (ChatActivity.currentChatActivity == null) {
                    Thread.sleep(10);
                }
                ((ChatActivity) ChatActivity.currentChatActivity).gmcat = this;
            }
            else
            {
                connection = client.getConnection();


            }
			while (connection.isOpen()) {
                int result = session.fetchMessages(lastToken, messages);

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
            		String[] recievedMessages = newMessages.getText().split(ChatSession.messageDelim);
            		Log.d("message", "actual message count: " + recievedMessages.length);
            		for (String message : recievedMessages) {
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
