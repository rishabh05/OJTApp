/*@ID: CN20140001
 *@Description: srcReadyReckonerData is for ReadyReckoner information Screen 
 * This class is used to search particular BA's information with ReadyReckoner data
 * based on store code or counter name
 * Content are fetched from server.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 22/03/2014
 * @Modified Date: 27/08/2014
 */
package com.ojt.readyreckoner;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.ojt.components.Components;
import com.ojt.database.OJTDAO;
import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.utilities.Utility;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;


public class ReadyReckonerData extends Activity implements OnClickListener
{
	private ArrayList<String> searchDataLabel=null;
	private ArrayList<String> searchData=null;
	private String strMonth[]={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
	private LinearLayout linearLayoutBAInfoMain=null,linearLayoutReadyRecInfoMain=null,linearLayoutBAInfoSub=null,linearLayoutReadyRecInfoSub=null;
	private Button btnBack=null;
	private TextView txtHeading=null;
	private Handler handlerTime=null;
	private Runnable runnable=null;
	private int[] intMonthOrder=null;
	private int[] intMonth=null;
	private ImageView imgBAInfo=null,imgReadyRec=null,imgPrev=null,imgLastAudit=null,
			imgSummary=null;
	private LinearLayout linearLayoutPrevAuditInfoMain=null;
	private LinearLayout linearLayoutPrevAuditInfoSub=null;
	private LinearLayout linearLayoutLastAuditScoreMain=null;
	private LinearLayout linearLayoutLastAuditScoreSub=null;
	private LinearLayout linearLayoutAuditSummaryMain=null;
	private LinearLayout linearLayoutAuditSummarySub=null;
	private JSONArray jArrayBA=null,jArrayReadyRec=null,jArrayMainScore=null,jArrayAuditReport=null
			,jArraySubScore=null;
	private String strBA=null,strReadyRec=null,strMainScore=null,strAuditReport=null
			,strSubScore=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.readyreckonerdata);
		Utility.context=this;
		searchData = new ArrayList<String>();
		searchDataLabel = new ArrayList<String>();
		
		txtHeading=(TextView) findViewById(R.id.headingtxt);

		imgBAInfo=(ImageView) findViewById(R.id.bainfoimg);
		imgReadyRec=(ImageView) findViewById(R.id.readyrecinfoimg);
		imgPrev=(ImageView) findViewById(R.id.prevauditinfoimg);
		imgLastAudit=(ImageView) findViewById(R.id.lastauditscoreimg);
		imgSummary=(ImageView) findViewById(R.id.auditsummaryimg);
		
		btnBack=(Button) findViewById(R.id.backbtn);
		btnBack.setOnClickListener(this);
		
