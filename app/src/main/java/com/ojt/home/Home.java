/*@ID: CN20140001
 *@Description: srcHome is for Menu Screen 
 * This class have BA Audit, Ready Reckoner, Training, Config options to access 
 * their functionality,
 * @Developer: Arunachalam
 * @Version 1.0
 * @Stage: 1
 * @Date: 11/03/2014
 * @Modified Date: 26/08/2014
 */
package com.ojt.home;

import com.ojt.baaudit.BASearch;
import com.ojt.baaudit.PendingAudits;
import com.ojt.configscreen.ConfigScreen;
import com.ojt.database.OJTDAO;
import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.readyreckoner.ReadyReckoner;
import com.ojt.training.Training;
import com.ojt.utilities.Utility;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class Home extends Activity implements OnClickListener
{
	private Button btnAudit=null,btnReadyrec=null,btnTraining=null,btnConfig=null, pendingaudit;
	private Intent intent=null;
	private Handler handlerTime=null;
	private Runnable runnable=null;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		Utility.context=this;
		homeUIDef();
		timeLimit();
		NotificationManager notificationManager =
			    (NotificationManager) getSystemService(Training.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
			checkPermission();
		}

		Utility.logFile("Home sscreen -> oncreate()"
				,true);
	}
	//Store data while minimize the screen
	@Override
	protected void onPause() 
	{
		super.onPause();
		Utility.setLastActivity(false,Utility.strAuName);
	}
	@Override
	protected void onResume() {
		Utility.pushCount();
		Utility.logFile("Home screen-> onResume()",true);
		Cursor cursor = getPendingAudits();
		if(cursor.getCount() > 0) {
			pendingaudit.setText(getResources().getString(R.string.pendingauditbtn_text) + "(" + cursor.getCount() + ")");
			Utility.logFile(getResources().getString(R.string.pendingauditbtn_text) + "(" + cursor.getCount() + ")"
					,true);
		}else{
			pendingaudit.setText(getResources().getString(R.string.pendingauditbtn_text));
		}
		super.onResume();
	}
	//Define Audit,Readyreckoner,Training button
	private void homeUIDef()
	{
		btnAudit=(Button) findViewById(R.id.baauditbtn);
		btnReadyrec=(Button) findViewById(R.id.readyrecbtn);
		btnTraining=(Button) findViewById(R.id.trainingbtn);
		pendingaudit=(Button) findViewById(R.id.pendingaudit);
		btnConfig=(Button) findViewById(R.id.configbtn);
		Utility.txtCount=(TextView) findViewById(R.id.msgcounttxt);
		
		btnAudit.setOnClickListener(this);
		btnConfig.setOnClickListener(this);
		btnReadyrec.setOnClickListener(this);
		pendingaudit.setOnClickListener(this);
		btnTraining.setOnClickListener(this);


	}
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		setContentView(R.layout.home);
		homeUIDef();
		super.onConfigurationChanged(newConfig);
	}

	public void checkPermission(){
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
				){//Can add more as per requirement

			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE},
					123);
		}
	}

	@Override
	public void onClick(View v) 
	{
		if(v==btnAudit)//Audit. Move to BASearch screen.
		{
			Utility.logFile("Home screen-> BASearch -> going for audit",true);
			btnAudit.setEnabled(false);
			intent=new Intent(Home.this,BASearch.class);
			startActivity(intent);
			Home.this.finish();
		}
		else if(v==btnConfig)//Config. Move to Config screen.
		{
			btnConfig.setEnabled(false);
			intent=new Intent(Home.this,ConfigScreen.class);
			startActivity(intent);
			Home.this.finish();
		}
		else if(v==pendingaudit)
		{
			Utility.logFile("Home screen-> PendingAudits -> Pending audit click",true);
			btnConfig.setEnabled(false);
			intent=new Intent(Home.this,PendingAudits.class);
			startActivity(intent);
//			Home.this.finish();
//			Cursor cursor = getPendingAudits();
//			if (cursor != null) {
//				Toast.makeText(this, "Count = " + cursor.getCount(), Toast.LENGTH_LONG).show();
//				if (cursor.moveToFirst()) {
//					do {
//						Log.i("status", "" + cursor.getString(cursor.getColumnIndex("status")));
//						Log.i("bppath", "" + cursor.getString(cursor.getColumnIndex("bppath")));
//					} while (cursor.moveToNext());
//				}
//				cursor.close();
//			}
		}
		else if(v==btnReadyrec)//Readyreckoner. Move to Readyreckoner screen.
		{
			btnReadyrec.setEnabled(false);
			intent=new Intent(Home.this,ReadyReckoner.class);
			startActivity(intent);
			Home.this.finish();
		}
		else if(v==btnTraining)//Training. Move to Training screen.
		{
			Utility.logFile("Home screen-> Training click",true);
			SharedPreferences preference = getSharedPreferences("TrainingDes",MODE_PRIVATE);
			Editor editor = preference.edit();
			editor.putInt("backid", 0);
			editor.commit();
			btnTraining.setEnabled(false);
			intent=new Intent(Home.this,Training.class);
			startActivity(intent);
			Home.this.finish();
			OJTDAO.exportDatabse("ojtapplication_database6", this);
		}
	}

	private Cursor getPendingAudits(){
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		database.deleteVal("status=?", new String[]{"0"}, strAuditData);
		Cursor cursor=database.getAll(strAuditData);
		return cursor;
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
				intent = new Intent(Home.this, Login.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Home.this.startActivity(intent);
				Home.this.finish();
			}
		};
		handlerTime.postDelayed(runnable, (Utility.intTimeout*1000));
	}
	//Back event. Exit the application
	@Override
	public void onBackPressed() 
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
		builder.setMessage(getResources().getString(R.string.exit_msg)).setPositiveButton(getResources().getString(R.string.yes_msg), new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) 
		{
			dialog.cancel();
			SharedPreferences preference = getSharedPreferences("OJTSession",MODE_PRIVATE);
			Editor editor = preference.edit();
			editor.putString("lastActivity", "");
			editor.commit();
			
			Utility.context=null;
			intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			finish();
			System.exit(0);
		}
		}).setNegativeButton(getResources().getString(R.string.no_msg), new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.cancel();
			}
		}).show();
	}
	@Override
	protected void onDestroy() 
	{
		if(handlerTime!=null)handlerTime.removeCallbacks(runnable);
		btnAudit=null;
		btnReadyrec=null;
		btnTraining=null;
		btnConfig=null;
		intent=null;
		handlerTime=null;
		runnable=null;
		super.onDestroy();
	}
}