/*@ID: CN20140001
 *@Description: srcJSONParser 
 * This class for get / send the data from / to server
 * @Developer: Arunachalam_Sumtwo
 * @Version 1.0
 * @Stage: 1
 * @Date: 10/03/2014
 * @Modified Date: 25/05/2014
 */
package com.ojt.connectivity;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.ojt.database.OJTDAO;
import com.ojt.notification.R;
import com.ojt.utilities.Utility;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;

public class JSONParser {
    private static StringBuilder stringBuilder = new StringBuilder();
    private static int intStatusCode = 0;
    private static HttpClient httpClient = null;
    private static HttpGet httpGet;
    private static JSONObject jObj;

    //Make connection with server and get data
    public static JSONObject connect(final String url, Context context) throws JSONException {
        stringBuilder.setLength(0);
        jObj = new JSONObject();
        // final HttpParams httpParameters = new BasicHttpParams();
        // HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
        // HttpConnectionParams.setSoTimeout(httpParameters, 23000);
        httpClient = getNewHttpClient();
        httpGet = new HttpGet(url);
        HttpResponse response;
        try {
            response = httpClient.execute(httpGet);
            intStatusCode = response.getStatusLine().getStatusCode();
            if (intStatusCode == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    String result = convertStreamToString(instream);
                    jObj = new JSONObject(result);
                    instream.close();
                }
            }
        } catch (ConnectTimeoutException e) {
            jObj = new JSONObject("{\"Error\":\"Timeout Error\"}");
            Log.i("ConnectTimeoutException", e.toString());
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~connect" + "~URL:" + url + "~error:Timeout" + e.toString(), true);
        } catch (ClientProtocolException e) {
            Log.i("ClientProtocolException", e.toString());
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~connect" + "~URL:" + url + "~clientprotocalerror:" + e.toString(), true);
        } catch (IOException e) {
            Log.i("IOException", e.toString());
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~connect" + "~URL:" + url + "~ioerror:" + e.toString(), true);
        } catch (JSONException e) {
            Log.i("JSONException", e.toString());
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~connect" + "~URL:" + url + "~jsonerror:" + e.toString(), true);
        }

