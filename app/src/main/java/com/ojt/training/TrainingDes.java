/*@ID: CN20140001
 *@Description: srcTrainingDes is for Training Description Screen 
 * This class is used to show the Training content with detail.
 * Content are fetched from SDCard.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 21/05/2014
 * @Modified Date: 21/05/2014
 */
package com.ojt.training;
import java.io.File;

import com.ojt.connectivity.JSONParser;
import com.ojt.database.OJTDAO;
import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.utilities.Utility;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

public class TrainingDes extends Activity  
{
	private String strDisplayID=null,strFileName=null,strMsgID=null,strTitle=null;
	private ImageView imageView=null;
	private VideoView videoView=null;
	private Button btnBack=null;
	private Handler handlerTime=null;
	private Runnable runnable=null;
	private Handler handler=null;
	private int intBackID;
	private ProgressDialog prgDialog=null;
	private Button btnPlay=null;
	private TextView txtHeading=null;
	private MediaController mediaController=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trainingdes);
		Utility.context=this;
		mediaController=new MediaController(this);
		txtHeading=(TextView) findViewById(R.id.headingtxt);
				
		videoView=(VideoView) findViewById(R.id.trainingvideo);
	    videoView.setVisibility(View.INVISIBLE);
	        
		imageView=(ImageView)findViewById(R.id.trainingExpandimg);
		imageView.setVisibility(View.INVISIBLE);
		
		btnBack=(Button) findViewById(R.id.backbtn);
        btnBack.setOnClickListener(new OnClickListener() 
        {
			@Override
			public void onClick(View v) 
			{
				 back();
			}
		});
        btnPlay=(Button) findViewById(R.id.playbtn);
        btnPlay.setVisibility(View.INVISIBLE);
        btnPlay.setOnClickListener(new OnClickListener() 
        {
			@Override
			public void onClick(View v) 
			{
				Boolean isSDcard = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
				if(isSDcard)
				{
					File file = new File( Environment.getExternalStorageDirectory() + "/"+ Utility.context.getResources().getString(R.string.app_name)+"/Training/Content_File/"+strFileName);
					if(file.exists())
					{
						playVideoFile(file);
						v.setVisibility(View.INVISIBLE);
					}
					else
					{
						Utility.trainingAlert(getResources().getString(R.string.no_file));
					}
				}
				else
				{
				 	Utility.alert(getResources().getString(R.string.no_sdcard));
				}
			}
		});
        
        SharedPreferences preference = getSharedPreferences("TrainingDes",MODE_PRIVATE);
		
        intBackID=preference.getInt("backid", 0);
        strDisplayID=preference.getString("key","");
        strFileName=preference.getString("filename","");
        strMsgID=preference.getString("msgid","");
        strTitle=preference.getString("title","");
        
		handler= new Handler() 
		{
			@Override
			public void handleMessage(Message msg) 
			{
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
				if(prgDialog!=null)prgDialog.dismiss();
				switch(msg.getData().getInt("status"))
				{
					case 1:
						File file = new File( Environment.getExternalStorageDirectory() + "/"+ Utility.context.getResources().getString(R.string.app_name)+"/Training/Content_File/"+strFileName);
						if(file.exists())
						{
							Utility.updateMessageStatus(strMsgID);
							if(strDisplayID.equalsIgnoreCase("0"))
							{
								imageView.setVisibility(View.VISIBLE);
								Drawable drawable = (Drawable)Drawable.createFromPath(file.getAbsolutePath());
								imageView.setImageDrawable(drawable);
							}
							else if(strDisplayID.equalsIgnoreCase("1"))
							{
								playVideoFile(file);
							}
						}
						else
						{
							Utility.trainingAlert(getResources().getString(R.string.no_file));
						}
						break;
					case 2:
						Utility.trainingAlert(getResources().getString(R.string.server_error));
						break;
					case 3:
						Utility.trainingAlert(getResources().getString(R.string.sdcard_full));
						break;
					case 4:
						Utility.trainingAlert(getResources().getString(R.string.low_network));
						break;
				}
			}
		 };
		txtHeading.setText(strTitle);
		loadData();
		timeLimit();
	}
	//Store data while minimize the screen
	@Override
	protected void onPause() {
		Utility.setLastActivity(false,Utility.strAuName);
		super.onPause();
	}
	//Maintain video position while change the orientation
	@Override
	protected void onSaveInstanceState(Bundle outState) 
	{
		if(strDisplayID.equalsIgnoreCase("1"))
		outState.putInt("Position", videoView.getCurrentPosition());
		super.onSaveInstanceState(outState);
	}
	//Continue the video while change the orientation
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) 
	{
		if(strDisplayID.equalsIgnoreCase("1"))
		{
			videoView.seekTo(savedInstanceState.getInt("Position"));
		}
		super.onRestoreInstanceState(savedInstanceState);
	}
	@Override
	protected void onResume() 
	{
		super.onResume();
		Utility.context=this;
		if(videoView.getVisibility()==View.VISIBLE)
		{
			 videoView.start();
			 btnPlay.setVisibility(View.INVISIBLE);
			 mediaController.show();
		}
	}
	//Display image or video file from sdcard
	private void loadData() 
	{
		//Check sdcard available or not
		Boolean isSDcard = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if(isSDcard)
		{
			File file = new File( Environment.getExternalStorageDirectory() + "/"+ Utility.context.getResources().getString(R.string.app_name)+"/Training/Content_File/"+strFileName);
			if(file.exists())
			{
				Utility.updateMessageStatus(strMsgID);
				//Display image file
				if(strDisplayID.equalsIgnoreCase("0"))
				{
					imageView.setVisibility(View.VISIBLE);
					Bitmap bitmap=Utility.decodeFile(file);
					if(bitmap!=null)
						imageView.setImageBitmap(bitmap);
				}
				else
				{
					//Play video file
					playVideoFile(file);
				}
			}
			else
			{
				if(Utility.hasConnection())
				{
					loadContent();
				}
				else
				{
					Utility.trainingAlert(Utility.context.getResources().getString(R.string.no_network));
				}
			}
		}
		else
		{
		 	Utility.trainingAlert(getResources().getString(R.string.no_sdcard));
		}
	}
	//Play video file from sd card using Mediacontroller
	private void playVideoFile(File file)
	{
		videoView.setVisibility(View.VISIBLE);
		videoView.setSoundEffectsEnabled(true);
		videoView.setMediaController(mediaController);
		videoView.setVideoPath(file.getAbsolutePath());
		videoView.start();
		videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			public void onCompletion(MediaPlayer mp) {
				btnPlay.setVisibility(View.VISIBLE);
				videoView.setMediaController(null);
			}
		});
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
				final Intent mainIntent = new Intent(TrainingDes.this, Login.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				TrainingDes.this.startActivity(mainIntent);
				TrainingDes.this.finish();
			}
		};
		handlerTime.postDelayed(runnable, (Utility.intTimeout*1000));
	} 
	//Back event
	@Override
	public void onBackPressed()
	{
		back();
		super.onBackPressed();
	}
	//Back to Training screen
	private void back() 
	{
		btnBack.setEnabled(false);
		Intent	intent=new Intent(TrainingDes.this,Training.class);
		intent.putExtra("backid", intBackID);
		startActivity(intent);
		TrainingDes.this.finish();
	}
	//Download content from server
	private void loadContent() 
	{
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) 
		{
		   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}
		else 
		{
		   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		}
		String strLogin=Utility.context.getResources().getString(R.string.login_table);
		String strMainSection=Utility.context.getResources().getString(R.string.mainsection_table);
		String strSection=Utility.context.getResources().getString(R.string.subsection_table);
		String strAuditData=Utility.context.getResources().getString(R.string.auditdata_table);
			
		OJTDAO database=new OJTDAO(Utility.context,Utility.context. getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
	    Cursor cursor=database.getVal("msgid=?", new String[]{strMsgID}, "notification");
	    if(cursor!=null)
	    {
	    	if(cursor.moveToFirst())
	    	{
	    		final String strFileURL=cursor.getString(cursor.getColumnIndex("contenturl"));
	    		final String strFileName=cursor.getString(cursor.getColumnIndex("filename"));
	    		prgDialog=ProgressDialog.show(Utility.context,Utility.context.getResources().getString(R.string.app_name),Utility.context.getResources().getString(R.string.loading_text), true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						try
						{
							String strResult=JSONParser.downloadContent(strFileURL, strFileName);
							Thread.sleep(5000);
							if(strResult.equalsIgnoreCase("true+1"))
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~TrainingDes~loadContent~URL"+strFileURL+" downloadcontent success.",true);
								Message msgobj;
								msgobj = handler.obtainMessage();
								Bundle bundle = new Bundle();
								bundle.putInt("status", 1);
								msgobj.setData(bundle);
								handler.sendMessage(msgobj);
							}
							else if(strResult.equalsIgnoreCase("true+0"))
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~TrainingDes~loadContent~URL"+strFileURL+" sdcard full.",true);
								Message msgobj;
								msgobj = handler.obtainMessage();
								Bundle bundle = new Bundle();
								bundle.putInt("status", 3);
								msgobj.setData(bundle);
								handler.sendMessage(msgobj);
							}
							else if(strResult.equalsIgnoreCase("false+1"))
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~TrainingDes~loadContent~URL"+strFileURL+" timeout error.",true);
								Message msgobj;
								msgobj = handler.obtainMessage();
								Bundle bundle = new Bundle();
								bundle.putInt("status", 4);
								msgobj.setData(bundle);
								handler.sendMessage(msgobj);
							}
							else
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~TrainingDes~loadContent~URL"+strFileURL+" error"+strResult,true);
								Message msgobj;
								msgobj = handler.obtainMessage();
								Bundle bundle = new Bundle();
								bundle.putInt("status", 2);
								msgobj.setData(bundle);
								handler.sendMessage(msgobj);
							}
						}
						catch(Exception e)
						{
							Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~TrainingDes~loadContent~URL"+strFileURL+" error"+e.toString(),true);
							Message msgobj;
							msgobj = handler.obtainMessage();
							Bundle bundle = new Bundle();
							bundle.putInt("status", 2);
							msgobj.setData(bundle);
							handler.sendMessage(msgobj);
						}
					}
				}).start();
	    	}
	    	cursor.close();
	    }
	    database.close();
	}
	@Override
	protected void onDestroy() 
	{
		if(handlerTime!=null)handlerTime.removeCallbacks(runnable);
		imageView=null;
		btnBack=null;
		handlerTime=null;
		runnable=null;
		strDisplayID=null;
		strFileName=null;
		strMsgID=null;
		strTitle=null;
		videoView=null;
		handler=null;
		prgDialog=null;
		btnPlay=null;
		txtHeading=null;
		mediaController=null;
		super.onDestroy();
	}
}