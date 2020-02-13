/*@ID: CN20140001
 *@Description: srcTrainingDes is for Training Description Screen 
 * This class is used to show the Training content with detail.
 * Content are fetched from server via push message.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 20/05/2014
 * @Modified Date: 27/08/2014
 */
package com.ojt.training;

import java.io.File;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import com.ojt.baaudit.BAAuditForm;
import com.ojt.connectivity.JSONParser;
import com.ojt.database.OJTDAO;
import com.ojt.home.Home;
import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.utilities.Utility;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
public class Training extends Activity
{
	private EditText edttxtSearch=null;
	private ListView listView=null;
	private ArrayList<TrainingData> arrayList=null,trainingArrayList=null;
	private TrainingListAdapter trainingListAdapter=null;
	private Intent intent=null;
	private ProgressDialog prgDialog;
	private Button btnBack=null,btnRefresh=null;
	private String strMsgID=null,strFileName=null;
	private int intBackID;
	private TextView txtNoTrainingData=null;
	private boolean check=false;
	private Handler handler=null,handlerTime=null,handlerTraining=null;
	private Runnable runnable=null;
	private RelativeLayout relativeLayoutBg;
	private OJTDAO database=null;
	private String strContentIDs[]=null;
	private String strUserName = "",strPassword="";
	private SharedPreferences preference=null; 
	private float floatCoaching=0.0f;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.training);
		Utility.context=this;
		preference = getSharedPreferences("TrainingDes",MODE_PRIVATE);
		
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		intBackID=preference.getInt("backid", 0);
		//Get last coaching time
		String strCoach=getCoachingTime();
		if(strCoach!=null)
		{
			floatCoaching=Float.parseFloat(strCoach);
		}
		if(Utility.intTrainingCount==0&&intBackID==1)
		{
			Utility.strTrainingIntime=Utility.currentDate()+" "+Utility.currentTimesecond();
		}
		txtNoTrainingData=(TextView) findViewById(R.id.nodatatxt);
		btnBack=(Button) findViewById(R.id.backbtn);
        btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				back();
				
			}
		});
        
        btnRefresh=(Button) findViewById(R.id.refreshbtn);
        btnRefresh.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshTraining();
				
			}
		});
		listView=(ListView) findViewById(R.id.searchlist);
		edttxtSearch=(EditText) findViewById(R.id.searchedttxt);
		arrayList=new ArrayList<TrainingData>();
		listView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3)
			{
				Boolean isSDcard = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
				if(isSDcard)
				{
					if(trainingArrayList.size()==0) return;
					
					TrainingData trainingData=trainingArrayList.get(arg2);
					strMsgID=trainingData.getID();
					strFileName=trainingData.getContentName();
					//Check whether content type is image or video or document
					if(trainingData.getFileType().equalsIgnoreCase("image"))
					{
						Editor editor = preference.edit();
						editor.putString("msgid", strMsgID);
						editor.putString("key", "0");
						editor.putString("title", trainingData.getTitle());
						editor.putString("filename", strFileName);
						editor.putInt("backid", intBackID);
						editor.commit();
						intent=new Intent(Training.this,TrainingDes.class);
						startActivity(intent);
						Training.this.finish();
					}
					else if(trainingData.getFileType().equalsIgnoreCase("video"))
					{
						Editor editor = preference.edit();
						editor.putString("msgid", strMsgID);
						editor.putString("key", "1");
						editor.putString("title", trainingData.getTitle());
						editor.putString("filename", strFileName);
						editor.putInt("backid", intBackID);
						editor.commit();
						intent=new Intent(Training.this,TrainingDes.class);
						startActivity(intent);
						Training.this.finish();
					}
					else
					{
						File file = new File( Environment.getExternalStorageDirectory() + "/"+ Utility.context.getResources().getString(R.string.app_name)+"/Training/Content_File/"+strFileName);
						if (file.exists()) 
						{
							loadDocument(file); 
						}
						else
						{
							if(Utility.hasConnection())
							{
								loadContent();
							}
							else
							{
								Utility.alert(getResources().getString(R.string.no_network));
							}
						}
						
					}
				}
				else
				{
				 	Utility.alert(getResources().getString(R.string.no_sdcard));
				}
			}
		});
		//Search option
		edttxtSearch.addTextChangedListener(new TextWatcher() 
		{
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) 
			{
				ArrayList<TrainingData> tempArrayList = new ArrayList<TrainingData>();
		        for(TrainingData trainingData: arrayList)
		        {
		           check=false;
		           if(trainingData.getTitle()!=null)
		           {
		        	   if (s.length() <= trainingData.getTitle().length()) 
			           {
		        		   if (trainingData.getTitle().toLowerCase().contains(s.toString().toLowerCase())) 
				           {
		        			   tempArrayList.add(trainingData);
		        			   check=true;
				           }
		               }
		           }
		           if(check==true)continue;
		           if(trainingData.getReference()!=null)
		           {
		        	   if (s.length() <= trainingData.getReference().length()) 
			           {
		        		   if (trainingData.getReference().toLowerCase().contains(s.toString().toLowerCase())) 
				           {
		        			   tempArrayList.add(trainingData);
				           }
		               }
		           }
		        }
		        trainingArrayList=tempArrayList;
		        trainingListAdapter = new TrainingListAdapter(Training.this,R.layout.traininglistrow, tempArrayList);
		        if(trainingListAdapter.getCount()!=0)
				{
					txtNoTrainingData.setVisibility(View.INVISIBLE);
				}
				else
				{
					txtNoTrainingData.setVisibility(View.VISIBLE);
				}
		        listView.setAdapter(trainingListAdapter);
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) 
			{}			
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
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
						if (file.exists()) 
						 {
							loadDocument(file); 
					     }
						 else
						 {
							 Utility.alert(getResources().getString(R.string.no_file));
						 }
						break;
					case 2:
						Utility.alert(getResources().getString(R.string.server_error));
						break;
					case 3:
						Utility.alert(getResources().getString(R.string.sdcard_full));
						break;
					case 4:
						Utility.alert(getResources().getString(R.string.low_network));
						break;
				}
			}
		 };
		 
		 handlerTraining= new Handler() 
		 {
			@Override
			public void handleMessage(Message msg) 
			{
				setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
				btnRefresh.setEnabled(true);
				if(prgDialog!=null)prgDialog.dismiss();
				switch(msg.getData().getInt("status"))
				{
					case 1:
						if(edttxtSearch!=null)edttxtSearch.setText("");
						loadData();
						break;
					case 2:
						Utility.alert(getResources().getString(R.string.low_network));
						break;
					case 3:
						Utility.alert(getResources().getString(R.string.server_error));
						break;
					
				}
			}
		 };
		 relativeLayoutBg=(RelativeLayout) findViewById(R.id.trainingmainlayout);
			// Handle the soft keyboard 
			 relativeLayoutBg.setOnTouchListener(new OnTouchListener()
			 {
				@Override
				public boolean onTouch(View v, MotionEvent event) 
				{
					if(v instanceof EditText)
						 ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(v,InputMethodManager.SHOW_FORCED);
					else
						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
					return false;
				}
			});
		 Utility.intTrainingCount++; 
		 timeLimit();//calculate the no of times viewed this page.
		
	}
	//Store data while minimize the screen
	@Override
	protected void onPause() {
		if(intBackID==1)
		{
			Utility.strTrainingOuttime=Utility.currentDate()+" "+Utility.currentTimesecond();
			saveCoachingTime();
		}
		Utility.setLastActivity(false,Utility.strAuName);
		super.onPause();
	}
	@Override
	protected void onResume() 
	{
		if(edttxtSearch!=null)edttxtSearch.setText("");
		Utility.context=this;
		loadData();
		super.onResume();
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
				final Intent mainIntent = new Intent(Training.this, Login.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				Training.this.startActivity(mainIntent);
				Training.this.finish();
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
	//Back to Home Screen
	private void back() 
	{
		btnBack.setEnabled(false);
		if(intBackID==0)
		{
			intent=new Intent(Training.this,Home.class);
		}
		else
		{	
			intent=new Intent(Training.this,BAAuditForm.class);
		}
		 startActivity(intent);
		 Training.this.finish();
	}
	//Get document extension
	private String getMimeType(String strURL)
	{
	    String strType = null;
	    String strExtension = MimeTypeMap.getFileExtensionFromUrl(strURL);
	    if (strExtension != null) 
	    {
	        MimeTypeMap mime = MimeTypeMap.getSingleton();
	        strType = mime.getMimeTypeFromExtension(strExtension);
	    }
	    return strType;
	}
	//Load pdf or document file
	private void loadDocument(File file)
	{
		Uri path = Uri.fromFile(file);
        intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(path, getMimeType(path.getPath()));
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try
        {
        	Utility.updateMessageStatus(strMsgID);
        	startActivity(intent);
        } 
        catch (ActivityNotFoundException e)
        {
        	Utility.alert(getResources().getString(R.string.no_pdfdoc_found));
			Log.i("Error",e.toString());
        }
	}
	//Get training data from local database and display it
	 private void loadData() 
	{
		NotificationManager notificationManager =
			    (NotificationManager) getSystemService(Training.NOTIFICATION_SERVICE);
		notificationManager.cancel(0);
		deleteInactive();
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		TrainingData trainingData=null;
		String strImageName=null;
		String strID=null;
		String strFileName=null;
		String strTitle=null;
		String strFileType=null;
		String strStatus=null;
		String strImageURL=null;
		String strReference=null;
		arrayList.clear();
		//Get unread message
		OJTDAO database=new OJTDAO(Utility.context, Utility.context.getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursor=database.getVal("status=?", new String[]{"0"}, "notification");
		if(cursor!=null)
		{
			if(cursor.moveToLast())
			{
				do
				{
					trainingData=new TrainingData();
					strImageName=cursor.getString(cursor.getColumnIndex("imagename"));
					strID=cursor.getString(cursor.getColumnIndex("msgid"));
					strFileName=cursor.getString(cursor.getColumnIndex("filename"));
					strTitle=cursor.getString(cursor.getColumnIndex("title"));
					strFileType=cursor.getString(cursor.getColumnIndex("filetype"));
					strStatus=""+cursor.getInt(cursor.getColumnIndex("status"));
					strImageURL=cursor.getString(cursor.getColumnIndex("imageurl"));
					strReference=cursor.getString(cursor.getColumnIndex("reference"));
					
					trainingData.setImageName(strImageName);
					trainingData.setID(strID);
					trainingData.setContentName(strFileName);
					trainingData.setTitle(strTitle);
					trainingData.setFileType(strFileType);
					trainingData.setStatus(strStatus);
					trainingData.setImageURL(strImageURL);
					trainingData.setReference(strReference);
					arrayList.add(trainingData);
				}while(cursor.moveToPrevious());
			}
			cursor.close();
		}
		
		//Get read message
		cursor=database.getVal("status=?", new String[]{"1"}, "notification");
		if(cursor!=null)
		{
			if(cursor.moveToLast())
			{
				do
				{
					trainingData=new TrainingData();
					strImageName=cursor.getString(cursor.getColumnIndex("imagename"));
					strID=cursor.getString(cursor.getColumnIndex("msgid"));
					strFileName=cursor.getString(cursor.getColumnIndex("filename"));
					strTitle=cursor.getString(cursor.getColumnIndex("title"));
					strFileType=cursor.getString(cursor.getColumnIndex("filetype"));
					strStatus=""+cursor.getInt(cursor.getColumnIndex("status"));
					strImageURL=cursor.getString(cursor.getColumnIndex("imageurl"));
					strReference=cursor.getString(cursor.getColumnIndex("reference"));
					
					trainingData.setImageName(strImageName);
					trainingData.setID(strID);
					trainingData.setContentName(strFileName);
					trainingData.setTitle(strTitle);
					trainingData.setFileType(strFileType);
					trainingData.setStatus(strStatus);
					trainingData.setImageURL(strImageURL);
					trainingData.setReference(strReference);
					arrayList.add(trainingData);
				}while(cursor.moveToPrevious());
			}
			cursor.close();
		}
		database.close();
		trainingArrayList=arrayList;
		trainingListAdapter=new TrainingListAdapter(this, R.layout.traininglistrow, arrayList);
		listView.setAdapter(trainingListAdapter);
		if(trainingListAdapter.getCount()!=0)
		{
			txtNoTrainingData.setVisibility(View.INVISIBLE);
			edttxtSearch.setVisibility(View.VISIBLE);
		}
		else
		{
			txtNoTrainingData.setVisibility(View.VISIBLE);
			edttxtSearch.setVisibility(View.INVISIBLE);
		}
	}
	 //Delete the Training content which is inactive state
	private void deleteInactive() 
	{
		int i=0;
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		String strImageName=null;
		String strFileName=null;
		
		OJTDAO database=new OJTDAO(Utility.context, Utility.context.getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursor=database.getVal("state=?", new String[]{"2"}, "notification");
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				do
				{
					if(i==0)
					{
						Toast toast= Toast.makeText(getApplicationContext(),getResources().getString(R.string.content_expired), Toast.LENGTH_SHORT);  
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();	
					}
						
					strImageName=cursor.getString(cursor.getColumnIndex("imagename"));
					strFileName=cursor.getString(cursor.getColumnIndex("filename"));
					if(strFileName!=null&&strImageName!=null)
					{
						strFileName=Environment.getExternalStorageDirectory() + "/"+ Utility.context.getResources().getString(R.string.app_name)+"/Training/Content_File/"+strFileName;
						deleteFiles(strFileName);
						strImageName=Environment.getExternalStorageDirectory() + "/"+ Utility.context.getResources().getString(R.string.app_name)+"/Training/"+strImageName;
						deleteFiles(strImageName);
					}
					i++;
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		database.deleteVal("state=?", new String[]{"2"},"notification");
		database.close();
	}
	//Delete Training content files from SD card 
	private void deleteFiles(String strFileName) 
	{
		File file = new File(strFileName);
		if (file.exists()) 
		{
			if (file.isFile()) 
		    {
		        file.delete();
		    } 
		}
	}
	//Download the Training content file from server
	private void loadContent() 
	{
		int currentOrientation = getResources().getConfiguration().orientation;
		if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
		   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		}
		else {
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
							Log.i("Response",""+strResult);
							Thread.sleep(5000);
							if(strResult.equalsIgnoreCase("true+1"))
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~Training~loadContent~URL"+strFileURL+" downloadcontent success.",true);
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
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~TrainingDes~loadContent~URL"+strFileURL+" timeout error."+strResult,true);
								Message msgobj;
								msgobj = handler.obtainMessage();
								Bundle bundle = new Bundle();
								bundle.putInt("status", 4);
								msgobj.setData(bundle);
								handler.sendMessage(msgobj);
							}
							else
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~Training~loadContent~URL"+strFileURL+" error."+strResult,true);
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
							Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~Training~loadContent~URL"+strFileURL+" error."+e.toString(),true);
							Log.i("Error",e.toString());
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
	//Calculate coaching time save it
	private void saveCoachingTime()
	{
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		String strCoachingTime=Utility.getTimeDiff(Utility.strTrainingIntime,Utility.strTrainingOuttime);
		strCoachingTime=""+(floatCoaching+Float.parseFloat(strCoachingTime));
		OJTDAO database=new OJTDAO(this, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
	    ContentValues contentValues = new ContentValues();
	    contentValues=new ContentValues(); 
		contentValues.put("coachingtime", strCoachingTime);
		database.update(contentValues, strAuditData, "status=?", new String[]{"0"});
		database.close();
		strLogin=null;
		strMainSection=null;
		strSection=null;
		strAuditData=null;
		strCoachingTime=null;
	}
	//Get coaching time from local database
	private String getCoachingTime()
	{
		
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		String strCoachingTime=null;
		
		OJTDAO database=new OJTDAO(this, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursor=database.getVal("status=?",new String[]{"0"}, strAuditData);
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				strCoachingTime=cursor.getString(cursor.getColumnIndex("coachingtime"));
			}
			cursor.close();
		}
		database.close();
		strLogin=null;
		strMainSection=null;
		strSection=null;
		strAuditData=null;
		return strCoachingTime;
	}
	//Refresh the Training page

	private void refreshTraining() 
	{
		final String strLogin,strMainSection, strSection, strAuditData;
		strLogin=getResources().getString(R.string.login_table);
		strMainSection=getResources().getString(R.string.mainsection_table);
		strSection=getResources().getString(R.string.subsection_table);
		strAuditData=getResources().getString(R.string.auditdata_table);
		if(Utility.hasConnection())
		{
			database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
			database.create(strLogin,strMainSection,strSection,strAuditData);
			Cursor cursor=database.getAll(strLogin);
			if(cursor!=null)
			{
				if(cursor.moveToFirst())
				{
					do
					{
						strUserName=cursor.getString(cursor.getColumnIndex("user"));
						strPassword=cursor.getString(cursor.getColumnIndex("password"));
					}while(cursor.moveToNext());
				}
			}
			cursor.close();
			strUserName=strUserName.replaceAll(" ","+");
			strPassword=strPassword.replaceAll(" ","+");
			int currentOrientation = getResources().getConfiguration().orientation;
			if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
			}
			else {
			   setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
			}
			btnRefresh.setEnabled(false);
			prgDialog=ProgressDialog.show(Utility.context,Utility.context.getResources().getString(R.string.app_name),Utility.context.getResources().getString(R.string.loading_text), true);
			new Thread(new Runnable() 
			{
				@Override
				public void run() 
				{
					try
					{
						JSONObject jobj=JSONParser.connect(getResources().getString(R.string.server_url)+"AuditorLogin?auid="+strUserName+"&pwd="+strPassword, Training.this);
						Log.i("Response",jobj.toString());
						if(jobj.has("Status"))
						{
							if(jobj.has("TrainingContent"))
							{
								JSONArray jArray=jobj.getJSONArray("TrainingContent");
								strContentIDs=new String[jArray.length()];
								for(int i=0;i<jArray.length();i++)
								{
									String strSubJobj=jArray.get(i).toString();
									strSubJobj=strSubJobj.replace("=", "\":\"");
									strSubJobj=strSubJobj.replace(", ", "\",\"");
									strSubJobj=strSubJobj.replace("{", "{\"");
									strSubJobj=strSubJobj.replace("}", "\"}");
									JSONObject subJobj=new JSONObject(strSubJobj); 
									
									String strContentID=""+subJobj.getString("contentid").replaceAll("~", ",");
									strContentIDs[i]=strContentID;
											
									Cursor cursor=database.getVal("msgid=?", new String[]{strContentID}, "notification");
									if(cursor.getCount()==0)//check already present or not
									{
									
										String strFileType=""+subJobj.getString("content_type").replaceAll("~", ",");
										//Thumbimage url
										String strThumbURL=subJobj.getString("thumbnail_path").replaceAll("~", ",");
										strThumbURL=URLEncoder.encode(strThumbURL, "UTF-8");
										strThumbURL=getResources().getString(R.string.server_url)+"ContentDownloadServlet?content="+strThumbURL;
										//Content url
										String strContentURL=subJobj.getString("contentpath").replaceAll("~", ",");
										strContentURL=URLEncoder.encode(strContentURL, "UTF-8");
										strContentURL=getResources().getString(R.string.server_url)+"ContentDownloadServlet?content="+strContentURL;
										ContentValues contentvalues = new ContentValues();
										contentvalues.put("msgid", ""+strContentID);
										contentvalues.put("title", ""+subJobj.getString("content_title").replaceAll("~", ","));
										contentvalues.put("contenturl",strContentURL);
										contentvalues.put("imageurl",""+strThumbURL);
								        contentvalues.put("status",0);
								        contentvalues.put("intime","");
								        contentvalues.put("outtime","");
								        contentvalues.put("reference",""+subJobj.getString("reference_tag").replaceAll("~",","));
								        contentvalues.put("filetype",""+strFileType);
								        contentvalues.put("arrivetime",Utility.currentDate()+" "+Utility.currentTimesecond());
								        contentvalues.put("state",""+subJobj.getString("content_status").replaceAll("~", ","));
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
								}
								if(strContentIDs.length!=0)
								{
									String query = "UPDATE notification set state=2"
										    + " WHERE msgid NOT IN (" + Utility.makePlaceholders(strContentIDs.length) + ")";
										
										SQLiteDatabase sqldb=openOrCreateDatabase(getResources().getString(R.string.db_name), MODE_PRIVATE,null);
										Cursor cursor=sqldb.rawQuery(query, strContentIDs);
										if(cursor!=null)
										{
											if(cursor.moveToFirst())
											{
												do
												{
													Log.i("state",""+cursor.getString(cursor.getColumnIndex("state")));
												}while(cursor.moveToNext());
											}
											cursor.close();
										}
										sqldb.close();
								}
							}
							else
							{
								String query = "UPDATE notification set state=2";
									
									SQLiteDatabase sqldb=openOrCreateDatabase(getResources().getString(R.string.db_name), MODE_PRIVATE,null);
									Cursor cursor=sqldb.rawQuery(query, null);
									if(cursor!=null)
									{
										if(cursor.moveToFirst())
										{
											do
											{
												Log.i("state",""+cursor.getString(cursor.getColumnIndex("state")));
											}while(cursor.moveToNext());
										}
										cursor.close();
									}
									sqldb.close();
									
							}
							Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~Training~refreshTraining"+"~auid:"+strUserName+" pwd:"+strPassword+"~refreshTraining success get all details.",true);
							database.close();
							Message msgobj;
							msgobj = handlerTraining.obtainMessage();
						    Bundle bundle = new Bundle();
						    bundle.putInt("status", 1);
						    msgobj.setData(bundle);
						    handlerTraining.sendMessage(msgobj);
						}
						else
						{
							if(jobj.has("Error"))
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~Training~refreshTraining"+"~auid:"+strUserName+" pwd:"+strPassword+"~refreshTraining timeout. "+jobj.toString(),true);
								Message msgobj;
								msgobj = handlerTraining.obtainMessage();
							    Bundle bundle = new Bundle();
							    bundle.putInt("status", 2);
							    msgobj.setData(bundle);
							    handlerTraining.sendMessage(msgobj);
							}
							else
							{	
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~Training~refreshTraining"+"~auid:"+strUserName+" pwd:"+strPassword+"~empty status. "+jobj.toString(),true);
								Message msgobj;
								msgobj = handlerTraining.obtainMessage();
							    Bundle bundle = new Bundle();
							    bundle.putInt("status", 3);
							    msgobj.setData(bundle);
							    handlerTraining.sendMessage(msgobj);
							}
						}
					}
					catch(Exception e)
					{
						Log.i("Exception:",e.toString());
						Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~Training~refreshTraining"+"~auid:"+strUserName+" pwd:"+strPassword+"~error:"+e.toString(),true);
						Message msgobj;
						msgobj = handlerTraining.obtainMessage();
					    Bundle bundle = new Bundle();
					    bundle.putInt("status", 3);
					    msgobj.setData(bundle);
					    handlerTraining.sendMessage(msgobj);
					}		
				}
			}).start();
		}
		else
		{
			Utility.alert(getResources().getString(R.string.no_network));
		}
	}
	@Override
	protected void onDestroy()
	{
		if(handlerTime!=null)handlerTime.removeCallbacks(runnable);
		edttxtSearch=null;
		listView=null;
		arrayList=null;
		trainingArrayList=null;
		trainingListAdapter=null;
		intent=null;
		prgDialog=null;
		btnBack=null;
		btnRefresh=null;
		strMsgID=null;
		strFileName=null;
		txtNoTrainingData=null;
		handler=null;
		handlerTime=null;
		handlerTraining=null;
		runnable=null;
		relativeLayoutBg=null;
		database=null;
		strContentIDs=null;
		strUserName = null;
		strPassword=null;
		super.onDestroy();
	}
}
