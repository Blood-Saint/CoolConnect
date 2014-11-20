package com.example.dummywifi.models;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Created by alex on 10/26/14.
 */
public class ChatMessage implements Serializable{
    private static final long serialVersionUID = -5967900946938355970L;
    public enum Types {MESSAGE,ACK,COMMAND,INITIAL};

    private String text;
    private Types type;
    private final UUID id;
    private final long timestamp;


    public ChatMessage(String text) {
        this.id = UUID.randomUUID();
        this.timestamp = System.currentTimeMillis();
        this.text = text;
        type = Types.MESSAGE;
    }
    
    public ChatMessage(String text, Types type)
    {
    	this.id = UUID.randomUUID();
        this.timestamp = System.currentTimeMillis();
        this.text = text;
    	this.type = type;
    }
    

    public String getText() {
        return text;
    }

    public void setText(String newText) { text = newText; }
    
    public Types getType()
    {
    	return type;
    }

    public void setType(Types nType) { type = nType; }

    public UUID getId() { return id; }

    public long getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatMessage)) return false;

        ChatMessage message = (ChatMessage) o;

        if (!id.equals(message.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "text='" + text + '\'' +
                ", type=" + type +
                ", id=" + id +
                ", timestamp=" + timestamp +
                '}';
    }
}
