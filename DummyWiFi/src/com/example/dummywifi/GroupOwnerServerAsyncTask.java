package com.example.dummywifi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

import com.example.dummywifi.Messenger.ChatSession;
import com.example.dummywifi.models.Client;
import com.example.dummywifi.util.Connection;

//import com.example.android.wifidirect.WiFiDirectActivity;

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
    /**
     * @param context
     * @param statusText
     */
    public GroupOwnerServerAsyncTask(/*Context context, View statusText*/) {
        /*this.context = context;
        this.statusText = (TextView) statusText;*/
    	session = new ChatSession();
    	session.queueMessage("Welcome to Spot!");
    	session.queueMessage("Start typing below to send a message.");

    }

    public void closeServer() {
        try {
            serverSocket.close();
        } catch(IOException e) {
            Log.e("netcode", e.getMessage());
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
	           
	            GroupOwnerWorkerAsyncTask gowat = new GroupOwnerWorkerAsyncTask(client, session);
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
