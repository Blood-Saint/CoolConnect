package com.example.dummywifi;




import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.content.BroadcastReceiver;
import android.text.Editable;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.lang.Math;



public class MainActivity extends Activity {
	
	public static String username = "Chad";
	
	public  ListView listView;
	//private ListView newView;
	private ArrayAdapter<String> arrayAdapter;
	private EditText editText1;
	public static ArrayList<String> listItems=new ArrayList<String>();
	public static ArrayList<WifiP2pConfig> configItems=new ArrayList<WifiP2pConfig>();
	
	public int clickCount = 0; 
	
	public static ProgressDialog progressDialog = null;
	private Button p2pbutt;
	private Button discoverbutt;	
	private Button createBtn;
	//We need an intent filter to catch only the intents we care about
	private final IntentFilter intentFilter = new IntentFilter();
	
	
	//We need a boolean to determine if the devices WiFiP2p is enabled
	private boolean isWifiP2pEnabled = false;
	
	//Declare the WifiP2pManager
	private WifiP2pManager mManager;
	
	//Declare the Channel
	private Channel mChannel;

	//We need to make a MyBroadCastReceiver to... receive broadcasts!?
	BroadcastReceiver mReceiver;
	
	//This handler will take care of receiving messages from GroupMemberClientAsyncTask
	//The "handling" will be starting the ChatActivity, this way we know sockets are
	//open before we start that ChatActivity.
	public Handler handler;
	
