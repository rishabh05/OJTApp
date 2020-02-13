/*@ID: CN20140001
 *@Description: srcSingleTouchEvent is View for digital sign process 
 * @Developer: Arunachalam
 * @Version 1.0
 * @Date: 15/03/2014
 * @Modified Date: 26/08/2014
 */
package com.ojt.baaudit;
import com.ojt.utilities.Utility;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
public class SingleTouchEvent extends View 
{
  private Paint paint=null;
  private Path path = new Path();  
  public Canvas canvas=null;        
  public Bitmap bitmap=null;
  public View view=null;
  public SingleTouchEvent(Context context, AttributeSet attrs) 
  {
    super(context, attrs);
    paint = new Paint();
    paint.setAntiAlias(true);
    paint.setDither(true);
    paint.setColor(0xFF003F87);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeJoin(Paint.Join.ROUND);
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setStrokeWidth(3); 
  }
  //Convert sign as image format
  @Override
  protected void onSizeChanged(int intWidth, int intHeight, int intOldWidth, int intOldHeight) 
  {
      super.onSizeChanged(intWidth, intHeight, intOldWidth, intOldHeight);  
      bitmap = Bitmap.createBitmap(intWidth, intHeight, Bitmap.Config.ARGB_8888);            
      canvas = new Canvas(bitmap);    
      return;
  }
  @Override
  protected void onDraw(Canvas canvas) 
  {
    canvas.drawPath(path, paint);
    canvas.drawBitmap(bitmap, 0, 0, paint);
  }
  //Get left,right,top and bottom position
  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    float eventX = event.getX();
    float eventY = event.getY();
    
    switch (event.getAction()) 
    {
    	case MotionEvent.ACTION_DOWN:
    		path.moveTo(eventX, eventY);
    		return true;
    	case MotionEvent.ACTION_MOVE:
    		path.lineTo(eventX, eventY);
    		Utility.hasSignImage=true;
    		break;
    	case MotionEvent.ACTION_UP:
    		break;
    	default:
    		return false;
    }    
    // Schedules a repaint.
    invalidate();
    return true;
  } 
 }