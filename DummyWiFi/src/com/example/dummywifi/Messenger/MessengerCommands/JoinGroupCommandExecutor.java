package com.example.dummywifi.Messenger.MessengerCommands;

import com.example.dummywifi.Messenger.ChatSession;
import com.example.dummywifi.models.Client;

public class JoinGroupCommandExecutor implements CommandExecutor {

	public static final String COMMAND_MESSAGE = "joingroup";
	
	@Override
	public boolean executeCommand(ChatSession session, Client caller, String command, String[] args) {
		session.clientJoin(caller);
		
		return true;
	}

}
