/*@ID: CN20140001
 *@Description: srcGCMIntentService is communicate with GCM server for
 *           receive push message
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 10/03/2014
 * @Modified Date: 26/08/2014
 */
package com.ojt.notification;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.ojt.database.OJTDAO;
import com.ojt.service.PushReceiver;
import com.ojt.training.Training;
import com.ojt.utilities.Utility;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService
{
    private static final String TAG = "GCMIntentService";
    public GCMIntentService()
    {
    	super(Utility.SENDER_ID);
    }
    //Receive the device while register the device on GCM Server
    @Override
    protected void onRegistered(Context context, String registrationId)
    {
        Log.i(TAG, "Device registered: regId = " + registrationId);
    }
    @Override
    protected void onUnregistered(Context context, String registrationId)
    {
        Log.i(TAG, "Device unregistered");
    }
    //Receive the message from GCM Server
    @Override
    protected void onMessage(final Context context, final Intent intent)
    {
    	try
    	{
    		Log.i("status",""+intent.getStringExtra("content_status"));
    		Log.i("id",""+intent.getStringExtra("contentid"));
    		Log.i("content_title",""+intent.getStringExtra("content_title"));
    		Log.i("contentpath",""+intent.getStringExtra("contentpath"));
    		Log.i("thumbnail_path",""+intent.getStringExtra("thumbnail_path"));
    		Log.i("reference_tag",""+intent.getStringExtra("reference_tag"));
    		Log.i("content_type",""+intent.getStringExtra("content_type"));
			generateNotification(context, intent.getStringExtra("message_title"),intent.getStringExtra("content_status"),
    		intent.getStringExtra("contentid"),intent.getStringExtra("content_title"),
    		intent.getStringExtra("contentpath"),intent.getStringExtra("thumbnail_path"),
    		intent.getStringExtra("reference_tag"),intent.getStringExtra("content_type"));
    	}catch(Exception e)
    	{
    	  	Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~GCMIntentService~onMessage"+"~Exception "+e.toString(),true);
    		Log.i("Error",e.toString());
    	}
    }

    @Override
    protected void onDeletedMessages(Context context, int total)
    {
        Log.i(TAG, "Received deleted messages notification");
    }
    @Override
    public void onError(Context context, String errorId)
    {
        Log.i(TAG, "Received error: " + errorId);
    	Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~GCMIntentService~onError"+"~GCM ERROR. "+errorId,true);
	}
    @Override
    protected boolean onRecoverableError(Context context, String errorId)
    {
        // log message


        Log.i(TAG, "Received recoverable error: " + errorId);
    	Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~GCMIntentService~onRecoverableError"+"~GCM ERROR. "+errorId,true);
        return super.onRecoverableError(context, errorId);
    }
    //Store the training content details in loacal database and notify it
   private static void generateNotification(Context context, String strMessageTitle,String strContentStatus,
			String strContentID,String strTitle,String strContentURL, String strThumbURL,
			String strReference,String strFileType)
    {
    	String strLogin=context.getResources().getString(R.string.login_table);
		String strMainSection=context.getResources().getString(R.string.mainsection_table);
		String strSection=context.getResources().getString(R.string.subsection_table);
		String strAuditData=context.getResources().getString(R.string.auditdata_table);

		OJTDAO database=new OJTDAO(context, context.getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		if(!strContentStatus.equalsIgnoreCase("2"))
		{
			Cursor cursor=database.getVal("msgid=?", new String[]{strContentID}, "notification");
			if(cursor.getCount()!=0)
			{
				cursor.close();
				database.close();
				return;
			}
			try
			{
				strThumbURL=strThumbURL.replaceAll("~", ",");
				strThumbURL=URLEncoder.encode(strThumbURL, "UTF-8");
				strThumbURL=context.getResources().getString(R.string.server_url)+"ContentDownloadServlet?content="+strThumbURL;

				strContentURL=strContentURL.replaceAll("~", ",");
				strContentURL=URLEncoder.encode(strContentURL, "UTF-8");
				strContentURL=context.getResources().getString(R.string.server_url)+"ContentDownloadServlet?content="+strContentURL;
			} catch (UnsupportedEncodingException e)
			{
				Log.i("Error",e.toString());
				Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~GCMIntentService~generateNotification"+"~UnsupportedEncodingException "+e.toString(),true);

			}
			ContentValues contentvalues = new ContentValues();
			contentvalues.put("msgid", strContentID.replaceAll("~", ","));
			contentvalues.put("title", strTitle.replaceAll("~", ","));
			contentvalues.put("contenturl",strContentURL);
			contentvalues.put("imageurl",strThumbURL);
	        contentvalues.put("status",0);
	        contentvalues.put("intime","");
	        contentvalues.put("outtime","");
	        contentvalues.put("reference",strReference.replaceAll("~", ","));
	        contentvalues.put("filetype",strFileType.replaceAll("~", ","));
	        contentvalues.put("arrivetime",Utility.currentDate()+" "+Utility.currentTimesecond());
	        contentvalues.put("state",strContentStatus.replaceAll("~", ","));
	        contentvalues.put("imagename",strContentID+".png");
	        if(strFileType.equalsIgnoreCase("image"))
	        {
	        	contentvalues.put("filename",strContentID+".png");
	        }
	        else if(strFileType.equalsIgnoreCase("video"))
	        {
	        	contentvalues.put("filename",strContentID+".mp4");
	        }
	        else if(strFileType.equalsIgnoreCase("pdf"))
	        {
	        	contentvalues.put("filename",strContentID+".pdf");
	        }
	        else if(strFileType.equalsIgnoreCase("document"))
	        {
	        	contentvalues.put("filename",strContentID+".doc");
	        }
	        database.insert(contentvalues,"notification");
		}
		else
		{
			Cursor cursor=database.getVal("msgid=?", new String[]{strContentID}, "notification");
			if(cursor.getCount()==0)
			{
				cursor.close();
				database.close();
				return;
			}
			ContentValues contentvalues = new ContentValues();
			contentvalues.put("state",strContentStatus);
			database.update(contentvalues, "notification", "msgid=?", new String[]{strContentID});
		}
        database.close();
        String strTraining="com.ojt.training.Training";

        if(Utility.context==null)
        {
        	notification(context,strContentID,strContentStatus,strTitle,strMessageTitle);
        }
        else if(!Utility.context.getClass().getName().equalsIgnoreCase(strTraining)) //check current screen is training or not.
		{
        	notification(context,strContentID,strContentStatus,strTitle,strMessageTitle);
		}
    	else
    	{
    		Intent intentTraining = new Intent(context, Training.class);
		  	intentTraining.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		   	Utility.context.startActivity(intentTraining);
    	}
     }
   //Make Notification for push message
    @SuppressWarnings("deprecation")
	private static void notification(Context context,String strContentID,String strContentStatus,String strTitle,String strMessageTitle)
    {
    	int icon =R.drawable.app_icon;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//		Notification notification = new Notification(icon, strMessageTitle, when);
		Intent notificationIntent = new Intent(context, PushReceiver.class);
		notificationIntent.setAction("com.ojt.service");
	    PendingIntent pintent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
//	    notification.setLatestEventInfo(context, strMessageTitle, strContentID, pintent);

		Notification.Builder builder = new Notification.Builder(context);

		builder.setSmallIcon(icon)
				.setContentTitle(strMessageTitle)
       			.setContentIntent(pintent);

		Notification notification = builder.getNotification();
		notificationManager.notify(icon, notification);

//	    notification.flags |= Notification.FLAG_AUTO_CANCEL;
//	    notificationManager.notify(0, notification);
     }
  }