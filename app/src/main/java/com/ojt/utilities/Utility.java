/*@ID: CN20140001
 *@Description: srcUtility is common class
 *This class for declare the variables and methods 
 *         which are used in this app.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 11/03/2014
 * @Modified Date: 28/08/2014
 */
package com.ojt.utilities;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ojt.database.OJTDAO;
import com.ojt.notification.R;
import com.ojt.training.Training;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

@SuppressLint("TrulyRandom")
public final class Utility
{
	public static Context context;
	public static String strBAId=null,strBATime=null,strAurecID=null,strAuName=null;
	public static String strMlearning="Nil",strComments="No Comments",strAttitude=null;
	public static int intTimeout;
	public static final String SENDER_ID ="839167516018";
	public static int intPushCount,intTrainingCount,intNotification;
	public static TextView txtCount=null,txtCountAudit=null;
	public static String strTrainingIntime="",strTrainingOuttime="",strPrevMsg="";
	public static boolean hasSignImage=false;
	// Display alert message.
	public static void alert(String strMessage)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(strMessage).setPositiveButton("OK", new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.cancel();
			}
		});
		
		AlertDialog dialog=builder.create();
		dialog.setCancelable(false);
		try {
			dialog.show();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	//Save the lastactivity while minimize the app
	public static void setLastActivity(boolean check,String strUser)
	{
		@SuppressWarnings("static-access")
		SharedPreferences preference = Utility.context.getSharedPreferences("OJTSession",Utility.context.MODE_PRIVATE);
		Editor editor = preference.edit();
		editor.putString("lastActivity", Utility.context.getClass().getName());
		editor.putString("User", strUser);
		editor.putBoolean("subScreen", check);
		editor.commit();
	}
	//Alert for Training module
	public static void trainingAlert(String strMessage)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(strMessage).setPositiveButton("OK", new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.cancel();
				Intent intent=new Intent(context,Training.class);
				context.startActivity(intent);
				Activity act=(Activity) context;
				act.finish();
			}
		}).setCancelable(false).show();
	}
	/*Update the message status 
	 * 0--->Unread Message
	 * 1--->Read Message
	 */
	public static void updateMessageStatus(String strMsgID)
	{
		String strLogin=context.getResources().getString(R.string.login_table);
		String strMainSection=context.getResources().getString(R.string.mainsection_table);
		String strSection=context.getResources().getString(R.string.subsection_table);
		String strAuditData=context.getResources().getString(R.string.auditdata_table);
			
		OJTDAO database=new OJTDAO(context,context.getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
	    ContentValues contentvalues = new ContentValues();
	    contentvalues.put("status",1);
	    database.update(contentvalues, "notification", "msgid=?", new String[]{strMsgID});
	    database.close();
	    //Change push count
	    pushCount();
	}
	//Calculate push message count and display in corresponding icon
	public static void pushCount() 
	{
		String strLogin=context.getResources().getString(R.string.login_table);
		String strMainSection=context.getResources().getString(R.string.mainsection_table);
		String strSection=context.getResources().getString(R.string.subsection_table);
		String strAuditData=context.getResources().getString(R.string.auditdata_table);
			
		OJTDAO database=new OJTDAO(context, context.getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
	    Cursor cursor=database.getVal("status=?", new String[]{"0"}, "notification");
	    Cursor cursor1=database.getVal("state=?", new String[]{"2"}, "notification");
	    if(cursor!=null)
	    {
	        intPushCount=cursor.getCount();
	        cursor.close();
	    }
	    if(cursor1!=null)
	    {
	    	intPushCount=intPushCount+cursor1.getCount();
	    	cursor1.close();
	    }
	    database.close();
        if(intPushCount!=0)
		{
        	 Handler handler = new Handler(Looper.getMainLooper());
             handler.post(new Runnable()
             {
                  public void run() 
                  {
                 	 if(context!=null)
                 	 {
                 		 if(txtCount!=null)
                 		 {
                 			 txtCount.setVisibility(View.VISIBLE);
                 			 txtCount.setText(""+intPushCount);
                 		 }
                 		 if(txtCountAudit!=null)
                 		 {
                 			txtCountAudit.setVisibility(View.VISIBLE);
                 			txtCountAudit.setText(""+intPushCount);
                 		 }
                 	 }
                 }         
              });
        	
		}
        else
        {
        	if(txtCount!=null)      txtCount.setVisibility(View.INVISIBLE);
        	if(txtCountAudit!=null) txtCountAudit.setVisibility(View.INVISIBLE);
        }
	   
	}
	
	//Check the Network connection.
	public static boolean hasConnection()
	{
	    ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
	        Context.CONNECTIVITY_SERVICE);

	    NetworkInfo wifiNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
	    if (wifiNetwork != null && wifiNetwork.isConnected()) 
	    {
	      return true;
	    }

	    NetworkInfo mobileNetwork = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	    if (mobileNetwork != null && mobileNetwork.isConnected()) 
	    {
	      return true;
	    }

	    NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
	    if (activeNetwork != null && activeNetwork.isConnected()) 
	    {
	      return true;
	    }
	    return false;
	  }
	//Get current Date 
	public static String currentDate() 
	{
		String strDate=new SimpleDateFormat("yyyy-MM-dd").format(System.currentTimeMillis());
		return strDate;
	}
	//Get current Time with seconds
	public static String currentTimesecond() 
	{
		String strTime=new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis());
		return strTime;
	}
	//Get BAName from local database
	public static String getBAName() 
	{
		String strBAName=null;
		String strLogin=context.getResources().getString(R.string.login_table);
		String strMainSection=context.getResources().getString(R.string.mainsection_table);
		String strSection=context.getResources().getString(R.string.subsection_table);
		String strAuditData=context.getResources().getString(R.string.auditdata_table);
		OJTDAO database=new OJTDAO(context, context.getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursor=database.getVal("status=?", new String[]{"0"}, strAuditData);
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				strBAName=cursor.getString(cursor.getColumnIndex("baname"));
			}
			cursor.close();
		}
		database.close();
		strLogin=null;
		strMainSection=null;
		strSection=null;
		strAuditData=null;
		cursor=null;
		return strBAName;
	}
	//Check whether audit is start or not
	public static boolean auditStart()
	{
		boolean check=false;
		String strLogin=context.getResources().getString(R.string.login_table);
		String strMainSection=context.getResources().getString(R.string.mainsection_table);
		String strSection=context.getResources().getString(R.string.subsection_table);
		String strAuditData=context.getResources().getString(R.string.auditdata_table);
		
		OJTDAO database=new OJTDAO(context,context. getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursorSelectmain=database.getVal("score=? and remarks=? and training=?", new String[]{"0","null","no"},strMainSection);
		Cursor cursorSelectsub=database.getVal("score=? and remarks=? and training=?", new String[]{"0","null","no"},strSection);
		
		Cursor cursorMain=database.getAll(strMainSection);
		Cursor cursorSub=database.getAll(strSection);
		
		if(cursorMain!=null&&cursorSelectmain!=null&&cursorSelectsub!=null&&cursorSub!=null)
		{
			if(cursorMain.getCount()==cursorSelectmain.getCount()&&cursorSelectsub.getCount()==cursorSub.getCount()&&
					(strComments.equalsIgnoreCase("")||strComments.equalsIgnoreCase("No Comments"))&& strAttitude==null && (strMlearning.equalsIgnoreCase("Nil")||strMlearning.equalsIgnoreCase("")))
			{
				check=false;
			}
			else
			{
				check=true;
			}
			cursorMain.close();
			cursorSub.close();
			cursorSelectmain.close();
			cursorSelectsub.close();
		}
		
		database.close();
		strLogin=null;
		strMainSection=null;
		strSection=null;
		strAuditData=null;
		cursorSelectmain=null;
		cursorSelectsub=null;
		cursorMain=null;
		cursorSub=null;
		return check;
	}
	//Maintain log details in Logfile.
	public static void logFile(String strMessage,boolean newLine)
	{
		if(strPrevMsg.equalsIgnoreCase(strMessage)) return;
		Boolean isSDcard = android.os.Environment.getExternalStorageState().
				equals(android.os.Environment.MEDIA_MOUNTED);
		if(isSDcard)
		{
			File file = new File(Environment.getExternalStorageDirectory() + "/"+ 
						context.getResources().getString(R.string.app_name)+"/Log_File");
			file.mkdirs();
			final File fileOutput = new File(file, "logfile.txt");
	        FileOutputStream fileOutputStream;
			try
			{
				fileOutputStream = new FileOutputStream(fileOutput,true);
				if(fileOutputStream != null)
				{   
					OutputStreamWriter writer = new OutputStreamWriter(
							fileOutputStream, "UTF-8");
		            BufferedWriter bufferedWriter = new BufferedWriter(writer);
		            bufferedWriter.write(strMessage);
		            if(newLine)bufferedWriter.newLine();
		            bufferedWriter.close();
					fileOutputStream.close();	   
					strPrevMsg=strMessage;
			    }	
			} catch (FileNotFoundException e) {
				Log.i("FileNotFoundException",e.toString());
			} catch (IOException e){
				Log.i("IOException",e.toString());
			}
		}
	}
	//Calculate time duration for training module
	@SuppressLint("DefaultLocale")
	public static String getTimeDiff(String strDateOne, String strDateTwo) 
	{
		SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date dateOne = null,dateTwo = null;
		try 
		{
			dateOne=simpleDateFormat.parse(strDateOne);
			dateTwo=simpleDateFormat.parse(strDateTwo);
		} catch (ParseException e) 
		{
			Log.i("Error",e.toString());
			logFile(currentDate()+" "+currentTimesecond()+"~"+strAuName+"~Utility~getTimeDiff"+"~ParseException "+e.toString(),true);
		}
		long longDiff = dateTwo.getTime() - dateOne.getTime();
		long longDiffSeconds = longDiff / 1000 % 60;
		long longDiffMinutes = longDiff / (60 * 1000) % 60;
		return String.format("%02d.%02d", longDiffMinutes,longDiffSeconds); 
	}
	//Calculate Free size of SDCard
	@SuppressWarnings("deprecation")
	public static long getFreeSize()
	{
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		long bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
		long megAvailable = bytesAvailable / 1024;
		return megAvailable;
	}
	//Convert a image file to bitmap
	public static Bitmap decodeFile(File path) 
	{
	    Bitmap bitmap=null;
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inDither=false;                     //Disable Dithering mode
        options.inPurgeable=true;                   //Tell to gc that whether it needs free memory, the Bitmap can be cleared
        options.inInputShareable=true;              //Which kind of reference will be used to recover the Bitmap data after being clear, when it will be used in the future
        options.inTempStorage=new byte[32 * 1024];
        options.inSampleSize=1;
 
        FileInputStream fileInputStream=null;
        try 
        {
        	fileInputStream = new FileInputStream(path);
        }
        catch (FileNotFoundException e)
        {
           Log.i("Filenotfound error",""+e);
   		   logFile(currentDate()+" "+currentTimesecond()+"~"+strAuName+
   				   "~Utility~decodeFile"+"~FileNotFoundException "+e.toString(),true);
   	    }
 
        try 
        {
            if(fileInputStream!=null)
            {
                bitmap=BitmapFactory.decodeFileDescriptor(fileInputStream.getFD(), null, options);
            }
        }
        catch (IOException e)
        {
        	Log.i("IOException",""+e);
        	logFile(currentDate()+" "+currentTimesecond()+"~"+strAuName+
    				   "~Utility~decodeFile"+"~IOException "+e.toString(),true);
        }
        finally
        {
            if(fileInputStream!=null) 
            {
                try 
                {
                	fileInputStream.close();
                }
                catch (IOException e) 
                {
                	Log.i("IOException",""+e);
                	logFile(currentDate()+" "+currentTimesecond()+"~"+strAuName+
         				   "~Utility~decodeFile"+"~IOException_finally "+e.toString(),true);
                }
            }
        }
        return bitmap;
    }
	//Convert from inputstream to string
	public static String convertStreamToString(InputStream inputStream)
	{
	    BufferedReader reader = new BufferedReader
	    		(new InputStreamReader(inputStream));
	    StringBuilder stringBuilder = new StringBuilder();
	    String line = null;
	    try 
	    {
			while ((line = reader.readLine()) != null) 
			{
				stringBuilder.append(line).append("\n");
			}
			reader.close();
		} catch (IOException e)
		{
			Log.i("IOException",""+e);
        	logFile(currentDate()+" "+currentTimesecond()+"~"+strAuName+
 				   "~Utility~convertStreamToString"+"~IOException_finally "+e.toString(),true);
		}
	    return stringBuilder.toString();
	}
	//Read log file
	public static String getStringFromFile (String strPath)  
	{
		File file = new File(strPath);
	    FileInputStream fileInputStream=null;
	    String strLog=null;
		try 
		{
			fileInputStream = new FileInputStream(file);
			strLog = convertStreamToString(fileInputStream);
		    fileInputStream.close();  
		}
		catch (FileNotFoundException e) 
		{
	      	logFile(currentDate()+" "+currentTimesecond()+"~"+strAuName+
  				   "~Utility~getStringFromFile"+"~FileNotFoundException "+e.toString(),true);
		} catch (IOException e) {
			logFile(currentDate()+" "+currentTimesecond()+"~"+strAuName+
	  				   "~Utility~getStringFromFile"+"~IOException "+e.toString(),true);
		}
	          
	    return strLog;
	}
	public static String makePlaceholders(int intlen)
	{
	    if (intlen < 1)
	    {
	        throw new RuntimeException("No placeholders");
	    } else 
	    {
	        StringBuilder stringBuilder = new StringBuilder(intlen * 2 - 1);
	        stringBuilder.append("?");
	        for (int i = 1; i < intlen; i++) {
	        	stringBuilder.append(",?");
	        }
	        return stringBuilder.toString();
	    }
	}
	@SuppressLint("TrulyRandom")
	public static String updateBatchno(boolean status,String strBatchNo)
	{
		String strNewBatchNo="0";
		String strLogin=Utility.context.getResources().getString(R.string.login_table);
		String strMainSection=Utility.context.getResources().getString(R.string.mainsection_table);
		String strSection=Utility.context.getResources().getString(R.string.subsection_table);
		String strAuditData=Utility.context.getResources().getString(R.string.auditdata_table);

		OJTDAO database=new OJTDAO(Utility.context, Utility.context.getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		ContentValues contentValues=new ContentValues();
		if(status)
	    {
	    	SecureRandom random = new SecureRandom();
	    	strNewBatchNo = new BigInteger(130, random).toString(32);
	    }
	    contentValues.put("batchno", strNewBatchNo);
	    database.update(contentValues, strAuditData, "status=?",new String[]{"1"});
//		database.update(contentValues, strAuditData, "batchno=? and status=?",new String[]{strBatchNo,"1"});
		database.close();
		return strNewBatchNo;
	}
}