package com.bell.notification;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.bell.util.Constants;
import com.bell.util.JsonTransformer;
import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import static com.bell.util.Constants.DeviceTypes.*;

public class NotificationBroadcaster {

	public static boolean send(ResultSet rs, Notification msg) throws SQLException, IOException {
		if( msg == null ) return false;
		
		ArrayList<String> androidDevices = new ArrayList<String>();
		ArrayList<String> appleDevices = new ArrayList<String>();
		
		while(rs.next()) {
			if( rs.getInt("DeviceType") == ANDROID ) {
				androidDevices.add(rs.getString("DeviceMessagerID"));
			} else if( rs.getInt("DeviceType") == APPLE ) {
				appleDevices.add(rs.getString("DeviceMessagerID"));
			}
		}
		rs.close();
		
		if( androidDevices.size() != 0 ) {
			
			/* Send to Android Devices */
			 
	        // Instance of com.android.gcm.server.Sender, that does the
	        // transmission of a Message to the Google Cloud Messaging service.
	        Sender sender = new Sender( Constants.GOOGLE_API_KEY );
	         
	        // This Message object will hold the data that is being transmitted
	        // to the Android client devices.  For this demo, it is a simple text
	        // string, but could certainly be a JSON object.
	        Message message = new Message.Builder()
	         
	        // If multiple messages are sent using the same .collapseKey()
	        // the android target device, if it was offline during earlier message
	        // transmissions, will only receive the latest message for that key when
	        // it goes back on-line.
	        
	        // TODO change this to a poll of all new messages rather than a specific message
	        // this is an attempt at a unique string generator so Google doesn't toss
	        // the messages on the floor because of a similar collapse key
	        .timeToLive(30)
	        .delayWhileIdle(true)
	        .addData("message", JsonTransformer.toJson(msg))
	        .build();
	         
	        // use this for multicast messages.  The second parameter
	        // of sender.send() will need to be an array of register ids.
	        MulticastResult result = sender.send(message, androidDevices, 1);
	         
	        if (result.getResults() != null) {
	            int canonicalRegId = result.getCanonicalIds();
	            if (canonicalRegId != 0) {
	            } else {
	            	if( result.getFailure() > 0 ) {
	            		for(Result r : result.getResults() ) {
	            			System.out.println("GCM ERROR: " + r.getErrorCodeName());	            			
	            		}
			            return false;
	            	}
		        }
	        } 
		} 
		
		if( appleDevices.size() != 0 ) {
			
		} 
		
		if( androidDevices.size() == 0 && appleDevices.size() == 0) {
			return false;
		}
        
        return true;
	}
}
