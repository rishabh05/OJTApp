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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ojt.connectivity.JSONParser;
import com.ojt.database.OJTDAO;
import com.ojt.home.Home;
import com.ojt.login.Login;
import com.ojt.notification.R;
import com.ojt.utilities.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

public class PendingAudits extends Activity
{

	ListView pendingAudits;
	Button submit;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pendingaudit);

		pendingAudits = (ListView)findViewById(R.id.list);
		pendingAudits.setAdapter(new CustomAdapter(this, getPendingAudits()));
		submit = (Button)findViewById(R.id.submit);
		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						String strBatchNo = Utility.updateBatchno(true, "0");
						Utility.logFile("Home screen-> PendingAudits -> NewBatNo:		"+strBatchNo,true);
						Utility.logFile("Home screen-> PendingAudits -> going to send data",true);
						JSONParser.sendData(PendingAudits.this.getString(R.string.server_url) + "AuditingReport", strBatchNo);
					}
				}).start();

				AlertDialog.Builder builder = new AlertDialog.Builder(PendingAudits.this);
				builder.setMessage("Audit Submiting in background.").setPositiveButton
						(Utility.context.getResources().getString(R.string.ok_msg), new DialogInterface.OnClickListener()
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
						Intent intent=new Intent(PendingAudits.this,Home.class);
						startActivity(intent);
						PendingAudits.this.finish();

					}
				}).setCancelable(false).show();
			}
		});
	}

	private Cursor getPendingAudits(){
		String strLogin=getResources().getString(R.string.login_table);
		String strMainSection=getResources().getString(R.string.mainsection_table);
		String strSection=getResources().getString(R.string.subsection_table);
		String strAuditData=getResources().getString(R.string.auditdata_table);
		OJTDAO database=new OJTDAO(Utility.context, getResources().getString(R.string.db_name));
		database.create(strLogin,strMainSection,strSection,strAuditData);
		Cursor cursor=database.getAll(strAuditData);
		return cursor;
	}

	/********* Adapter class extends with BaseAdapter and implements with OnClickListener ************/
	class CustomAdapter extends BaseAdapter implements OnClickListener {

		/*********** Declare Used Variables *********/
		private Activity activity;
		private LayoutInflater inflater=null;
		Cursor cursor;
		/*************  CustomAdapter Constructor *****************/
		public CustomAdapter(Activity a, Cursor cursor) {

			/********** Take passed values **********/
			activity = a;
			this.cursor = cursor;
			inflater = (LayoutInflater)activity.
					getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}

		/******** What is the size of Passed Arraylist Size ************/
		public int getCount() {
			if(cursor == null){
				return 0;
			}
			return cursor.getCount();
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		/********* Create a holder Class to contain inflated xml file elements *********/
		public class ViewHolder{

			public TextView text;

		}

		/****** Depends upon data size called for each row , Create each ListView row *****/
		public View getView(int position, View convertView, ViewGroup parent) {

			View vi = convertView;
			ViewHolder holder;

			if(convertView==null){

				/****** Inflate tabitem.xml file for each row ( Defined below ) *******/
				vi = inflater.inflate(R.layout.list_row, null);

				/****** View Holder Object to contain tabitem.xml file elements ******/

				holder = new ViewHolder();
				holder.text = (TextView) vi.findViewById(R.id.rowtext);
				vi.setTag( holder );
			}
			else {
				holder = (ViewHolder) vi.getTag();
			}
			cursor.moveToPosition(position);
			holder.text.setText(cursor.getString(cursor.getColumnIndex("comments")));
			return vi;
		}

		@Override
		public void onClick(View v) {
			Log.v("CustomAdapter", "=====Row button clicked=====");
		}

	}

}