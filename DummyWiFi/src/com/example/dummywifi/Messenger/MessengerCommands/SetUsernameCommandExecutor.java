package com.example.dummywifi.Messenger.MessengerCommands;

import com.example.dummywifi.Messenger.ChatSession;
import com.example.dummywifi.models.Client;

public class SetUsernameCommandExecutor implements CommandExecutor {

	public static final String COMMAND_MESSAGE = "setusername";
	
	public boolean isValidUserName(String userName, ChatSession session) {
		for (Client c: session.getConnectedClients()){ 
			if (c.getUserName().equalsIgnoreCase(userName)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean executeCommand(ChatSession session, Client caller, String command, String[] args) {
		
		if (args.length == 2) { // !setusername <username>
			if (isValidUserName(args[1], session))
				caller.setUserName(args[1]);
		}
		else if (args.length == 3) { // !setusername <username> <deviceid>
			if (isValidUserName(args[1], session)){	
				caller.setUserName(args[1]);
				caller.setUserName(args[2]);
			}
		}
		else 
			return false;
		
		return true;
	}

}
