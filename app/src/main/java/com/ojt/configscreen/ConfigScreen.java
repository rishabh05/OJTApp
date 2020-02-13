/*@ID: CN20140001
 *@Description: srcConfigscreen is for Config screen 
 * This class is used for display app version and 
 *   screen idle time out 
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 10/03/2014
 * @Modified Date: 26/08/2014
 */
package com.ojt.configscreen;
import com.ojt.home.Home;
import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.utilities.Utility;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
public class ConfigScreen extends Activity 
{
	private TextView txtTimeOut=null,txtVersion=null;
	private Handler handlerTime=null;
	private Runnable runnable=null;
	private Button btnBack=null;
	private PackageInfo packInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configpage);
		Utility.context=this;
		btnBack=(Button) findViewById(R.id.backbtn);
		btnBack.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) 
			{
				back();
				
			}
		});
		txtTimeOut=(TextView) findViewById(R.id.timeouttxt);
		txtTimeOut.setText(""+Utility.intTimeout+" Sec");
		txtVersion=(TextView) findViewById(R.id.versiontxt);
		try
		{
			packInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			txtVersion.setText(packInfo.versionName);
		} 
		catch (NameNotFoundException e)
		{
			Log.i("NameNotFoundException",e.toString());
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~ConfigScreen~create"+"~Namenotfoundexception "+e.toString(),true);
		}
		timeLimit();
	}
	//Store data while minimize the screen
	@Override
	protected void onPause() 
	{
		super.onPause();
		Utility.setLastActivity(false,Utility.strAuName);
	}
	//Reset session time when idle recover
	@Override
	public void onUserInteraction()
	{
	    super.onUserInteraction();
	    //Remove any previous callback
	    handlerTime.removeCallbacks(runnable);
	    timeLimit();
	}
	/*
	 * Calculate session time.
	 * Redirect to login page when session expired
	 */
	private void timeLimit() 
	{
		handlerTime=new Handler();
		runnable=new Runnable()
		{
			@Override
			public void run()
			{
				final Intent mainIntent = new Intent(ConfigScreen.this, Login.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ConfigScreen.this.startActivity(mainIntent);
				ConfigScreen.this.finish();
			}
		};
		handlerTime.postDelayed(runnable, (Utility.intTimeout*1000));
	}
	//Back event
	@Override
	public void onBackPressed() 
	{
		back();
	}
	private void back()//Back. Back to Home screen
	{
		startActivity(new Intent(this,Home.class));
		this.finish();
	}
	@Override
	protected void onDestroy() 
	{
		if(handlerTime!=null)handlerTime.removeCallbacks(runnable);
		txtTimeOut=null;
		txtVersion=null;
		handlerTime=null;
		runnable=null;
		btnBack=null;
		packInfo=null;
		super.onDestroy();
	}
}
