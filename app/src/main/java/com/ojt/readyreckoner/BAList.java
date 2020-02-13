/*@ID: CN20140001
 *@Description: srcBAList is for BAs Screen 
 * This class is used to list out BAs 
 * based on store code or counter name
 * Content are fetched from server.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 21/03/2014
 * @Modified Date: 26/08/2014
 */
package com.ojt.readyreckoner;

import java.util.ArrayList;

import org.json.JSONObject;

import com.ojt.connectivity.JSONParser;
import com.ojt.database.OJTDAO;
import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.utilities.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class BAList extends Activity 
{
	private ArrayList<RowItem> listBAs=null,arrayListBA=null;
	private ListView listView=null;
	private ProgressDialog prgDialog=null;
	private Handler handler=null;
	private Intent intent=null;
	private String strDetailsID=null;
	private Button btnBack=null;
	private Handler handlerTime=null;
	private Runnable runnable=null;
	private EditText edttxtSearch=null;
	private CustomListAdapter adapter=null ;
	private TextView txtNoBA=null;
	protected boolean check;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.balist);
		Utility.context=this;
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		edttxtSearch=(EditText) findViewById(R.id.searchedttxt);
		txtNoBA=(TextView) findViewById(R.id.nobatxt);
		btnBack=(Button) findViewById(R.id.backbtn);
		btnBack.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				back();
			}
		});
		listBAs = new ArrayList<RowItem>();
		listView=(ListView) findViewById(R.id.balistView);
		handler= new Handler() 
		{
			@Override
			public void handleMessage(Message msg) 
			{
				if(prgDialog!=null)prgDialog.dismiss();
				switch(msg.getData().getInt("status"))
				{
					case 1:
						intent=new Intent(BAList.this,ReadyReckonerData.class); 
						startActivity(intent);
						BAList.this.finish();
						break;
					case 2:
						Utility.alert(getResources().getString(R.string.server_error));
						break;
					case 3:
						Utility.alert(getResources().getString(R.string.low_network));
						break;
				}
			}
		 };
		getBAList();
		adapter = new CustomListAdapter(this,R.layout.listrow, listBAs);
		arrayListBA=listBAs;
		listView.setAdapter(adapter);
		if(adapter.getCount()!=0)
		{
        	txtNoBA.setVisibility(View.INVISIBLE);
        	edttxtSearch.setVisibility(View.VISIBLE);
		}
		else
		{
			txtNoBA.setVisibility(View.VISIBLE);
			edttxtSearch.setVisibility(View.INVISIBLE);
		}
		listView.setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) 
			{
				RowItem rowItem=arrayListBA.get(arg2);
				strDetailsID=rowItem.getDetailID();
				getData();
			}
		});
		//Search option
		 edttxtSearch.addTextChangedListener(new TextWatcher() 
		 {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				ArrayList<RowItem> tempArrayList = new ArrayList<RowItem>();
		        for(RowItem rowItem: listBAs)
		        {
		           check=false;
		           if(rowItem.getName()!=null)
		           {
		        	   if (s.length() <= rowItem.getName().length()) 
			           {
		        		   if (rowItem.getName().toLowerCase().contains(s.toString().toLowerCase())) 
				           {
		        			   tempArrayList.add(rowItem);
		        			   check=true;
				           }
		               }
		           }
		           if(check==true)continue;
		           if(rowItem.getID()!=null)
		           {
		        	   if (s.length() <= rowItem.getID().length()) 
			           {
		        		   if (rowItem.getID().toLowerCase().contains(s.toString().toLowerCase())) 
				           {
		        			   tempArrayList.add(rowItem);
				           }
		               }
		           }
		        }
		        arrayListBA=tempArrayList;
		        adapter = new CustomListAdapter(BAList.this,R.layout.listrow,tempArrayList);
				listView.setAdapter(adapter);
				
		        if(adapter.getCount()!=0)
				{
		        	txtNoBA.setVisibility(View.INVISIBLE);
				}
				else
				{
					txtNoBA.setVisibility(View.VISIBLE);
				}
		  	
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		}); 
		timeLimit();
	}
	//Store data while minimize the screen
	@Override
	protected void onPause() 
	{
		Utility.setLastActivity(false,Utility.strAuName);
		super.onPause();
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
				final Intent mainIntent = new Intent(BAList.this, Login.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				BAList.this.startActivity(mainIntent);
				BAList.this.finish();
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
	//Back to ReadyReckoner Screen
	private void back() 
	{
		startActivity(new Intent(BAList.this,ReadyReckoner.class));
		BAList.this.finish();
	}
	//Get particular BA's information
	protected void getData() 
	{
		if(Utility.hasConnection())
		{
			prgDialog=ProgressDialog.show(Utility.context,Utility.context.getResources().getString(R.string.app_name),Utility.context.getResources().getString(R.string.loading_text), true);
			new Thread(new Runnable() 
			{
				public void run() 
				{
					try
					{
						JSONObject jobj=JSONParser.connect(getResources().getString(R.string.server_url)+"ReadyReckoner?baid="+strDetailsID,BAList.this);
						Log.i("Response",jobj.toString());
						if(jobj.has("BAInfo")) 
						{
							String strLogin=getResources().getString(R.string.login_table);
							String strMainSection=getResources().getString(R.string.mainsection_table);
							String strSection=getResources().getString(R.string.subsection_table);
							String strAuditData=getResources().getString(R.string.auditdata_table);
							OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
							database.create(strLogin,strMainSection,strSection,strAuditData);
							database.delete("readyreckoner_bainfo");
							ContentValues contentValues=null;
							contentValues=new ContentValues();
							contentValues.put("bainfo", jobj.getJSONArray("BAInfo").toString());
							if(jobj.has("ReadyReckonerInfo"))
							{
								contentValues.put("readyreckonerinfo", jobj.getJSONArray("ReadyReckonerInfo").toString());
							}
							else
							{
								contentValues.put("readyreckonerinfo", "");
							}
							if(jobj.has("LastMainscore"))
							{
								contentValues.put("lastmainscore", jobj.getJSONArray("LastMainscore").toString());
							}
							else
							{
								contentValues.put("lastmainscore", "");
							}
							if(jobj.has("LastSubscore"))
							{
								contentValues.put("lastsubscore", jobj.getJSONArray("LastSubscore").toString());
							}
							else
							{
								contentValues.put("lastsubscore","");
							}
							if(jobj.has("LastAuditReport"))
							{
								contentValues.put("lastauditreport", jobj.getJSONArray("LastAuditReport").toString());
							}
							else
							{
								contentValues.put("lastauditreport", "");
							}
							database.insert(contentValues, "readyreckoner_bainfo");
							database.close();
							Message msgobj;
							msgobj = handler.obtainMessage();
						    Bundle bundle = new Bundle();
						    bundle.putInt("status", 1);
						    msgobj.setData(bundle);
						    handler.sendMessage(msgobj);
						}
						else
						{
							if(jobj.has("Error"))
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAList~getData"+"~strDetailsID:"+strDetailsID+" timeout error:"+jobj.toString(),true);
								Message msgobj;
								msgobj = handler.obtainMessage();
							    Bundle bundle = new Bundle();
							    bundle.putInt("status", 3);
							    msgobj.setData(bundle);
							    handler.sendMessage(msgobj);
							}
							else
							{
								Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAList~getData"+"~strDetailsID:"+strDetailsID+" empty response:"+jobj.toString(),true);
								Message msgobj;
								msgobj = handler.obtainMessage();
							    Bundle bundle = new Bundle();
							    bundle.putInt("status", 2);
							    msgobj.setData(bundle);
							    handler.sendMessage(msgobj);
							}	
						}
					}
					catch(Exception e)
					{
						Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAList~getData"+"~strDetailsID:"+strDetailsID+" error:"+e.toString(),true);
						Log.i("Exception:",e.toString());
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
		else
		{
			Utility.alert(getResources().getString(R.string.no_network));
		}
	}
	//Get all BA details based on particular Store code or Counter name
	private void getBAList()
	{
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursor=database.getAll("readyreckoner1");
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				do
				{
					RowItem item = new RowItem(""+cursor.getString(cursor.getColumnIndex("name")),
							""+cursor.getString(cursor.getColumnIndex("pbid")),
							""+cursor.getString(cursor.getColumnIndex("detailsid")));
					listBAs.add(item);
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		database.close();
		strLogin=null;
		strMainSection=null;
		strSection=null;
		strAuditData=null;
	}
	@Override
	protected void onDestroy() 
	{
		if(handlerTime!=null)handlerTime.removeCallbacks(runnable);
		listBAs=null;
		listView=null;
		prgDialog=null;
		handler=null;
		intent=null;
		strDetailsID=null;
		btnBack=null;
		handlerTime=null;
		runnable=null;
		super.onDestroy();
	}
}