package com.example.dummywifi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.example.dummywifi.Messenger.ChatSession;
import com.example.dummywifi.models.Client;
import com.example.dummywifi.util.Connection;

//import com.example.android.wifidirect.WiFiDirectActivity;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class GroupOwnerServerAsyncTask implements Runnable {

	//private Context context;
    //private TextView statusText;
	ChatSession session; // the session that this task is serving
    private ServerSocket serverSocket;
    private Activity mainActivity;

    List<GroupMemberClientAsyncTask> memberList;

    /**
     * @param //context
     * @param //statusText
     */
    public GroupOwnerServerAsyncTask(Activity mainActivity) {
        /*this.context = context;
        this.statusText = (TextView) statusText;*/
        this.mainActivity = mainActivity;
    	session = new ChatSession();
    	session.queueMessage("Welcome to Spot!");
    	session.queueMessage("Start typing below to send a message.");
        memberList = new ArrayList<GroupMemberClientAsyncTask>();

    }

    public void closeServer() {
        try {
            serverSocket.close();
        } catch(IOException e) {
            Log.e("netcode", e.getMessage());
        }

    }

    public void connectTo(SocketAddress targetAddress)
    {
        Socket socket = new Socket();
        //Connection connection = null;
        Connection connection = null;
        // above line originally out of try
        try {
            socket.bind(null);
            socket.connect(targetAddress, 3000); //this is timeout
            connection = new Connection(socket);
            Client client = new Client(connection, session.getNextId());
            client.setUserName("user" + new Random().nextInt(100));
            GroupMemberClientAsyncTask target = new GroupMemberClientAsyncTask(mainActivity, client, session);
            memberList.add(target);
            Thread serverClientThread = new Thread(target);
            serverClientThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
    	//ServerSocket serverSocket;
    	
        try {
            serverSocket = new ServerSocket(8888); //8888
            Log.d("netcode", "Server: Socket opened");
            
            while (!serverSocket.isClosed()) { // shouldn't happen unless maybe the wifi gets turned off
            	// keep waiting for clients to come, then accept them and make a worker for them

	            Socket clientSocket = serverSocket.accept();
	            
	            Log.d("netcode", "Server: a connection with a client has been established");
	            
	            Connection connection = new Connection(clientSocket);	                 
	            Client client = new Client(connection, session.getNextId());
	            client.setUserName("user" + new Random().nextInt(100));
	           
	            GroupMemberClientAsyncTask gowat = new GroupMemberClientAsyncTask(client, session);
                memberList.add(gowat);
	            Log.d("netcode", "Worker created, running it");
	            
	            Thread workerThread = new Thread(gowat);
	            workerThread.start();
	            
	            Log.d("netcode", "Worker thread started, status is: " + workerThread.getState());
	                                   
            }
            
            serverSocket.close();
            return; 
        } catch (IOException e) {
            Log.e("netcode", e.getMessage());
            
            return;
        }
    }	

}
