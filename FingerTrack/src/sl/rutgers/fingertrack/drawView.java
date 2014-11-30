package sl.rutgers.fingertrack;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PointF;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class drawView  extends View implements OnTouchListener{
	
	private final static int radius = 80;
	private Paint paint;
	//arrays to store multi-touch points
	private SparseArray<PointF> mActivePointers;
	//pool of colors
	private int[] colors = { Color.BLUE, Color.GREEN, Color.MAGENTA,
		      Color.BLACK, Color.CYAN, Color.GRAY, Color.RED, Color.DKGRAY,
		      Color.LTGRAY, Color.YELLOW };
	
	public drawView(Context context)
	{
		super(context);
		//initialize array and paint
		mActivePointers = new SparseArray<PointF>();
		paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setTextSize(30);
		paint.setTextAlign(Align.LEFT);
		
		setFocusable(true);
		
		//set a listener for touch
		this.setOnTouchListener(this);
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		//draw a circle for every touch point
		for (int size = mActivePointers.size(),i=0;i<size;i++) {
			PointF point = mActivePointers.valueAt(i);
			int id = mActivePointers.keyAt(i);
			if(point!=null) {
				paint.setColor(colors[i % 9]);
			canvas.drawCircle(point.x, point.y, radius, paint);
			//show position information 
			canvas.drawText("print id : "+id+"x: "+point.x+" y: "+point.y, 50, 50*(i+1), paint);
			}
		}
	}
	
	//method responds touch issue
	@Override
	public boolean onTouch(View view, MotionEvent event)
	{
		int pointerIndex = event.getActionIndex();
		int pointerId = event.getPointerId(pointerIndex);
		int maskedAction = event.getActionMasked();
		
		switch(maskedAction) {
		//add a new point
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN: {
			PointF f = new PointF();
			f.x = event.getX(pointerIndex);
			f.y = event.getY(pointerIndex);
			mActivePointers.put(pointerId, f);
			break;
		}
		//change position information for moving points
		case MotionEvent.ACTION_MOVE: {
			for(int size = event.getPointerCount(),i = 0;i<size;i++) {
				PointF point = mActivePointers.get(event.getPointerId(i));
				if(point != null) {
					point.x = event.getX(i);
					point.y = event.getY(i);
					
				}
			}
			break;
		}
		//delete untouched point
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_CANCEL: {
			mActivePointers.remove(pointerId);
			break;
		}
		}
		invalidate();
		
		return true;
	}

}
