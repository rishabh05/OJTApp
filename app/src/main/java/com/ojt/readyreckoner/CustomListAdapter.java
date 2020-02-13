/*@ID: CN20140001
 *@Description: srcCustomListAdapter is adapter class 
 * This class is used to list out BAs 
 * based on store code or counter name
 * Content are fetched from server.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 21/03/2014
 */
package com.ojt.readyreckoner;

import java.util.List;

import com.ojt.notification.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomListAdapter extends  ArrayAdapter<RowItem>
{
	Context context=null;
	public CustomListAdapter(Context context, int resourceId,List<RowItem> items) 
	{
        super(context, resourceId, items);
        this.context = context;
    }
    private class ViewHolder
    {
        TextView txtName;
        TextView txtID;
    }
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        ViewHolder holder = null;
        RowItem rowItem = getItem(position);
        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) 
        {
            convertView = mInflater.inflate(R.layout.listrow, null);
            holder = new ViewHolder();
            holder.txtName = (TextView) convertView.findViewById(R.id.nametxt);
            holder.txtID = (TextView) convertView.findViewById(R.id.idtxt);
            convertView.setTag(holder);
        }
        else
            holder = (ViewHolder) convertView.getTag();
 
        holder.txtName.setText(rowItem.getName());
        holder.txtID.setText(rowItem.getID());
        return convertView;
    }
}