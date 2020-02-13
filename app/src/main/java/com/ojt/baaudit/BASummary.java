/*@ID: CN20140001
 *@Description: srcBASummary is for Audit Summary Screen 
 * This class is used to display summary report 
 * Content are fetched from server.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 15/03/2014
 * @Modified Date: 28/08/2014
 */
package com.ojt.baaudit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import com.ojt.home.Home;
import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.components.Components;
import com.ojt.database.OJTDAO;
import com.ojt.connectivity.JSONParser;
import com.ojt.utilities.Utility;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils.TruncateAt;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;


public class BASummary extends Activity implements OnClickListener
{
	private OJTDAO database=null;
	private ArrayList<String> auditSummary=null;
	private int intOverallScore;
	private int intTotalScore;
	private TextView txtDate=null;
	private TextView txtTime=null;
	private LinearLayout linearLayoutMain=null;
	private RelativeLayout relativeSignImage=null;
	private Button btnCaptImg=null,btnSubmit=null,btnScore=null,btnSignSubmit=null;
	private ImageView imgCap=null;
	private SingleTouchEvent detailView=null;
	private boolean hasBAPicture; 
	private Button btnClear=null,btnInfo=null,btnSearch=null,btnBack=null;
	private TextView txtHead=null;
	private int intOverallPer;
	private String strOverallColor=null;
	private String strHigherPriority1=null,strHigherPriority2=null;
	private int intHigherPriorScore1,intHigherPriorScore2;
	private RadioGroup radioGroup=null;
	private String strAttitudeColors[]={"Red","Yellow","Green"},
			strAttitudeColor=null,strBAName=null,strFilePath=null,strSignFilePath=null;
	private ScrollView layoutBg=null;
	boolean hasCheck=false;
	private Handler handlerTime=null;
	private Runnable runnable=null;
	private EditText edttxtComments=null,edttxtMlearning=null;
	private TextView txtOverallColor=null;
	private int intRedclrMax,intYellowclrMin,intYellowclrMax,intGreenclrMin;
	private ImageView imgSign=null;
	private RelativeLayout relativeLayoutSign;
	private RadioButton radioFair,radioGood,radioVgood;
	private int intSubmitstate=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.basummary);
		Utility.context=this;
		hasBAPicture=false;
		Utility.hasSignImage=false;
		auditSummary=new ArrayList<String>();
		strHigherPriority1=getResources().getString(R.string.higher_priority_mid1);
		strHigherPriority2=getResources().getString(R.string.higher_priority_mid2);
		
		imgSign=(ImageView) findViewById(R.id.imageView1);
		imgSign.setVisibility(View.INVISIBLE);
		imgSign.setDrawingCacheEnabled(true);
		imgSign.buildDrawingCache();
		
		txtOverallColor=(TextView) findViewById(R.id.overallclrtxt);
		txtHead=(TextView) findViewById(R.id.headingtxt);
		txtDate=(TextView)findViewById(R.id.datetxt);
		txtTime=(TextView)findViewById(R.id.timetxt);
	
		radioGroup=(RadioGroup) findViewById(R.id.attituderg);
		
		edttxtComments=(EditText) findViewById(R.id.commentsedttxt);
		edttxtComments.setFilters(new InputFilter[] { filter });
		edttxtComments.clearFocus();
		edttxtComments.setCursorVisible(false);
		edttxtComments.setMovementMethod(new ScrollingMovementMethod());
		edttxtMlearning=(EditText) findViewById(R.id.mlearningedttxt);
		edttxtMlearning.setMovementMethod(new ScrollingMovementMethod());
		edttxtMlearning.clearFocus();
		edttxtMlearning.setCursorVisible(false);
		
		btnCaptImg=(Button) findViewById(R.id.imgcapbtn);
		btnCaptImg.setOnClickListener(this);
		btnSubmit=(Button) findViewById(R.id.basubmitbtn);
		btnSubmit.setOnClickListener(this);
		btnSignSubmit=(Button) findViewById(R.id.signsumitbtn);
		btnSignSubmit.setOnClickListener(this);
		btnSignSubmit.setEnabled(false);
		btnClear=(Button) findViewById(R.id.summaryclearbtn);
		btnClear.setOnClickListener(this);
		btnInfo=(Button) findViewById(R.id.bainfobtn);
		btnInfo.setOnClickListener(this);
		btnSearch=(Button) findViewById(R.id.basearchbtn);
		btnSearch.setOnClickListener(this);
		btnBack=(Button) findViewById(R.id.backbtn);
		btnBack.setOnClickListener(this);
		btnScore=(Button) findViewById(R.id.auditscorebtn);
		btnScore.setOnClickListener(this);
		imgCap=(ImageView) findViewById(R.id.capimg);
		imgCap.setDrawingCacheEnabled(true);
	    imgCap.buildDrawingCache();
		linearLayoutMain=(LinearLayout) findViewById(R.id.summarylinear);
		
		relativeLayoutSign=(RelativeLayout) findViewById(R.id.basignlinear);
		detailView= new SingleTouchEvent(this.getApplicationContext(), null);
		relativeLayoutSign.addView(detailView);
		
		relativeSignImage=(RelativeLayout) findViewById(R.id.basignrelative);
		layoutBg=(ScrollView) findViewById(R.id.auditsummaryscroll);
		
		radioFair=(RadioButton) findViewById(R.id.fairradio);
		radioGood=(RadioButton) findViewById(R.id.goodradio);
		radioVgood=(RadioButton) findViewById(R.id.vgoodradio);
		
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() 
		{
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) 
			{
				RadioButton radioButton=(RadioButton) group.findViewById(checkedId);
				Utility.strAttitude=radioButton.getText().toString();
			}
		});

		getSummaryData();
		Utility.logFile("BASummary -> onCreate ->	 after getSummaryData()",true);
		
		if(!Utility.strComments.equalsIgnoreCase("No Comments"))
		{
			edttxtComments.setText(Utility.strComments);
		}
		edttxtComments.setOnTouchListener(new OnTouchListener() 
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				 edttxtComments.setCursorVisible(true);
		         v.getParent().requestDisallowInterceptTouchEvent(true);
	             switch (event.getAction() & MotionEvent.ACTION_MASK)
	             {
	             	case MotionEvent.ACTION_UP:
	             		v.getParent().requestDisallowInterceptTouchEvent(false);
	             		break;
	             }
	             return false;
			}
		});
		if(!Utility.strMlearning.equalsIgnoreCase("Nil"))
		{
			edttxtMlearning.setText(Utility.strMlearning);
		}
		edttxtMlearning.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "1440")});
		edttxtMlearning.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				edttxtMlearning.setCursorVisible(true);
				v.getParent().requestDisallowInterceptTouchEvent(true);
	             switch (event.getAction() & MotionEvent.ACTION_MASK)
	             {
	             	case MotionEvent.ACTION_UP:
	             		v.getParent().requestDisallowInterceptTouchEvent(false);
	             		break;
	             }
				return false;
			}
		});
		
		SharedPreferences preference = getSharedPreferences("OJTSession", MODE_PRIVATE);
		if(preference.getBoolean("subScreen", false))
		{
			 relativeSignImage.setVisibility(View.VISIBLE);
			 edttxtComments.setFocusable(false);
			 edttxtComments.setFocusableInTouchMode(false); 
			 edttxtComments.setClickable(false); 
			 
			 edttxtMlearning.setFocusable(false);
			 edttxtMlearning.setFocusableInTouchMode(false); 
			 edttxtMlearning.setClickable(false);
			 
			 radioFair.setEnabled(false);
			 radioGood.setEnabled(false);
			 radioVgood.setEnabled(false);
		}
		else
		{
			 relativeSignImage.setVisibility(View.INVISIBLE);
			 edttxtComments.setFocusable(true);
			 edttxtComments.setFocusableInTouchMode(true); 
			 edttxtComments.setClickable(true); 
			 
			 edttxtMlearning.setFocusable(true);
			 edttxtMlearning.setFocusableInTouchMode(true); 
			 edttxtMlearning.setClickable(true);
			 
			 radioFair.setEnabled(true);
			 radioGood.setEnabled(true);
			 radioVgood.setEnabled(true);
		}
		try
		{
			String strCurrentDate=Utility.currentDate();
			if(strCurrentDate!=null)
			{
				if(strCurrentDate.indexOf("-")!=-1)
				{
					String strDate[]=strCurrentDate.split("-");
					if(strDate.length>0&&strDate.length<4)
					{
						txtDate.setText("Date : "+strDate[2]+"-"+strDate[1]+"-"+strDate[0]);
					}
				}
			}
		}
		catch(Exception e)
		{
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName
					+"~BASummary~oncreate~date error"+e.toString(),true);
		}
		txtTime.setText("Time : "+Utility.currentTimesecond());
		//BAName
		try
		{
			strBAName=Utility.getBAName();
			if(strBAName!=null)	txtHead.setText(txtHead.getText()+" - "+strBAName);
			
		}catch(Exception e){
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAAuditScore~addForm~baname error"+e.toString(),true);
		}
		// Handle the soft keyboard 
		layoutBg.setOnTouchListener(new OnTouchListener(){
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
		setSummary();
		setAttitude();
		timeLimit();
		hasLastAudit();
		Utility.logFile("BASummary -> onCreate ->	 at last of oncreate()",true);
	}
	//Store data while minimize the screen
	@Override
	protected void onPause() 
	{
		super.onPause();
		if(intSubmitstate==1)return;
		getAttitudeColor();
		Utility.logFile("BASummary-> onPause -> before store data		",true);
		storeData("0");
		if(relativeSignImage.getVisibility()==View.VISIBLE)
		{
			Utility.setLastActivity(true,Utility.strAuName);
		}
		else
		{
			Utility.setLastActivity(false,Utility.strAuName);
		}
	}
	//Get Summary data from loacal databse
	private void getSummaryData()
	{
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		String strTemp=null;
		
		OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursor=database.getVal("status=?", new String[]{"0"}, strAuditData);
		if(cursor!=null)
		{
			Utility.logFile("BASummary -> onCreate ->	 getCount:-		"+cursor.getCount(),true);
			if(cursor.moveToFirst())
			{
				//Mlearning
				Utility.strMlearning=""+cursor.getString(cursor.getColumnIndex("mlearning")).replace("+"," ");
				if(Utility.strMlearning.contains("minute"))
				{
					Utility.strMlearning=Utility.strMlearning.replace("minutes", "");
					Utility.strMlearning=Utility.strMlearning.replace("minute", "");
				}
				//Comments
				Utility.strComments=""+cursor.getString(cursor.getColumnIndex("comments")).replace("+"," ");
				//Attitude
				strTemp=""+cursor.getString(cursor.getColumnIndex("attitudecolor"));
				if(!strTemp.equalsIgnoreCase("null"))
				{
					for(int i=0;i<strAttitudeColors.length;i++)
					{
						if(strTemp.equalsIgnoreCase(strAttitudeColors[i]))
						{
							RadioButton radioButton=(RadioButton) radioGroup.getChildAt(i);
							radioButton.setSelected(true);
							Utility.strAttitude=radioButton.getText().toString();
						}
					}
				}
				//Image and Sign file path
				strFilePath=cursor.getString(cursor.getColumnIndex("bppath"));
				strSignFilePath=cursor.getString(cursor.getColumnIndex("sipath"));
			}
			cursor.close();
		}
		database.close();
	}
	//Set Attitude color
	private void setAttitude() 
	{
		if(Utility.strAttitude==null)return;
		for(int i=0;i<radioGroup.getChildCount();i++)
		{
			RadioButton radioButton=(RadioButton)radioGroup.getChildAt(i);
			if(Utility.strAttitude.equalsIgnoreCase(radioButton.getText().toString()))
			{
				hasCheck=true;
				strAttitudeColor=strAttitudeColors[i];
				radioButton.setChecked(true);
				break;
			}
		}
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
				btnScore.setEnabled(true);
				btnScore.setBackgroundResource(R.drawable.auditscorebtn);
			}
			else
			{
				btnScore.setEnabled(false);
				btnScore.setBackgroundResource(R.drawable.auditscorebtn_disable);
			}
			cursor.close();
		}
		else
		{
			btnScore.setEnabled(false);
			btnScore.setBackgroundResource(R.drawable.auditscorebtn_disable);
		}
		
		database.close();
		
		strLogin=null;
		strMainSection=null;
		strSection=null;
		strAuditData=null;
		cursor=null;
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
				final Intent mainIntent = new Intent(BASummary.this, Login.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				BASummary.this.startActivity(mainIntent);
				BASummary.this.finish();
			}
		};
		handlerTime.postDelayed(runnable, (Utility.intTimeout*1000));
	}
	//Summary Report 
	private void setSummary()
	{
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		String strMainid=null,strMainName=null;
		float floatVal;
		Cursor cursorMain=null;
		
		auditSummary.clear();
		database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		cursorMain=database.getAll(strMainSection);
		if(cursorMain!=null)
		{
			if(cursorMain.moveToFirst())
			{
				intTotalScore=0;
				intOverallScore=0;
				do
				{
					strMainid=cursorMain.getString(cursorMain.getColumnIndex("id"));
					if(!getResources().getString(R.string.no_score_mid).equalsIgnoreCase(strMainid))
					{
						strMainName=cursorMain.getString(cursorMain.getColumnIndex("name"));
						intTotalScore=cursorMain.getInt(cursorMain.getColumnIndex("score"));
						if(strMainid.equalsIgnoreCase(strHigherPriority1))
						{
							intHigherPriorScore1=intTotalScore;
						}
						else if(strMainid.equalsIgnoreCase(strHigherPriority2))
						{
							intHigherPriorScore2=intTotalScore;
						}
						intOverallScore=intOverallScore+intTotalScore;
						auditSummary.add(strMainid+"-"+strMainName+"-"+intTotalScore);
					}
				}while(cursorMain.moveToNext());
			}
			cursorMain.close();
		}
		database.close();
		
		String strdata[];
		addHeading("Section", "Score");
		for(int i=0;i<auditSummary.size();i++)
		{
			strdata=auditSummary.get(i).split("-");
			if(strdata!=null&&strdata.length>0)
				addHeading(strdata[1], strdata[2]);
		}
		addHeading("Total", ""+intOverallScore);
		
		//Overallscore percentage
		floatVal=(float)intOverallScore/auditSummary.size();
		intOverallPer=(int) (floatVal*100);
		
		intRedclrMax=Integer.parseInt(getResources().getString(R.string.strredclrmax));
		intYellowclrMin=Integer.parseInt(getResources().getString(R.string.stryellowclrmin));
		intYellowclrMax=Integer.parseInt(getResources().getString(R.string.stryellowclrmax));
		intGreenclrMin=Integer.parseInt(getResources().getString(R.string.strgreenclrmin));
		//Overallcolor grade logic
		if(intOverallScore<=intRedclrMax&&intHigherPriorScore1==0)
		{
			strOverallColor="Red";
			txtOverallColor.setBackgroundColor(Color.RED);
		}
		else if(intOverallScore<=intRedclrMax&&intHigherPriorScore1==1)
		{
			strOverallColor="Yellow";
			txtOverallColor.setBackgroundColor(Color.YELLOW);
		}
		else if(intOverallScore>=intYellowclrMin && intOverallScore<=intYellowclrMax)
		{
			strOverallColor="Yellow";
			txtOverallColor.setBackgroundColor(Color.YELLOW);
		}
		else if(intOverallScore>=intGreenclrMin&&intHigherPriorScore1==1&&intHigherPriorScore2==1)
		{
			strOverallColor="Green";
			txtOverallColor.setBackgroundColor(Color.GREEN);
		}
		else if(intOverallScore>=intGreenclrMin&&intHigherPriorScore1==0&&intHigherPriorScore2==0)
		{
			strOverallColor="Yellow";
			txtOverallColor.setBackgroundColor(Color.YELLOW);
		}
		else if(intOverallScore>=intGreenclrMin&&intHigherPriorScore1==0&&intHigherPriorScore2==1)
		{
			strOverallColor="Yellow";
			txtOverallColor.setBackgroundColor(Color.YELLOW);
		}
		else if(intOverallScore>=intGreenclrMin&&intHigherPriorScore1==1&&intHigherPriorScore2==0)
		{
			strOverallColor="Yellow";
			txtOverallColor.setBackgroundColor(Color.YELLOW);
		}
		
		if(strFilePath!=null)
		{
			File file=new File(strFilePath);
			if(file.exists())
			{
				 Drawable drawable = (Drawable) 
                 Drawable.createFromPath(file.getAbsolutePath());
				 imgCap.setImageDrawable(drawable);
				 btnSignSubmit.setEnabled(true);
				 hasBAPicture=true;
			}
			else
			{
				imgCap.setImageResource(R.drawable.capimg);
				btnSignSubmit.setEnabled(false);
				hasBAPicture=false;
			}
		}
		
		if(strSignFilePath!=null)
		{
			File file=new File(strSignFilePath);
			if(file.exists())
			{
				Drawable drawable = (Drawable) 
	            Drawable.createFromPath(file.getAbsolutePath());
				imgSign.setImageDrawable(drawable);
				imgSign.setVisibility(View.VISIBLE);
				Utility.hasSignImage=true;
				detailView.setVisibility(View.INVISIBLE);
			}
			else
			{
				Utility.hasSignImage=false;
				detailView.setVisibility(View.VISIBLE);
				imgSign.setVisibility(View.INVISIBLE);
			}
		}
		else
		{
			Utility.hasSignImage=false;
			detailView.setVisibility(View.VISIBLE);
			imgSign.setVisibility(View.INVISIBLE);
		}
		strLogin=null;
		strMainSection=null;
		strSection=null;
		strAuditData=null;
		strMainid=null;
		strMainName=null;
		cursorMain=null;
	}
	//Create component based on summary data
	
	private void addHeading(String strparticular,String strscore)
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
		layoutParams=new LayoutParams(LayoutParams.MATCH_PARENT,35);
		if(strparticular.equalsIgnoreCase("Section")||strparticular.equalsIgnoreCase("Total"))
		{
			linearLayout.setBackgroundResource(R.drawable.menu);
		}
		else
		{
			linearLayout.setBackgroundResource(R.drawable.transparentbg);
		}
		linearLayoutMain.setPadding(5,0,5,0);
		linearLayoutMain.addView(linearLayout,layoutParams);
		textView=null;
		layoutParams=null;
		linearLayout=null;
	}
	//Image capture
	private void capturePicture()
	{
		hasBAPicture=false;
		Intent intent;
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
	@Override
	public void onClick(View v) 
	{
		if(v==btnCaptImg)
		{
			capturePicture(); //Take Photo
		}
		else if(v==btnSubmit) //Submit icon. Show Image and Sign cature screen
		{
			if(relativeSignImage.getVisibility()==View.INVISIBLE)
			{
				getAttitudeColor();
				if(hasCheck)
				{
					submit();
				}
				else
				{
					Utility.alert(getResources().getString(R.string.empty_attitude));
				}
			}
		}
		else if(v==btnSignSubmit) // Submit button(Placed on image capture screen). Upload all submit data to server.
		{
			Utility.logFile("BASummary-> on submit click",true);
			storeData("1");
			uploadData();
		}
		else if(v==btnClear) // Clear. Clear the sign
		{
			clear();
		}
		else if(v==btnInfo)// BA's Info. Move to BA's Information screen
		{
			if(relativeSignImage.getVisibility()==View.INVISIBLE)
			{
				btnInfo.setEnabled(false);
				getAttitudeColor();
				Utility.logFile("BASummary-> btnInfo click -> before store data		",true);
				storeData("0");
				startActivity(new Intent(BASummary.this,BASearchData.class));
				BASummary.this.finish();
			}
		}
		else if(v==btnSearch)// BA Search. Move to BA Search screen
		{
			if(relativeSignImage.getVisibility()==View.INVISIBLE)
			{
				 getAttitudeColor();
				Utility.logFile("BASummary-> btn search -> before store data		",true);
				 storeData("0");
				if(Utility.auditStart())
				{
					 AlertDialog.Builder builder = new AlertDialog.Builder(Utility.context);
					 builder.setMessage(getResources().getString(R.string.auditloss_msg)).setPositiveButton(Utility.context.getResources().getString(R.string.yes_msg), new DialogInterface.OnClickListener() 
					 {
						 @Override
						 public void onClick(DialogInterface dialog, int which) 
						 {
							 dialog.cancel();
							 btnSearch.setEnabled(false);
							 startActivity(new Intent(BASummary.this,BASearch.class));
							 BASummary.this.finish();
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
					AlertDialog.Builder builder = new AlertDialog.Builder(Utility.context);
					 builder.setMessage(getResources().getString(R.string.auditempty_msg)).setPositiveButton(Utility.context.getResources().getString(R.string.yes_msg), new DialogInterface.OnClickListener() 
					 {
						 @Override
						 public void onClick(DialogInterface dialog, int which) 
						 {
							 dialog.cancel();
							 btnSearch.setEnabled(false); 
							 startActivity(new Intent(BASummary.this,BASearch.class));
							 BASummary.this.finish();
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
			}
		}
		else if(v==btnBack)// Back. Move to Audit Form screen
		{
			back();
		}
		else if(v==btnScore)// Last score. Move to Last Audit Score screen
		{	
			if(relativeSignImage.getVisibility()==View.INVISIBLE)
			{
				btnScore.setEnabled(false);
				getAttitudeColor();
				Utility.logFile("BASummary-> BAAuditScore -> before store data		",true);
			    storeData("0");
				startActivity(new Intent(BASummary.this,BAAuditScore.class));
				BASummary.this.finish();
			}
		}
	}
	//Get Attitude colour
	private void getAttitudeColor()
	{
		strAttitudeColor=null;
		for(int i=0;i<radioGroup.getChildCount();i++)
		{
			RadioButton radioButton=(RadioButton)radioGroup.getChildAt(i);
			if(radioButton.isChecked())
			{
				hasCheck=true;
				strAttitudeColor=strAttitudeColors[i];
				break;
			}
		}
	}
	//Back event
	@Override
	public void onBackPressed() 
	{
		back();
	}
	//Back to Audit Form screen
	private void back() 
	{
		if(relativeSignImage.getVisibility()==View.INVISIBLE)
		{   
			btnBack.setEnabled(false);
			storeData("0");
			Utility.logFile("BASummary-> BAAuditForm -> before store data		",true);
			startActivity(new Intent(BASummary.this,BAAuditForm.class));
			BASummary.this.finish();
		}
	}
	//Clear the signature
	private void clear() 
	{
		if(detailView!=null)
		{
			relativeLayoutSign.removeView(detailView);
		}
		detailView= new SingleTouchEvent(this.getApplicationContext(), null);
		relativeLayoutSign.addView(detailView);
		detailView.setVisibility(View.VISIBLE);
		imgSign.setVisibility(View.INVISIBLE);
		Utility.hasSignImage=false;
	}
	//Upload image and summary data to server
	private void  storeData(String strStatus)
	{
		Utility.logFile("BASummary-> storeData -> strStatus		"+strStatus,true);
		if(strStatus.equalsIgnoreCase("1"))
			relativeSignImage.setVisibility(View.INVISIBLE);
		//Store image in sd card data into database
		String strBAImagePath=storeBAImage();
		String strSignImagePath=storeSignImage();
		Utility.strComments=edttxtComments.getText().toString();
		Utility.strMlearning=edttxtMlearning.getText().toString();
		
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		
		ContentValues contentValues=new ContentValues(); 
		contentValues.put("summary", auditSummary.toString());
		contentValues.put("overallscore", intOverallScore);
		contentValues.put("overallper", intOverallPer);
		contentValues.put("overallcolor", strOverallColor);
		contentValues.put("attitudecolor", strAttitudeColor);
		contentValues.put("aend", Utility.currentDate()+" "+Utility.currentTimesecond());
		contentValues.put("status", strStatus);
		
		if(Utility.strComments.length()>0)
		{
			contentValues.put("comments", Utility.strComments.replace(" ","+"));
		}
		else
		{
			contentValues.put("comments", "No+Comments");
		}
		
		if(Utility.strMlearning.length()>0)
		{
			if(Utility.strMlearning.equalsIgnoreCase("1"))
			{
				 Utility.strMlearning=Utility.strMlearning+" "+"minute";
			}
			else
			{
				Utility.strMlearning=Utility.strMlearning+" "+"minutes";
			}
			
			contentValues.put("mlearning", Utility.strMlearning.replace(" ","+"));
		}
		else
		{
			contentValues.put("mlearning", "Nil");
		}
		
		if(strBAImagePath!=null)
			contentValues.put("bppath", strBAImagePath);
		else
			contentValues.put("bppath", "");
		
		if(strSignImagePath!=null)
			contentValues.put("sipath", strSignImagePath);
		else
			contentValues.put("sipath", "");
		
		database.update(contentValues, strAuditData, "status=?", new String[]{"0"});
		Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" +
				Utility.strAuName + "data saved into database", true);
		database.close();
		
		database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursor=database.getAll(strAuditData);
		if(cursor!=null)
		{
			Utility.logFile("BASummary-> storeData -> cursor.getCount()		"+cursor.getCount(),true);
			if(cursor.moveToFirst())
			{
				do
				{
					Log.i("status",""+cursor.getString(cursor.getColumnIndex("status")));
					Log.i("bppath",""+cursor.getString(cursor.getColumnIndex("bppath")));
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		database.close();
		strBAImagePath=null;
		strSignImagePath=null;
		strLogin=null;
		strMainSection=null;
		strSection=null;
		strAuditData=null;
		contentValues=null;
	}
	private void uploadData()
	{
		intSubmitstate=1;
		SharedPreferences preference = getSharedPreferences("OJTSession",MODE_PRIVATE);
		Editor editor = preference.edit();
		editor.putString("lastActivity", "");
		editor.commit();
		if(Utility.hasConnection())
		{
			Utility.logFile("BASummary-> uploadData -> Utility.hasConnection() == true",true);
			sendData();
			AlertDialog.Builder builder = new AlertDialog.Builder(Utility.context);
			 builder.setMessage(getResources().getString(R.string.success_msg)).setPositiveButton(Utility.context.getResources().getString(R.string.ok_msg), new DialogInterface.OnClickListener() 
			 {
				 @Override
				 public void onClick(DialogInterface dialog, int which) 
				 {
					 try {
						 Thread.sleep(2000);
					 } catch (InterruptedException e) {
						 e.printStackTrace();
					 }
					 dialog.cancel();
					 Intent intent=new Intent(BASummary.this,Home.class); 
					 startActivity(intent);
					 BASummary.this.finish();
					 
				 }
			 }).setCancelable(false).show(); 
			
		}
		else
		{
			Utility.logFile("BASummary-> uploadData => No network connection-> Utility.hasConnection() == false",true);
			AlertDialog.Builder builder = new AlertDialog.Builder(Utility.context);
			 builder.setMessage(getResources().getString(R.string.failure_msg)).setPositiveButton(Utility.context.getResources()
					 .getString(R.string.ok_msg), new DialogInterface.OnClickListener()
			 {
				 @Override
				 public void onClick(DialogInterface dialog, int which) 
				 {
					 dialog.cancel();
					 Intent intent=new Intent(BASummary.this,Home.class); 
					 startActivity(intent);
					 BASummary.this.finish();
					 
				 }
			 }).setCancelable(false).show(); 
		}
		
	}
	//Store sign image in sd card
	private String storeSignImage() 
	{
		String strFilePath=null;
		if(Utility.hasSignImage)
		{
			Bitmap bitmap;
			if(imgSign.getVisibility()==View.INVISIBLE)
			{
				bitmap = Bitmap.createBitmap(detailView.getWidth(), detailView.getHeight(),Bitmap.Config.RGB_565);
				Canvas canvas = new Canvas(bitmap);
				canvas.drawColor(Color.WHITE);
				canvas.drawBitmap(bitmap, 0, 0, null);
				detailView.draw(canvas);
			}
			else
			{
				bitmap = imgSign.getDrawingCache();
			}
	        File file = new File(Environment.getExternalStorageDirectory() + "/"+ getResources().getString(R.string.app_name)+"/Sign_Image");
			file.mkdirs();
			final File fileOutput = new File(file, "Signimg_"+System.currentTimeMillis()+".JPEG");
		      
	        FileOutputStream fileOutputStream;
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
			} catch (FileNotFoundException e) 
			{
				Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASummary~storeSignImage~storeSignImage error"+e.toString() ,true);
				Log.i("FileNotFoundException",e.toString());
			} catch (IOException e)
			{
				Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASummary~storeSignImage~storeSignImage error"+e.toString() ,true);
				Log.i("IOException",e.toString());
			}
			file=null;
			fileOutputStream=null;
			bitmap=null;
		}
		return strFilePath;
	}
	//Store BA's Picture in sd card.
	private String storeBAImage() 
	{
		String strfilePath=null;
		if(hasBAPicture)
		{
			Bitmap bitmap = imgCap.getDrawingCache();
			File file = new File(Environment.getExternalStorageDirectory() + "/"+ getResources().getString(R.string.app_name)+"/BAs_Picture");
			file.mkdirs();
			File fileOutput = new File(file, "BAimg_"+System.currentTimeMillis()+".JPEG");
		    FileOutputStream fileOutputStream;
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
				strfilePath=fileOutput.getAbsolutePath();
			} catch (FileNotFoundException e)
			{
				Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASummary~storeBAImage~storeBAImage error"+e.toString() ,true);
				Log.i("FileNotFoundException",e.toString());
			} catch (IOException e) 
			{
				Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BASummary~storeBAImage~storeBAImage error"+e.toString() ,true);
				Log.i("IOException",e.toString());
			}
			file=null;
			fileOutputStream=null;
			bitmap=null;
		}
		return strfilePath;
	}
	//Upload data and image to server
	private void sendData() 
	{
		new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				String strBatchNo=Utility.updateBatchno(true,"0");
				Utility.logFile("BASummary-> sendData -> NewBatNo:		"+strBatchNo,true);
				JSONParser.sendData(getResources().getString(R.string.server_url)+"AuditingReport",strBatchNo);
			}
		}).start();
	}

	//Submit. Show Sign and image capture layout for take picture and get sign.
	private void submit() 
	{
		 AlertDialog.Builder builder = new AlertDialog.Builder(Utility.context);
		 builder.setMessage(getResources().getString(R.string.basubmitbtn_msg)).
				 setPositiveButton(Utility.context.getResources().getString(R.string.yes_msg), new DialogInterface.OnClickListener()
		 {
			 @Override
			 public void onClick(DialogInterface dialog, int which) 
			 {
				 dialog.cancel();
				 relativeSignImage.setVisibility(View.VISIBLE);
				 edttxtComments.setFocusable(false);
				 edttxtComments.setFocusableInTouchMode(false); 
				 edttxtComments.setClickable(false); 
				 
				 edttxtMlearning.setFocusable(false);
				 edttxtMlearning.setFocusableInTouchMode(false); 
				 edttxtMlearning.setClickable(false); 
				 btnSubmit.setEnabled(false);
				 
				 radioFair.setEnabled(false);
				 radioGood.setEnabled(false);
				 radioVgood.setEnabled(false);
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
	//Take photo and load imageview
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{  
		 if (requestCode == 1 && resultCode == RESULT_OK && data!=null)
		 { 
			 if( data.getExtras()!=null)
			 {
		    	 Bitmap bitmap = (Bitmap) data.getExtras().get("data"); 
			     imgCap.setImageBitmap(bitmap);
			     imgCap.setDrawingCacheEnabled(true);
			     imgCap.buildDrawingCache();
			     hasBAPicture=true;
			     btnSignSubmit.setEnabled(true);
			 }
		 } 
	}
	//Edittext filter for accept only alphabets.
	private InputFilter filter = new InputFilter() 
	{
		@Override
		public CharSequence filter(CharSequence source, int start, int end,
				Spanned dest, int dstart, int dend) 
		{ 
			if (source instanceof SpannableStringBuilder)
			{
				SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
				for (int i = end - 1; i >= start; i--) 
				{ 
					char currentChar = source.charAt(i);
					if (!Character.isLetter(currentChar) && !Character.isSpaceChar(currentChar) && currentChar!='.') 
					{    
						sourceAsSpannableBuilder.delete(i, i+1);
					}     
				}
				return source;
	        } 
			else 
			{
	            StringBuilder filteredStringBuilder = new StringBuilder();
	            for (int i = start; i < end; i++) 
	            { 
	                char currentChar = source.charAt(i);
	                if (Character.isLetter(currentChar) || Character.isSpaceChar(currentChar) || currentChar=='.') 
	                {    
	                    filteredStringBuilder.append(currentChar);
	                }  
	                
	             }
	            return filteredStringBuilder.toString();
	        }
		}
	};
	//Edittext filter , only accept 1 to 1440
	private class InputFilterMinMax implements InputFilter 
	{
	    private int min, max;
	    @SuppressWarnings("unused")
		public InputFilterMinMax(int min, int max)
	    {
	        this.min = min;
	        this.max = max;
	    }
	    public InputFilterMinMax(String min, String max) 
	    {
	        this.min = Integer.parseInt(min);
	        this.max = Integer.parseInt(max);
	    }
	    @Override
	    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {   
	        try 
	        {
	            int input = Integer.parseInt(dest.toString() + source.toString());
	            if (isInRange(min, max, input))
	                return null;
	        } catch (NumberFormatException nfe) { }     
	        return "";
	    }
	    private boolean isInRange(int a, int b, int c) 
	    {
	        return b > a ? c >= a && c <= b : c >= b && c <= a;
	    }
	}
	@Override
	protected void onDestroy() 
	{
		if(handlerTime!=null)handlerTime.removeCallbacks(runnable);
		database=null;
		auditSummary=null;
		txtDate=null;
		txtTime=null;
		linearLayoutMain=null;
		relativeLayoutSign=null;
		relativeSignImage=null;
		btnCaptImg=null;
		btnSubmit=null;
		btnScore=null;
		btnSignSubmit=null;
		imgCap=null;
		detailView=null;
		btnClear=null;
		btnInfo=null;
		btnSearch=null;
		btnBack=null;
		txtHead=null;
		strOverallColor=null;
		strHigherPriority1=null;
		strHigherPriority2=null;
		radioGroup=null;
		strAttitudeColors=null;
		strAttitudeColor=null;
		layoutBg=null;
		handlerTime=null;
		runnable=null;
		edttxtComments=null;
		edttxtMlearning=null;
		txtOverallColor=null;
		super.onDestroy();
	}
}