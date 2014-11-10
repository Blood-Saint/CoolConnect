package com.example.dummywifi.util;

import android.util.Log;

import com.example.dummywifi.models.ChatMessage;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by alex on 11/1/14.
 */
class AckManager implements Runnable{
    private static final long RETRY_TIMEOUT = 5 * 1000; // milliseconds

    private final Connection connection;

    private final ConcurrentHashMap<UUID, ChatMessage> nonAckedMessages;
    private final ConcurrentSkipListSet<UUID> retriedMessages;
    private final ConcurrentSkipListSet<UUID> receivedMessages;

    AckManager(Connection connection) {
        this.connection = connection;
        this.nonAckedMessages = new ConcurrentHashMap<UUID, ChatMessage>();
        this.retriedMessages = new ConcurrentSkipListSet<UUID>();
        this.receivedMessages = new ConcurrentSkipListSet<UUID>();
    }

    void ackReceived(UUID msgId) {
        ChatMessage retval = nonAckedMessages.remove(msgId);
        if (retval == null) {
            Log.w("AckManager", "Unexpected ACK for message ID " + msgId);
        } else {
            retriedMessages.remove(msgId);
        }
    }

    void messageReceived(UUID msgId) {
        receivedMessages.add(msgId);
    }

    void messageSent(ChatMessage message) {
        nonAckedMessages.put(message.getId(), message);
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(750);
            } catch (InterruptedException e) {
                // who freaking cares
            }
            /*
             * We need to do two things on each sweep, send out ACKs for received messages, and
             * check for messages that haven't been acked within the RETRY_TIMEOUT
             */

            // Ack messages that have been received
            for (UUID msgId : receivedMessages) {
                connection.sendAck(msgId);
                receivedMessages.remove(msgId);
            }

            // Check for messages that need to be retried
            long sweepTime = System.currentTimeMillis();
            for (ChatMessage message : nonAckedMessages.values()) {
                if (sweepTime - message.getTimestamp() > RETRY_TIMEOUT) {
                    if (retriedMessages.contains(message.getId())) {
                        // we have already retried this message, time to give up
                        Log.e("AckManager", "giving up on resending " + message.getId());
                        retriedMessages.remove(message.getId());
                        nonAckedMessages.remove(message.getId());
                    } else {
                        // we haven't tried this message again yet, let's do it now
                        Log.e("AckManager", "ACK timeout, resending " + message.getId());
                        retriedMessages.add(message.getId());
                        connection.sendMessage(message);
                    }
                }
            }
        }
    }
}
