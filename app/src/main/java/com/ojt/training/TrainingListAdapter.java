/*@ID: CN20140001
 *@Description: srcCustomListAdapter is adapter class 
 * This class is used to list out Training Content 
 * Content are fetched from server.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 22/05/2014
 */
package com.ojt.training;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.List;

import com.ojt.connectivity.JSONParser;
import com.ojt.notification.R;
import com.ojt.utilities.Utility;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TrainingListAdapter extends  ArrayAdapter<TrainingData>
{
	private Context context=null;
	private String strImgURL = null;
	public TrainingListAdapter(Context context, int resourceId,List<TrainingData> items) 
	{
        super(context, resourceId, items);
        this.context = context;
    }
    private class ViewHolder
    {
        TextView txtName,txtRef;
        ImageView imgContent;
        RelativeLayout relativeLayout;
        ProgressBar progressBar;
    }
    public View getView(int position, View convertView, ViewGroup parent) 
    {
    	final ViewHolder holder;
        TrainingData rowItem = getItem(position);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) 
        {
            convertView = mInflater.inflate(R.layout.traininglistrow, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.trainingtext);
            holder.txtName.setSelected(true);
            holder.txtName.requestFocus();
            holder.imgContent = (ImageView) convertView.findViewById(R.id.trainingimage);
            holder.relativeLayout=(RelativeLayout) convertView.findViewById(R.id.traininglayout);
            holder.progressBar=(ProgressBar) convertView.findViewById(R.id.progressBar1);
            holder.txtRef=(TextView) convertView.findViewById(R.id.refedttxt);
            holder.txtRef.setSelected(true);
            holder.txtRef.requestFocus();
            holder.progressBar.setVisibility(View.INVISIBLE);
            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.txtName.setText(rowItem.getTitle());
        holder.txtRef.setText(rowItem.getReference());
        holder.txtRef.setBackgroundColor(Color.TRANSPARENT);
        File file = new File( Environment.getExternalStorageDirectory() + "/"+ Utility.context.getResources().getString(R.string.app_name)+"/Training/"+rowItem.getImageName());
		if(file.exists())
		{
			Bitmap bitmap=Utility.decodeFile(file);
			holder.imgContent.setImageBitmap(bitmap);
		}
		else
		{
			if(Utility.hasConnection())
			{
				strImgURL=rowItem.getImageURL();
				if(strImgURL!=null)
		    		new ImageDownloader(holder.imgContent,""+rowItem.getImageName(),holder.progressBar).execute(strImgURL);
			}
			else
			{
				holder.progressBar.setVisibility(View.INVISIBLE);
			}
		}
		if(rowItem.getStatus().equalsIgnoreCase("1"))
		{
			holder.relativeLayout.setBackgroundResource(R.drawable.contentread);
		}
		else
		{
			holder.relativeLayout.setBackgroundResource(R.drawable.contentunread);
		}
        return convertView;
    }
   
	class ImageDownloader extends AsyncTask<String, Void, Bitmap> 
	{
		String strImageName;
		ProgressBar progressBar;
		@SuppressWarnings("rawtypes")
		private WeakReference imageViewReference;
		@SuppressWarnings({ "rawtypes", "unchecked" })
		public ImageDownloader(ImageView imageView,String strImageName,ProgressBar progressBar) 
		{
	      this.strImageName=strImageName;
	      this.progressBar=progressBar;
	      imageViewReference = new WeakReference(imageView);
		}
		@Override
		protected void onPreExecute() 
		{
			progressBar.setVisibility(View.VISIBLE);
			super.onPreExecute();
		}
		@SuppressWarnings("finally")
		protected Bitmap doInBackground(String... urls) 
		{
			Bitmap bitmap = null;
			try
			{
				bitmap = JSONParser.downloadthumbnail(urls[0], strImageName);
			}
			catch (UnsupportedEncodingException e)
			{
				Log.i("Error",e.toString());
			}
			finally
			{
				return bitmap;
			}
		}
		protected void onPostExecute(Bitmap bitmap)
		{
			if (imageViewReference != null)
			{
				ImageView imageView = (ImageView) imageViewReference.get();
		        if (imageView != null)
		        {
		        	if (bitmap != null) 
		        	{
		        		imageView.setImageBitmap(bitmap);
		            } 
		        	else 
		        	{
		        		imageView.setImageDrawable(imageView.getContext().getResources()
		                            .getDrawable(R.drawable.nothumbnail));
		            }
		        }
		    }
			if(progressBar!=null)
			{
				progressBar.setVisibility(View.INVISIBLE);
			}
		 }
	}
}