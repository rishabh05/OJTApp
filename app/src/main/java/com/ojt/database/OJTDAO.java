/*@ID: CN20140001
 *@Description: srcDatabase 
 * This class is used to create database for store and retrieve the data on the device.
 * This is not for storing hte data on the Server
 * @Developer: Arunachalam
 * @Version 1.0
 * @Stage: 1
 * @Date: 10/03/2014
 */
package com.ojt.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class OJTDAO 
{
	private SQLiteDatabase sqldb;

	@SuppressWarnings("static-access")
	//Create Database
	public OJTDAO(Context context,String strdbname)
	{
		sqldb=context.openOrCreateDatabase(strdbname, context.MODE_PRIVATE,null);
	}
	//Create Table
	public void create(String strLogin,String strMainSection,String strSubSection,String strAuditData)
	{
		sqldb.execSQL("create table if not exists "+strLogin+"(user varchar2(255),password varchar2(50),auditname varchar2(55),recid integer,timeout integer,baname varchar2(100),baid varchar2(16))");
		sqldb.execSQL("create table if not exists "+strMainSection+"(sno integer,id integer, name varchar2(255),score integer,remarks varchar2(50),training varchar2(5))");
		sqldb.execSQL("create table if not exists "+strSubSection+"(psno integer,sno integer,mid integer,id integer, name varchar2(255),score integer,remarks varchar2(50),training varchar2(5))");
		sqldb.execSQL("create table if not exists "+strAuditData+"(batchno varchar2(50),mlearning varchar2(15),comments varchar2(155),status integer,pbid integer,"
				+ "badetailsid integer,intime DATETIME,baname varchar2(55),"
				+ "mainsection varchar2(355), subsection varchar2(355),summary varchar2(355),"
				+ "bsipath varchar2(50),bppath varchar2(50),sipath varchar2(50),"
				+ "overallscore integer,overallper integer,overallcolor varchar2(5),"
				+ "attitudecolor varchar2(5),astart DATETIME,aend DATETIME,"
				+ "alocation varchar2(250),storecode varchar2(50),countername varchar2(255),"
				+ "coachingtime varchar2(10),"
				+ "bainfo varchar2(500),readyreckonerinfo varchar2(500))");
		sqldb.execSQL("create table if not exists lastmainscore(sno integer,id integer,training varchar2(5), percentage integer,name varchar2(255),score integer,remarks varchar2(50))");
		sqldb.execSQL("create table if not exists lastsubscore(psno integer,sno integer,mid integer,id integer, training varchar2(5),name varchar2(255),score integer,remarks varchar2(50))");
		sqldb.execSQL("create table if not exists readyreckoner(storecode varchar2(255),countername varchar2(255))");
		sqldb.execSQL("create table if not exists notification(msgid varchar2(50),imageurl varchar2(260),"
				+ "contenturl varchar2(260),title varchar2(260),filetype varchar2(10),status int,state int,reference varchar2(256),imagename varchar2(200)"
				+ ",filename varchar2(200),intime DATETIME,outtime DATETIME"
				+ ",arrivetime DATETIME)");
		sqldb.execSQL("create table if not exists lastauditreport(auditon varchar2(15),auditontime varchar2(15),"
				+ "auditby varchar2(200),attitudecolor varchar2(15),overallcolor varchar2(15),comments varchar2(155)"
				+ ",mlearning varchar2(15))");
		
		sqldb.execSQL("create table if not exists readyreckoner1(pbid varchar2(50),name varchar2(255),detailsid varchar2(20))");
		
		sqldb.execSQL("create table if not exists readyreckoner_bainfo"
				+ "(bainfo varchar2(500),readyreckonerinfo varchar2(500)"
				+ ",lastmainscore varchar2(500)"
				+ ",lastsubscore varchar2(500)"
				+ ",lastauditreport varchar2(500))");
		
		
	}
	//Insert values into table
	public void insert(ContentValues contentValues,String strTable)
	{
		sqldb.insert(strTable, null, contentValues);
	}
	
	//Update values
	public void update(ContentValues contentValues,String strTable,String strFiedls,String strValues[])
	{
		sqldb.update(strTable,contentValues, strFiedls, strValues);
	}
	//Check whether record is present or not
	public boolean isAvailable(String strFields,String []strValues,String strTable)
	{
		Cursor cursor=sqldb.query(strTable,null,strFields,strValues,  null, null, null);
		if(cursor!=null)
		{
			if(cursor.getCount()>0)
				return true;
			else
				return false;
		}
		else
			return false;
	}
	//Get all values from table
	public Cursor getAll(String strTable)
	{
		return sqldb.query(strTable,null,null, null, null, null, null);
	}
	//Get values from table based on particular fields
	public Cursor getVal(String strFields,String[] strValues,String strTable)
	{
		return sqldb.query(strTable,null,strFields,strValues,  null, null, null);
	}
	//Delete table
	public void delete(String strTable)
	{
		sqldb.delete(strTable, null,null);
	}
	//Delete particular table.
	public void deleteVal(String strFields,String[] strValues,String strTable)
	{
		sqldb.delete(strTable,strFields,strValues);
	}
	//Close the database
	public void close()
	{
		if(sqldb!=null)
			sqldb.close();
		sqldb=null;
	}




	public static void exportDatabse(String databaseName, Context context) {
		try {
			File sd = Environment.getExternalStorageDirectory();
			File data = Environment.getDataDirectory();

			if (sd.canWrite()) {
				String currentDBPath = "//data//"+context.getPackageName()+"//databases//"+databaseName+"";
				String backupDBPath = "ojtapplication_database6.db";
				File currentDB = new File(data, currentDBPath);
				File backupDB = new File(sd, backupDBPath);

				if (currentDB.exists()) {
					FileChannel src = new FileInputStream(currentDB).getChannel();
					FileChannel dst = new FileOutputStream(backupDB).getChannel();
					dst.transferFrom(src, 0, src.size());
					src.close();
					dst.close();
				}
			}
		} catch (Exception e) {

		}
	}


	private static String DB_PATH = "";
	//private final Context mContext = null;
	private static String DB_NAME ="ojtapplication_database6.db";// Database name

	public void copyDataBase(Context context) throws IOException
	{
		DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
		InputStream mInput = context.getAssets().open(DB_NAME);
		String outFileName = DB_PATH + DB_NAME;
		OutputStream mOutput = new FileOutputStream(outFileName);
		byte[] mBuffer = new byte[1024];
		int mLength;
		while ((mLength = mInput.read(mBuffer))>0)
		{
			mOutput.write(mBuffer, 0, mLength);
		}
		mOutput.flush();
		mOutput.close();
		mInput.close();
	}

}