        return jObj;
    }

    //  Method for https secure connection
    private static HttpClient getNewHttpClient() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory socketFactory = new SSLsocketFactory(trustStore);
            socketFactory.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            HttpParams params = new BasicHttpParams();
            HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
            HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
            HttpConnectionParams.setConnectionTimeout(params, 20000);
            HttpConnectionParams.setSoTimeout(params, 23000);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", socketFactory, 443));
            ClientConnectionManager clientconnectionmanager = new ThreadSafeClientConnManager(params, registry);
            return new DefaultHttpClient(clientconnectionmanager, params);
        } catch (Exception e) {
            return new DefaultHttpClient();
        }
    }

    // Data convert from inputstream to string format
    private static String convertStreamToString(InputStream is) throws UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
        } catch (IOException e) {
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~convertStreamToString~io error:" + e.toString(), true);
            Log.i("IOException", e.toString());
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~convertStreamToString~closing io error:" + e.toString(), true);
                Log.i("IOException", e.toString());
            }
        }
        return stringBuilder.toString();
    }

    //Send data to server
    public static void sendData(String strURL, String strBatchNo) {
        try {
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" +
                    Utility.strAuName + "~JSONParser~sendData~URL:" + strURL + " inside send data.", true);

            Utility.logFile("JSONParser-> sendData -> NewBatNo:		"+strBatchNo+"  strURL:     "+strURL,true);

            String strtemp, strData[], strSummary, strSummarData[];
            String strBSIpath, strSIpath, strBPpath;
            String strBASearchImage, strBAImage, strSignImage;
            String strBASearchImageName, strBAImageName, strSignImageName;
            List<String> list, listSummary;
            Bitmap bitmap;
            ByteArrayOutputStream out;
            byte[] byteArr;
            jObj = new JSONObject();

            final HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
            HttpConnectionParams.setSoTimeout(httpParameters, 23000);
            httpClient = getNewHttpClient();
            HttpPost post = new HttpPost(strURL);

            String strLogin = Utility.context.getResources().getString(R.string.login_table);
            String strMainSection = Utility.context.getResources().getString(R.string.mainsection_table);
            String strSection = Utility.context.getResources().getString(R.string.subsection_table);
            String strAuditData = Utility.context.getResources().getString(R.string.auditdata_table);

            OJTDAO database = new OJTDAO(Utility.context, Utility.context.getResources().getString(R.string.db_name));
            database.create(strLogin, strMainSection, strSection, strAuditData);

            Cursor cursor = database.getVal("status=? and batchno=?", new String[]{"1", strBatchNo}, strAuditData);
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            if (cursor != null) {
                Utility.logFile("total audit count " + cursor.getCount() + "		strBatchNo=	" + strBatchNo, true);
                if (cursor.moveToFirst()) {
                    params.add(new BasicNameValuePair("recordcount", "" + cursor.getCount()));
                    System.out.println("recordcount" + "" + cursor.getCount());
                    params.add(new BasicNameValuePair("batchno", "" + strBatchNo));
                    int k = 1;
                    do {
                        Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName +
                                        "~JSONParser~sendData~URL:" + strURL + " astart." +
                                        cursor.getString(cursor.getColumnIndex("astart"))
                                , true);
                        params.add(new BasicNameValuePair("batchno" + k, "" +
                                cursor.getString(cursor.getColumnIndex("batchno"))));
                        params.add(new BasicNameValuePair("badetailsid" + k, "" +
                                cursor.getInt(cursor.getColumnIndex("badetailsid"))));
                        params.add(new BasicNameValuePair("auditon" + k,
                                cursor.getString(cursor.getColumnIndex("astart"))));
                        params.add(new BasicNameValuePair("storecode" + k,
                                cursor.getString(cursor.getColumnIndex("storecode"))));
                        params.add(new BasicNameValuePair("countername" + k,
                                cursor.getString(cursor.getColumnIndex("countername")).replace(" ", "+")));
                        params.add(new BasicNameValuePair("auditby" + k, Utility.strAurecID));
                        params.add(new BasicNameValuePair("overscore" + k, "" +
                                cursor.getInt(cursor.getColumnIndex("overallscore"))));
                        params.add(new BasicNameValuePair("overpercent" + k, "" +
                                cursor.getInt(cursor.getColumnIndex("overallper"))));
                        params.add(new BasicNameValuePair("overcolor" + k, "" +
                                cursor.getString(cursor.getColumnIndex("overallcolor"))));
                        params.add(new BasicNameValuePair("attitudecolor" + k, "" +
                                cursor.getString(cursor.getColumnIndex("attitudecolor"))));
                        params.add(new BasicNameValuePair("starttime" + k, "" +
                                cursor.getString(cursor.getColumnIndex("astart"))));
                        params.add(new BasicNameValuePair("endtime" + k, "" +
                                cursor.getString(cursor.getColumnIndex("aend"))));
                        System.out.println("comments send:" + cursor.getString(cursor.getColumnIndex("comments")));
                        params.add(new BasicNameValuePair("comment" + k, "" +
                                cursor.getString(cursor.getColumnIndex("comments"))));
                        System.out.println("mlearn send:" + cursor.getString(cursor.getColumnIndex("mlearning")));
                        params.add(new BasicNameValuePair("mlearning" + k, "" +
                                cursor.getString(cursor.getColumnIndex("mlearning"))));
                        if (cursor.getString(cursor.getColumnIndex("coachingtime")) != null)
                            params.add(new BasicNameValuePair("coachingtime" + k, "" +
                                    cursor.getString(cursor.getColumnIndex("coachingtime"))));
                        else
                            params.add(new BasicNameValuePair("coachingtime" + k, "0.0"));

                        String strLocation = cursor.getString(cursor.getColumnIndex("alocation"));
                        String strLatitude = " ", strLongitude = " ";
                        if (strLocation != null) {
                            if (strLocation.length() > 0) {
                                if (strLocation.indexOf("~") != -1) {
                                    String strLocations[] = strLocation.split("~");
                                    strLatitude = strLocations[0];
                                    strLongitude = strLocations[1];

                                }
                            }
                        }
                        params.add(new BasicNameValuePair("latitude" + k, "" + strLatitude));
                        params.add(new BasicNameValuePair("longitude" + k, "" + strLongitude));

                        strtemp = cursor.getString(cursor.getColumnIndex("mainsection"));
                        strSummary = cursor.getString(cursor.getColumnIndex("summary"));

                        strBPpath = cursor.getString(cursor.getColumnIndex("bppath"));
                        strSIpath = cursor.getString(cursor.getColumnIndex("sipath"));
                        strBSIpath = cursor.getString(cursor.getColumnIndex("bsipath"));

                        strtemp = strtemp.replace("[", "");
                        strtemp = strtemp.replace("]", "");
                        System.out.println(strtemp);

                        list = new ArrayList<String>(Arrays.asList(strtemp.split(",")));
                        //summary data store in arraylist
                        strSummary = strSummary.replace("[", "");
                        strSummary = strSummary.replace("]", "");
                        System.out.println(strSummary);

                        listSummary = new ArrayList<String>(Arrays.asList(strSummary.split(",")));

                        params.add(new BasicNameValuePair("mainseccount" + k, "" + list.size()));
                        for (int i = 0; i < list.size(); i++) {
                            strData = list.get(i).split(Pattern.quote("*"));
                            if (i < listSummary.size()) {
                                strSummarData = listSummary.get(i).split(Pattern.quote("-"));
                                if (strSummarData.length > 0) {
                                    strSummary = strSummarData[2];
                                } else {
                                    strSummary = "0";
                                }
                            }
                            if (strData.length > 0) {
                                if (!Utility.context.getResources().getString(R.string.no_score_mid).equalsIgnoreCase(strData[0].trim())) {
                                    strtemp = strData[0] + "~" + strData[1] + "~" + strSummary + "~" + strData[3] + "~" + strData[2];
                                } else {
                                    strtemp = strData[0] + "~-1~-1~" + strData[3] + "~" + strData[2];
                                }
                            } else {
                                strtemp = "null";
                            }
                            System.out.println(strtemp);
                            params.add(new BasicNameValuePair("main" + k + i, strtemp.trim()));
                        }
                        strtemp = cursor.getString(cursor.getColumnIndex("subsection"));
                        strtemp = strtemp.replace("[", "");
                        strtemp = strtemp.replace("]", "");
                        System.out.println(strtemp);

                        list = new ArrayList<String>(Arrays.asList(strtemp.split(",")));
                        params.add(new BasicNameValuePair("subseccount" + k, "" + list.size()));
                        for (int i = 0; i < list.size(); i++) {
                            strData = list.get(i).split(Pattern.quote("*"));
                            if (strData.length > 0) {
                                if (!Utility.context.getResources().getString(R.string.no_score_mid).equalsIgnoreCase(strData[1].trim())) {
                                    strtemp = strData[0] + "~" + strData[1] + "~" + strData[2] + "~" + strData[4] + "~" + strData[3];
                                } else {
                                    strtemp = strData[0] + "~" + strData[1] + "~-1~" + strData[4] + "~" + strData[3];
                                }
                            } else {
                                strtemp = "null";
                            }
                            System.out.println(strtemp);
                            params.add(new BasicNameValuePair("sub" + k + i, strtemp.trim()));
                        }
                        //BA Search image
                        if (strBSIpath != null) {

                            File file = new File(strBSIpath);
                            if (file.exists()) {
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                bitmap = BitmapFactory.decodeFile(strBSIpath, options);
                                out = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                                byteArr = out.toByteArray();
                                strBASearchImage = Base64.encodeToString(byteArr, 0);
                                strBASearchImageName = file.getName();
                            } else {
                                strBASearchImage = "NoImage";
                                strBASearchImageName = "NoName";
                            }

                        } else {
                            strBASearchImage = "NoImage";
                            strBASearchImageName = "NoName";
                        }
                        //BA's photo
                        if (strBPpath != null) {

                        } else {
                            strBAImage = "NoImage";
                            strBAImageName = "NoName";
                        }
                        //sign photo
                        if (strSIpath != null) {

                        } else {
                            strSignImage = "NoImage";
                            strSignImageName = "NoName";
                        }
                        // Lets just clear out the images for now.
                        strSignImage = "NoImage";
                        strSignImageName = "NoName";
                        strBAImage = "NoImage";
                        strBAImageName = "NoName";
                        //strBASearchImage="NoImage";
                        //strBASearchImageName="NoName";

                        // First Copy the Image Names so we can now sedn teh raw data
                        params.add(new BasicNameValuePair("basearchimagename" + k, strBASearchImageName));
                        params.add(new BasicNameValuePair("baimagename" + k, strBAImageName));
                        params.add(new BasicNameValuePair("signname" + k, strSignImageName));
                        // SendRawData(params);

                        params.add(new BasicNameValuePair("basearchimage" + k, strBASearchImage));
                        params.add(new BasicNameValuePair("baimage" + k, strBAImage));
                        params.add(new BasicNameValuePair("signature" + k, strSignImage));

                        k++;
                    } while (cursor.moveToNext());
                }
            }
            int num_send_attmps = 0;
            boolean send_failure = true;
            try {
                Utility.logFile("Parameters:     " + params.toString(), true);
            }catch(Exception e){
                Utility.logFile("Errors to print in parameters:     " + e.toString(), true);
            }
            UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            post.setEntity(ent);
            while (send_failure && (++num_send_attmps <= 3)) {
                HttpResponse responsePOST = httpClient.execute(post);
                
                Utility.logFile("after httpclient execute ", true);
                HttpEntity resEntity = responsePOST.getEntity();
                if (resEntity != null) {

                    //	Log.i("Response", jObj.toString());
                    try {
                        String content = EntityUtils.toString(resEntity);
                        Utility.logFile("response Entity:   " +content , true);
                        jObj = new JSONObject(content);
                        Utility.logFile("Audit submit response =  " + jObj.toString(), true);

                        if (jObj.has("Status") && jObj.has("batchno")) {
                            if (jObj.getString("Status").equalsIgnoreCase("1")) {
                                Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName
                                        + "~JSONParser~sendData~URL:" + strURL + " audit submit success.", true);
                                Cursor cursorDel = database.getVal("batchno=? and status=?", new String[]{strBatchNo, "1"},
                                        strAuditData);
                                if (cursorDel != null) {
                                    try {
                                        Utility.logFile("deleting audit count =  " + cursorDel.getCount(), true);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Utility.logFile("Exception in deleting audit =  " + e.toString(), true);
                                    }
                                    if (cursorDel.moveToFirst()) {
                                        do {
                                            strBPpath = cursorDel.getString(cursorDel.getColumnIndex("bppath"));
                                            strSIpath = cursorDel.getString(cursorDel.getColumnIndex("sipath"));
                                            strBSIpath = cursorDel.getString(cursorDel.getColumnIndex("bsipath"));
                                            deleteFiles(strBSIpath, strSIpath, strBPpath);
                                        } while (cursorDel.moveToNext());
                                    }
                                }
                                cursorDel.close();
                                strBatchNo = jObj.getString("batchno");
                                database.deleteVal("batchno=? and status=?", new String[]{strBatchNo, "1"}, strAuditData);
                                send_failure = false;        // Successfully sent, now got to next
                                //Utility.alert("Your Audit was submitted successfully");
                            }
                        } else {
                            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~sendData~URL:" + strURL + " audit submit unsuccess." + jObj.toString(), true);
                            Thread.sleep(300000);    // Sleep for 5 minutes and then try again. Hopefully they have better signal
                            //Utility.alert("Unable to upload the Audit.\nPlease move to a Stronger Signal Location and Click \"OK\"");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utility.logFile("Exception in Audit submit response =  " + e.toString(), true);
                    }
                } else {
                    Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~sendData~URL:" + strURL + " response entity is null.", true);
                    send_failure = false;
                }
            }
            database.close();
            System.out.println("false" + strBatchNo);
            Utility.updateBatchno(false, strBatchNo);

            strtemp = null;
            strData = null;
            strSummary = null;
            strSummarData = null;
            strBSIpath = null;
            strSIpath = null;
            strBPpath = null;
            strBASearchImage = null;
            strBAImage = null;
            strSignImage = null;
            strBASearchImageName = null;
            strBAImageName = null;
            strSignImageName = null;
            list = null;
            listSummary = null;
            bitmap = null;
            out = null;
            byteArr = null;
        } catch (ConnectTimeoutException e) {
            System.out.println("cefalse" + strBatchNo);
            Utility.updateBatchno(false, strBatchNo);
            Utility.logFile("sendData method ConnectTimeoutException strBatchNo =  " + strBatchNo, true);
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" +
                    Utility.strAuName + "~JSONParser~sendData~URL:" + strURL + " timeout error " + e.toString(), true);
            Log.i("ConnectTimeoutException", e.toString());
        } catch (Exception e) {
            System.out.println("exfalse" + strBatchNo);
            Utility.updateBatchno(false, strBatchNo);
            Utility.logFile("sendData method strBatchNo =  " + strBatchNo, true);
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~"
                    + Utility.strAuName + "~JSONParser~sendData~URL:" + strURL + " error " + e.toString(), true);
            Log.i("Exception", e.toString());
        }
    }



    //Delete the ba image and sign image from sdcard once audit submitted
    private static void deleteFiles(String strBA, String strSign, String strFinal) {
        try {
            File file;
            if (strBA != null) {
                file = new File(strBA);
                file.delete();
            }
            if (strSign != null) {
                file = new File(strSign);
                file.delete();
            }
            if (strFinal != null) {
                file = new File(strFinal);
                file.delete();
            }
        } catch (Exception e) {
            Log.i("Exception", e.toString());
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~deleteFiles~error " + e.toString(), true);
        }
    }

    //Update logfile to server.
    public static JSONObject updateLog(String url, String strDevice, String strLog) {
        try {
            jObj = new JSONObject();
            final HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
            HttpConnectionParams.setSoTimeout(httpParameters, 23000);
            httpClient = getNewHttpClient();
            HttpPost post = new HttpPost(url);

            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("datetime", "" + Utility.currentDate() + " " + Utility.currentTimesecond().replace(" ", "+")));
            params.add(new BasicNameValuePair("devicedetails", "" + strDevice));
            params.add(new BasicNameValuePair("user", "user"));
            params.add(new BasicNameValuePair("class", "Login"));
            params.add(new BasicNameValuePair("method", "updateLog"));
            params.add(new BasicNameValuePair("logmsg", "" + strLog.replace(" ", "+")));

            UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            post.setEntity(ent);
            HttpResponse responsePOST = httpClient.execute(post);
            HttpEntity resEntity = responsePOST.getEntity();
            if (resEntity != null) {
                jObj = new JSONObject(EntityUtils.toString(resEntity));
                Log.i("Response", jObj.toString());
                if (jObj.has("Status")) {
                    if (jObj.getString("Status").equalsIgnoreCase("1")) {
            //            deleteLogfile();
                    }
                }
            } else {
                Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~updateLog~URL:" + url + " response entity is null.", true);
            }

        } catch (ConnectTimeoutException e) {
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~updateLog~URL:" + url + " timeout error " + e.toString(), true);
            Log.i("ConnectTimeoutException", e.toString());
        } catch (Exception e) {
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~updateLog~URL:" + url + " error " + e.toString(), true);
            Log.i("Exception", e.toString());
        }
        return jObj;
    }

    //Delete updated logfile from sdcard
    private static void deleteLogfile() {
        File folder, files[];
        //Delete BAs Picture
        folder = new File(Environment.getExternalStorageDirectory() + "/" + Utility.context.getResources().getString(R.string.app_name) + "/Log_File");
        files = folder.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                file.delete();
            }
        }
    }

    //Download the Training Content
    public static String downloadContent(String strURL, String strFilename) throws UnsupportedEncodingException {
        String downloadSuccess = "false+0";
        strURL = strURL.trim();
        final HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 20000);
        HttpConnectionParams.setSoTimeout(httpParameters, 23000);
        httpClient = getNewHttpClient();
        httpGet = new HttpGet(strURL);
        HttpResponse response = null;
        ;
        File file = null, fileOutput = null;
        FileOutputStream fileOutputStream = null;
        try {
            response = httpClient.execute(httpGet);
            intStatusCode = response.getStatusLine().getStatusCode();
            if (intStatusCode == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    if (Utility.getFreeSize() >= (entity.getContentLength() / 1024)) {
                        file = new File(Environment.getExternalStorageDirectory() + "/" + Utility.context.getResources().getString(R.string.app_name) + "/Training/Content_File/");
                        file.mkdirs();
                        fileOutput = new File(file, strFilename);
                        fileOutputStream = new FileOutputStream(fileOutput);
                        InputStream instream = entity.getContent();
                        int bufferSize = 1024;
                        byte[] buffer = new byte[bufferSize];
                        int len = 0;
                        while ((len = instream.read(buffer)) != -1) {
                            fileOutputStream.write(buffer, 0, len);
                        }
                        instream.close();
                        fileOutputStream.close();
                        downloadSuccess = "true+1";
                    } else {
                        Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~downloadContent~URL:" + strURL + " no memory to store this content.", true);
                        downloadSuccess = "true+0";
                    }
                } else {
                    Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~downloadContent~URL:" + strURL + " entity null.", true);
                    Log.i("Response", "Response entity null");
                }
            }
        } catch (ConnectTimeoutException e) {
            downloadSuccess = "false+1";
            Log.i("ConnectTimeoutException", e.toString());
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~downloadContent~URL:" + strURL + " timeout error " + e.toString(), true);
        } catch (ClientProtocolException e) {
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~downloadContent~URL:" + strURL + " ClientProtocolException " + e.toString(), true);
            Log.i("Error", e.toString());
        } catch (IOException e) {
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~downloadContent~URL:" + strURL + " IOException " + e.toString(), true);
            Log.i("Error", e.toString());
        }
        return downloadSuccess;
    }

    //Download Training Thumbnail images from server
    public static Bitmap downloadthumbnail(String strURL, String strFilename) throws UnsupportedEncodingException {
        strURL = strURL.trim();
        Bitmap bitmap = null;
        final HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, 7000);
        HttpConnectionParams.setSoTimeout(httpParameters, 10000);
        httpClient = getNewHttpClient();
        httpGet = new HttpGet(strURL);
        File file = null, fileOutput = null;
        FileOutputStream fileOutputStream = null;
        HttpResponse response = null;
        ;
        try {
            response = httpClient.execute(httpGet);
            intStatusCode = response.getStatusLine().getStatusCode();
            if (intStatusCode == 200) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = entity.getContent();
                    file = new File(Environment.getExternalStorageDirectory() + "/" + Utility.context.getResources().getString(R.string.app_name) + "/Training/");
                    file.mkdirs();
                    fileOutput = new File(file, strFilename);
                    fileOutputStream = new FileOutputStream(fileOutput);
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, len);
                    }
                    fileOutputStream.close();
                    bitmap = Utility.decodeFile(fileOutput);
                }
            }
        } catch (ConnectTimeoutException e) {
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~downloadthumbnail~URL:" + strURL + " timeout error " + e.toString(), true);
            Log.i("ConnectTimeoutException", e.toString());
        } catch (ClientProtocolException e) {
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~downloadthumbnail~URL:" + strURL + " ClientProtocolException " + e.toString(), true);
            Log.i("Error", e.toString());
        } catch (IOException e) {
            Utility.logFile(Utility.currentDate() + " " + Utility.currentTimesecond() + "~" + Utility.strAuName + "~JSONParser~downloadthumbnail~URL:" + strURL + " IOException " + e.toString(), true);
            Log.i("Error", e.toString());
        }
        return bitmap;
    }
}