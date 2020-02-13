/*@ID: CN20140001
 *@Description: srcBAAuditForm is for Audit Form Screen 
 * This class is used to show the new audit form.
 * Content are fetched from server.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 19/03/2014
 * @Modified Date: 28/08/2014
 */
package com.ojt.baaudit;
import java.util.ArrayList;

import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.components.Components;
import com.ojt.database.OJTDAO;
import com.ojt.training.Training;
import com.ojt.utilities.Utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class BAAuditForm extends Activity implements OnClickListener
{
	private int intSno,intPrevID,intID;
	private LinearLayout linearLayoutMain=null;
	private ArrayList<String> mainSection=null,subSection=null;
	private String strID=null;
	private TextView txtDate=null,txtTime=null,txtHeading=null;
	private LinearLayout linearLayoutHeading=null;
	private Button btnNext=null,btnInfo=null,btnSummary=null,btnScore=null,btnTraining=null;
	private ScrollView scrollView=null;
	private Handler handler=null;
	private Runnable runnable=null;
	private InputMethodManager inputMethodManager=null;
	private boolean isEmpty=false;
	private OJTDAO database=null;
	private SharedPreferences preference=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baauditform);
		Utility.context=this;
		preference = getSharedPreferences("OJTSession", MODE_PRIVATE);
		Editor editor=preference.edit();
		editor.putBoolean("AuditStart", true);
		editor.commit();
		inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		Utility.txtCountAudit=(TextView) findViewById(R.id.msgcounttxt);
		
		btnNext=(Button)findViewById(R.id.nextbtn);
		btnNext.setOnClickListener(this);
		btnInfo=(Button) findViewById(R.id.bainfobtn);
		btnInfo.setOnClickListener(this);
		btnSummary=(Button)findViewById(R.id.auditsummarybtn);
		btnSummary.setOnClickListener(this);
		btnScore=(Button) findViewById(R.id.auditscorebtn);
		btnScore.setOnClickListener(this);
		btnTraining=(Button) findViewById(R.id.trainingbtn);
		btnTraining.setOnClickListener(this);
		
		mainSection=new ArrayList<String>();
		subSection=new ArrayList<String>();
		
		txtHeading=(TextView) findViewById(R.id.headingtxt);
		txtDate=(TextView)findViewById(R.id.datetxt);
		txtTime=(TextView)findViewById(R.id.timetxt);
		
		linearLayoutMain=(LinearLayout) findViewById(R.id.auditformlinear);
		linearLayoutHeading=(LinearLayout) findViewById(R.id.headinglinear);
		
		scrollView=(ScrollView) findViewById(R.id.auditformscroll);
		// Handle the soft keyboard 
		scrollView.setOnTouchListener(new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event) 
			{
				if(v instanceof EditText)
				{
					 ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(v,InputMethodManager.SHOW_FORCED);
				}
				else
				{
					if(getCurrentFocus() instanceof EditText){
						EditText editText=(EditText) getCurrentFocus();
						editText.clearFocus();
						editText.setCursorVisible(false);
						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
					}
				}
				return false;
			}
		});
		addForm();
		timeLimit();
		hasLastAudit();
		Utility.pushCount();
	}
	//Store data while minimize the screen
	@Override
	protected void onPause() 
	{
		super.onPause();
		saveData();
		Utility.setLastActivity(false,Utility.strAuName);
	}
	//Check Previous audit data is available or not 
	private void hasLastAudit() 
	{
		Cursor cursor=database.getAll("lastmainscore");
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				btnSummary.setEnabled(true);
				btnSummary.setBackgroundResource(R.drawable.auditsummarybtn);
				
				btnScore.setEnabled(true);
				btnScore.setBackgroundResource(R.drawable.auditscorebtn);
			}
			else
			{
				btnSummary.setEnabled(false);
				btnSummary.setBackgroundResource(R.drawable.auditsummarybtn_disable);
				
				btnScore.setEnabled(false);
				btnScore.setBackgroundResource(R.drawable.auditscorebtn_disable);
			}
		}
		else
		{
			btnSummary.setEnabled(false);
			btnSummary.setBackgroundResource(R.drawable.auditsummarybtn_disable);
			
			btnScore.setEnabled(false);
			btnScore.setBackgroundResource(R.drawable.auditscorebtn_disable);
		}
		cursor.close();
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
				final Intent mainIntent = new Intent(BAAuditForm.this, Login.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); 
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				BAAuditForm.this.startActivity(mainIntent);
				BAAuditForm.this.finish();
			}
		};
		handler.postDelayed(runnable, (Utility.intTimeout*1000));
	}
	//Create new form with current data and time
	private void addForm()
	{
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
			Log.i("Error",e.toString());
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAAuditForm~addForm"+"~time date error"+e.toString(),true);
		}
		txtTime.setText("Time : "+Utility.currentTimesecond());
		
		//BAName
		try
		{
			String strBAName=Utility.getBAName();
			if(strBAName!=null)	txtHeading.setText(txtHeading.getText()+" - "+strBAName);
			
		}catch(Exception e)
		{
			Log.i("Error",e.toString());
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAAuditForm~addForm~baname error"+e.toString(),true);
		}
		addHeading();
		addData();
	}
	//Load data from local database
	private void addData() 
	{
		Cursor cursorMain=null,cursorSub=null;
		String strTraining=null;
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		int i=1,intid=-1,intpsno;
		boolean check=false;
		String strMainid,strMainname,strSubid,strSubname;
		database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		mainSection.clear();
		subSection.clear();
		cursorMain=database.getAll(strMainSection);
		
		if(cursorMain!=null)
		{
			if(cursorMain.moveToFirst())
			{
				do
				{
					intid++;
					strMainid=cursorMain.getString(cursorMain.getColumnIndex("id"));
					strMainname=cursorMain.getString(cursorMain.getColumnIndex("name"));
					strTraining=cursorMain.getString(cursorMain.getColumnIndex("training"));
					//Add main section
					ContentValues contentvaluesMain=new ContentValues();
					contentvaluesMain.put("sno",intid);
					database.update(contentvaluesMain, strMainSection, "id=?", new String[]{strMainid});
					check=database.isAvailable("mid=?", new String[]{strMainid},strSection);
					if(getResources().getString(R.string.no_score_mid).equalsIgnoreCase(strMainid))
					{
						addComponent(strTraining,""+i,strMainname,intid,-1,cursorMain.getString(cursorMain.getColumnIndex("remarks")),check);
					}
					else
					{
						addComponent(strTraining,""+i,strMainname,intid,cursorMain.getInt(cursorMain.getColumnIndex("score")),cursorMain.getString(cursorMain.getColumnIndex("remarks")),check);
					}
					//Add sub section
					cursorSub=database.getVal("mid=?", new String[]{strMainid},strSection);
					if(cursorSub!=null){
						if(cursorSub.moveToFirst()){
							intpsno=intid;
							do{
								intid++;
								strSubid=cursorSub.getString(cursorSub.getColumnIndex("id"));
								strSubname=cursorSub.getString(cursorSub.getColumnIndex("name"));
								strTraining=cursorSub.getString(cursorSub.getColumnIndex("training"));
								
								ContentValues contentvaluesSub=new ContentValues();
								contentvaluesSub.put("psno",intpsno);
								contentvaluesSub.put("sno",intid);
								database.update(contentvaluesSub, strSection, "id=?", new String[]{strSubid});
								if(getResources().getString(R.string.no_score_mid).equalsIgnoreCase(strSubid))
								{
									addComponent(strTraining,"",strSubname,intid,-1,cursorSub.getString(cursorSub.getColumnIndex("remarks")),false);
								}
								else
								{
									addComponent(strTraining,"",strSubname,intid,cursorSub.getInt(cursorSub.getColumnIndex("score")),cursorSub.getString(cursorSub.getColumnIndex("remarks")),false);
								}
							}while(cursorSub.moveToNext());
						}
						cursorSub.close();
					}
					i++;
				}while(cursorMain.moveToNext());
				cursorMain.close();
			}
		}
		ContentValues contentvalues=new ContentValues(); 
		contentvalues.put("astart", Utility.currentDate()+" "+Utility.currentTimesecond());
		database.update(contentvalues, strAuditData, "status=?", new String[]{"0"});
		strTraining=null;
		strLogin=null;
		strMainSection=null;
		strSection=null;
		strAuditData=null;
		strMainid=null;
		strMainname=null;
		strSubid=null;
		strSubname=null;
		cursorMain=null;
		cursorSub=null;
	}
	//Create component(TextView, CheckBox,EditText) based on data
	private void addComponent(String strTraining,String strSNo,String strParticular,int intCompID,int intScore,String strRemarks,boolean check)
	{
		try
		{
			TextView textView=null;
			LayoutParams layoutParams=null;
			CheckBox checkBox=null;
			LinearLayout linearLayout=null;
			EditText editText=null;
			//Main Layout
			LinearLayout linearLayoutSubHeading=Components.linearLayout();
			linearLayoutSubHeading.setWeightSum(4);
			linearLayoutSubHeading.setId(intCompID);
			//Sno	
			textView=Components.textView(strSNo);
			textView.setTextColor(getResources().getColor(R.color.form_datatxt_textcolor));
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_data_textsize));
			if(!strSNo.equalsIgnoreCase(""))
			{
				textView.setTypeface(null, Typeface.BOLD);
				textView.setTextColor(getResources().getColor(R.color.baaudit_heading_textcolor));
			}
			textView.setGravity(Gravity.CENTER);
			layoutParams=new LayoutParams(0, 40);
			layoutParams.weight=0.3f;
			layoutParams.leftMargin=5;
			layoutParams.gravity=Gravity.CENTER_VERTICAL;
			linearLayoutSubHeading.addView(textView, layoutParams);
			//Particular	
			textView=Components.textView(strParticular);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_data_textsize));
			textView.setTextColor(getResources().getColor(R.color.form_datatxt_textcolor));
			if(!strSNo.equalsIgnoreCase(""))
			{
				textView.setTypeface(null, Typeface.BOLD);
				textView.setTextColor(getResources().getColor(R.color.baaudit_heading_textcolor));
			}
			textView.setSingleLine();
			textView.setEllipsize(TruncateAt.MARQUEE);
			textView.setSelected(true);
			layoutParams=new LayoutParams(0, LayoutParams.WRAP_CONTENT);
			layoutParams.weight=1.7f;
			layoutParams.leftMargin=5;
			layoutParams.gravity=Gravity.CENTER;
			linearLayoutSubHeading.addView(textView, layoutParams);
			//check
			linearLayout=Components.linearLayout();
			checkBox=Components.checkBox("");
			if(check)
			{
				checkBox.setFocusable(false);
				checkBox.setFocusableInTouchMode(false); 
				checkBox.setClickable(false); 
			}
			//checkbox checked or not based on score value
			if(intScore==1)
			{
				checkBox.setChecked(true);
			}
			else if(intScore==0)
			{
				checkBox.setChecked(false);
			}
			else
			{
				checkBox.setVisibility(View.INVISIBLE);
			}
			checkBox.setButtonDrawable(R.drawable.checkstyle);
			layoutParams=new LayoutParams(25, 25);
			linearLayout.setGravity(Gravity.CENTER);
			linearLayout.addView(checkBox,layoutParams);
			layoutParams=new LayoutParams(0, 40);
			layoutParams.weight=0.5f;
			layoutParams.leftMargin=15;
			layoutParams.gravity=Gravity.CENTER_VERTICAL;
			linearLayoutSubHeading.addView(linearLayout, layoutParams);
			//Calculate audit score and check whether all sub section are selected or not
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener()
			{
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
				{
					LinearLayout linearLayout=(LinearLayout) buttonView.getParent().getParent();
					strID=""+linearLayout.getId();
					String strMainSection=getResources().getString(R.string.mainsection_table);
					String strSection=getResources().getString(R.string.subsection_table);
					ContentValues contentValues=new ContentValues();
				
					if(buttonView.isChecked())
					{
						contentValues.put("score", 1);
					}
					else
					{
						contentValues.put("score", 0);
					}
					//Score update
					database.update(contentValues, strSection, "sno=?", new String[]{strID});
					database.update(contentValues, strMainSection, "sno=?", new String[]{strID});
					
					TextView txtSno=(TextView) linearLayout.getChildAt(0);
					if(txtSno.getText().toString().equalsIgnoreCase(""))
					{
						Cursor cursorSub1=database.getVal("sno=?", new String[]{strID},strSection);
						if(cursorSub1!=null)
						{
							if(cursorSub1.moveToFirst())
							{
								String strmid=""+cursorSub1.getInt(cursorSub1.getColumnIndex("mid"));
								int intpsno=cursorSub1.getInt(cursorSub1.getColumnIndex("psno"));
								LinearLayout linearLayoutForm=(LinearLayout) linearLayoutMain.getChildAt(intpsno);
								CheckBox checkBox=(CheckBox) ((LinearLayout) linearLayoutForm.getChildAt(2)).getChildAt(0);
						
								cursorSub1=database.getVal("mid=? and score=?", new String[]{strmid,"0"},strSection);
								if(cursorSub1!=null)
								{
									if(!cursorSub1.moveToFirst())
									{
										checkBox.setChecked(true);
										contentValues=new ContentValues();
										contentValues.put("score", 1);
										database.update(contentValues, strMainSection, "sno=?", new String[]{""+intpsno});
									}
									else
									{
										checkBox.setChecked(false);
										contentValues=new ContentValues();
										contentValues.put("score", 0);
										database.update(contentValues, strMainSection, "sno=?", new String[]{""+intpsno});
									}
								}
							}
						}
						cursorSub1.close();
					}
					
					linearLayout=null;
					strMainSection=null;
					strSection=null;
					txtSno=null;
				}
			});
			//Training
			linearLayout=Components.linearLayout();
			checkBox=Components.checkBox("");
			checkBox.setButtonDrawable(R.drawable.checkstyle);
			if(strTraining.equalsIgnoreCase("no"))
			{
				checkBox.setChecked(false);
			}
			else
			{
				checkBox.setChecked(true);
			}
			layoutParams=new LayoutParams(25, 25);
			linearLayout.setGravity(Gravity.CENTER);
			linearLayout.addView(checkBox,layoutParams);
			layoutParams=new LayoutParams(0, 40);
			layoutParams.weight=0.5f;
			layoutParams.leftMargin=10;
			layoutParams.gravity=Gravity.CENTER_VERTICAL;
			linearLayoutSubHeading.addView(linearLayout, layoutParams);
			checkBox.setOnTouchListener(new OnTouchListener() 
			{
				@Override
				public boolean onTouch(View v, MotionEvent event) 
				{
					if(event.getAction()==MotionEvent.ACTION_DOWN)
					{
						isEmpty=remarksCheck(v);
					}
					return isEmpty;
				}
			});
			
			//Remarks
			editText=Components.editText("");
			editText.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_data_textsize));
			editText.setTextColor(getResources().getColor(R.color.form_datatxt_textcolor));
			editText.setSingleLine();
			if(!strSNo.equalsIgnoreCase(""))
			{
				editText.setTypeface(null, Typeface.BOLD);
			}
			if(strRemarks.length()!=0&&!strRemarks.equalsIgnoreCase("null"))
			{
				editText.setText(strRemarks.replace("+"," "));
			}
			editText.setBackgroundColor(Color.TRANSPARENT);
			editText.setGravity(Gravity.CENTER);
			InputFilter[] maxLength = new InputFilter[2];
			maxLength[0] = new InputFilter.LengthFilter(20);
			maxLength[1] = filter;
			editText.setFilters(maxLength);
			layoutParams=new LayoutParams(0, LayoutParams.WRAP_CONTENT);
			layoutParams.weight=1.0f;
			layoutParams.leftMargin=5;
			layoutParams.rightMargin=5;
			layoutParams.gravity=Gravity.CENTER;
			linearLayoutSubHeading.addView(editText, layoutParams);
			editText.setOnTouchListener(new OnTouchListener()
			{
				@Override
				public boolean onTouch(View v, MotionEvent event) 
				{
					if(event.getAction()==MotionEvent.ACTION_DOWN)
					{
						isEmpty=remarksCheck(v);
					}
					return isEmpty;
				}
			});
			editText.clearFocus();
			editText.setCursorVisible(false);
			editText.setFocusable(true);
			editText.setClickable(true);
			editText.setImeOptions(0x00000006);
			checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					LinearLayout linearLayout=(LinearLayout) buttonView.getParent().getParent();
					EditText editText=(EditText) linearLayout.getChildAt(4);
					if(!isChecked)
					{
						editText.clearFocus();
						editText.setCursorVisible(false);
						editText.setText("");
						((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(buttonView.getWindowToken(), 0);
					}
				}
			});
			LayoutParams linearlayoutparms=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
			if(!strSNo.equalsIgnoreCase(""))
			{
				linearLayoutSubHeading.setBackgroundResource(R.drawable.tablerowdark);
				
			}
			else
			{
				linearLayoutSubHeading.setBackgroundResource(R.drawable.tablerowlight);
			}
			linearLayoutMain.addView(linearLayoutSubHeading,linearlayoutparms);
			
			editText=null;
			checkBox=null;
			textView=null;
			linearLayout=null;
			layoutParams=null;
		}catch(Exception e)
		{
			Utility.logFile(Utility.currentDate()+" "+Utility.currentTimesecond()+"~"+Utility.strAuName+"~BAAuditForm~addComponent"+"~error"+e.toString(),true);
		}
	}
	//Check remark field is empty or not
	private boolean remarksCheck(View v)
	{
		if(v instanceof CheckBox)
		{
			intID=((LinearLayout)v.getParent().getParent()).getId();
		}
		else
		{
			intID=((LinearLayout)v.getParent()).getId();
		}
		if(intPrevID!=intID)
		{
			LinearLayout linearLayoutPrevChild=(LinearLayout) linearLayoutMain.getChildAt(intPrevID);
			CheckBox checkBox=(CheckBox) ((LinearLayout)linearLayoutPrevChild.getChildAt(3)).getChildAt(0);
			EditText editText=(EditText) linearLayoutPrevChild.getChildAt(4);
			editText.setCursorVisible(false);
			if(checkBox.isChecked()&&editText.length()==0)
			{
				alert(getResources().getString(R.string.empty_remarks),editText);
				editText.requestFocus();
				editText.setCursorVisible(true);
				return true;
			}
			
		}
		LinearLayout linearLayoutChild=(LinearLayout) linearLayoutMain.getChildAt(intID);
		EditText editText1=(EditText) linearLayoutChild.getChildAt(4);
		editText1.requestFocus();
		editText1.setCursorVisible(true);
		inputMethodManager.showSoftInput(editText1, InputMethodManager.SHOW_IMPLICIT);
		intPrevID=intID;
		return false;
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
	//Alert for empty remark field
	private void alert(String strmessage,final View v)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(BAAuditForm.this);
		builder.setMessage(strmessage).setPositiveButton("OK", new DialogInterface.OnClickListener() 
		{
			@Override
			public void onClick(DialogInterface dialog, int which) 
			{
				dialog.cancel();
				inputMethodManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
			}
		}).setCancelable(false).show();
	}
	//Add Form Heading
	private void addHeading()
	{
		TextView textView=null;
		LayoutParams layoutParams=null;
		
		LinearLayout linearLayoutSubHeading=Components.linearLayout();
		linearLayoutSubHeading.setWeightSum(4);
		//Sno	
		textView=Components.textView("S.NO");
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTextColor(getResources().getColor(R.color.form_dataheadingtxt_textcolor));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_heading_textsize));
		textView.setGravity(Gravity.CENTER);
		layoutParams=new LayoutParams(0, 40);
		layoutParams.weight=0.3f;
		layoutParams.leftMargin=5;
		linearLayoutSubHeading.addView(textView, layoutParams);
		//Particular	
		textView=Components.textView("PARTICULAR");
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTextColor(getResources().getColor(R.color.form_dataheadingtxt_textcolor));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_heading_textsize));
		textView.setGravity(Gravity.CENTER);
		layoutParams=new LayoutParams(0, 40);
		layoutParams.weight=1.7f;
		layoutParams.leftMargin=5;
		linearLayoutSubHeading.addView(textView, layoutParams);
		//Check	
		textView=Components.textView("CHECK");
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTextColor(getResources().getColor(R.color.form_dataheadingtxt_textcolor));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_heading_textsize));
		textView.setGravity(Gravity.CENTER);
		layoutParams=new LayoutParams(0, 40);
		layoutParams.weight=0.5f;
		layoutParams.leftMargin=5;
		linearLayoutSubHeading.addView(textView, layoutParams);
		//Training	
		textView=Components.textView("TRAINING");
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTextColor(getResources().getColor(R.color.form_dataheadingtxt_textcolor));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_heading_textsize));
		textView.setGravity(Gravity.CENTER);
		layoutParams=new LayoutParams(0, 40);
		layoutParams.weight=0.5f;
		layoutParams.leftMargin=5;
		linearLayoutSubHeading.addView(textView, layoutParams);
		//Remarks
		textView=Components.textView("REMARKS");
		textView.setTypeface(null, Typeface.BOLD);
		textView.setTextColor(getResources().getColor(R.color.form_dataheadingtxt_textcolor));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_SP,getResources().getDimension(R.dimen.auditform_heading_textsize));
		textView.setGravity(Gravity.CENTER);
		layoutParams=new LayoutParams(0, 40);
		layoutParams.weight=1.0f;
		layoutParams.leftMargin=5;
		
		linearLayoutSubHeading.addView(textView, layoutParams);
		LayoutParams linearlayoutparms=new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
		linearLayoutHeading.addView(linearLayoutSubHeading,linearlayoutparms);
		
		textView=null;
		layoutParams=null;
	}
	@Override
	public void onClick(View v) 
	{
		if(!remarksValidate())return;
		saveData();
		if(v==btnNext)
		{ //Next. Move to Audit Summary Screen.
			btnNext.setEnabled(false);
			startActivity(new Intent(BAAuditForm.this,BASummary.class));
		}
		else if(v==btnInfo)
		{// Information, Move to BAInfo screen
			btnInfo.setEnabled(false);
			startActivity(new Intent(BAAuditForm.this,BASearchData.class));
		}
		else if(v==btnSummary)
		{//Summary, Move to Prevoius Summary screen
			btnSummary.setEnabled(false);
			startActivity(new Intent(BAAuditForm.this,BAPrevSummary.class));
		}
		else if(v==btnScore)
		{//Audit Score , Move to Audit Score screen
			btnScore.setEnabled(false);
			startActivity(new Intent(BAAuditForm.this,BAAuditScore.class));
		}
		else if(v==btnTraining)
		{//Training , Move to Training screen
			btnTraining.setEnabled(false);
			SharedPreferences preference = getSharedPreferences("TrainingDes",MODE_PRIVATE);
			Editor editor = preference.edit();
			editor.putInt("backid", 1);
			editor.commit();
			startActivity(new Intent(BAAuditForm.this,Training.class));
		}
		BAAuditForm.this.finish();
	}
	//Remarks field validation
	private boolean remarksValidate()
	{
		boolean check=true;
		LinearLayout linearLayout=(LinearLayout) linearLayoutMain.getChildAt(intID);
		CheckBox checkBox=(CheckBox) ((LinearLayout)linearLayout.getChildAt(3)).getChildAt(0);
		EditText editText=(EditText) linearLayout.getChildAt(4);
		editText.setCursorVisible(false);
		
		LinearLayout linearLayoutPrevoius=(LinearLayout) linearLayoutMain.getChildAt(intPrevID);
		CheckBox checkBox1=(CheckBox) ((LinearLayout)linearLayoutPrevoius.getChildAt(3)).getChildAt(0);
		EditText editText1=(EditText) linearLayoutPrevoius.getChildAt(4);
		editText1.setCursorVisible(false);
		
		if(checkBox.isChecked()&&editText.length()==0)
		{
			alert(getResources().getString(R.string.empty_remarks),editText);
			editText.requestFocus();
			editText.setCursorVisible(true);
			check=false;
		}
		else if(checkBox1.isChecked()&&editText1.length()==0)
		{
			alert(getResources().getString(R.string.empty_remarks),editText1);
			editText1.requestFocus();
			editText1.setCursorVisible(true);
			check=false;
		}
		return check;
	}
	//Collect all form data and stored in local database.
	private void saveData() 
	{
		ContentValues contentvalues;
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		String strRemarks,strTraining;
		OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		mainSection.clear();
		Cursor cursor=database.getAll(strMainSection);
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				do
				{
					strRemarks="null";
					intSno=cursor.getInt(cursor.getColumnIndex("sno"));
					LinearLayout linearLayout=(LinearLayout) linearLayoutMain.getChildAt(intSno);
					EditText editText=(EditText) linearLayout.getChildAt(4);
					CheckBox checkBox=(CheckBox) ((LinearLayout) linearLayout.getChildAt(3)).getChildAt(0);
					
					if(editText.getText().length()>0&&checkBox.isChecked())
					{
						checkBox.setChecked(true);
					}
					else
					{
						checkBox.setChecked(false);
					}
					//Training
					if(checkBox.isChecked())
					{
						strTraining="yes";
					}
					else
					{
						strTraining="no";
					}
					contentvalues=new ContentValues();
					contentvalues.put("training", strTraining);
					contentvalues.put("remarks", strRemarks.replace(" ","+"));
					strID=""+intSno;
					database.update(contentvalues, strMainSection, "sno=?", new String[]{strID});
					//Remarks
					if(editText.getText()!=null)
					{
						if(!editText.getText().toString().equalsIgnoreCase(" ")&&editText.getText().length()!=0)
						{
							strRemarks=editText.getText().toString();
							contentvalues=new ContentValues();
							contentvalues.put("remarks", strRemarks.replace(" ","+"));
							strID=""+intSno;
							database.update(contentvalues, strMainSection, "sno=?", new String[]{strID});
						}
					}
					mainSection.add(""+cursor.getInt(cursor.getColumnIndex("id"))
							+"*"+cursor.getString(cursor.getColumnIndex("score"))
							+"*"+strRemarks.replace(" ","+")
							+"*"+strTraining);
				}while(cursor.moveToNext());
			}
		}
		subSection.clear();
		cursor.close();
		cursor=database.getAll(strSection);
		if(cursor!=null)
		{
			if(cursor.moveToFirst())
			{
				do
				{
					strRemarks="null";
					intSno=cursor.getInt(cursor.getColumnIndex("sno"));
					LinearLayout linearLayout=(LinearLayout) linearLayoutMain.getChildAt(intSno);
					EditText editText=(EditText) linearLayout.getChildAt(4);
					CheckBox checkBox=(CheckBox) ((LinearLayout) linearLayout.getChildAt(3)).getChildAt(0);
					if(editText.getText().length()>0&&checkBox.isChecked())
					{
						checkBox.setChecked(true);
					}
					else
					{
						checkBox.setChecked(false);
					}
					//Training
					if(checkBox.isChecked())
					{
						strTraining="yes";
					}
					else
					{
						strTraining="no";
					}
					contentvalues=new ContentValues();
					contentvalues.put("training", strTraining);
					contentvalues.put("remarks", strRemarks.replace(" ","+"));
					strID=""+intSno;
					database.update(contentvalues, strSection, "sno=?", new String[]{strID});
					//Remarks
					if(editText.getText()!=null)
					{
						if(!editText.getText().toString().equalsIgnoreCase(" ")&&editText.getText().length()!=0)
						{
							strRemarks=editText.getText().toString();
							contentvalues=new ContentValues();
							contentvalues.put("remarks", strRemarks.replace(" ","+"));
							strID=""+intSno;
							database.update(contentvalues, strSection, "sno=?", new String[]{strID});
						}
					}
					
					subSection.add(""+cursor.getInt(cursor.getColumnIndex("mid"))
							+"*"+cursor.getInt(cursor.getColumnIndex("id"))
							+"*"+cursor.getString(cursor.getColumnIndex("score"))
							+"*"+strRemarks.replace(" ","+")
							+"*"+strTraining);
				}while(cursor.moveToNext());
			}
			cursor.close();
		}
		contentvalues=new ContentValues();
		contentvalues.put("mainsection", mainSection.toString());
		contentvalues.put("subsection", subSection.toString());
		database.update(contentvalues, strAuditData, "status=?", new String[]{"0"});
		database.close();
	}
	//Back Event
	@Override
	public void onBackPressed() 
	{
		if(!remarksValidate())return;
		saveData();
		if(btnScore.isEnabled())
		{
			startActivity(new Intent(BAAuditForm.this,BAAuditScore.class));
			BAAuditForm.this.finish();
		}
		else
		{
			startActivity(new Intent(BAAuditForm.this,BASearchData.class));
			BAAuditForm.this.finish();
		}
	}
	@Override
	protected void onDestroy() 
	{
		if(handler!=null)handler.removeCallbacks(runnable);
		linearLayoutMain=null;
		mainSection=null;
		subSection=null;
		strID=null;
		txtDate=null;
		txtTime=null;
		txtHeading=null;
		linearLayoutHeading=null;
		btnNext=null;
		btnInfo=null;
		btnSummary=null;
		btnScore=null;
		btnTraining=null;
		scrollView=null;
		handler=null;
		runnable=null;
		inputMethodManager=null;
		if(database!=null)database.close();
		super.onDestroy();
	}
}