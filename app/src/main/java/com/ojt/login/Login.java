/*@ID: CN20140001
 *@Description: srcLogin is for Auditor login screen
 * This class send username and password to server for check whether user is authorized or not.
 * Server returns 1 for Authenticated and 0 for invalid user.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 10/03/2014
 * @Modified Date: 26/08/2014
 */
package com.ojt.login;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.ojt.home.Home;
import com.ojt.notification.R;
import com.ojt.database.OJTDAO;
import com.ojt.connectivity.JSONParser;
import com.ojt.utilities.Utility;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Login extends Activity 
{
	private EditText edttxtUser=null,edttxtPwd=null;
	private Button btnLogin=null;
	private Handler handlerLogin=null;
	private OJTDAO database=null;
	private ProgressDialog prgDialog=null;
	private RelativeLayout relativeLayoutBg=null;
	private static String strUserName=null;
	private static String strPassword=null;
	private String strRegId="";
	private SharedPreferences sharedPreferences,preference;
	private String strSubJobj=null;
	private String strPhoneModel=null;
	private String strAndroidVersion=null;
	private String strLog=null;
	private String strFilePath="";
	private String strContentid[]=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		Fabric.with(this, new Crashlytics());
		setContentView(R.layout.login);
		Utility.context=this;
		preference = getSharedPreferences("OJTSession", MODE_PRIVATE);
        //Set current date and time 
		android.provider.Settings.System.putInt(getContentResolver(), 
		android.provider.Settings.Global.AUTO_TIME, 1);
		Utility.logFile("Login screen -> oncreate()", true);
		//To check device memory
		if(Utility.getFreeSize()<200)
		{
			AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		    alertDialog.setTitle(getResources().getString(R.string.app_name));
	        alertDialog.setMessage(getResources().getString(R.string.insufficent_memory));
	        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	            public void onClick(DialogInterface dialog,int which) {
	               Activity act=(Activity) Utility.context;
	               act.finish();
	               return;
	            }
	        });
	        alertDialog.show();
		}
		else
		{
			strPhoneModel = android.os.Build.MODEL;
			strAndroidVersion = android.os.Build.VERSION.RELEASE;
			sharedPreferences=getSharedPreferences("OJTPref", 0);
		    strPhoneModel=strPhoneModel.replace(" ","+");
		    strAndroidVersion=strAndroidVersion.replace(" ","+");
		    this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		    	
			loginUIDef();
			if(!preference.getBoolean("isDelete", false))
			{
				//deleteFile();
			}
			handlerLogin= new Handler() 
			{
				@Override
				public void handleMessage(Message msg) 
				{
					if(prgDialog!=null)prgDialog.dismiss();
					switch(msg.getData().getInt("status"))
					{
						case 1:
							String strUser=preference.getString("User", "");
							if(strUser!=null)
							{
								if(strUser.equalsIgnoreCase(Utility.strAuName))
								{
									Class<?> activityClass;
							        try 
							        {
							            activityClass = Class.forName(
							            		preference.getString("lastActivity", Home.class.getName()));
							        } catch(ClassNotFoundException ex) {
							            activityClass = Home.class;
							        }
							        startActivity(new Intent(Login.this, activityClass));
							        Login.this.finish();
								}
								else
								{
									startActivity(new Intent(Login.this, Home.class));
							        Login.this.finish();
								}
							}
							else
							{
								startActivity(new Intent(Login.this, Home.class));
						        Login.this.finish();
							}
							break;

						case 2:
							Utility.alert(getResources().getString(R.string.invalid_user_pwd));
							break;

						case 3:
							Utility.alert(getResources().getString(R.string.server_error));
							break;

						case 4:
							Utility.alert(getResources().getString(R.string.low_network));
							break;

						case 5:
							//Utility.alert(getResources().getString(R.string.device_unregister));
							break;

						default:
							Utility.alert("Login Error = "+msg.getData().get("status"));
							break;

					}
				}
			 };
			
			registerDeviceID();
			strFilePath=Environment.getExternalStorageDirectory() + "/" +
					Utility.context.getResources().getString(R.string.app_name)+"/Log_File/logfile.txt";
			File file=new File(strFilePath);
			if(file.exists())
			{
				try
				{
					strLog=Utility.getStringFromFile(strFilePath);
				} catch (Exception e) 
				{
					Log.i("Exception",e.toString());
				}
				if(strLog!=null)
				{
					if(strLog.length()>0&&Utility.hasConnection()) new LogFile().execute();
				}
			}
			
		}
	}
	private boolean deleteDirectory(File path)
	{
	    if(path.exists())
	    {
	      File[] files = path.listFiles();
	      if (files == null) 
	      {
	          return true;
	      }
	      for(int i=0; i<files.length; i++) 
	      {
	         if(files[i].isDirectory()) 
	         {
	           deleteDirectory(files[i]);
	         }
	         else
	         {
	           files[i].delete();
	         }
	      }
	    }
	    return( path.delete() );
	}
	private void deleteFile() {
		try
		{
			String strFilePath=Environment.getExternalStorageDirectory() + "/" +
					getResources().getString(R.string.app_name);
			File file=new File(strFilePath);
			if(deleteDirectory(file))
			{
					Editor editor=preference.edit();
					editor.putBoolean("isDelete", true);
					editor.commit();
			}
		}
		catch(Exception e)
		{
			Log.i("Exception",e.toString());
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~Login~deleteFile"+"~Error "+e.toString(),true);
		}
	}
	//Register the device on server
	private void registerDeviceID()
	{
		if(!sharedPreferences.getBoolean("checkInstall", false))
		{
			if(Utility.hasConnection())
			{
				new RegisterDeviceID().execute();
			}
			else
			{
				AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
			    alertDialog.setTitle(getResources().getString(R.string.app_name));
		        alertDialog.setMessage(getResources().getString(R.string.no_network));
		        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog,int which) {
		               Activity act=(Activity) Utility.context;
		               act.finish();
		            }
		        });
		        alertDialog.show();
			}
		}
	}
	private void loginUIDef(){
		//User Name
		edttxtUser=(EditText)findViewById(R.id.usernameedttext);
		edttxtUser.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				edttxtUser.setCursorVisible(true);
				return false;
			}
		});
		//Password
		edttxtPwd=(EditText)findViewById(R.id.passwordedttext);
		//Login
		btnLogin=(Button)findViewById(R.id.loginbtn);
		btnLogin.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
				login();
			}
		});
		relativeLayoutBg=(RelativeLayout) findViewById(R.id.layoutbg);
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
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		//newConfig.
		setContentView(R.layout.login);
		loginUIDef();
		edttxtUser.setText(strUserName);
		edttxtPwd.setText(strPassword);
		edttxtUser.setCursorVisible(false);
		super.onConfigurationChanged(newConfig);
	}

	public void forceCrash() {
		throw new RuntimeException("This is a crash");
	}

	//  This method for login credential
	private void login() 
	{

	//	forceCrash();
		Utility.logFile("Login sscreen -> submit() click"
				,true);

		final String strLogin,strMainSection, strSection, strAuditData;
		
		strUserName=edttxtUser.getText().toString();
		strPassword=edttxtPwd.getText().toString();
		if(strUserName.length()==0)
		{
			Utility.alert(getResources().getString(R.string.empty_user));
		}
		else if(strPassword.length()==0)
		{
			Utility.alert(getResources().getString(R.string.empty_pwd));
		}
		else
		{
			strUserName=strUserName.replace(" ","+");
			strPassword=strPassword.replace(" ","+");
			if(Utility.hasConnection())
			{
				strLogin=getResources().getString(R.string.login_table);
				strMainSection=getResources().getString(R.string.mainsection_table);
				strSection=getResources().getString(R.string.subsection_table);
				strAuditData=getResources().getString(R.string.auditdata_table);
				database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
				database.create(strLogin,strMainSection,strSection,strAuditData);
				database.delete(strLogin);
				prgDialog=ProgressDialog.show(Utility.context,Utility.context.getResources().getString(R.string.app_name),Utility.context.getResources().getString(R.string.loading_text), true);
				new Thread(new Runnable() 
				{
					@Override
					public void run() 
					{
						try
						{
							JSONObject jobj=JSONParser.connect(getResources().getString(R.string.server_url)+"AuditorLogin?auid="+strUserName
									+"&pwd="+strPassword, Login.this);
							Log.i("Response",jobj.toString());
							if(jobj.has("Status"))
							{
								if(jobj.getString("Status").equalsIgnoreCase("1"))
								{
									if(jobj.has("AUDetails"))
									{
										Utility.logFile("=========================================== \n"
												+ ""+Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName
												+"~Login~login"+"~auid:"+strUserName+" pwd:"+strPassword+"~success:get auditor details."
												,true);
										JSONArray jarray=jobj.getJSONArray("AUDetails");
										JSONObject subJobj;
										ContentValues contentvalues;
										for(int i=0;i<jarray.length();i++)
										{
											subJobj=new JSONObject(jarray.get(i).toString()); 
											Utility.strAuName=subJobj.getString("AuName");
											Utility.strAurecID=subJobj.getString("RecoredId");
											Utility.intTimeout=Integer.parseInt(subJobj.getString("Timeout"));
											contentvalues=new ContentValues();
											contentvalues.put("user", strUserName);
											contentvalues.put("password", strPassword);
											contentvalues.put("auditname", Utility.strAuName);
											contentvalues.put("recid", Utility.strAurecID);
											contentvalues.put("timeout", Utility.intTimeout);
											contentvalues.put("baname", "");
											contentvalues.put("baid", "");
											database.insert(contentvalues, strLogin);
										}
										if(jobj.has("TrainingContent"))
										{
											JSONArray jArray=jobj.getJSONArray("TrainingContent");
											strContentid=new String[jArray.length()];
											String strContentID,strFileType,strThumbURL,strQuery,strContentURL;
											SQLiteDatabase sqldb;
											Cursor cursor;
											for(int i=0;i<jArray.length();i++)
											{
												strSubJobj=jArray.get(i).toString();
												strSubJobj=strSubJobj.replace("=", "\":\"");
												strSubJobj=strSubJobj.replace(", ", "\",\"");
												strSubJobj=strSubJobj.replace("{", "{\"");
												strSubJobj=strSubJobj.replace("}", "\"}");
												subJobj=new JSONObject(strSubJobj); 
												
												strContentID=""+subJobj.getString("contentid").replaceAll("~", ",");
												strContentid[i]=strContentID;
												
												cursor=database.getVal("msgid=?", new String[]{strContentID}, "notification");
												if(cursor.getCount()==0)//check already training content present or not
												{
													strFileType=""+subJobj.getString("content_type").replaceAll("~", ",");
													//Thumbimage url
													strThumbURL=subJobj.getString("thumbnail_path").replaceAll("~", ",");
													strThumbURL=URLEncoder.encode(strThumbURL, "UTF-8");
													strThumbURL=getResources().getString(R.string.server_url)+"ContentDownloadServlet?content="+strThumbURL;
													//Content url
													strContentURL=subJobj.getString("contentpath").replaceAll("~", ",");
													strContentURL=URLEncoder.encode(strContentURL, "UTF-8");
													strContentURL=getResources().getString(R.string.server_url)+"ContentDownloadServlet?content="+strContentURL;
													
													contentvalues = new ContentValues();
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
												cursor.close();
											}
											if(strContentid.length!=0)
											{
												strQuery = "UPDATE notification set state=2"
													    + " WHERE msgid NOT IN (" + Utility.makePlaceholders(strContentid.length) + ")";
													
												sqldb=openOrCreateDatabase(getResources().getString(R.string.db_name), MODE_PRIVATE,null);
												cursor=sqldb.rawQuery(strQuery, strContentid);
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
											String strQuery = "UPDATE notification set state=2";
											SQLiteDatabase sqldb=openOrCreateDatabase(getResources().getString(R.string.db_name), MODE_PRIVATE,null);
											Cursor cursor=sqldb.rawQuery(strQuery, null);
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
										database.close();
										Message msgobj;
										msgobj = handlerLogin.obtainMessage();
									    Bundle bundle = new Bundle();
									    bundle.putInt("status", 1);
									    msgobj.setData(bundle);
									    handlerLogin.sendMessage(msgobj);
									}
									else
									{
										Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName
												+"~Login~login"+"~auid:"+strUserName+" pwd:"+strPassword+"~empty auditor details. "+jobj.toString(),true);
										Message msgobj;
										msgobj = handlerLogin.obtainMessage();
									    Bundle bundle = new Bundle();
									    bundle.putInt("status", 3);
									    msgobj.setData(bundle);
									    handlerLogin.sendMessage(msgobj);
									}
								}
								else
								{
									//invalid user
									 Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName
											 +"~Login~login"+"~auid:"+strUserName+" pwd:"+strPassword+"~invalid user. "+jobj.toString(),true);
									 Message msgobj;
									 msgobj = handlerLogin.obtainMessage();
								     Bundle bundle = new Bundle();
								     bundle.putInt("status", 2);
								     msgobj.setData(bundle);
								     handlerLogin.sendMessage(msgobj);
								}
							}
							else
							{
								if(jobj.has("Error"))
								{
									Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName
											+"~Login~login"+"~auid:"+strUserName+" pwd:"+strPassword+"~login timeout. "+jobj.toString(),true);
									Message msgobj;
									msgobj = handlerLogin.obtainMessage();
								    Bundle bundle = new Bundle();
								    bundle.putInt("status", 4);
								    msgobj.setData(bundle);
								    handlerLogin.sendMessage(msgobj);
								}
								else
								{	
									Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName
											+"~Login~login"+"~auid:"+strUserName+" pwd:"+strPassword+"~empty status. "+jobj.toString(),true);
									Message msgobj;
									msgobj = handlerLogin.obtainMessage();
								    Bundle bundle = new Bundle();
								    bundle.putInt("status", 3);
								    msgobj.setData(bundle);
								    handlerLogin.sendMessage(msgobj);
								}
							}
						}
						catch(Exception e)
						{
							Log.i("Exception:",e.toString());
							Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName
									+"~Login~login"+"~auid:"+strUserName+" pwd:"+strPassword+"~error:"+e.toString(),true);
							if(handlerLogin==null) return;
							Message msgobj;
							msgobj = handlerLogin.obtainMessage();
						    Bundle bundle = new Bundle();
						    bundle.putInt("status", 3);
						    msgobj.setData(bundle);
						    handlerLogin.sendMessage(msgobj);
						}		
					}
				}).start();
			}
			else
			{
				Utility.alert(getResources().getString(R.string.no_network));
			}
		}
	}
	//Register the device on server
	private boolean gcmRegister()
	{
		boolean check=false;
		if (strRegId.equalsIgnoreCase("")) 
		{
			try {
				GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(Login.this);
				gcm.register(Utility.SENDER_ID);
				strRegId = GCMRegistrar.getRegistrationId(Login.this);
				check=false;
			} catch (IOException e) {
				Log.i("Exception",e.toString());
			}
		}
		else
		{
			check=true;
		}
		return check;
	}
	//Back event
	@Override
	public void onBackPressed() 
	{
		Utility.context=null;
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_HOME);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		finish();
		System.exit(0);
		super.onBackPressed();
	}
	//Update the log information to server
	class LogFile extends AsyncTask<String, Void, String> 
	{
		private JSONObject jObj=new JSONObject();
		public LogFile() 
		{}
		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
		}
		protected String doInBackground(String... urls) 
		{
			
			try {
				jObj = JSONParser.updateLog(getResources().getString(R.string.server_url)+
						"ClientApplicationLog",strPhoneModel.replace(" ","+")+"+"+strAndroidVersion.replace(" ","+")
						,strLog.replace(" " ,"+"));
			} catch (NotFoundException e) {
				Log.i("NotFoundException",e.toString());
			} catch (Exception e) {
				Log.i("Exception",e.toString());
			}
			return jObj.toString();
		}
		protected void onPostExecute(final String strResult)
		{
			Log.i("Response on postExecute",""+strResult);
		}
	}
	//Register the device on server
	class RegisterDeviceID extends AsyncTask<String, Void, String> 
	{
		boolean check=false;
		long startTime;
		int intVersion;
		public RegisterDeviceID() 
		{
			try 
			{
				intVersion=getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			} catch (NameNotFoundException e)
			{
				Log.i("NameNotFoundException", e.toString());
				Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~Login~RegisterDeviceID"+"~NameNotFoundException: "+e.toString(),true);
			}
		}
		@Override
		protected void onPreExecute() 
		{
			super.onPreExecute();
			startTime=System.currentTimeMillis();
		}
		protected String doInBackground(String... urls) 
		{
			strRegId = GCMRegistrar.getRegistrationId(Login.this);
			if (strRegId.equalsIgnoreCase("")) 
			{
				do
				{
					check=gcmRegister();
				}while(!check&&(System.currentTimeMillis()-startTime)<=20000);
			}
			return strRegId;
		}
		protected void onPostExecute(final String strResult)
		{
			Log.i("Response on postExecute",strResult);
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName
					+"~Login~postexcute"+"~deviceid:"+strResult,true);
			if(strResult != null && strResult.length()==0)
			{
				Utility.logFile("	 Unable to register the device on server	",true);
				Utility.alert(getResources().getString(R.string.device_unregister));
			}
			else
			{
				new Thread(new Runnable()
				{
					@Override
					public void run() 
					{
						JSONObject jobj;
						try 
						{
							jobj = JSONParser.connect(getResources().getString(R.string.server_url)+"DeviceRegistration?deviceid="+strResult+"&model="
									+strPhoneModel+"&version="+strAndroidVersion+"&appversion="+intVersion, Login.this);
							//Log.i("Response",jobj.toString());
							if(jobj == null){
								Utility.logFile("	Login-> jobj == null"+jobj.toString(),true);
								return;
							}
							Utility.logFile("	 DeviceRegistration	Response:	"+jobj.toString(),true);
							if(jobj.has("Status"))
							{
								if(jobj.getString("Status").equalsIgnoreCase("1"))
								{
									Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+
											Utility.strAuName+"~Login~postexcute"+"~success:deviceid registered on server:status 1",
											true);
									Editor editor=sharedPreferences.edit();
									editor.putBoolean("checkInstall", true);
									editor.commit();
								}
								else
								{
									Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName
											+"~Login~postexcute"+"~device id registration:invalid status "+jobj.toString(),true);
									if(handlerLogin==null) return;
									Message msgobj;
									msgobj = handlerLogin.obtainMessage();
								    Bundle bundle = new Bundle();
								    bundle.putInt("status", 5);
								    msgobj.setData(bundle);
								    handlerLogin.sendMessage(msgobj);
								}
							}
							else
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName
										+"~Login~postexcute"+"~device id registration:empty status "+jobj.toString(),true);
								if(handlerLogin==null) return;
								Message msgobj;
								msgobj = handlerLogin.obtainMessage();
							    Bundle bundle = new Bundle();
							    bundle.putInt("status", 5);
							    msgobj.setData(bundle);
							    handlerLogin.sendMessage(msgobj);
							}
						} 
						catch (NotFoundException e)
						{
							Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName
									+"~Login~postexcute"+"~device id registration:error "+e.toString(),true);
							Log.i("NotFoundException",e.toString());
							if(handlerLogin==null) return;
							Message msgobj;
							msgobj = handlerLogin.obtainMessage();
						    Bundle bundle = new Bundle();
						    bundle.putInt("status", 5);
						    msgobj.setData(bundle);
						    handlerLogin.sendMessage(msgobj);
						} 
						catch (Exception e)
						{
							Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName
									+"~Login~postexcute"+"~device id registration:error "+e.toString(),true);
							Log.i("JSONException",e.toString());
							if(handlerLogin==null) return;
							Message msgobj;
							msgobj = handlerLogin.obtainMessage();
						    Bundle bundle = new Bundle();
						    bundle.putInt("status", 5);
						    msgobj.setData(bundle);
						    handlerLogin.sendMessage(msgobj);
						} 
					}
				}).start();
			}
		}
	}
	@Override
	protected void onDestroy() 
	{
		new RegisterDeviceID().cancel(true);
		new LogFile().cancel(true);
		edttxtUser=null;
		edttxtPwd=null;
		btnLogin=null;
		handlerLogin=null;
		prgDialog=null;
		relativeLayoutBg=null;
		strUserName=null;
		strPassword=null;
		super.onDestroy();
	}
}