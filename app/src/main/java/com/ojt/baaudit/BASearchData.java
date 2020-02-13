/*@ID: CN20140001
 *@Description: srcBASearchData is for BA's Information Screen 
 * This class is used to display information about BA's 
 *      and ready reckoner 
 * Data are fetched from server.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 13/03/2014
 * @Modified Date: 27/08/2014
 */
package com.ojt.baaudit;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.components.Components;
import com.ojt.database.OJTDAO;
import com.ojt.utilities.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class BASearchData extends Activity implements OnClickListener 
{
	private LinearLayout linearLayoutMain=null;
	private ArrayList<String> searchData=null; 
	private ArrayList<String> searchDataLabel=null; 
	private TextView txtHeading=null;
	private Button btnAudit=null,btnBack=null,btnNext=null;
	private String strMonth[]={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
	private Handler handlerTime=null;
	private Runnable runnable=null;
	private int[] intMonthOrder;
	private int[] intMonth;
	private int tempIndex,tempMonth;
	private JSONObject subJobj=null;
	private String strSubJobj=null,strBAName=null;
	private JSONArray jArrayBA=null,jArrayReadyRec=null;
	private SharedPreferences preference=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basearchdata);
		Utility.context=this;
		
		preference = getSharedPreferences("OJTSession", MODE_PRIVATE);
		searchData = new ArrayList<String>();
		searchDataLabel = new ArrayList<String>();
		linearLayoutMain=(LinearLayout) findViewById(R.id.searchdatalinear);
		txtHeading=(TextView) findViewById(R.id.headingtxt);
		
		btnAudit=(Button) findViewById(R.id.auditbtn);
		btnAudit.setOnClickListener(this);
		btnBack=(Button) findViewById(R.id.backbtn);
		btnBack.setOnClickListener(this);
		btnNext=(Button) findViewById(R.id.nextbtn);
		btnNext.setOnClickListener(this);
		
		getBAInfo_Readyrec();
		//Get BA's info
		if(jArrayBA!=null)
		{
			if(jArrayBA.length()!=0)
			{
				searchData.clear();
				searchDataLabel.clear();
				try
				{
					for(int i=0;i<jArrayBA.length();i++)
					{
						strSubJobj=jArrayBA.get(i).toString();
						strSubJobj=strSubJobj.replace("=", "\":\"");
						strSubJobj=strSubJobj.replace(", ", "\",\"");
						strSubJobj=strSubJobj.replace("{", "{\"");
						strSubJobj=strSubJobj.replace("}", "\"}");
						
						subJobj=new JSONObject(strSubJobj); 
						searchDataLabel.add("BA Name");
						searchDataLabel.add("BA ID");
						searchDataLabel.add("Counter Name");
						searchDataLabel.add("Store Name");
						searchDataLabel.add("Store Code");
						searchDataLabel.add("Brand");
						searchDataLabel.add("Channel");
						searchDataLabel.add("Classification");
						searchDataLabel.add("Region");
						searchDataLabel.add("State");
						searchDataLabel.add("City");
						searchDataLabel.add("FM Code");
						searchDataLabel.add("FM Name");
						searchDataLabel.add("FS Code");
						searchDataLabel.add("FS Name");
						
						searchData.add(subJobj.getString("BAName").replace("+", " "));
						searchData.add(subJobj.getString("BApbid").replace("+", " "));
						searchData.add(subJobj.getString("CounterName").replace("+", " "));
						searchData.add(subJobj.getString("StoreName").replace("+", " "));
						searchData.add(subJobj.getString("StoreCode").replace("+", " "));
						searchData.add(subJobj.getString("Catagory").replace("+", " "));
						searchData.add(subJobj.getString("Channel").replace("+", " "));
						searchData.add(subJobj.getString("Classification").replace("+", " "));
						searchData.add(subJobj.getString("Region").replace("+", " "));
						searchData.add(subJobj.getString("State").replace("+", " "));
						searchData.add(subJobj.getString("City").replace("+", " "));
						searchData.add(subJobj.getString("FMCode").replace("+", " "));
						searchData.add(subJobj.getString("FMName").replace("+", " "));
						searchData.add(subJobj.getString("FSCode").replace("+", " "));
						searchData.add(subJobj.getString("FSName").replace("+", " "));
					}
					addComponent(false,"BA Info",searchData,searchDataLabel);
					strBAName=searchData.get(0);
					if(strBAName!=null)txtHeading.setText(strBAName);
				}catch(Exception e)
				{
					Log.i("Error",e.toString());
					Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearchData~oncreate~bainfo error"+e.toString(),true);
				}
			}
		}
		//Ready Reckoner Info
		if(jArrayReadyRec!=null)
		{
			if(jArrayReadyRec.length()>0)
			{
				searchData.clear();
				searchDataLabel.clear();
				try
				{
					intMonthOrder=new int[jArrayReadyRec.length()];
					intMonth=new int[jArrayReadyRec.length()];
					
					for(int i=0;i<jArrayReadyRec.length();i++)
					{
						strSubJobj=jArrayReadyRec.get(i).toString();
						strSubJobj=strSubJobj.replace("=", "\":\"");
						strSubJobj=strSubJobj.replace(", ", "\",\"");
						strSubJobj=strSubJobj.replace("{", "{\"");
						strSubJobj=strSubJobj.replace("}", "\"}");
						
						subJobj=new JSONObject(strSubJobj); 
						intMonth[i]=Integer.parseInt(subJobj.getString("month"))%12;
						intMonthOrder[i]=i;
					}
					for(int i=0;i<intMonth.length;i++)
					{
						for(int j=i+1;j<intMonth.length;j++)
						{
							if(intMonth[i]>=intMonth[j])
							{
								tempIndex=intMonthOrder[i];
								intMonthOrder[i]=intMonthOrder[j];
								intMonthOrder[j]=tempIndex;
								
								tempMonth=intMonth[i];
								intMonth[i]=intMonth[j];
								intMonth[j]=tempMonth;
							}
						}
					}
					LayoutParams layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, 45);
					TextView textView=Components.textView("Ready Reckoner Info");
					textView.setBackgroundResource(R.drawable.menutopbarbg);
					textView.setGravity(Gravity.CENTER_VERTICAL);
					textView.setTextColor(getResources().getColor(R.color.basearch_data_heading_textcolor));
					textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
					textView.setTypeface(null, Typeface.BOLD);
					textView.setPadding(10,0,0,0);
					linearLayoutMain.addView(textView,layoutParams);
					
					strSubJobj=jArrayReadyRec.get(intMonthOrder[0]).toString();
					strSubJobj=strSubJobj.replace("=", "\":\"");
					strSubJobj=strSubJobj.replace(", ", "\",\"");
					strSubJobj=strSubJobj.replace("{", "{\"");
					strSubJobj=strSubJobj.replace("}", "\"}");
					JSONObject subJobj1=new JSONObject(strSubJobj); 
					
					strSubJobj=jArrayReadyRec.get(intMonthOrder[1]).toString();
					strSubJobj=strSubJobj.replace("=", "\":\"");
					strSubJobj=strSubJobj.replace(", ", "\",\"");
					strSubJobj=strSubJobj.replace("{", "{\"");
					strSubJobj=strSubJobj.replace("}", "\"}");
					JSONObject subJobj2=new JSONObject(strSubJobj); 
					
					strSubJobj=jArrayReadyRec.get(intMonthOrder[2]).toString();
					strSubJobj=strSubJobj.replace("=", "\":\"");
					strSubJobj=strSubJobj.replace(", ", "\",\"");
					strSubJobj=strSubJobj.replace("{", "{\"");
					strSubJobj=strSubJobj.replace("}", "\"}");
					JSONObject subJobj3=new JSONObject(strSubJobj); 
					String strMonth1=strMonth[Integer.parseInt(subJobj1.getString("month"))-1] + " - " +subJobj1.getString("year").replace("+", " ");
					String strMonth2=strMonth[Integer.parseInt(subJobj2.getString("month"))-1] + " - " +subJobj2.getString("year").replace("+", " ");
					String strMonth3=strMonth[Integer.parseInt(subJobj3.getString("month"))-1] + " - " +subJobj3.getString("year").replace("+", " ");
					
					addMonth("Items", strMonth1, strMonth2, strMonth3);
					addMonth("Sales Target", subJobj1.getString("monthtarget").replace("+", " "), subJobj2.getString("monthtarget").replace("+", " "), subJobj3.getString("monthtarget").replace("+", " "));
					addMonth("Sales Achieved", subJobj1.getString("monthachived").replace("+", " "), subJobj2.getString("monthachived").replace("+", " "), subJobj3.getString("monthachived").replace("+", " "));
					addMonth("Color Cosmetics Target", subJobj1.getString("colourcosmetictarget").replace("+", " "), subJobj2.getString("colourcosmetictarget").replace("+", " "), subJobj3.getString("colourcosmetictarget").replace("+", " "));
					addMonth("Color Cosmetics Achieved",subJobj1.getString("colourcosmeticachived").replace("+", " "), subJobj2.getString("colourcosmeticachived").replace("+", " "), subJobj3.getString("colourcosmeticachived").replace("+", " "));
					addMonth("Skin Category Target", subJobj1.getString("skincategorytarget").replace("+", " "), subJobj2.getString("skincategorytarget").replace("+", " "), subJobj3.getString("skincategorytarget").replace("+", " "));
					addMonth("Skin Category Achieved", subJobj1.getString("skincategoryachived").replace("+", " "), subJobj2.getString("skincategoryachived").replace("+", " "), subJobj3.getString("skincategoryachived").replace("+", " "));
					addMonth("Focus Pack Color Target", subJobj1.getString("focuscolortarget").replace("+", " "), subJobj2.getString("focuscolortarget").replace("+", " "), subJobj3.getString("focuscolortarget").replace("+", " "));
					addMonth("Focus Pack Color Achieved", subJobj1.getString("focuscolorachived").replace("+", " "), subJobj2.getString("focuscolorachived").replace("+", " "), subJobj3.getString("focuscolorachived").replace("+", " "));
					addMonth("Focus Skin Care Regime Target", subJobj1.getString("skinregimetarget").replace("+", " "), subJobj2.getString("skinregimetarget").replace("+", " "), subJobj3.getString("skinregimetarget").replace("+", " "));
					addMonth("Focus Skin Care Regime Achieved", subJobj1.getString("skinregimeachived").replace("+", " "), subJobj2.getString("skinregimeachived").replace("+", " "), subJobj3.getString("skinregimeachived").replace("+", " "));
					addMonth("Unit Bill Target", subJobj1.getString("unitbilltarget").replace("+", " "), subJobj2.getString("unitbilltarget").replace("+", " "), subJobj3.getString("unitbilltarget").replace("+", " "));
					addMonth("Unit Bill Achieved", subJobj1.getString("unitbillachived").replace("+", " "), subJobj2.getString("unitbillachived").replace("+", " "), subJobj3.getString("unitbillachived").replace("+", " "));
					addMonth("Value Basket Target", subJobj1.getString("valuebaskettarget").replace("+", " "), subJobj2.getString("valuebaskettarget").replace("+", " "), subJobj3.getString("valuebaskettarget").replace("+", " "));
					addMonth("Value Basket Achieved", subJobj1.getString("valuebasketachived").replace("+", " "), subJobj2.getString("valuebasketachived").replace("+", " "), subJobj3.getString("valuebasketachived").replace("+", " "));
					
					layoutParams=null;
					textView=null;
					subJobj1=null;
					subJobj2=null;
					subJobj3=null;
					strMonth1=null;
					strMonth2=null;
					strMonth3=null;
				}catch(Exception e)
				{
					Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearchData~oncreate~readyrec error"+e.toString(),true);
					Log.i("Error",e.toString());
				}
			}
		}
		hasLastAudit();
		timeLimit();
	}
	//Store data while minimize the screen
	@Override
	protected void onPause() 
	{
		super.onPause();
		Utility.setLastActivity(false,Utility.strAuName);
	}
	//Check Previous audit data is available or not 
	private void hasLastAudit() 
	{
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		
		OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursor=database.getAll("lastmainscore");
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				btnNext.setEnabled(true);
				btnNext.setBackgroundResource(R.drawable.nextbtn);
			}
			else
			{
				btnNext.setEnabled(false);
				btnNext.setBackgroundResource(R.drawable.nextbtn_disable);
			}
			cursor.close();
		}
		else
		{
			btnNext.setEnabled(false);
			btnNext.setBackgroundResource(R.drawable.nextbtn_disable);
		}
		
		database.close();
		strLogin=null;
		strMainSection=null;
		strSection=null;
		strAuditData=null;
		cursor=null;
	}
	private void getBAInfo_Readyrec()
	{
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		String strBAInfo=null,strReadyRec=null;
		
		OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		
		Cursor cursor=database.getVal("status=?", new String[]{"0"}, strAuditData);
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				strBAInfo=cursor.getString(cursor.getColumnIndex("bainfo"));
				strReadyRec=cursor.getString(cursor.getColumnIndex("readyreckonerinfo"));
				if(strBAInfo!=null)
				{
					try
					{
						jArrayBA=new JSONArray(strBAInfo);
					} 
					catch (JSONException e)
					{
						Log.i("BA JSONException",""+e);
						Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearchData~getBAInfo_Readyrec~BA json parser error"+e.toString(),true);
					}
				}
				if(strReadyRec!=null)
				{
					try
					{
						jArrayReadyRec=new JSONArray(strReadyRec);
					}
					catch (JSONException e) 
					{
						Log.i("JSONException",""+e);
						Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASearchData~getBAInfo_Readyrec~Readyreckoner json parser error"+e.toString(),true);
					}
				}
			}
			cursor.close();
		}
		database.close();
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
				final Intent mainIntent = new Intent(BASearchData.this, Login.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				BASearchData.this.startActivity(mainIntent);
				BASearchData.this.finish();
			}
		};
		handlerTime.postDelayed(runnable, (Utility.intTimeout*1000));
	}
	private void addMonth(String strItems,String strValue1,String strValue2,String strValue3)
	{
		TextView textView=null;
		LayoutParams layoutParams=null;
		
		LinearLayout linearLayoutSub=new LinearLayout(this);
		linearLayoutSub.setOrientation(LinearLayout.HORIZONTAL);
		linearLayoutSub.setWeightSum(4);
		linearLayoutSub.setPadding(15,0,0,0);
		//items
		textView=Components.textView(strItems);
		textView.setGravity(Gravity.CENTER_VERTICAL);
		textView.setSingleLine();
		textView.setEllipsize(TruncateAt.MARQUEE);
		textView.setSelected(true);
		layoutParams=new LayoutParams(0, 35);
		layoutParams.weight=1.6f;
		layoutParams.leftMargin=5;
		layoutParams.gravity=Gravity.CENTER_VERTICAL;
		textView.setTextColor(getResources().getColor(R.color.basearch_data_textcolor));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
		if(strItems.equalsIgnoreCase("items"))
		{
			textView.setTypeface(null, Typeface.BOLD);
			textView.setGravity(Gravity.CENTER);
		}
		linearLayoutSub.addView(textView,layoutParams);
		//month-1
		addMonth(strItems, strValue1, linearLayoutSub);
		//month-2
		addMonth(strItems, strValue2, linearLayoutSub);
		//month-3
		addMonth(strItems, strValue3, linearLayoutSub);
		layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, 35);
		layoutParams.topMargin=0;
		if(strItems.equalsIgnoreCase("items"))
		{
			linearLayoutSub.setBackgroundResource(R.drawable.subtitlebg);
		}
		else
		{
			linearLayoutSub.setBackgroundResource(R.drawable.transparentbg);
		}
		linearLayoutMain.addView(linearLayoutSub,layoutParams);
		
		textView=null;
		linearLayoutSub=null;
		layoutParams=null;
	}
	// Add the month details for Readyreckoner
	private void addMonth(String strItems,String strValue, LinearLayout linearLayout)
	{
		TextView textView=null;
		LayoutParams layoutParams=null;
		textView=Components.textView(strValue);
		textView.setGravity(Gravity.CENTER);
		textView.setSingleLine();
		textView.setEllipsize(TruncateAt.MARQUEE);
		textView.setSelected(true);
		layoutParams=new LayoutParams(0, 35);
		layoutParams.weight=0.8f;
		layoutParams.leftMargin=5;
		layoutParams.gravity=Gravity.CENTER_VERTICAL;
		textView.setTextColor(getResources().getColor(R.color.basearch_data_textcolor));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
		linearLayout.addView(textView,layoutParams);
		if(strItems.equalsIgnoreCase("items"))
		{
			textView.setTypeface(null, Typeface.BOLD);
		}
	}
	//Create Component based on search data.
	private void addComponent(boolean color,String strHead,ArrayList<String> listData,ArrayList<String> listDatalbl) 
	{
		LayoutParams layoutParams=null;
		TextView textView=null;
		
		layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, 45);
		textView=Components.textView(strHead);
		textView.setBackgroundResource(R.drawable.menutopbarbg);
		textView.setGravity(Gravity.CENTER_VERTICAL);
		textView.setTextColor(getResources().getColor(R.color.basearch_data_heading_textcolor));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
		textView.setTypeface(null, Typeface.BOLD);
		textView.setPadding(10,0,0,0);
		linearLayoutMain.addView(textView,layoutParams);
		for(int i=0;i<listData.size();i++)
		{
			LinearLayout linearlayoutSub=new LinearLayout(this);
			linearlayoutSub.setOrientation(LinearLayout.HORIZONTAL);
			linearlayoutSub.setWeightSum(3);
			linearlayoutSub.setPadding(15,0,0,0);
			//Data label
			textView=Components.textView(listDatalbl.get(i).toString());
			textView.setGravity(Gravity.CENTER_VERTICAL);
			textView.setSingleLine();
			textView.setEllipsize(TruncateAt.MARQUEE);
			textView.setSelected(true);
			layoutParams=new LayoutParams(0, 35);
			layoutParams.weight=1.4f;
			layoutParams.leftMargin=5;
			layoutParams.gravity=Gravity.CENTER_VERTICAL;
			textView.setTextColor(getResources().getColor(R.color.basearch_data_textcolor));
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
			linearlayoutSub.addView(textView,layoutParams);
			
			textView=Components.textView(" - ");
			textView.setGravity(Gravity.CENTER);
			layoutParams=new LayoutParams(0, 35);
			layoutParams.weight=0.2f;
			layoutParams.leftMargin=5;
			textView.setTextColor(getResources().getColor(R.color.basearch_data_textcolor));
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
			linearlayoutSub.addView(textView,layoutParams);
			//Data
			textView=Components.textView(listData.get(i).toString());
			textView.setSingleLine();
			textView.setEllipsize(TruncateAt.MARQUEE);
			textView.setSelected(true);
			textView.setGravity(Gravity.CENTER_VERTICAL);
			layoutParams=new LayoutParams(0, 35);
			layoutParams.weight=1.4f;
			layoutParams.leftMargin=5;
			textView.setTextColor(getResources().getColor(R.color.basearch_data_textcolor));
			layoutParams.gravity=Gravity.CENTER_VERTICAL;
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
			linearlayoutSub.addView(textView,layoutParams);
			layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, 35);
			layoutParams.topMargin=0;
			linearlayoutSub.setBackgroundResource(R.drawable.transparentbg);
			linearLayoutMain.addView(linearlayoutSub,layoutParams);
			layoutParams=null;
			textView=null;
			linearlayoutSub=null;
		}
	}
	@Override
	public void onClick(View v) 
	{
		if(v==btnAudit) //Audit. Move to Audit Form screen
		{
			btnAudit.setEnabled(false);
			startActivity(new Intent(BASearchData.this,BAAuditForm.class));
			BASearchData.this.finish();
		}
		else if(v==btnBack)
		{
			back();
		}
		else if(v==btnNext) //Next. Move to Last Summary screen
		{
			btnNext.setEnabled(false);
			startActivity(new Intent(BASearchData.this,BAPrevSummary.class));
			BASearchData.this.finish();
		}
	}
	//Back  event
	@Override
	public void onBackPressed() 
	{
		back();
	}
	//Back to BASearch Screen
	private void back() 
	{
		if(Utility.auditStart())
		{
			 AlertDialog.Builder builder = new AlertDialog.Builder(Utility.context);
			 builder.setMessage(getResources().getString(R.string.auditloss_msg)).setPositiveButton(Utility.context.getResources().getString(R.string.yes_msg), new DialogInterface.OnClickListener() 
			 {
				 @Override
				 public void onClick(DialogInterface dialog, int which) 
				 {
					dialog.cancel();
					startActivity(new Intent(BASearchData.this,BASearch.class));
					BASearchData.this.finish();
					 
				 }
			 }).setNegativeButton(Utility.context.getResources().getString(R.string.no_msg), new DialogInterface.OnClickListener() 
			 {
				 @Override
				 public void onClick(DialogInterface dialog, int which) 
				 {
					 dialog.cancel();
					
				 }
			 }).setCancelable(false).show(); 
		}
		else if(preference.getBoolean("AuditStart",false))
		{
			 AlertDialog.Builder builder = new AlertDialog.Builder(Utility.context);
			 builder.setMessage(getResources().getString(R.string.auditempty_msg)).setPositiveButton(Utility.context.getResources().getString(R.string.yes_msg), new DialogInterface.OnClickListener() 
			 {
				 @Override
				 public void onClick(DialogInterface dialog, int which) 
				 {
					dialog.cancel();
					btnBack.setEnabled(false);
					startActivity(new Intent(BASearchData.this,BASearch.class));
					BASearchData.this.finish();
					 
				 }
			 }).setNegativeButton(Utility.context.getResources().getString(R.string.no_msg), new DialogInterface.OnClickListener() 
			 {
				 @Override
				 public void onClick(DialogInterface dialog, int which) 
				 {
					 dialog.cancel();
					
				 }
			 }).setCancelable(false).show(); 
			
		}
		else
		{
			btnBack.setEnabled(false);
			startActivity(new Intent(BASearchData.this,BASearch.class));
			BASearchData.this.finish();
		}
	}
	@Override
	protected void onDestroy() 
	{
		if(handlerTime!=null)handlerTime.removeCallbacks(runnable);
		linearLayoutMain=null;
		searchData=null; 
		searchDataLabel=null; 
		txtHeading=null;
		btnAudit=null;
		btnBack=null;
		btnNext=null;
		strMonth=null;
		handlerTime=null;
		runnable=null;
		intMonthOrder=null;
		intMonth=null;
		subJobj=null;
		super.onDestroy();
	}
}