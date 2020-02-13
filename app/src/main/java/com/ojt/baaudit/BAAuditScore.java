/*@ID: CN20140001
 *@Description: srcBAAuditScore is for Previous Audit Score screen 
 * This class is used to show the Last Audit Score.
 * Content are fetched from server.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Stage: 1
 * @Date: 20/03/2014
 * @Modified Date: 28/08/2014
 */
package com.ojt.baaudit;

import com.ojt.components.Components;
import com.ojt.database.OJTDAO;
import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.utilities.Utility;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;


public class BAAuditScore extends Activity implements OnClickListener
{
	private Button btnBack=null,btnAudit=null;
	private Intent intent=null;
	private TextView txtHeading=null;
	private LinearLayout linearLayoutMain=null;
	private LinearLayout linearLayoutHeading=null;
	private TextView txtDate=null;
	private TextView txtTime=null,txtAuditedby=null;
	private Handler handler=null;
	private Runnable runnable=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baauditscore);
		Utility.context=this;
		btnBack=(Button) findViewById(R.id.backbtn);
		btnBack.setOnClickListener(this);
		btnAudit=(Button) findViewById(R.id.auditbtn);
		btnAudit.setOnClickListener(this);
		
		txtHeading=(TextView) findViewById(R.id.headingtxt);
		txtAuditedby=(TextView) findViewById(R.id.auditedbytxt);
		txtDate=(TextView)findViewById(R.id.datetxt);
		txtTime=(TextView)findViewById(R.id.timetxt);
		
		linearLayoutMain=(LinearLayout) findViewById(R.id.auditformlinear);
		linearLayoutHeading=(LinearLayout) findViewById(R.id.headinglinear);
		addForm();
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
	    handler.removeCallbacks(runnable);
	    timeLimit();
	}
	/*
	 * Calculate session time.
	 * Redirect to login page when session expired
	 */
	private void timeLimit() 
	{
		handler=new Handler();
		runnable=new Runnable()
		{
			@Override
			public void run()
			{
				final Intent mainIntent = new Intent(BAAuditScore.this, Login.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				BAAuditScore.this.startActivity(mainIntent);
				BAAuditScore.this.finish();
			}
		};
		handler.postDelayed(runnable, (Utility.intTimeout*1000));
	}
	//Load Previous audit score data
	private void addForm() 
	{
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		String strAuditOn=null,strAuditOnTime=null,strAuditBy=null,strBAName=null;
		
		OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursor=database.getAll("lastauditreport");
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				strAuditOn=cursor.getString(cursor.getColumnIndex("auditon"));
				strAuditOnTime=cursor.getString(cursor.getColumnIndex("auditontime"));
				strAuditBy=cursor.getString(cursor.getColumnIndex("auditby"));
			}
			cursor.close();
		}
		database.close();
		strLogin=null;
		strMainSection=null;
		strSection=null;
		strAuditData=null;
		//BAName
		try
		{
			strBAName=Utility.getBAName();
			if(strBAName!=null)	txtHeading.setText(txtHeading.getText()+" - "+strBAName);
			
		}catch(Exception e){
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAAuditScore~addForm~baname error"+e.toString(),true);
		}
		
		//Previous audit date
		try
		{
			if(strAuditOn!=null)
			{
				if(strAuditOn.indexOf("-")!=-1)
				{
					String strDate[]=strAuditOn.split("-");
					if(strDate.length>0&&strDate.length<4)
					{
						txtDate.setText(txtDate.getText().toString()+strDate[2]+"-"+strDate[1]+"-"+strDate[0]);
					}
				}
			}
		}
		catch(Exception e)
		{
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAAuditScore~addForm~auditon error"+e.toString(),true);
		}
		//Previous audit time
		try
		{
			if(strAuditOnTime!=null)
			{
				if(strAuditOnTime.indexOf("-")!=-1)
				{
					String strTime[]=strAuditOnTime.split("-");
					if(strTime.length>0&&strTime.length<4)
					{
						txtTime.setText(txtTime.getText().toString()+strTime[0]+":"+strTime[1]+":"+strTime[2]);
					}
				}
			}
		}
		catch(Exception e)
		{
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAAuditScore~addForm~auditontime error"+e.toString(),true);
		}
		// Audited by
		try
		{
			if(strAuditBy!=null)
			{
				txtAuditedby.setText(txtAuditedby.getText().toString()+ strAuditBy.replace("+", " "));
			}
		}catch(Exception e)
		{
			Log.i("Error",e.toString());
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAAuditScore~addForm~auditby error"+e.toString(),true);
	
		}
		addHeading();
		addData();
	}
	//Form Heading
	private void addHeading() 
	{
		TextView textView=null;
		LayoutParams layoutParams=null;
		
		LinearLayout linearLayout=Components.linearLayout();
		linearLayout.setWeightSum(4);
		//Sno	
		textView=Components.textView("S.NO");
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_heading_textsize));
		textView.setTextColor(getResources().getColor(R.color.form_dataheadingtxt_textcolor));
		textView.setGravity(Gravity.CENTER);
		layoutParams=new LayoutParams(0, 40);
		layoutParams.weight=0.3f;
		layoutParams.leftMargin=5;
		linearLayout.addView(textView, layoutParams);
		//Particular	
		textView=Components.textView("PARTICULAR");
		textView.setSingleLine();
		textView.setEllipsize(TruncateAt.MARQUEE);
		textView.setSelected(true);
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_heading_textsize));
		textView.setTextColor(getResources().getColor(R.color.form_dataheadingtxt_textcolor));
		textView.setGravity(Gravity.CENTER);
		layoutParams=new LayoutParams(0, 40);
		layoutParams.weight=1.7f;
		layoutParams.leftMargin=5;
		linearLayout.addView(textView, layoutParams);
		//Score	
		textView=Components.textView("SCORE");
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_heading_textsize));
		textView.setTextColor(getResources().getColor(R.color.form_dataheadingtxt_textcolor));
		textView.setGravity(Gravity.CENTER);
		layoutParams=new LayoutParams(0, 40);
		layoutParams.weight=0.5f;
		layoutParams.leftMargin=5;
		linearLayout.addView(textView, layoutParams);
		//Training	
		textView=Components.textView("TRAINING");
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_heading_textsize));
		textView.setTextColor(getResources().getColor(R.color.form_dataheadingtxt_textcolor));
		textView.setGravity(Gravity.CENTER);
		layoutParams=new LayoutParams(0, 40);
		layoutParams.weight=0.5f;
		layoutParams.leftMargin=5;
		linearLayout.addView(textView, layoutParams);
		//Remarks
		textView=Components.textView("REMARKS");
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_heading_textsize));
		textView.setSingleLine();
		textView.setEllipsize(TruncateAt.MARQUEE);
		textView.setSelected(true);
		textView.setTextColor(getResources().getColor(R.color.form_dataheadingtxt_textcolor));
		textView.setGravity(Gravity.CENTER);
		layoutParams=new LayoutParams(0, 40);
		layoutParams.weight=1.0f;
		layoutParams.leftMargin=5;
		linearLayout.addView(textView, layoutParams);
	
		LayoutParams linearlayoutparms=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		linearLayoutHeading.addView(linearLayout,linearlayoutparms);
		textView=null;
		layoutParams=null;
		linearLayout=null;
	}
	//Load audit score data from local database
	private void addData()
	{
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		int i=1,intID=-1,intPrevSNO;
		Cursor cursorSub=null,cursorMain=null;
		String strMainID=null,strMainName=null,strSubID=null,strSubName=null;
		OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		
		cursorMain=database.getAll("lastmainscore");
		if(cursorMain!=null)
		{
			if(cursorMain.moveToFirst())
			{
				do
				{
					intID++;
					strMainID=cursorMain.getString(cursorMain.getColumnIndex("id"));
					strMainName=cursorMain.getString(cursorMain.getColumnIndex("name"));
					
					ContentValues contentvaluesMain=new ContentValues();
					contentvaluesMain.put("sno",intID);
					database.update(contentvaluesMain, "lastmainscore", "id=?", new String[]{strMainID});
					if(getResources().getString(R.string.no_score_mid).equalsIgnoreCase(strMainID))
					{
						addComponent(""+i,strMainName,intID,-1,cursorMain.getString(cursorMain.getColumnIndex("remarks")),cursorMain.getString(cursorMain.getColumnIndex("training")));
					}
					else
					{
						addComponent(""+i,strMainName,intID,cursorMain.getInt(cursorMain.getColumnIndex("score")),cursorMain.getString(cursorMain.getColumnIndex("remarks")),cursorMain.getString(cursorMain.getColumnIndex("training")));
					}
		
					cursorSub=database.getVal("mid=?", new String[]{strMainID},"lastsubscore");
					if(cursorSub!=null)
					{
						if(cursorSub.moveToFirst())
						{
							intPrevSNO=intID;
							do
							{
								intID++;
								strSubID=cursorSub.getString(cursorSub.getColumnIndex("id"));
								strSubName=cursorSub.getString(cursorSub.getColumnIndex("name"));
								
								ContentValues contentvaluesSub=new ContentValues();
								contentvaluesSub.put("psno",intPrevSNO);
								contentvaluesSub.put("sno",intID);
								database.update(contentvaluesSub, "lastsubscore", "id=?", new String[]{strSubID});
								if(getResources().getString(R.string.no_score_mid).equalsIgnoreCase(strSubID))
								{
									addComponent("",strSubName,intID,-1,cursorSub.getString(cursorSub.getColumnIndex("remarks")),cursorSub.getString(cursorSub.getColumnIndex("training")));
								}
								else
								{
									addComponent("",strSubName,intID,cursorSub.getInt(cursorSub.getColumnIndex("score")),cursorSub.getString(cursorSub.getColumnIndex("remarks")),cursorSub.getString(cursorSub.getColumnIndex("training")));
								}
							}while(cursorSub.moveToNext());
						}
						cursorSub.close();
					}
					i++;
				}while(cursorMain.moveToNext());
			}
			cursorMain.close();
		}
		database.close();
		strLogin=null;
		strMainSection=null;
		strSection=null;
		strAuditData=null;
		strMainID=null;
		strMainName=null;
		strSubID=null;
		strSubName=null;
		cursorMain=null;
		cursorSub=null;
	}
	//Create component(TextView, CheckBox,EditText) based on data
	private void addComponent(String strSno,String strParticular,int intCompid,int intScore,String strRemarks,String strTraining)
	{
		try
		{
			TextView textView=null;
			LayoutParams layoutParams=null;
			CheckBox checkBox=null;
			LinearLayout linearLayout=null;
			EditText editText=null;
			
			LinearLayout linearLayoutHeading=Components.linearLayout();
			linearLayoutHeading.setWeightSum(4);
			linearLayoutHeading.setId(intCompid);
			//Sno	
			textView=Components.textView(strSno);
			if(!strSno.equalsIgnoreCase(""))
			{
				textView.setTypeface(null, Typeface.BOLD);
				textView.setTextColor(getResources().getColor(R.color.baaudit_heading_textcolor));
			}
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_data_textsize));
			textView.setTextColor(getResources().getColor(R.color.form_datatxt_textcolor));
			textView.setGravity(Gravity.CENTER);
			layoutParams=new LayoutParams(0, 40);
			layoutParams.weight=0.3f;
			layoutParams.leftMargin=5;
			layoutParams.gravity=Gravity.CENTER_VERTICAL;
			linearLayoutHeading.addView(textView, layoutParams);
			//Particular	
			textView=Components.textView(strParticular);
			if(!strSno.equalsIgnoreCase(""))
			{
				textView.setTypeface(null, Typeface.BOLD);
			}
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_data_textsize));
			textView.setTextColor(getResources().getColor(R.color.form_datatxt_textcolor));
			layoutParams=new LayoutParams(0, LayoutParams.WRAP_CONTENT);
			layoutParams.weight=1.7f;
			layoutParams.leftMargin=5;
			layoutParams.gravity=Gravity.CENTER;
			linearLayoutHeading.addView(textView, layoutParams);
			//score
			if(intScore==-1)
			{
				textView=Components.textView("");
			}
			else
			{
				textView=Components.textView(""+intScore);
			}
			textView.setGravity(Gravity.CENTER);
			if(!strSno.equalsIgnoreCase(""))
			{
				textView.setTypeface(null, Typeface.BOLD);
			}
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_data_textsize));
			textView.setTextColor(getResources().getColor(R.color.form_datatxt_textcolor));
			layoutParams=new LayoutParams(0, LayoutParams.WRAP_CONTENT);
			layoutParams.weight=0.5f;
			layoutParams.leftMargin=5;
			layoutParams.gravity=Gravity.CENTER;
			linearLayoutHeading.addView(textView, layoutParams);
			//Training
			linearLayout=Components.linearLayout();
			checkBox=Components.checkBox("");
			checkBox.setFocusable(false);
			checkBox.setFocusableInTouchMode(false); 
			checkBox.setClickable(false); 
			checkBox.setButtonDrawable(R.drawable.precheckstyle);
			if(strTraining.equalsIgnoreCase("yes"))
			{
				checkBox.setChecked(true);
			}
			else
			{
				checkBox.setChecked(false);
			}
			layoutParams=new LayoutParams(25, 25);
			linearLayout.setGravity(Gravity.CENTER);
			linearLayout.addView(checkBox,layoutParams);
			layoutParams=new LayoutParams(0, 40);
			layoutParams.weight=0.5f;
			layoutParams.leftMargin=10;
			layoutParams.gravity=Gravity.CENTER_VERTICAL;
			linearLayoutHeading.addView(linearLayout, layoutParams);
			//Remarks
			editText=Components.editText("");
			if(!strSno.equalsIgnoreCase(""))
			{
				editText.setTypeface(null, Typeface.BOLD);
			}
			if(strRemarks.length()!=0&&!strRemarks.equalsIgnoreCase("null"))
			{
				editText.setText(strRemarks);
			}
			editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_data_textsize));
			editText.setTextColor(getResources().getColor(R.color.form_datatxt_textcolor));
			editText.setBackgroundColor(Color.TRANSPARENT);
			editText.setGravity(Gravity.CENTER);
			InputFilter[] maxLength = new InputFilter[1];
			maxLength[0] = new InputFilter.LengthFilter(20);
			editText.setFilters(maxLength);
			layoutParams=new LayoutParams(0, LayoutParams.WRAP_CONTENT);
			layoutParams.weight=1.0f;
			layoutParams.leftMargin=5;
			layoutParams.rightMargin=5;
			layoutParams.gravity=Gravity.CENTER;
			linearLayoutHeading.addView(editText, layoutParams);
			editText.setFocusable(false);
			editText.setFocusableInTouchMode(false); 
			editText.setClickable(false); 
			
			LayoutParams linearlayoutparms=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			if(!strSno.equalsIgnoreCase(""))
			{
				linearLayoutHeading.setBackgroundResource(R.drawable.tablerowdark);
				
			}
			else
			{
				linearLayoutHeading.setBackgroundResource(R.drawable.tablerowlight);
			}
			linearLayoutMain.addView(linearLayoutHeading,linearlayoutparms);
			textView=null;
			layoutParams=null;
			checkBox=null;
			linearLayout=null;
			editText=null;
			linearLayoutHeading=null;
		}catch(Exception e)
		{
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAAuditScore~addComponent"+"~error"+e.toString(),true);
		}
	}
	@Override
	public void onClick(View v) 
	{
		if(v==btnBack)
		{
			back();
		}
		else if(v==btnAudit)//Audit. Move to Audit Form screen.
		{
			btnAudit.setEnabled(false);
			intent=new Intent(BAAuditScore.this,BAAuditForm.class);
			startActivity(intent);
			BAAuditScore.this.finish();
		}
	}
	//Back event
	@Override
	public void onBackPressed() 
	{
		back();
	}
	//Back. Move to Last Summary screen
	private void back()
	{
		btnBack.setEnabled(false);
		intent=new Intent(BAAuditScore.this,BAPrevSummary.class);
		startActivity(intent);
		BAAuditScore.this.finish();
	}
	@Override
	protected void onDestroy() 
	{
		if(handler!=null)handler.removeCallbacks(runnable);
		btnBack=null;
		btnAudit=null;
		intent=null;
		txtHeading=null;
		linearLayoutMain=null;
		linearLayoutHeading=null;
		txtDate=null;
		txtTime=null;
		txtAuditedby=null;
		handler=null;
		runnable=null;
		super.onDestroy();
	}
}