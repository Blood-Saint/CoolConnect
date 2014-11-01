package com.example.dummywifi.models;

import java.io.Serializable;

/**
 * Created by alex on 10/26/14.
 */
public class ChatMessage implements Serializable{
    private static final long serialVersionUID = -5967900946938355970L;
    public enum Types {MESSAGE,ACK,COMMAND};

    private final String text;
    private final Types type;


    public ChatMessage(String text) {
        this.text = text;
        type = Types.MESSAGE;
    }
    
    public ChatMessage(String text, Types type)
    {
    	this.text = text;
    	this.type = type;
    }
    

    public String getText() {
        return text;
    }
    
    public Types getType()
    {
    	return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage)) return false;

        ChatMessage message = (ChatMessage) o;

        if (!text.equals(message.text)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }
}
