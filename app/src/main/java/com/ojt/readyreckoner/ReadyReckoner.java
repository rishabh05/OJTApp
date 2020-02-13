/*@ID: CN20140001
 *@Description: srcReadyReckoner is for ReadyReckoner search Screen 
 * This class is used to search particular BA's information with ReadyReckoner data
 * based on store code or counter name
 * Content are fetched from server.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 21/03/2014
 * @Modified Date: 28/08/2014
 */
package com.ojt.readyreckoner;

import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ojt.connectivity.JSONParser;
import com.ojt.database.OJTDAO;
import com.ojt.home.Home;
import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.utilities.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
public class ReadyReckoner extends Activity implements OnClickListener
{
	private Button btnBack=null,btnLookUp=null;
	private ProgressDialog prgDialog=null;
	private Handler handler=null;
	private Intent intent=null;
	private String strRecData=null;
	private RelativeLayout relativeLayoutBg=null;
	private Handler handlerTime=null;
	private Runnable runnable=null;
	private EditText edttxtReadyRec=null;  
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.readyreckoner);
		Utility.context=this;
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		edttxtReadyRec=(EditText) findViewById(R.id.readyrecactxt);
        
		btnBack=(Button) findViewById(R.id.backbtn);
		btnBack.setOnClickListener(this); 
		btnLookUp=(Button) findViewById(R.id.lookupbtn);
		btnLookUp.setOnClickListener(this);
		
		handler= new Handler() 
		{
			@Override
			public void handleMessage(Message msg) 
			{
				if(prgDialog!=null)prgDialog.dismiss();
				switch(msg.getData().getInt("status"))
				{
					case 1:
						intent=new Intent(ReadyReckoner.this,BAList.class); 
						startActivity(intent);
						ReadyReckoner.this.finish();
						break;
					case 2:
						Utility.alert(getResources().getString(R.string.invalid_readyrec));
						break;
					case 3:
						Utility.alert(getResources().getString(R.string.server_error));
						break;
					case 4:
						Utility.alert(getResources().getString(R.string.no_BA));
						break;
					case 5:
						Utility.alert(getResources().getString(R.string.low_network));
						break;
				}
			}
		 };
		 relativeLayoutBg=(RelativeLayout) findViewById(R.id.layoutbg);
			// Handle the soft keyboard 
		 relativeLayoutBg.setOnTouchListener(new OnTouchListener(){
				@Override
				public boolean onTouch(View v, MotionEvent event) 
				{
					hideKeyboard(v);
					return false;
				}
			});
			timeLimit();
	}
	//Store data while minimize the screen
	@Override
	protected void onPause() {
		Utility.setLastActivity(false,Utility.strAuName);
		super.onPause();
	}
	//Handle soft keyboard
	private void hideKeyboard(View v)
	{
		if(v instanceof EditText)
			 ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(v,InputMethodManager.SHOW_FORCED);
		else
		{
			((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
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
				final Intent mainIntent = new Intent(ReadyReckoner.this, Login.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ReadyReckoner.this.startActivity(mainIntent);
				ReadyReckoner.this.finish();
			}
		};
		handlerTime.postDelayed(runnable, (Utility.intTimeout*1000));
	}
	@Override
	public void onClick(View v) 
	{
		if(v==btnBack)//Back. Move to Home screen
		{
			back();
		}
		else if(v==btnLookUp)//Look up. Get all BA details based on Store code or Counter name.
		{
			hideKeyboard(v);
			strRecData=edttxtReadyRec.getText().toString();
			if(strRecData.length()!=0)
			{
				strRecData=strRecData.replace(" ","+");
				getData();
			}
			else
			{
				Utility.alert(getResources().getString(R.string.empty_readyrec));
			}
		}
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
		startActivity(new Intent(ReadyReckoner.this,Home.class));
		ReadyReckoner.this.finish();
	}
	//Fetch BA's information from server
	private void getData()
	{
		if(Utility.hasConnection())
		{
			prgDialog=ProgressDialog.show(Utility.context,Utility.context.getResources().getString(R.string.app_name),Utility.context.getResources().getString(R.string.loading_text), true);
			new Thread(new Runnable() 
			{
				String strSubJobj=null;
				String strLogin=null,strMainSection=null,strSection=null,strAuditData=null;
				OJTDAO database=null;
				
				public void run() 
				{
					try
					{
						strLogin=getResources().getString(R.string.login_table);
						strMainSection=getResources().getString(R.string.mainsection_table);
						strSection=getResources().getString(R.string.subsection_table);
						strAuditData=getResources().getString(R.string.auditdata_table);
						database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
						database.create(strLogin,strMainSection,strSection,strAuditData);
						database.delete("readyreckoner1");
						String strBAName=null,strID=null,strDetailsID=null;
						ContentValues contentValues=null;
						
						strRecData=URLEncoder.encode(strRecData,"UTF-8");
						JSONObject jobj=JSONParser.connect(getResources().getString(R.string.server_url)+"StoreSearch?lookup="+strRecData,ReadyReckoner.this);
						Log.i("Response",jobj.toString());
						JSONArray jArray = null;
						if(jobj.has("BAList")) 
						{
							jArray=jobj.getJSONArray("BAList");
							strSubJobj=jArray.get(0).toString();
							strSubJobj=strSubJobj.replace("=", "\":\"");
							strSubJobj=strSubJobj.replace(", ", "\",\"");
							strSubJobj=strSubJobj.replace("{", "{\"");
							strSubJobj=strSubJobj.replace("}", "\"}");
							JSONObject subJObj=new JSONObject(strSubJobj);
							if(subJObj.has("Status"))
							{
								if(subJObj.getString("Status").equalsIgnoreCase("1"))
								{
									for(int i=0;i<jArray.length();i++)
									{
										strSubJobj=jArray.get(i).toString();
										strSubJobj=strSubJobj.replace("=", "\":\"");
										strSubJobj=strSubJobj.replace(", ", "\",\"");
										strSubJobj=strSubJobj.replace("{", "{\"");
										strSubJobj=strSubJobj.replace("}", "\"}");
										subJObj=new JSONObject(strSubJobj);
										strBAName=subJObj.getString("BAName").replace("+", " ");
										strID=subJObj.getString("BAPbid").replace("+", " ");
										strDetailsID=subJObj.getString("BADetailsid").replace("+", " ");
										contentValues=new ContentValues();
										contentValues.put("pbid", strID);
										contentValues.put("name", strBAName);
										contentValues.put("detailsid", strDetailsID);
										database.insert(contentValues, "readyreckoner1");
									}
									Message msgobj;
									msgobj = handler.obtainMessage();
								    Bundle bundle = new Bundle();
								    bundle.putInt("status", 1);
								    msgobj.setData(bundle);
								    handler.sendMessage(msgobj);
								}
								else if(subJObj.getString("Status").equalsIgnoreCase("2"))
								{
									Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~ReadyReckoner~getData"+"~strRecData:"+strRecData+" no ba found "+jobj.toString(),true);
									Message msgobj;
									msgobj = handler.obtainMessage();
								    Bundle bundle = new Bundle();
								    bundle.putInt("status", 4);
								    msgobj.setData(bundle);
								    handler.sendMessage(msgobj);
								}
								else//Invalid storecode or countername
								{
									Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~ReadyReckoner~getData"+"~strRecData:"+strRecData+" invalid "+jobj.toString(),true);
									Message msgobj;
									msgobj = handler.obtainMessage();
								    Bundle bundle = new Bundle();
								    bundle.putInt("status", 2);
								    msgobj.setData(bundle);
								    handler.sendMessage(msgobj);
								}
							}
							else
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~ReadyReckoner~getData"+"~strRecData:"+strRecData+" no status "+jobj.toString(),true);
								Message msgobj;
								msgobj = handler.obtainMessage();
							    Bundle bundle = new Bundle();
							    bundle.putInt("status", 3);
							    msgobj.setData(bundle);
							    handler.sendMessage(msgobj);
							}
						}
						else
						{
							if(jobj.has("Error"))
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~ReadyReckoner~getData"+"~strRecData:"+strRecData+" timeout error "+jobj.toString(),true);
								Message msgobj;
								msgobj = handler.obtainMessage();
							    Bundle bundle = new Bundle();
							    bundle.putInt("status", 5);
							    msgobj.setData(bundle);
							    handler.sendMessage(msgobj);
							}
							else
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~ReadyReckoner~getData"+"~strRecData:"+strRecData+" empty status "+jobj.toString(),true);
								Message msgobj;
								msgobj = handler.obtainMessage();
							    Bundle bundle = new Bundle();
							    bundle.putInt("status", 3);
							    msgobj.setData(bundle);
							    handler.sendMessage(msgobj);
							}	
						}
					}
					catch(Exception e)
					{
						Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~ReadyReckoner~getData"+"~strRecData:"+strRecData+" error "+e.toString(),true);
						Log.i("Exception:",e.toString());
						Message msgobj;
						msgobj = handler.obtainMessage();
					    Bundle bundle = new Bundle();
					    bundle.putInt("status", 3);
					    msgobj.setData(bundle);
					    handler.sendMessage(msgobj);
					}
					finally
					{
						if(database!=null)database.close();
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
		btnBack=null;
		btnLookUp=null;
		prgDialog=null;
		handler=null;
		intent=null;
		strRecData=null;
		relativeLayoutBg=null;
		handlerTime=null;
		runnable=null;
		super.onDestroy();
	}
}