package com.example.dummywifi.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.UUID;

import android.util.Log;

import com.example.dummywifi.models.ChatMessage;

/*
 * Abstraction layer over socket
 * Useful for sending & receiving data with less code & no need to understand sockets
*/
public class Connection {
	public static final int MAX_READ_SIZE = 2048;
	
	private final Socket connectionSocket;
    private ObjectInputStream inputStream = null;
    private ObjectOutputStream outputStream = null;

    private final AckManager ackManager;

	public Connection(Socket source) {
		this.connectionSocket = source;

		try {
			source.setSoTimeout(750);
		} catch (SocketException e) {
			Log.e("connection", "failed to set timeout: " + e);
		}

        this.ackManager = new AckManager(this);
        Thread ackThread = new Thread(ackManager);
        ackThread.setUncaughtExceptionHandler(
                new Thread.UncaughtExceptionHandler() {
                    @Override
                    public void uncaughtException(Thread thread, Throwable throwable) {
                        Log.e("AckManager", "AckManager has crashed!",throwable);
                    }
                }
        );
        ackThread.start();
	}
	
	public boolean isOpen() {
		return (!connectionSocket.isClosed()) && connectionSocket.isConnected() && (!connectionSocket.isInputShutdown()) && (!connectionSocket.isOutputShutdown());
	}

    protected synchronized boolean sendMessage(ChatMessage message) {
        if (!this.isOpen()) return false;
        // initialize the stream on first use, not possible in constructor because the socket is not yet connected
        if (outputStream == null){
            try {
                outputStream = new ObjectOutputStream(connectionSocket.getOutputStream());
            } catch (IOException e) {
                Log.e("connection","failed to initialize outputStream" + e);
                return false;
            }
        }

        try {
            outputStream.writeObject(message);
            outputStream.flush();
        } catch (IOException e) {
            Log.w("connection", "IO error while writing message " + e.getMessage());
            return false;
        }
        return true;
    }

    private synchronized ChatMessage receiveMessage() {
        if (!this.isOpen()) return null;
        // initialize the stream on first use, not possible in constructor because the socket is not yet connected
        if (inputStream == null){
            try {
                inputStream = new ObjectInputStream(connectionSocket.getInputStream());
            } catch (IOException e) {
                Log.e("connection","failed to initialize inputStream" + e);
            }
        }

        try {
            Object result = inputStream.readObject();
            ChatMessage message =  (ChatMessage) result;

            return message;
        } catch (ClassCastException e) {
            Log.e("connection","received message of unexpected type " + e);
            return null;
        } catch (StreamCorruptedException e){
            Log.w("connection", "stream corrupted on receive message: " + e);
            return null;
        } catch (SocketTimeoutException e) {
            // this is totally expected, since we are just polling on the inStream all the time
            return null;
        }catch (IOException e) {
            Log.w("connection", "IO error when receiving message " + e);
            return null;
        } catch (ClassNotFoundException e) {
            Log.e("connection", "class not found when receiving message " + e);
            return null;
        }
    }

	// wrapper for sending text
	public boolean sendText(String text) {
        ChatMessage message = new ChatMessage(text);
        ackManager.messageSent(message);
        return sendMessage(message);
	}

    /**
     * This function is only called by the chat owner, it is a workaround for the fact
     * that we are "sending" all messages to ourself in order to write messages
     * to the UI for display with username.
     * @param text message text WITH USERNAME
     * @return
     */
    public boolean sendNamedText(String text) {
        ChatMessage message = new ChatMessage(text);
        // don't bother telling ackmgr about this, we aren't expecting any acks
        return sendMessage(message);
    }
    // wrapper for sending commands
    public boolean sendCommand(String text) {
        ChatMessage message = new ChatMessage(text, ChatMessage.Types.COMMAND);
        ackManager.messageSent(message);
        return sendMessage(message);
    }

    protected boolean sendAck(UUID ackId) { return sendMessage(new ChatMessage(ackId.toString(), ChatMessage.Types.ACK)); }
	
	public ChatMessage receiveString() {
        ChatMessage message = receiveMessage();
        if (message != null) {
        	switch (message.getType()) {
                case MESSAGE:
                    ackManager.messageReceived(message.getId());
                    return message;
                case ACK:
                    ackManager.ackReceived(UUID.fromString(message.getText()));
                    return receiveString(); // hack, since for now we are only checking for one message at a time
                case COMMAND:
                    ackManager.messageReceived(message.getId());
                    return message;
            }
        }
        return null;
	}
	
	public void close() {
		Log.i("connection","closing socket");
        try {
			outputStream.close();
            inputStream.close();
            connectionSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
