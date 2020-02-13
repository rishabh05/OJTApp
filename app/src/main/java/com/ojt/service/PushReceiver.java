/*@ID: CN20140001
 *@Description: srcPushReceiver 
 * This class is used to check audit is started or not when push message received
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 20/03/2014
 * @Modified Date: 28/07/2014
 */
package com.ojt.service;


import com.ojt.home.Home;
import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.training.Training;
import com.ojt.utilities.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

public class PushReceiver extends BroadcastReceiver
{
	Intent intentMain=null;
	private SharedPreferences preference;
	@SuppressWarnings("static-access")
	@Override
	public void onReceive(final Context context, final Intent intent) 
	{
		preference = context.getSharedPreferences("OJTSession", context.MODE_PRIVATE);
		if(Utility.context==null)//It occur when app close stage
		{
			intentMain = new Intent(context, Login.class);
			intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intentMain);
		}
		else if(Utility.context.getClass().getName().contains("Training"))//It occur when app open stage
		{
			intentMain = new Intent(context, Training.class);
			intentMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			Utility.context.startActivity(intentMain);
		}
		else if(!Utility.context.getClass().getName().contains("Login"))
		{
			if(Utility.context.getClass().getName().equalsIgnoreCase("com.ojt.baaudit.BASearchData")
					||Utility.context.getClass().getName().equalsIgnoreCase("com.ojt.baaudit.BAAuditForm")
					||Utility.context.getClass().getName().equalsIgnoreCase("com.ojt.baaudit.BAAuditScore")
					||Utility.context.getClass().getName().equalsIgnoreCase("com.ojt.baaudit.BAPrevSummary")
					||Utility.context.getClass().getName().equalsIgnoreCase("com.ojt.baaudit.BASummary"))
			{
				if(Utility.auditStart()||preference.getBoolean("AuditStart", false))
				{
					 AlertDialog.Builder builder = new AlertDialog.Builder(Utility.context);
					 builder.setMessage(context.getResources().getString(R.string.auditloss_msg)).setPositiveButton(Utility.context.getResources().getString(R.string.yes_msg), new DialogInterface.OnClickListener() 
					 {
						 @Override
						 public void onClick(DialogInterface dialog, int which) 
						 {
							dialog.cancel();
							intentMain=new Intent(context,Home.class);
							intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.startActivity(intentMain);
							Activity act=(Activity) Utility.context;
							act.finish();
						 }
					 }).setNegativeButton(Utility.context.getResources().getString(R.string.no_msg), new DialogInterface.OnClickListener() 
					 {
						 @Override
						 public void onClick(DialogInterface dialog, int which) 
						 {
							 dialog.cancel();
						 }
					 }).show(); 
				}
				else
				{
					intentMain=new Intent(context,Home.class);
					intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intentMain);
					Activity act=(Activity) Utility.context;
					act.finish();
				}
			}
			else
			{
				intentMain = new Intent(context, Home.class);
				intentMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intentMain);
				Activity act=(Activity) Utility.context;
				act.finish();
			}
		}
		else
		{
			intentMain = new Intent(context, Login.class);
			intentMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			Utility.context.startActivity(intentMain);
		} 
	} 
}
