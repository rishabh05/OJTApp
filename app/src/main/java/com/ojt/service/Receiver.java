/*@ID: CN20140001
 *@Description: srcReceiver 
 * This class is used to check network connection enabled or not..
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 20/03/2014
 */
package com.ojt.service;
import com.ojt.connectivity.JSONParser;
import com.ojt.notification.R;
import com.ojt.utilities.Utility;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
public class Receiver extends BroadcastReceiver 
{
	int intStatus;
	private static boolean firstConnect = true;
	ConnectivityManager connectivityManager=null;
	NetworkInfo activeNetwork=null;
	// Receive method for broadcast receiver,when Network is on
	@Override
	public void onReceive(final Context context, Intent intent) 
	{
		intStatus=this.getConnectivityStatus(context);
		if(intStatus==1||intStatus==2) 
	    {
			if(firstConnect) 
			{ 
				// Submit Audit data to server. when Network is enabled.
				if(Utility.context!=null)
				{
//					new Thread(new Runnable()
//					{
//						@SuppressLint("TrulyRandom")
//						@Override
//						public void run()
//						{
//							String strBatchNo=Utility.updateBatchno(true,"0");
//							JSONParser.sendData(Utility.context.getString(R.string.server_url)+"AuditingReport",strBatchNo);
//						}
//					}).start();
				}
				firstConnect = false;
	    	}
	    }
		else
		{
			 firstConnect= true;
		}
	}
	//Check network connection using connectivity manager.
	private int getConnectivityStatus(Context context) 
	{
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (null != activeNetwork)
        {
            if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                return 1;
            if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
            	return 2;
        }
        return 0;
    }
}