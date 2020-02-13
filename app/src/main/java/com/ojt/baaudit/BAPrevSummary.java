/*@ID: CN20140001
 *@Description: srcBAPrevSummary is for LastAudit Summary Screen 
 * This class is used to display summary report of last audit data 
 * Content are fetched from server.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 21/03/2014
 * @Modified Date: 28/08/2014
 */
package com.ojt.baaudit;

import com.ojt.components.Components;
import com.ojt.database.OJTDAO;
import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.utilities.Utility;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils.TruncateAt;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class BAPrevSummary extends Activity implements OnClickListener
{
	private Button btnNext=null,btnBack=null,btnAudit=null;
	private Intent intent=null;
	private int intTotalScore,intOverallScore;
	private LinearLayout linearLayoutMain=null;
	private TextView txtAuditedby=null;
	private TextView txtDate=null;
	private TextView txtTime=null;
	private TextView txtHeading=null;
	private TextView txtComments=null,txtAttitude=null,txtAttitudeColor=null,
			txtOverallColor=null,txtMlearning=null;
	private Handler handler=null;
	private Runnable runnable=null;
	private String strAuditBy=null,strAuditOn=null,strAuditOnTime=null,
			strOverallColor=null,strAttitudeColor=null,strComments=null,strMlearning=null;
	private ScrollView scrollView=null;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.prevsummary);
		Utility.context=this;
		btnBack=(Button) findViewById(R.id.backbtn);
		btnBack.setOnClickListener(this);
		btnAudit=(Button) findViewById(R.id.auditbtn);
		btnAudit.setOnClickListener(this);
		btnNext=(Button) findViewById(R.id.nextbtn);
		btnNext.setOnClickListener(this);
		
		txtAuditedby=(TextView) findViewById(R.id.auditedbytxt);
		txtDate=(TextView)findViewById(R.id.datetxt);
		txtTime=(TextView)findViewById(R.id.timetxt);
		txtHeading=(TextView) findViewById(R.id.headingtxt);
		txtAttitude=(TextView) findViewById(R.id.preattitudetxt);
		txtOverallColor=(TextView) findViewById(R.id.preoverallclrtxt);
		txtComments=(TextView) findViewById(R.id.premcommentstxt);
		txtComments.setSelected(true);
		txtComments.setMovementMethod(new ScrollingMovementMethod());
		txtComments.setPadding(5,5,5,5);
		txtAttitudeColor=(TextView) findViewById(R.id.preattitudeclrtxt);
		txtMlearning=(TextView) findViewById(R.id.premlearningtxt);
		
		linearLayoutMain=(LinearLayout) findViewById(R.id.summarylinear);
		linearLayoutMain=(LinearLayout) findViewById(R.id.summarylinear);
		scrollView=(ScrollView) findViewById(R.id.auditsummaryscroll);
		
		scrollView.setOnTouchListener(new OnTouchListener() {
	       
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				txtComments.getParent()
                 .requestDisallowInterceptTouchEvent(false);
				 return false;
			}
	    });
		txtComments.setOnTouchListener(new OnTouchListener() {

	        @Override
	        public boolean onTouch(View arg0, MotionEvent arg1) {
	            arg0.getParent().requestDisallowInterceptTouchEvent(true);
	            return false;
	        }
	    });
		getSummary();
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
				final Intent mainIntent = new Intent(BAPrevSummary.this, Login.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				BAPrevSummary.this.startActivity(mainIntent);
				BAPrevSummary.this.finish();
			}
		};
		handler.postDelayed(runnable, (Utility.intTimeout*1000));
	}
	@Override
	public void onClick(View v) 
	{
		if(v==btnBack)
		{
			back();
		}
		else if(v==btnAudit)// Audit, Move to Audit Form screen
		{
			btnAudit.setEnabled(false);
			intent=new Intent(BAPrevSummary.this,BAAuditForm.class);
			startActivity(intent);
			BAPrevSummary.this.finish();
		}
		else if(v==btnNext) //Next, Move to Auditscore screen
		{
			btnNext.setEnabled(false);
			intent=new Intent(BAPrevSummary.this,BAAuditScore.class);
			startActivity(intent);
			BAPrevSummary.this.finish();
		}
	}
	//Summary Report 
	private void getSummary() 
	{
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		String strMainName=null,strBAName=null;
		OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursor=database.getAll("lastmainscore");
		addComponent("Section", "Score");
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				intOverallScore=0;
				do
				{
					if(!getResources().getString(R.string.no_score_mid).equalsIgnoreCase(cursor.getString(cursor.getColumnIndex("id"))))
					{
						strMainName=cursor.getString(cursor.getColumnIndex("name"));
						intTotalScore=cursor.getInt(cursor.getColumnIndex("score"));
						intOverallScore=intOverallScore+intTotalScore;
						addComponent(strMainName, ""+intTotalScore);
					}
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		addComponent("Total", ""+intOverallScore);
		
		cursor=database.getAll("lastauditreport");
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				strAuditOn=cursor.getString(cursor.getColumnIndex("auditon"));
				strAuditOnTime=cursor.getString(cursor.getColumnIndex("auditontime"));
				strOverallColor=cursor.getString(cursor.getColumnIndex("overallcolor"));
				strAttitudeColor=cursor.getString(cursor.getColumnIndex("attitudecolor"));
				strAuditBy=cursor.getString(cursor.getColumnIndex("auditby"));
				strComments=cursor.getString(cursor.getColumnIndex("comments"));
				strMlearning=cursor.getString(cursor.getColumnIndex("mlearning"));
			}
			cursor.close();
		}
		database.close();
		
		//BAName
		try
		{
			strBAName=Utility.getBAName();
			if(strBAName!=null)	txtHeading.setText(txtHeading.getText()+" - "+strBAName);
			
		}catch(Exception e){
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAPrevSummary~getSummary~baname error"+e.toString(),true);
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
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAPrevSummary~getSummary~auditon error"+e.toString(),true);
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
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAPrevSummary~getSummary~auditontime error"+e.toString(),true);
		}
		//Previous Overallcolor and Attitudecolor
		try
		{
			if(strOverallColor!=null)
			{
				if(strOverallColor.toString().equalsIgnoreCase("Red"))
				{
					txtOverallColor.setBackgroundColor(Color.RED);
				}
				else if(strOverallColor.toString().equalsIgnoreCase("Yellow"))
				{
					txtOverallColor.setBackgroundColor(Color.YELLOW);
				}
				else if(strOverallColor.toString().equalsIgnoreCase("Green"))
				{
					txtOverallColor.setBackgroundColor(Color.GREEN);
				}
			}
			if(strAttitudeColor!=null)
			{
				
				if(strAttitudeColor.toString().equalsIgnoreCase("Red"))
				{
					txtAttitudeColor.setBackgroundColor(Color.RED);
					txtAttitude.setText(getResources().getString(R.string.fairradio_text));
				}
				else if(strAttitudeColor.toString().equalsIgnoreCase("Yellow"))
				{
					txtAttitudeColor.setBackgroundColor(Color.YELLOW);
					txtAttitude.setText(getResources().getString(R.string.goodradio_text));
				}
				else if(strAttitudeColor.toString().equalsIgnoreCase("Green"))
				{
					txtAttitudeColor.setBackgroundColor(Color.GREEN);
					txtAttitude.setText(getResources().getString(R.string.vgoodradio_text));
				}
			}
		}catch(Exception e)
		{
			Log.i("Error",e.toString());
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAPrevSummary~getSummary~color error"+e.toString(),true);
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
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAPrevSummary~getSummary~auditby error"+e.toString(),true);
	
		}
		//Comments and Mlearning
		try
		{
			if(strComments!=null)txtComments.setText(strComments);
			if(strMlearning!=null)txtMlearning.setText(strMlearning);
			
		}catch(Exception e)
		{
			Log.i("Error",e.toString());
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAPrevSummary~getSummary~comments error"+e.toString(),true);
		}
		
		
	}
	//Create component(TextView) based on summary data
	@SuppressLint("NewApi")
	private void addComponent(String strparticular,String strscore)
	{
		TextView textView=null;
		LayoutParams layoutParams=null;
		//Layout
		LinearLayout linearLayout=Components.linearLayout();
		linearLayout.setWeightSum(2);
		//Section	
		textView=Components.textView(strparticular);
		textView.setTextColor(getResources().getColor(R.color.summary_datatxt_textcolor));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditsummary_data_textsize));
		textView.setGravity(Gravity.LEFT);
		textView.setSingleLine();
		textView.setEllipsize(TruncateAt.MARQUEE);
		textView.setSelected(true);
		layoutParams=new LayoutParams(0, 35);
		layoutParams.weight=1.6f;
		layoutParams.leftMargin=5;
		linearLayout.addView(textView, layoutParams);
		//score
		textView=Components.textView(strscore);
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditsummary_data_textsize));
		textView.setGravity(Gravity.CENTER);
		layoutParams=new LayoutParams(0, 35);
		layoutParams.weight=0.4f;
		layoutParams.leftMargin=5;
		linearLayout.addView(textView, layoutParams);
		//Add all component. 
		LayoutParams linearlayoutparms=new LayoutParams(LayoutParams.MATCH_PARENT,35);
		if(strparticular.equalsIgnoreCase("Section")||strparticular.equalsIgnoreCase("Total"))
		{
			linearLayout.setBackgroundResource(R.drawable.menu);
		}
		else
		{
			linearLayout.setBackgroundResource(R.drawable.transparentbg);
		}
		linearLayoutMain.setPadding(5,0,5,0);
		linearLayoutMain.addView(linearLayout,linearlayoutparms);
		textView=null;
		layoutParams=null;
		linearLayout=null;
	}
	//Back Event
	@Override
	public void onBackPressed()
	{
		back();
	}
	//Back, Move to BA's information screen
	private void back()
	{
		btnBack.setEnabled(false);
		intent=new Intent(BAPrevSummary.this,BASearchData.class);
		startActivity(intent);
		BAPrevSummary.this.finish();
	}
	@Override
	protected void onDestroy() 
	{
		btnNext=null;
		btnBack=null;
		btnAudit=null;
		intent=null;
		linearLayoutMain=null;
		txtAuditedby=null;
		txtDate=null;
		txtTime=null;
		txtHeading=null;
		if(handler!=null)handler.removeCallbacks(runnable);
		super.onDestroy();
	}
}