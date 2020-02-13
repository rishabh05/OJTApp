/*@ID: CN20140001
 *@Description: srcComponents 
 * This class is used to create component dynamically which are used 
 *   in this app.
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 10/03/2014
 */
package com.ojt.components;
import com.ojt.utilities.Utility;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
public class Components 
{
	//Create Textview component
	public static TextView textView(String text)
	{
		TextView textview=new TextView(Utility.context);
		textview.setText(text);
		return textview;
	}
	//Create Edittext component
	public static EditText editText(String text)
	{
		EditText edittext=new EditText(Utility.context);
		edittext.setText(text);
		return edittext;
	}
	//Create Checkbox component
	public static CheckBox checkBox(String text){
		CheckBox checkbox=new CheckBox(Utility.context);
		checkbox.setText(text);
		return checkbox;
	}
	//Create Linearlayout component
	public static LinearLayout linearLayout()
	{
		LinearLayout linearlayout=new LinearLayout(Utility.context);
		return linearlayout;
	}
	//Create Linearlayout component
	public static RelativeLayout relativeLayout()
	{
		RelativeLayout relativelayout=new RelativeLayout(Utility.context);
		return relativelayout;
	}
	//Expand animation for particular view
	public static void expand(final View v) 
	{
	    v.measure(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
	    final int inttargetheight = v.getMeasuredHeight();
	    v.getLayoutParams().height = 0;
	    v.setVisibility(View.VISIBLE);
	    Animation animation = new Animation()
	    {
	        @Override
	        protected void applyTransformation(float interpolatedTime, Transformation t) 
	        {
	            v.getLayoutParams().height = interpolatedTime == 1
	                    ? LayoutParams.WRAP_CONTENT
	                    : (int)(inttargetheight * interpolatedTime);
	            v.requestLayout();
	        }

	        @Override
	        public boolean willChangeBounds() 
	        {
	            return true;
	        }
	    };
	    animation.setDuration(150);
	    v.startAnimation(animation);
	}
	//Collapse animation to particular view
	public static void collapse(final View v) 
	{
	    final int intinitialheight = v.getMeasuredHeight();
	    Animation animation = new Animation()
	    {
	        @Override
	        protected void applyTransformation(float interpolatedTime, Transformation t) 
	        {
	            if(interpolatedTime == 1)
	            {
	                v.setVisibility(View.GONE);
	            }
	            else
	            {
	                v.getLayoutParams().height = intinitialheight - (int)(intinitialheight * interpolatedTime);
	                v.requestLayout();
	            }
	        }
	        @Override
	        public boolean willChangeBounds() 
	        {
	            return true;
	        }
	    };
	    animation.setDuration(150);
	    v.startAnimation(animation);
	}
}