	//We need a setter function to set the value of the boolean isWifiP2pEnabled
	//Do this when the user's peer-to-peer wifi gets enabled from disabled or disabled from enabled.
	void setIsWifiP2pEnabled(boolean newVal){
		this.isWifiP2pEnabled = newVal;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        arrayAdapter = new ArrayAdapter<String>(this, 
		        android.R.layout.simple_list_item_1, listItems);
		
        listView = (ListView) findViewById(R.id.list);
		listView.setAdapter(arrayAdapter);
		
		//This is so the text box can be used. 
		editText1 = (EditText) findViewById(R.id.editText1);
		
		p2pbutt = (Button) findViewById(R.id.atn_direct_enable);
        discoverbutt = (Button) findViewById(R.id.atn_direct_discover);
        createBtn = (Button) findViewById(R.id.button1);
        
        /*We care about 4 intents:
        When the WiFi status changes, when the list of available peers changes,
        when the state of the WiFi connectivity has changed, and when the state
        of the device has changed */
        
        //The "WiFi has changed status" intent
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        
        //The "peers list has changed" intent
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        
        //The "state of the WiFi connectivity has changed" intent
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        
        //Indicates the device's details have changed
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        
        //This creates an instance of a WiFiP2pManager object
        //We are going to use it to make a WiFiP2pManager.Channel object
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        //This makes the WifiP2pManager.CHannel object
        mChannel = mManager.initialize(this,getMainLooper(), null);

        //Stops the keypad from automatically coming up on app start. 
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        
        p2pbutt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	if (mManager != null && mChannel != null) {

                    // Since this is the system wireless settings activity, it's
                    // not going to send us a result. We will be notified by
                    // WiFiDeviceBroadcastReceiver instead.
            		startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                } else {
                    Log.i("netcode", "channel or manager is null");
                }
            }
        });
        discoverbutt.setOnClickListener(new OnClickListener() {
        	// will use fillList later
            @Override
            public void onClick(View v) {
            	listItems.clear();
            	arrayAdapter.notifyDataSetChanged();
//            	configItems.clear();
            	
            	if (!isWifiP2pEnabled) {
                    Toast.makeText(MainActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                }
            	mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

    	    		@Override
    	    		public void onSuccess() {
    	    			for(int i = 0; i<configItems.size();i++){
    	    	      	  	listItems.add(configItems.get(i).deviceAddress);
    	    	        		arrayAdapter.notifyDataSetChanged();
    	    	        		Log.i("netcode","Device address is oooh:" + configItems.get(i).deviceAddress);
    	    	        }
    	    			Toast.makeText(MainActivity.this, "Discovery Initiated",
    	    					Toast.LENGTH_SHORT).show();
    	    		}

    	    		@Override
    	    		public void onFailure(int reasonCode) {
    	    			Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode,
    	    					Toast.LENGTH_SHORT).show();
    	    		}
    	        
    	    	});

            }
        });
        
        
        
        listView.setOnItemClickListener(new OnItemClickListener() {
        	
        	@Override
			public void onItemClick (AdapterView<?> parent, View view, int position, long id){
				WifiP2pConfig config = new WifiP2pConfig();
				config.deviceAddress = configItems.get(position).deviceAddress;
				config.wps.setup = WpsInfo.PBC;
				
				Log.i("netcode", "you clicked the listview item, connecting to:" + config.deviceAddress);
				
				if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(MainActivity.this, "Press back to cancel",
                        "Connecting to :" + config.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                        );
				
				mManager.connect(mChannel, config, new ActionListener(){
				
					
					@Override
					public void onSuccess() {
						// Don't start the chat intent until we get the socket open						
					}
					
					@Override 
					public void onFailure(int reasonCode) {
						Log.i("netcode","Reason:" + reasonCode);
					}
					
				});
			}
		});
        //We need functionality to close a group when the user turns it off.
        createBtn.setOnClickListener(new OnClickListener(){
        	public void onClick(View v){
        		if (!isWifiP2pEnabled) {
                    Toast.makeText(MainActivity.this, R.string.p2p_off_warning,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
          		mManager.createGroup(mChannel,new ActionListener(){
          		
                      @Override
                      public void onSuccess() {
                    	
                    	Editable textbox = editText1.getText();
                    	listItems.add(textbox.toString());
                    	arrayAdapter.notifyDataSetChanged();
                    	editText1.setText("");
                    	Log.i("createGroup", "success");
                    	//What to do if createGroup works great
                    	// No intent switching until the socket is connected
                      	//Intent chatIntent = new Intent(getApplicationContext(),ChatActivity.class);
          				//startActivity(chatIntent);
                      }

                      @Override
                      public void onFailure(int reasonCode) {
                    	  Log.i("createGroup","failed: Reason:" + reasonCode);
                      }
          		});
        	}
        });
        handler = new Handler() {
        	@Override
        	public void handleMessage(Message msg) {
        		if (msg.what == GroupMemberClientAsyncTask.GMCAT_JOIN_MESSAGE){
        			Intent chatIntent = new Intent(getApplicationContext(),ChatActivity.class);
        			startActivity(chatIntent);
        		}
        	}
        };
        
    }//End On Create

    //To be used with discovery later
    public void fillList() throws InterruptedException{
    	Thread t = new Thread()
    	{
    	    @Override
    	    public void run() {
    	    	mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

    	    		@Override
    	    		public void onSuccess() {
    	        		
    	    			Toast.makeText(MainActivity.this, "Discovery Initiated",
    	    					Toast.LENGTH_SHORT).show();
    	    		}

    	    		@Override
    	    		public void onFailure(int reasonCode) {
    	    			Toast.makeText(MainActivity.this, "Discovery Failed : " + reasonCode,
    	    					Toast.LENGTH_SHORT).show();
    	    		}
    	        
    	    	});
    	    }
    	};
    	
    	t.start();
    	t.join();
    	Toast.makeText(MainActivity.this, "fuck",
				Toast.LENGTH_SHORT).show();
    	for(int i = 0; i<configItems.size();i++){
      	  	listItems.add(configItems.get(i).deviceAddress);
        		arrayAdapter.notifyDataSetChanged();
        		Log.i("netcode","Device address is oooh:" + configItems.get(i).deviceAddress);
        	}

	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    /*This is the function That registers a MyBroadcastReceiver when the activity resumes from being paused.
      This allows the app to switch the broadcast receiver to switch on and off when the user pauses using the
      app but does not end the process completely.
    */
    
    @Override
    public void onPause(){
    	//Since this is an overwrite of Activities onPause() function, we call that as well
    	super.onPause();
    	//This is a call to unregister a previously registered BroadcastRecceiver object
    	if (mReceiver != null){
    		unregisterReceiver(mReceiver);
    	}
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	//Register the receiver to receive only the intents our intent Filter sets up
    	mReceiver = new MyBroadcastReceiver(mManager,mChannel,this);
    	registerReceiver(mReceiver,intentFilter);
    	
    }
    
    
    public void connect(WifiP2pConfig config) {
        mManager.connect(mChannel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            	//Intent chatIntent = new Intent(getApplicationContext(),ChatActivity.class);
  				//startActivity(chatIntent);
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }

        });
    }
    
    public WifiP2pManager getManager(){
    	return mManager;
    }
    
    public Channel getChannel(){
    	return mChannel;
    }
    
    public void makeName(View v){
    	Editable textbox = editText1.getText();
//    	Toast.makeText(MainActivity.this, textbox.toString(),
//				Toast.LENGTH_SHORT).show();
    	username = textbox.toString();
    }
}