		getData();
		//BA Info
		linearLayoutBAInfoMain=(LinearLayout) findViewById(R.id.bainfomainlinear);
		linearLayoutBAInfoMain.setOnClickListener(this);
		linearLayoutBAInfoSub=(LinearLayout) findViewById(R.id.bainfosublinear);
		if(strBA!=null)
		{
			if(strBA.length()>0)
			{
				try
				{
					jArrayBA=new JSONArray(strBA);
				} catch (JSONException e) {
					Log.i("Error",e.toString());
					Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~ReadyReckonerData~onCreate"+"~BA json error "+e.toString(),true);
				}
			}
		}
		loadData(jArrayBA,linearLayoutBAInfoSub,1);
		//ReadyRec Info
		linearLayoutReadyRecInfoMain=(LinearLayout) findViewById(R.id.readyrecinfomainlinear);
		linearLayoutReadyRecInfoMain.setOnClickListener(this);
		linearLayoutReadyRecInfoSub=(LinearLayout) findViewById(R.id.readyrecinfosublinear);
		if(strReadyRec!=null)
		{
			if(strReadyRec.length()>0)
			{
				try
				{
					jArrayReadyRec=new JSONArray(strReadyRec);
				} catch (JSONException e) {
					Log.i("Error",e.toString());
					Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~ReadyReckonerData~onCreate"+"~Readyreckoner json error "+e.toString(),true);
				}
			}
		}
		loadData(jArrayReadyRec,linearLayoutReadyRecInfoSub,2);
		//Prevoius Audit Info
		linearLayoutPrevAuditInfoMain=(LinearLayout) findViewById(R.id.prevauditinfomainlinear);
		linearLayoutPrevAuditInfoMain.setOnClickListener(this);
		linearLayoutPrevAuditInfoSub=(LinearLayout) findViewById(R.id.prevauditinfosublinear);
		//LastAudit Score
		linearLayoutLastAuditScoreMain=(LinearLayout) findViewById(R.id.lastauditscoremainlinear);
		linearLayoutLastAuditScoreMain.setOnClickListener(this);
		linearLayoutLastAuditScoreSub=(LinearLayout) findViewById(R.id.lastauditscoresublinear);
		linearLayoutLastAuditScoreSub.requestFocus();
		linearLayoutLastAuditScoreSub.setSelected(true);
		if(strMainScore!=null)
		{
			if(strMainScore.length()>0)
			{
				try
				{
					jArrayMainScore=new JSONArray(strMainScore);
				} catch (JSONException e) {
					Log.i("Error",e.toString());
					Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~ReadyReckonerData~onCreate"+"~Mainscore json error "+e.toString(),true);
				}
			}
		}
		if(strSubScore!=null)
		{
			if(strSubScore.length()>0)
			{
				try
				{
					jArraySubScore=new JSONArray(strSubScore);
				} catch (JSONException e) {
					Log.i("Error",e.toString());
					Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~ReadyReckonerData~onCreate"+"~Subscore json error "+e.toString(),true);
				}
			}
		}
		loadData(jArrayMainScore,linearLayoutLastAuditScoreSub,3);
		//Audit Summary
		linearLayoutAuditSummaryMain=(LinearLayout) findViewById(R.id.auditsummarymainlinear);
		linearLayoutAuditSummaryMain.setOnClickListener(this);
		linearLayoutAuditSummarySub=(LinearLayout) findViewById(R.id.auditsummarysublinear);
		linearLayoutAuditSummarySub.requestFocus();
		linearLayoutAuditSummarySub.setSelected(true);
		if(strAuditReport!=null)
		{
			if(strAuditReport.length()>0)
			{
				try
				{
					jArrayAuditReport=new JSONArray(strAuditReport);
				} catch (JSONException e) {
					Log.i("Error",e.toString());
					Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~ReadyReckonerData~onCreate"+"~Auditreport json error "+e.toString(),true);
				}
				
			}
		}
		loadData(jArrayAuditReport,linearLayoutAuditSummarySub,4);
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
				final Intent mainIntent = new Intent(ReadyReckonerData.this, Login.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				ReadyReckonerData.this.startActivity(mainIntent);
				ReadyReckonerData.this.finish();
			}
		};
		handlerTime.postDelayed(runnable, (Utility.intTimeout*1000));
	}
	@Override
	public void onClick(View v)
	{
		if(v==linearLayoutBAInfoMain)// BA information
		{
			if(linearLayoutBAInfoSub.getVisibility()==View.GONE)
			{
				imgBAInfo.setImageResource(R.drawable.uparrow);
				Components.expand(linearLayoutBAInfoSub);
			}
			else
			{
				imgBAInfo.setImageResource(R.drawable.downarrow);
				Components.collapse(linearLayoutBAInfoSub);
			}
		}
		else if(v==linearLayoutReadyRecInfoMain)// Readyreckoner information
		{
			if(linearLayoutReadyRecInfoSub.getVisibility()==View.GONE)
			{
				imgReadyRec.setImageResource(R.drawable.uparrow);
				Components.expand(linearLayoutReadyRecInfoSub);
			}
			else
			{
				imgReadyRec.setImageResource(R.drawable.downarrow);
				Components.collapse(linearLayoutReadyRecInfoSub);
			}
		}
		else if(v==linearLayoutPrevAuditInfoMain)// Previous audit information
		{
			if(linearLayoutPrevAuditInfoSub.getVisibility()==View.GONE)
			{
				imgPrev.setImageResource(R.drawable.uparrow);
				Components.expand(linearLayoutPrevAuditInfoSub);
			}
			else
			{
				imgPrev.setImageResource(R.drawable.downarrow);
				Components.collapse(linearLayoutPrevAuditInfoSub);
			}
		}
		else if(v==linearLayoutLastAuditScoreMain)// Last audit score information
		{
			if(linearLayoutLastAuditScoreSub.getVisibility()==View.GONE)
			{
				imgLastAudit.setImageResource(R.drawable.uparrow);
				Components.expand(linearLayoutLastAuditScoreSub);
			}
			else
			{
				imgLastAudit.setImageResource(R.drawable.downarrow);
				Components.collapse(linearLayoutLastAuditScoreSub);
			}
		}
		else if(v==linearLayoutAuditSummaryMain)// last audit summary information
		{
			if(linearLayoutAuditSummarySub.getVisibility()==View.GONE)
			{
				imgSummary.setImageResource(R.drawable.uparrow);
				Components.expand(linearLayoutAuditSummarySub);
			}
			else
			{
				imgSummary.setImageResource(R.drawable.downarrow);
				Components.collapse(linearLayoutAuditSummarySub);
			}
		}
		else if(v==btnBack)//Back. Back to BA's information screen
		{
			back();
		}
		
	}
	//Back event
	@Override
	public void onBackPressed() 
	{
		back();
		super.onBackPressed();
	}
	//Back to BA's Information Screen
	private void back()
	{
		btnBack.setEnabled(false);
		startActivity(new Intent(ReadyReckonerData.this,BAList.class));
		ReadyReckonerData.this.finish();
	}
	//set readyreckoner data 
	private void loadData(JSONArray jArray,LinearLayout subLinearLayout,int intCategory) 
	{
		String strMname=null,strBAName=null;
		String strSubJobj=null;
		searchData.clear();
		searchDataLabel.clear();
		try
		{
			if(intCategory==1)
			{
				for(int i=0;i<jArray.length();i++)
				{
					strSubJobj=jArray.get(i).toString();
					strSubJobj=strSubJobj.replace("=", "\":\"");
					strSubJobj=strSubJobj.replace(", ", "\",\"");
					strSubJobj=strSubJobj.replace("{", "{\"");
					strSubJobj=strSubJobj.replace("}", "\"}");
					JSONObject subJobj=new JSONObject(strSubJobj); 
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
					
					strBAName=subJobj.getString("BAName").replace("+", " ");
					txtHeading.setText(strBAName);
					searchData.add(strBAName);
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
				addComponent(searchData,searchDataLabel, subLinearLayout,false);
			}
			else if(intCategory==2)
			{
				if(jArray==null)
				{
					linearLayoutReadyRecInfoMain.setVisibility(View.INVISIBLE);
				}
				else
				{
					if(jArray.length()>0)
					{
						searchData.clear();
						searchDataLabel.clear();
						intMonthOrder=new int[jArray.length()];
						intMonth=new int[jArray.length()];
						for(int i=0;i<jArray.length();i++)
						{
							strSubJobj=jArray.get(i).toString();
							strSubJobj=strSubJobj.replace("=", "\":\"");
							strSubJobj=strSubJobj.replace(", ", "\",\"");
							strSubJobj=strSubJobj.replace("{", "{\"");
							strSubJobj=strSubJobj.replace("}", "\"}");
							JSONObject subJobj=new JSONObject(strSubJobj); 
							intMonth[i]=Integer.parseInt(subJobj.getString("month"))%12;
							intMonthOrder[i]=i;
						}
						for(int i=0;i<intMonth.length;i++)
						{
							for(int j=i+1;j<intMonth.length;j++)
							{
								if(intMonth[i]>=intMonth[j])
								{
									int tempIndex=intMonthOrder[i];
									intMonthOrder[i]=intMonthOrder[j];
									intMonthOrder[j]=tempIndex;
									
									int temp=intMonth[i];
									intMonth[i]=intMonth[j];
									intMonth[j]=temp;
								}
							}
						}
						strSubJobj=jArray.get(intMonthOrder[0]).toString();
						strSubJobj=strSubJobj.replace("=", "\":\"");
						strSubJobj=strSubJobj.replace(", ", "\",\"");
						strSubJobj=strSubJobj.replace("{", "{\"");
						strSubJobj=strSubJobj.replace("}", "\"}");
						JSONObject subJobj1=new JSONObject(strSubJobj); 
						
						strSubJobj=jArray.get(intMonthOrder[1]).toString();
						strSubJobj=strSubJobj.replace("=", "\":\"");
						strSubJobj=strSubJobj.replace(", ", "\",\"");
						strSubJobj=strSubJobj.replace("{", "{\"");
						strSubJobj=strSubJobj.replace("}", "\"}");
						JSONObject subJobj2=new JSONObject(strSubJobj); 
						
						strSubJobj=jArray.get(intMonthOrder[2]).toString();
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
					}
				}
			}
			else if(intCategory==3)
			{
				if(jArray==null)
				{
					linearLayoutPrevAuditInfoMain.setVisibility(View.INVISIBLE);
				}
				else
				{
					if(linearLayoutReadyRecInfoMain.getVisibility()==View.INVISIBLE)
					{
						int intMarginTop=linearLayoutPrevAuditInfoMain.getTop()-70;
						int intMarginLeft=linearLayoutPrevAuditInfoMain.getLeft();
						int intMarginRight=linearLayoutPrevAuditInfoMain.getRight();
						int intMarginBottom=linearLayoutPrevAuditInfoMain.getBottom();
						LinearLayout.LayoutParams lastTxtParams = new LinearLayout.LayoutParams(linearLayoutPrevAuditInfoMain.getLayoutParams().width, linearLayoutPrevAuditInfoMain.getLayoutParams().height);
						lastTxtParams.setMargins(intMarginLeft,intMarginTop, intMarginRight,intMarginBottom);
						linearLayoutPrevAuditInfoMain.setLayoutParams(lastTxtParams);
						linearLayoutPrevAuditInfoMain.invalidate();
					}
				for(int i=0;i<jArray.length();i++)
				{
					strSubJobj=jArray.get(i).toString();
					strSubJobj=strSubJobj.replace("=", "\":\"");
					strSubJobj=strSubJobj.replace(", ", "\",\"");
					strSubJobj=strSubJobj.replace("{", "{\"");
					strSubJobj=strSubJobj.replace("}", "\"}");
					JSONObject jObj=new JSONObject(strSubJobj);
					
					if(!jObj.getString("mainscore").equalsIgnoreCase("-1"))
					{
						strMname=jObj.getString("mainname").replace("+"," ")+"~"+"B";
						strMname=strMname.replace("-"," / ");
						searchDataLabel.add(strMname);
						searchData.add(jObj.getString("mainscore"));
					}
					if(jArraySubScore!=null)
					{
						for(int j=0;j<jArraySubScore.length();j++)
						{
							strSubJobj=jArraySubScore.get(j).toString();
							strSubJobj=strSubJobj.replace("=", "\":\"");
							strSubJobj=strSubJobj.replace(", ", "\",\"");
							strSubJobj=strSubJobj.replace("{", "{\"");
							strSubJobj=strSubJobj.replace("}", "\"}");
							JSONObject subJobj=new JSONObject(strSubJobj);
							if(subJobj.getString("mainid").equalsIgnoreCase(jObj.getString("mainid")))
							{
								if(!subJobj.getString("subscore").equalsIgnoreCase("-1"))
								{
									strMname=subJobj.getString("subname").replace("+"," ");
									strMname=strMname.replace("-"," / ");
									searchDataLabel.add(strMname);
									searchData.add(subJobj.getString("subscore"));
								}
							}
						}
					}
				}
				addComponent(searchData, searchDataLabel, subLinearLayout,false);
				}
			}
			else if(intCategory==4)
			{
				if(jArray==null)
				{
					linearLayoutPrevAuditInfoMain.setVisibility(View.INVISIBLE);
				}
				else
				{
					strSubJobj=jArray.get(0).toString();
					strSubJobj=strSubJobj.replace("=", "\":\"");
					strSubJobj=strSubJobj.replace(", ", "\",\"");
					strSubJobj=strSubJobj.replace("{", "{\"");
					strSubJobj=strSubJobj.replace("}", "\"}");
					JSONObject subJobj=new JSONObject(strSubJobj); 
					
					searchDataLabel.add("Overall Color Grade");
					searchDataLabel.add("Attitude  Color Grade");
					searchDataLabel.add("Mlearning mins per day");
					searchDataLabel.add("Auditor Comments");
					
					searchData.add(subJobj.getString("overallcolor"));
					searchData.add(subJobj.getString("attitudecolor"));
					searchData.add(subJobj.getString("mlearning").replace("+", " "));
					searchData.add(subJobj.getString("comment").replace("+", " "));
					addComponent(searchData,searchDataLabel,subLinearLayout,true);
				}
			}
		}
		catch(Exception e)
		{
			Log.i("Error",e.toString());
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~ReadyReckonerData~loaddata"+"~readyreckonerdata error:"+e.toString(),true);
		}
	}
	//Set Ready Reckoner data in corresponding component 
	private void addMonth(String strItems,String strValue1,String strValue2,String strValue3)
	{
		LinearLayout LinearLayoutSub=new LinearLayout(this);
		LinearLayoutSub.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayoutSub.setWeightSum(4);
		LinearLayoutSub.setPadding(15,0,0,0);
		//items
		EditText txtDatalabel=Components.editText(strItems);
		txtDatalabel.setGravity(Gravity.CENTER_VERTICAL);
		txtDatalabel.setSingleLine();
		txtDatalabel.setEllipsize(TruncateAt.END);
		txtDatalabel.setFocusable(false);
		txtDatalabel.setCursorVisible(false);
		txtDatalabel.setBackgroundColor(Color.TRANSPARENT);
		LayoutParams layoutParams=new LayoutParams(0, 45);
		layoutParams.weight=1.6f;
		layoutParams.leftMargin=5;
		layoutParams.gravity=Gravity.CENTER_VERTICAL;
		txtDatalabel.setTextColor(getResources().getColor(R.color.basearch_data_textcolor));
		txtDatalabel.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
		if(strItems.equalsIgnoreCase("items"))
		{
			txtDatalabel.setTypeface(null, Typeface.BOLD);
			txtDatalabel.setGravity(Gravity.CENTER);
		}
		LinearLayoutSub.addView(txtDatalabel,layoutParams);
		//month-1
		TextView txtData1=Components.textView(strValue1);
		txtData1.setGravity(Gravity.CENTER);
		txtData1.setSingleLine();
		txtData1.setEllipsize(TruncateAt.MARQUEE);
		txtData1.setSelected(true);
		LayoutParams layoutparams1=new LayoutParams(0, 45);
		layoutparams1.weight=0.8f;
		layoutparams1.leftMargin=5;
		layoutparams1.gravity=Gravity.CENTER_VERTICAL;
		txtData1.setTextColor(getResources().getColor(R.color.basearch_data_textcolor));
		txtData1.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
		LinearLayoutSub.addView(txtData1,layoutparams1);
		if(strItems.equalsIgnoreCase("items"))
		{
			txtData1.setTypeface(null, Typeface.BOLD);
		}
		//month-2
		TextView txtData2=Components.textView(strValue2);
		txtData2.setGravity(Gravity.CENTER);
		txtData2.setSingleLine();
		txtData2.setEllipsize(TruncateAt.MARQUEE);
		txtData2.setSelected(true);
		LayoutParams layoutparams2=new LayoutParams(0, 45);
		layoutparams2.weight=0.8f;
		layoutparams2.leftMargin=5;
		layoutparams2.gravity=Gravity.CENTER_VERTICAL;
		txtData2.setTextColor(getResources().getColor(R.color.basearch_data_textcolor));
		txtData2.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
		LinearLayoutSub.addView(txtData2,layoutparams2);
		if(strItems.equalsIgnoreCase("items"))
		{
			txtData2.setTypeface(null, Typeface.BOLD);
		}
		//month-3
		TextView txtData3=Components.textView(strValue3);
		txtData3.setGravity(Gravity.CENTER);
		txtData3.setSingleLine();
		txtData3.setEllipsize(TruncateAt.MARQUEE);
		txtData3.setSelected(true);
		LayoutParams layoutparams3=new LayoutParams(0, 45);
		layoutparams3.weight=0.8f;
		layoutparams3.leftMargin=5;
		layoutparams3.gravity=Gravity.CENTER_VERTICAL;
		txtData3.setTextColor(getResources().getColor(R.color.basearch_data_textcolor));
		txtData3.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
		LinearLayoutSub.addView(txtData3,layoutparams3);
		if(strItems.equalsIgnoreCase("items"))
		{
			txtData3.setTypeface(null, Typeface.BOLD);
		}
		LayoutParams layoutParamsSub=new LayoutParams(LayoutParams.MATCH_PARENT, 45);
		layoutParamsSub.topMargin=0;
		if(strItems.equalsIgnoreCase("items"))
		{
			LinearLayoutSub.setBackgroundResource(R.drawable.subtitlebg);
			
		}
		else
		{
			LinearLayoutSub.setBackgroundResource(R.drawable.transparentbg);
		}
		linearLayoutReadyRecInfoSub.addView(LinearLayoutSub,layoutParamsSub);
	}
	//Set BA's info and Last Audit Summary report in corresponding component
	private void addComponent(ArrayList<String> listData,ArrayList<String> listDataLabel,LinearLayout linearLayout,boolean color) 
	{
		LayoutParams layoutParams=null;
		boolean check=false;
		String strLabel=null;
		for(int i=0;i<listData.size();i++)
		{
			strLabel=listDataLabel.get(i).toString();
			LinearLayout linearLayoutSub=new LinearLayout(this);
			linearLayoutSub.setOrientation(LinearLayout.HORIZONTAL);
			linearLayoutSub.setWeightSum(3);
			linearLayoutSub.setPadding(15,0,0,0);
			if(strLabel.indexOf('~')>=0)
			{
				check=true;
				strLabel=strLabel.substring(0,strLabel.indexOf('~'));
			}
			else
			{
				check=false;
			}
			//Label
			EditText edttxtDataLabel=Components.editText(strLabel);
			edttxtDataLabel.setGravity(Gravity.CENTER_VERTICAL);
			edttxtDataLabel.setSingleLine();
			edttxtDataLabel.setEllipsize(TruncateAt.END);
			edttxtDataLabel.setFocusable(false);
			edttxtDataLabel.setCursorVisible(false);
			edttxtDataLabel.setBackgroundColor(Color.TRANSPARENT);
			edttxtDataLabel.setSelected(true);
			if(check)
			{
				edttxtDataLabel.setTypeface(null, Typeface.BOLD);
			}
			layoutParams=new LayoutParams(0, 45);
			layoutParams.weight=1.4f;
			layoutParams.leftMargin=5;
			layoutParams.gravity=Gravity.CENTER_VERTICAL;
			edttxtDataLabel.setTextColor(getResources().getColor(R.color.basearch_data_textcolor));
			edttxtDataLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
			linearLayoutSub.addView(edttxtDataLabel,layoutParams);
			//-
			TextView txtView=Components.textView(" - ");
			txtView.setGravity(Gravity.CENTER);
			if(check)
			{
				txtView.setTypeface(null, Typeface.BOLD);
			}
			layoutParams=new LayoutParams(0, 45);
			layoutParams.weight=0.2f;
			layoutParams.leftMargin=5;
			txtView.setTextColor(getResources().getColor(R.color.basearch_data_textcolor));
			txtView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
			linearLayoutSub.addView(txtView,layoutParams);
			//Data
			EditText edttxtData=Components.editText(listData.get(i).toString());
			edttxtData.setSingleLine();
			if(check)
			{
				edttxtData.setTypeface(null, Typeface.BOLD);
			}
			edttxtData.setEllipsize(TruncateAt.END);
			edttxtData.setSelected(true);
			edttxtData.setFocusable(false);
			edttxtData.setCursorVisible(false);
			edttxtData.setBackgroundColor(Color.TRANSPARENT);
			edttxtData.setGravity(Gravity.CENTER_VERTICAL);
			layoutParams=new LayoutParams(0, 45);
			layoutParams.weight=1.4f;
			layoutParams.leftMargin=5;
			
			if(listData.get(i).toString().equalsIgnoreCase("red"))
			{
				layoutParams=new LayoutParams(0, 35);
				layoutParams.weight=1.4f;
				layoutParams.leftMargin=5;
				edttxtData.setBackgroundColor(Color.RED);
				edttxtData.setText("");
			}
			else if(listData.get(i).toString().equalsIgnoreCase("yellow"))
			{
				layoutParams=new LayoutParams(0, 35);
				layoutParams.weight=1.4f;
				layoutParams.leftMargin=5;
				edttxtData.setBackgroundColor(Color.YELLOW);
				edttxtData.setText("");
			}
			else if(listData.get(i).toString().equalsIgnoreCase("green"))
			{
				layoutParams=new LayoutParams(0, 35);
				layoutParams.weight=1.4f;
				layoutParams.leftMargin=5;
				edttxtData.setBackgroundColor(Color.GREEN);
				edttxtData.setText("");
			}
			
			edttxtData.setTextColor(getResources().getColor(R.color.basearch_data_textcolor));
			layoutParams.gravity=Gravity.CENTER_VERTICAL;
			edttxtData.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
			linearLayoutSub.addView(edttxtData,layoutParams);
			
			layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT, 45);
			if(listData.get(i).toString().equalsIgnoreCase(""))
			{
				edttxtDataLabel.setText(listDataLabel.get(i).toString());
				txtView.setText("");
				edttxtData.setText("");
				edttxtDataLabel.setTypeface(null, Typeface.BOLD);
				edttxtDataLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.basearch_data_textsize));
				linearLayoutSub.setBackgroundResource(R.drawable.subtitlebg);
				
			}
			else
			{
				layoutParams.topMargin=0;
				linearLayoutSub.setBackgroundResource(R.drawable.transparentbg);
			}
			linearLayout.addView(linearLayoutSub,layoutParams);
		}
	}
	//Get ready reckoner data from local database
	private void getData()
	{
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursor=database.getAll("readyreckoner_bainfo");
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				do
				{
					strBA=cursor.getString(cursor.getColumnIndex("bainfo"));
					strReadyRec=cursor.getString(cursor.getColumnIndex("readyreckonerinfo"));
					strMainScore=cursor.getString(cursor.getColumnIndex("lastmainscore"));
					strSubScore=cursor.getString(cursor.getColumnIndex("lastsubscore"));
					strAuditReport=cursor.getString(cursor.getColumnIndex("lastauditreport"));
					
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		database.close();
	}
	@Override
	protected void onDestroy() 
	{
		if(handlerTime!=null)handlerTime.removeCallbacks(runnable);
		searchDataLabel=null;
		searchData=null;
		strMonth=null;
		linearLayoutBAInfoMain=null;
		linearLayoutReadyRecInfoMain=null;
		linearLayoutBAInfoSub=null;
		linearLayoutReadyRecInfoSub=null;
		btnBack=null;
		txtHeading=null;
		handlerTime=null;
		runnable=null;
		intMonthOrder=null;
		intMonth=null;
		imgBAInfo=null;
		imgReadyRec=null;
		imgPrev=null;
		imgLastAudit=null;
		imgSummary=null;
		linearLayoutPrevAuditInfoMain=null;
		linearLayoutPrevAuditInfoSub=null;
		linearLayoutLastAuditScoreMain=null;
		linearLayoutLastAuditScoreSub=null;
		linearLayoutAuditSummaryMain=null;
		linearLayoutAuditSummarySub=null;
		super.onDestroy();
	}
}