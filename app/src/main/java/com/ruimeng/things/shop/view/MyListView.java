package com.ruimeng.things.shop.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;

public class MyListView extends ListView {

	public MyListView(Context context) {
		super(context);
	}
	public MyListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public MyListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2,
				MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);

	}

//	private float xDistance, yDistance, xLast, yLast;
//	@Override
//	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		switch (ev.getAction()) {
//			case MotionEvent.ACTION_DOWN:
//				xDistance = yDistance = 0f;
//				xLast = ev.getX();
//				yLast = ev.getY();
//				break;
//			case MotionEvent.ACTION_MOVE:
//				final float curX = ev.getX();
//				final float curY = ev.getY();
//
//				xDistance += Math.abs(curX - xLast);
//				yDistance += Math.abs(curY - yLast);
//				xLast = curX;
//				yLast = curY;
//
//				if (xDistance > yDistance) {
//					return false;   //表示向下传递事件
//				}
//		}
//
//		return super.onInterceptTouchEvent(ev);
//	}

}
