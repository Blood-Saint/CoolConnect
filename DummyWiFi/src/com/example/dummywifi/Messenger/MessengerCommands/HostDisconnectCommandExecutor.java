package com.example.dummywifi.Messenger.MessengerCommands;

import com.example.dummywifi.Messenger.ChatSession;
import com.example.dummywifi.models.Client;

/**
 * Created by Blood Saint on 11/28/2014.
 */
public class HostDisconnectCommandExecutor implements CommandExecutor {

    public static final String COMMAND_MESSAGE = "disconnect";

    @Override
    public boolean executeCommand(ChatSession session, Client caller, String command, String[] args) {
        if (session.getIsBackup())
        {
            //become host
        }
        else
        {
            //look for new host
        }
        return true;
    }

}