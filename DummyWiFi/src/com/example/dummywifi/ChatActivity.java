package com.example.dummywifi;


import java.util.ArrayList;

import android.net.wifi.WifiInfo;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.inputmethod.EditorInfo;

import com.example.dummywifi.XUtil;
import com.example.dummywifi.ConnectionInfo;
import com.example.dummywifi.UI.DiscussArrayAdapter;
import com.example.dummywifi.UI.TextBubble;
import com.example.dummywifi.models.ChatMessage;

public class ChatActivity extends Activity {
	public Handler handler;
	
	String username = "username";
	
	public  ListView listView;
	private ArrayAdapter arrayAdapter;
	public static ArrayList<TextBubble> listItems=new ArrayList<TextBubble>();

	//Declare the WifiP2pManager
	public WifiP2pManager mManager;
	
	//Declare the Channel
	Channel mChannel;
	
	public static Activity currentChatActivity = null;
	public GroupMemberClientAsyncTask gmcat;
    //public GroupOwnerServerAsyncTask gosat;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		final EditText editText = (EditText) findViewById(R.id.send);
		
        //This creates an instance of a WiFiP2pManager object
        //We are going to use it to make a WiFiP2pManager.Channel object
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        //This makes the WifiP2pManager.CHannel object
        mChannel = mManager.initialize(this,getMainLooper(), null);
      
        arrayAdapter = new DiscussArrayAdapter(this.getApplicationContext(), android.R.layout.simple_list_item_1);
        ((DiscussArrayAdapter)arrayAdapter).setTextBubbleList(listItems);
        /*ArrayAdapter<String>(this, 
		        android.R.layout.simple_list_item_1, listItems);*/
        
        listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(arrayAdapter);
        
        editText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                	/*listItems.add("anon: "+editText.getText().toString());
                	arrayAdapter.notifyDataSetChanged();
                	listView.setSelection((arrayAdapter.getCount()) - 1);
                	editText.setText("");*/
                	if (gmcat != null) {
                		gmcat.queueMessageToSend(new ChatMessage(editText.getText().toString(), ChatMessage.Types.INITIAL));
                		editText.setText("");
                	}
                	
                	//EditText et_history = (EditText) findViewById(R.id.list);
            		//String history = et_history.getText().toString();
            		//String message = editText.getText().toString();
            		//if (message == "") return true;
            		//editText.setText ("");
            		//et_history.setText (history + username + ": " + message + "\n");
                    handled = true;
                }
                return handled;
            }
        });
        
        handler = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		Log.d("chatview", "received new message for display: " + (String)msg.obj);
        		if (msg.what == GroupMemberClientAsyncTask.GMCAT_NEW_MESSAGE){
        			
        			String newMessage = (String)msg.obj;
        			boolean left = !newMessage.startsWith("*");
        			if (!left) {
        				newMessage = newMessage.substring(1);
        			}
        			TextBubble bubble = new TextBubble(left, newMessage);
        			
        			listItems.add(bubble);
                	arrayAdapter.notifyDataSetChanged();
                	listView.setSelection((arrayAdapter.getCount()) - 1);
        			//EditText et_history = (EditText) findViewById(R.id.messages);
            		//String history = et_history.getText().toString();
            		
            		
            		
            		//et_history.setText (history + username + ": " + newMessage + "\n");
        			
        		}
        	}
        };
        ChatActivity.currentChatActivity = this;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public void onBackPressed() {
		Log.i("back","The back button was pressed");
		mManager.removeGroup(mChannel,new ActionListener() {
			
			@Override
            public void onSuccess() {
                Log.i("back","Disconnect Successful");
                gmcat.closeClient();

            }


            @Override
            public void onFailure(int reasonCode) {
            	Log.i("back","Disconnect Failed. Reason:" + reasonCode);
            }
		});
		super.onBackPressed();
	return;
	}


}
