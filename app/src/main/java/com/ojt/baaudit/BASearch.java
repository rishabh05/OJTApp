/*@ID: CN20140001
 *@Description: srcBAsearch is for BA Search Screen 
 * This class is used to search particular BA's information
 * Content are fetched from server.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 12/03/2014
 * @Modified Date: 28/08/2014
 */
package com.ojt.baaudit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ojt.home.Home;
import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.database.OJTDAO;
import com.ojt.connectivity.JSONParser;
import com.ojt.utilities.Utility;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
public class BASearch extends Activity implements OnClickListener,LocationListener
{
	private String strLatitude=null;
	private String strLongitude=null;
	private boolean isEnabled=false;
	private int intGPSTimeOut;
	private String strFilePath=null;
	private String strSearchBA=null;
	private Button btnLookup=null,btnBack=null,btnAudit=null,btnOk=null,btnCancel=null;
	private static Handler handlerData=null,handler=null,handlerTime=null;
	private EditText edttxtBASearch=null;
	private ProgressDialog prgDialog=null;
	private Button btnCaptImg=null;
	private ImageView imgBA=null;
	private RelativeLayout relativeLayoutBg=null,relativeLayoutBASign=null;
	private boolean isGPSEnabled=false,isAudit=false,isNetworkEnabled=false;
	private String strAddress=null;
	private LocationManager locationManager=null;
	private Location location=null;
	private Runnable runnable=null;
	private SharedPreferences preference=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basearch);
		Utility.context=this;
		Utility.strComments="No Comments";
		Utility.strMlearning="Nil";
		Utility.strAttitude=null;
		Utility.intTrainingCount=0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
			checkPermission();
		}
		preference = getSharedPreferences("OJTSession", MODE_PRIVATE);
		Editor editor=preference.edit();
		editor.putBoolean("AuditStart", false);
		editor.commit();
	
		intGPSTimeOut=Integer.parseInt(getResources().getString(R.string.gps_timeout));
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        
        edttxtBASearch=(EditText) findViewById(R.id.basearchactxt);
       
		btnLookup=(Button) findViewById(R.id.lookupbtn);
		btnLookup.setOnClickListener(this); 
		btnCaptImg=(Button) findViewById(R.id.imgcapbtn);
		btnCaptImg.setOnClickListener(this);	
		btnOk=(Button) findViewById(R.id.baokbtn);
		btnOk.setOnClickListener(this);
		btnOk.setEnabled(false);
		btnCancel=(Button) findViewById(R.id.bacancelbtn);
		btnCancel.setOnClickListener(this);
		btnBack=(Button) findViewById(R.id.backbtn);
		btnBack.setOnClickListener(this); 
		btnAudit=(Button) findViewById(R.id.auditbtn);
		btnAudit.setOnClickListener(this);
		
		imgBA=(ImageView) findViewById(R.id.capimg);
		
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		 
	    relativeLayoutBASign=(RelativeLayout) findViewById(R.id.basignrelative);
	    SharedPreferences preference = getSharedPreferences("OJTSession", MODE_PRIVATE);
		if(preference.getBoolean("subScreen", false))
		{
		 relativeLayoutBASign.setVisibility(View.VISIBLE);
		}
		handlerData= new Handler() 
		{
			@Override
			public void handleMessage(Message msg) 
			{
				if(prgDialog!=null)prgDialog.dismiss();
				switch(msg.getData().getInt("status"))
				{
					case 1:
						imgBA.setImageResource(R.drawable.capimg);
						btnOk.setEnabled(false);
						edttxtBASearch.setFocusable(false);
						edttxtBASearch.setFocusableInTouchMode(false); 
						edttxtBASearch.setClickable(false);
						btnLookup.setClickable(false);
						relativeLayoutBASign.setVisibility(View.VISIBLE);
						break;
					case 2:
						Utility.alert(getResources().getString(R.string.invalid_audit));
						break;
					case 3:
						Utility.alert(getResources().getString(R.string.server_error));
						break;
					case 4:
						Utility.alert(getResources().getString(R.string.low_network));
						break;
				}
			}
		 };
		 relativeLayoutBg=(RelativeLayout) findViewById(R.id.layoutbg);
		  // Handle the soft keyboard 
		 relativeLayoutBg.setOnTouchListener(new OnTouchListener()
		 {
				@Override
				public boolean onTouch(View v, MotionEvent event) 
				{
					hideKeyboard(v);
					return false;
				}
		 });
		 timeLimit();
		 setBAImage();
	}
	//Store data while minimize the screen
	@Override
	protected void onPause() 
	{
		super.onPause();
		storeData();
		if(relativeLayoutBASign.getVisibility()==View.VISIBLE)
			Utility.setLastActivity(true,Utility.strAuName);
		else
			Utility.setLastActivity(false,Utility.strAuName);
	}
	//Load BA image in image view 
	private void setBAImage()
	{
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursor=database.getVal("status=?", new String[]{"0"}, strAuditData);
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				strFilePath=cursor.getString(cursor.getColumnIndex("bsipath"));
				strSearchBA=""+cursor.getInt(cursor.getColumnIndex("pbid"));
			}
			cursor.close();
		}
		database.close();
		strLogin=null;
		strMainSection=null;
		strSection=null;
		strAuditData=null;
		if(relativeLayoutBASign.getVisibility()==View.VISIBLE)
		{
			edttxtBASearch.setText(strSearchBA);
		}
		else
		{
			edttxtBASearch.setText("");
		}
		if(strFilePath!=null)
		{
			File file=new File(strFilePath);
			if(file.exists())
			{
				 Drawable drawable = (Drawable) 
                 Drawable.createFromPath(file.getAbsolutePath());
				 imgBA.setImageDrawable(drawable);
				 btnOk.setEnabled(true);
			}
			else
			{
				imgBA.setImageResource(R.drawable.capimg);
				btnOk.setEnabled(false);
			}
		}
		else
		{
			imgBA.setImageResource(R.drawable.capimg);
			btnOk.setEnabled(false);
		}
	}
	//Handle the soft keyboard
	private void hideKeyboard(View v)
	{
		if(v instanceof EditText)
			 ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(v,InputMethodManager.SHOW_FORCED);
		else
		{
			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
			edttxtBASearch.clearFocus();
		}
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
				final Intent mainIntent = new Intent(BASearch.this, Login.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				BASearch.this.startActivity(mainIntent);
				BASearch.this.finish();
			}
		};
		handlerTime.postDelayed(runnable, (Utility.intTimeout*1000));
	}
	@Override
	public void onClick(View v) 
	{
		if(v==btnCaptImg)// Show image cature screen
		{
			capturePicture();
		}
		else if(v==btnLookup)//Get latitude,longitude and audit details when hits lookup button
		{
			hideKeyboard(v);
			isAudit=false;
			lookUp();
		}
		else if(v==btnBack)
		{
			if(relativeLayoutBASign.getVisibility()==View.INVISIBLE)
			{
				back();
			}
		}
		else if(v==btnAudit)//Get latitude,longitude and audit details when hits Audit icon
		{
			if(relativeLayoutBASign.getVisibility()==View.INVISIBLE)
			{
				isAudit=true;
				lookUp();
			}
		}
		else if(v==btnOk)// Save all data and Move to BA's information screen
		{
			storeData();
			nextScreen();
		}
		else if(v==btnCancel)// Cancel. Dismiss the image capture screen.
		{
			btnLookup.setClickable(true);
			edttxtBASearch.setFocusable(true);
			edttxtBASearch.setFocusableInTouchMode(true); 
			edttxtBASearch.setClickable(true); 
			relativeLayoutBASign.setVisibility(View.INVISIBLE);
		}
	}
	//Move to Audit Form screen or BaSearchdata screen
	private void nextScreen() 
	{
		Intent intent=null;
		relativeLayoutBASign.setVisibility(View.INVISIBLE);
		if(isAudit)
		{
			intent=new Intent(BASearch.this,BAAuditForm.class); 
		}
		else
		{
			intent=new Intent(BASearch.this,BASearchData.class); 
		}
		startActivity(intent);
		BASearch.this.finish();
		intent=null;
	}
	//Back event
	@Override
	public void onBackPressed() 
	{
		if(relativeLayoutBASign.getVisibility()==View.INVISIBLE)
		{
			back();
		}
	}
	//Back to Home screen
	private void back()
	{
		btnBack.setEnabled(false);
		Intent intent=new Intent(BASearch.this,Home.class); 
		startActivity(intent);
		BASearch.this.finish();
		intent=null;
	}
	//Get current location and BA's information.
	private void lookUp()
	{
		strSearchBA = edttxtBASearch.getText().toString();
		if(strSearchBA.length()==0)
		{
			Utility.alert(getResources().getString(R.string.empty_ba));
		}
		else
		{
			if(Utility.hasConnection())
			{
				strSearchBA=strSearchBA.replace(" ","+");
				getAccessGeo();
			}
			else
			{
				Utility.alert(getResources().getString(R.string.no_network));
			}
		}
	}
	//Get current location using GPS
	private void getAccessGeo() 
	{
		try
		{
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if(!isGPSEnabled&&!isNetworkEnabled)
			{
				Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearch~getAccessGeo~no gps and network provider",true);
				
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
			    alertDialog.setTitle(getResources().getString(R.string.app_name));
		        alertDialog.setMessage(getResources().getString(R.string.no_gps));
		        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog,int which) {
		                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		                Utility.context.startActivity(intent);
		            }
		        });
		        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		            dialog.cancel();
		            }
		        });
		        alertDialog.setCancelable(false);
		        alertDialog.show();
			}
			else
			{
				getLocation();
			}
		}
		catch(Exception e)
		{
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearch~getAccessGeo"+"~error:"+e.toString(),true);
			Log.i("Exception",e.toString());
		}
	}
	//To get latitude and longitude values
	private Runnable GpsEnabler = new Runnable()
	{
		public void run() 
		{                                                     
//			getLatLong();
			askPermissions();
			if(location!=null)
			{
				strLatitude=String.valueOf(location.getLatitude());
				strLongitude=String.valueOf(location.getLongitude());
			}
			if(strLatitude!=null && strLongitude!=null)
			{
				isEnabled=true;
				handler.post(GpsDisabler);
			}
			else
			{
				isEnabled=false;
				handler.post(GpsEnabler);
				handler.postDelayed(GpsDisabler, intGPSTimeOut*1000);
			}
		}
	};
	//To display the corresponding alert after gps timeout finished.
	private Runnable GpsDisabler = new Runnable() 
	{
	    public void run()
	    {
	    	handler.removeCallbacks(GpsEnabler);
    		handler.removeCallbacks(GpsDisabler);
    		if(prgDialog!=null)prgDialog.dismiss();
    		if(!isEnabled)
	    	{
	    		Utility.alert(getResources().getString(R.string.geo_failure));
	    	}
	    	else
	    	{
	    		locationManager.removeUpdates(BASearch.this);
	    		strAddress=location.getLatitude()+"~"+location.getLongitude();
	    		getData();
	    	}
	     }
	};
	//start to get location..
	public void getLocation()
	{
		location=null;
		handler=new Handler();
		handler.post(GpsEnabler);
		prgDialog=ProgressDialog.show(BASearch.this,Utility.context.getResources().getString(R.string.app_name),Utility.context.getResources().getString(R.string.access_geo), true);
	}


	public void checkPermission(){
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
				){//Can add more as per requirement

			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
					123);
		}
	}


	public void askPermissions(){
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
				ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
				){//Can add more as per requirement

			ActivityCompat.requestPermissions(this,
					new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},
					123);
		}else if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
				){
			try {
				getLatLong();
			}catch (Exception e){
				Utility.logFile("location permission exception "+e.toString(),true);
			}
		}

	}

	//Get Latitude and Longitude
	private void getLatLong()
	{
	    if (locationManager != null) 
  	    {
	    	if (isNetworkEnabled) 
  	    	{
  	    		Log.i("NETWORK Enabled", "NETWORK Enabled");
  	    		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
  	    		location = locationManager
                            .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            	if (location != null)
                {
            		Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearch~getLatLong~network location "+location.getLatitude()+" "+location.getLongitude(),true);
        	    }
  	    		else
  	    		{
  	    			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearch~getLatLong~network location:no location",true);
  	         	}
  	    	}
  	    	if(isGPSEnabled&&location==null)
  	    	{
  	    	    Log.i("GPS Enabled", "GPS Enabled");
  	    		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
  	    		location = locationManager
                        .getLastKnownLocation(LocationManager.GPS_PROVIDER);
        		if (location != null)
                {
  	    	 		Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearch~getLatLong"+"~gps location "+location.getLatitude()+" "+location.getLongitude(),true);
  	          	}
  	    		else
  	    		{
  	    	 		Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearch~getLatLong~gps location:no location",true);
  	     	    }
  	    	}
        }
  	    else
  	  	{
  	    	Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearch~getLatLong~location manager null",true);
   	    }
	}
	@Override
	public void onLocationChanged(final Location location) 
	{
		this.location=location;
		if (this.location != null)
        {
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearch~locationchanged"+"~location:"+location.getLatitude()+" "+location.getLongitude(),true);
	    }
		else
   	  	{
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearch~locationchanged~location not found",true);
		}
	}
	//Get audit details from server
	private void getData()
	{
		if(Utility.hasConnection())
		{
			prgDialog=ProgressDialog.show(BASearch.this,Utility.context.getResources().getString(R.string.app_name),"Loading...", true);
			new Thread(new Runnable() 
			{
			
				@Override
				public void run() 
				{
					try
					{
						strSearchBA=URLEncoder.encode(strSearchBA,"UTF-8");
						JSONObject jObj=JSONParser.connect(getResources().getString(R.string.server_url)+"FormDetails?lookup="+strSearchBA,
                                BASearch.this);
						Log.i("getdata_Response",jObj.toString());
						
						JSONArray jArray=null;
						JSONObject subJobj=null;
						String strSubJobj=null;
						if(jObj.has("Status")) 
						{
							if(jObj.getString("Status").equalsIgnoreCase("1"))
							{
								if(jObj.has("SubSection")&&jObj.has("MainSection")&&jObj.has("BAInfo"))
								{
									String strLogin=getResources().getString(R.string.login_table);
									String strMainSection=getResources().getString(R.string.mainsection_table);
									String strSection=getResources().getString(R.string.subsection_table);
									String strAuditData=getResources().getString(R.string.auditdata_table);
									ContentValues contentValues=null;
									OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
									database.create(strLogin,strMainSection,strSection,strAuditData);
									database.delete(strSection);
									database.delete(strMainSection);
									database.delete("lastmainscore");
									database.delete("lastsubscore");
									database.delete("lastauditreport");
									database.deleteVal("status=?", new String[]{"0"}, strAuditData);
									//sub Section
									jArray=jObj.getJSONArray("SubSection");
									for(int i=0;i<jArray.length();i++)
									{
										strSubJobj=jArray.get(i).toString();
										strSubJobj=strSubJobj.replace("=", "\":\"");
										strSubJobj=strSubJobj.replace(", ", "\",\"");
										strSubJobj=strSubJobj.replace("{", "{\"");
										strSubJobj=strSubJobj.replace("}", "\"}");
										
										subJobj=new JSONObject(strSubJobj); 
										contentValues=new ContentValues();
										contentValues.put("mid", subJobj.getString("id"));
										contentValues.put("id", subJobj.getString("subid"));
										contentValues.put("score", 0);
										contentValues.put("remarks", "null");
										contentValues.put("training", "no");
										contentValues.put("name", subJobj.getString("name").replace("+", " "));
										database.insert(contentValues, strSection);
									}
									//Main Section
									jArray=jObj.getJSONArray("MainSection");
									for(int i=0;i<jArray.length();i++)
									{
										strSubJobj=jArray.get(i).toString();
										strSubJobj=strSubJobj.replace("=", "\":\"");
										strSubJobj=strSubJobj.replace(", ", "\",\"");
										strSubJobj=strSubJobj.replace("{", "{\"");
										strSubJobj=strSubJobj.replace("}", "\"}");
										
										subJobj=new JSONObject(strSubJobj); 
										contentValues=new ContentValues();
										contentValues.put("id", subJobj.getString("id"));
										contentValues.put("score", 0);
										contentValues.put("remarks","null");
										contentValues.put("training","no");
										contentValues.put("name", subJobj.getString("name").replace("+", " "));
										database.insert(contentValues, strMainSection);
									}
									Cursor cursor=database.getVal("status=?", new String[]{"0"}, strAuditData);
									if(cursor!=null)
									{
										if(cursor.moveToFirst())
										{
											strFilePath=cursor.getString(cursor.getColumnIndex("bsipath"));
										}
										cursor.close();
									}
									//lastmainscore
									if(jObj.has("LastMainscore"))
									{
										jArray=jObj.getJSONArray("LastMainscore");
										for(int i=0;i<jArray.length();i++)
										{
											strSubJobj=jArray.get(i).toString();
											strSubJobj=strSubJobj.replace("=", "\":\"");
											strSubJobj=strSubJobj.replace(", ", "\",\"");
											strSubJobj=strSubJobj.replace("{", "{\"");
											strSubJobj=strSubJobj.replace("}", "\"}");
											
											subJobj=new JSONObject(strSubJobj); 
											contentValues=new ContentValues();
											contentValues.put("id", subJobj.getString("mainid"));
											contentValues.put("score", subJobj.getString("mainscore"));
											contentValues.put("training", subJobj.getString("training"));
											contentValues.put("percentage", subJobj.getString("mainpercent"));
											contentValues.put("remarks",subJobj.getString("remark").replace("+", " "));
											contentValues.put("name", subJobj.getString("mainname").replace("+", " "));
											database.insert(contentValues, "lastmainscore");
										}
									}
									//lastsubscore
									if(jObj.has("LastSubscore"))
									{
										jArray=jObj.getJSONArray("LastSubscore");
										for(int i=0;i<jArray.length();i++)
										{
											strSubJobj=jArray.get(i).toString();
											strSubJobj=strSubJobj.replace("=", "\":\"");
											strSubJobj=strSubJobj.replace(", ", "\",\"");
											strSubJobj=strSubJobj.replace("{", "{\"");
											strSubJobj=strSubJobj.replace("}", "\"}");
											
											subJobj=new JSONObject(strSubJobj);  
											contentValues=new ContentValues();
											contentValues.put("id", subJobj.getString("subid"));
											contentValues.put("mid", subJobj.getString("mainid"));
											contentValues.put("score", subJobj.getString("subscore"));
											contentValues.put("training", subJobj.getString("training"));
											contentValues.put("remarks",subJobj.getString("remark").replace("+", " "));
											contentValues.put("name", subJobj.getString("subname").replace("+", " "));
											database.insert(contentValues, "lastsubscore");
										}
									}
									if(jObj.has("LastAuditReport"))
									{
										jArray=jObj.getJSONArray("LastAuditReport");
										
										strSubJobj=jArray.get(0).toString();
										strSubJobj=strSubJobj.replace("=", "\":\"");
										strSubJobj=strSubJobj.replace(", ", "\",\"");
										strSubJobj=strSubJobj.replace("{", "{\"");
										strSubJobj=strSubJobj.replace("}", "\"}");
										
										subJobj=new JSONObject(strSubJobj); 
										contentValues=new ContentValues();
										contentValues.put("auditon", subJobj.getString("auditon").replace("+"," "));
										contentValues.put("auditontime", subJobj.getString("auditontime").replace("+"," "));
										contentValues.put("auditby", subJobj.getString("auditby").replace("+"," "));
										contentValues.put("attitudecolor", subJobj.getString("attitudecolor").replace("+"," "));
										contentValues.put("overallcolor",subJobj.getString("overallcolor").replace("+"," "));
										contentValues.put("comments", subJobj.getString("auditorcomment").replace("+"," "));
										contentValues.put("mlearning", subJobj.getString("mlearning").replace("+"," "));
										database.insert(contentValues, "lastauditreport");
									}
									//BA Info
									jArray=jObj.getJSONArray("BAInfo");
									strSubJobj=jArray.get(0).toString();
									strSubJobj=strSubJobj.replace("=", "\":\"");
									strSubJobj=strSubJobj.replace(", ", "\",\"");
									strSubJobj=strSubJobj.replace("{", "{\"");
									strSubJobj=strSubJobj.replace("}", "\"}");
									
									subJobj=new JSONObject(strSubJobj); 
									String strBAName=subJobj.getString("BAName").replace("+", " ");
									Utility.strBAId=subJobj.getString("BApbid").replace("+", " ");
									Utility.strBATime=Utility.currentDate()+" "+Utility.currentTimesecond();
									contentValues=new ContentValues();
									contentValues.put("batchno","0");
									contentValues.put("pbid",Utility.strBAId);
									contentValues.put("status", 0);
									contentValues.put("alocation",strAddress);
									contentValues.put("intime", Utility.strBATime);
									contentValues.put("baname", strBAName);
									contentValues.put("comments","No+Comments");
									contentValues.put("bainfo",""+jArray);
									contentValues.put("mlearning","Nil");
									contentValues.put("storecode",  subJobj.getString("StoreCode").replace("+", " "));
									contentValues.put("countername",  subJobj.getString("CounterName").replace("+", " "));
									contentValues.put("badetailsid", subJobj.getString("BADetailsid").replace("+", " "));
									//ReadyReckoner Info
									jArray=jObj.getJSONArray("ReadyReckonerInfo");
									contentValues.put("readyreckonerinfo",""+jArray);
									database.insert(contentValues, strAuditData);
									database.close();
									
									strLogin=null;
									strMainSection=null;
									strSection=null;
									strAuditData=null;
									contentValues=null;
									Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()
											+"~"+Utility.strAuName+"~BASearch~getData"+"~PBID:"+strSearchBA
											+" Success:get audit form,last score details.",true);
									Message msgobj;
									msgobj = handlerData.obtainMessage();
								    Bundle bundle = new Bundle();
								    bundle.putInt("status", 1);
								    msgobj.setData(bundle);
								    handlerData.sendMessage(msgobj);
								}
								else
								{
									Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()
											+"~"+Utility.strAuName+"~BASearch~getData~PBID:"+strSearchBA+" no data found",
											true);
									
									Message msgobj;
									msgobj = handlerData.obtainMessage();
								    Bundle bundle = new Bundle();
								    bundle.putInt("status", 3);
								    msgobj.setData(bundle);
								    handlerData.sendMessage(msgobj);
								}
							}
							else
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"
										+Utility.strAuName+"~BASearch~getData~PBID:"+strSearchBA+" invalid pbid",true);
								
								Message msgobj;
								msgobj = handlerData.obtainMessage();
							    Bundle bundle = new Bundle();
							    bundle.putInt("status", 2);
							    msgobj.setData(bundle);
							    handlerData.sendMessage(msgobj);	
							}
						}
						else
						{
							if(jObj.has("Error"))
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()
										+"~"+Utility.strAuName+"~BASearch~getData"+"~PBID:"+strSearchBA+" timeout error",true);
								
								Message msgobj;
								msgobj = handlerData.obtainMessage();
							    Bundle bundle = new Bundle();
							    bundle.putInt("status", 4);
							    msgobj.setData(bundle);
							    handlerData.sendMessage(msgobj);
							}
							else
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()
										+"~"+Utility.strAuName+"~BASearch~getData"+"~PBID:"+strSearchBA+" empty status"
										+jObj.toString(),true);
								
								Message msgobj;
								msgobj = handlerData.obtainMessage();
							    Bundle bundle = new Bundle();
							    bundle.putInt("status", 3);
							    msgobj.setData(bundle);
							    handlerData.sendMessage(msgobj);
							}
						}
						jArray=null;
						subJobj=null;
					}
					catch(Exception e)
					{
						Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName
								+"~BASearch~getData"+"~PBID:"+strSearchBA+" error"+e.toString(),true);
						
						Log.i("Exception:",e.toString());
						Message msgobj;
						msgobj = handlerData.obtainMessage();
					    Bundle bundle = new Bundle();
					    bundle.putInt("status", 3);
					    msgobj.setData(bundle);
					    handlerData.sendMessage(msgobj);
					}
				}
			}).start();
		}
		else
		{
			if(prgDialog!=null)prgDialog.dismiss();
			Utility.alert(getResources().getString(R.string.no_network));
		}
	}
	//Image capture
	private void capturePicture()
	{
		Intent intent=null;
		strFilePath=null;
		Boolean isSDcard = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		if(isSDcard)
		{
			intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
			intent.putExtra(android.provider.MediaStore.EXTRA_SCREEN_ORIENTATION,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			startActivityForResult(intent, 1);
		}
		else
		{
			Utility.alert(getResources().getString(R.string.no_sdcard));
		}
	}
	//Load image in image view after take picture
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{  
		 if (requestCode == 1 && resultCode == RESULT_OK && data!=null)
		 { 
			 if( data.getExtras()!=null)
			 {
				storeCapimg(data); 
			 }
		 } 
	}
	//Stored image in SD Card
	private void storeCapimg(Intent data) 
	{
		Bitmap bitmap = data.getParcelableExtra("data");
		imgBA.setImageBitmap(bitmap);
		btnOk.setEnabled(true);
		
		File file = new File( Environment.getExternalStorageDirectory() + 
				"/"+ getResources().getString(R.string.app_name)+"/BASearch_Image");
		file.mkdirs();
		final File fileOutput = new File(file, "BSI_"+System.currentTimeMillis()+".JPEG");
	    FileOutputStream fileOutputStream=null;
		try 
		{
			fileOutputStream = new FileOutputStream(fileOutput);
			if(fileOutputStream != null)
			{   	            		
		      	bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream );
		      	fileOutputStream.flush();
		      	fileOutputStream.close();	            		
			}	
			MediaStore.Images.Media.insertImage(getContentResolver(),fileOutput.getAbsolutePath(),fileOutput.getName(),fileOutput.getName());
			strFilePath=fileOutput.getAbsolutePath();	
		}
		catch (FileNotFoundException e)
		{
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearch~storeCapimg~error"+e.toString(),true);
			Log.i("FileNotFoundException",e.toString());
		} catch (IOException e) 
		{
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearch~storeCapimg~error"+e.toString(),true);
			Log.i("IOException",e.toString());
		}
		fileOutputStream=null;
		file=null;
		bitmap=null;
	}
	//Store image path in local database.
	private void storeData()
	{
		if(strFilePath!=null)
		{
			String strLogin=getResources().getString(R.string.login_table);
			String strMainSection=getResources().getString(R.string.mainsection_table);
			String strSection=getResources().getString(R.string.subsection_table);
			String strAuditData=getResources().getString(R.string.auditdata_table);
			OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
			database.create(strLogin,strMainSection,strSection,strAuditData);
			ContentValues contentvalues=new ContentValues();
			contentvalues.put("bsipath", strFilePath);
			database.update(contentvalues, strAuditData, "status=?", new String[]{"0"});
			database.close();
			strLogin=null;
			strMainSection=null;
			strSection=null;
			strAuditData=null;
			strFilePath=null;
		}
	}
	@Override
	protected void onDestroy() 
	{
		if(locationManager!=null)locationManager.removeUpdates(BASearch.this);
		strSearchBA=null;
		btnLookup=null;
		btnBack=null;
		btnAudit=null;
		btnOk=null;
		btnCancel=null;
		handler=null;
		edttxtBASearch=null;
		prgDialog=null;
		btnCaptImg=null;
		imgBA=null;
		relativeLayoutBg=null;
		relativeLayoutBASign=null;
		strAddress=null;
		locationManager=null;
		location=null;
		if(handlerTime!=null)handlerTime.removeCallbacks(runnable);
		super.onDestroy();
	}
	@Override
	public void onProviderDisabled(String provider) 
	{}
	@Override
	public void onProviderEnabled(String provider) 
	{}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) 
	{}
}