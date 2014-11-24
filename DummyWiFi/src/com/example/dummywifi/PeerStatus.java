package com.example.dummywifi;

import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import java.util.AbstractSequentialList;
import java.util.Collection;

public class PeerStatus implements PeerListListener {

	private int nPeers;
	private WifiP2pDeviceList peerList;
	private AbstractSequentialList ownerList;
	
	public int getPeerCount() {
		return nPeers;
	}
	
	public WifiP2pDeviceList getPeerList() {
		return peerList;
	}
	
	@Override
	public void onPeersAvailable(WifiP2pDeviceList peers) {
		Log.i("netcode","Hey your peer list changed fyi");
		nPeers = peers.getDeviceList().size();
		peerList = peers;		
		Log.i("netcode", "You have " + nPeers + " available.");
		
		MainActivity.configItems.clear();
		MainActivity.names.clear();
		
		for (WifiP2pDevice d : peerList.getDeviceList()) {
			Log.i("netcode", "Found peer: " + d.deviceName + " (" + d.deviceAddress + ")");
			
			if(d.isGroupOwner() == true){
				Log.i("netcode", "Found group owner: " + d.deviceName + " (" + d.deviceAddress + ")");
				WifiP2pConfig config = new WifiP2pConfig();
	            config.deviceAddress = d.deviceAddress;
	            config.wps.setup = WpsInfo.PBC;
				MainActivity.configItems.add(config);
				MainActivity.names.add(d.deviceName);
			}
		}
		MainActivity.updateList();
	}
	
	public PeerStatus() {
		nPeers = 0;
	}
	
	

}
