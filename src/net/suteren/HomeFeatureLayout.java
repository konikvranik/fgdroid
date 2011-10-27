package net.suteren;

import java.text.DateFormat;
import java.util.ArrayList;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class HomeFeatureLayout extends HorizontalScrollView {
	private static final int BIG_FONT = 18;
	private static final int SWIPE_MIN_DISTANCE = 5;
	private static final int SWIPE_THRESHOLD_VELOCITY = 300;

	private ArrayList<DayMenu> mItems = null;
	private GestureDetector mGestureDetector;
	private int mActiveFeature = 0;

	public HomeFeatureLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public HomeFeatureLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HomeFeatureLayout(Context context) {
		super(context);
	}

	public void setFeatureItems(ArrayList<DayMenu> items) {
		LinearLayout internalWrapper = new LinearLayout(getContext());
		internalWrapper.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		internalWrapper.setOrientation(LinearLayout.HORIZONTAL);
		internalWrapper.setClipChildren(true);
		addView(internalWrapper);
		this.mItems = items;

		Display display = ((WindowManager) getContext().getSystemService(
				Context.WINDOW_SERVICE)).getDefaultDisplay();
		int width = display.getWidth();

		for (int i = 0; i < items.size(); i++) {

			ScrollView fl = new ScrollView(getContext());
			fl.setLayoutParams(new LayoutParams(width,
					LayoutParams.WRAP_CONTENT));
			fl.setVerticalScrollBarEnabled(true);
			LinearLayout ll = new LinearLayout(getContext());
			ll.setLayoutParams(new LayoutParams(width,
					LayoutParams.WRAP_CONTENT));
			ll.setOrientation(LinearLayout.VERTICAL);
			ll.setClipChildren(true);

			TextView tv = prepareText();
			DateFormat fo = android.text.format.DateFormat
					.getDateFormat(getContext());
			tv.setText(fo.format(items.get(i).getDate().getTime()));
			tv.setTextSize(BIG_FONT);
			ll.addView(tv);

			tv = prepareText();
			tv.setText(getResources().getString(R.string.soup));
			tv.setTextSize(BIG_FONT);
			ll.addView(tv);

			LinearLayout grp = prepareGroup();

			for (Soup s : items.get(i).getSoups()) {
				tv = prepareText();
				tv.setText(s.getText());
				grp.addView(tv);
			}
			ll.addView(grp);

			tv = prepareText();
			tv.setText(getResources().getString(R.string.food));
			tv.setTextSize(BIG_FONT);
			ll.addView(tv);

			grp = prepareGroup();

			for (Food f : items.get(i).getFood()) {
				tv = prepareText();
				tv.setText(f.getText());
				grp.addView(tv);
			}
			ll.addView(grp);

			tv = prepareText();
			tv.setText(getResources().getString(R.string.superior));
			tv.setTextSize(BIG_FONT);
			ll.addView(tv);

			grp = prepareGroup();

			for (Superior s : items.get(i).getSuperior()) {
				tv = prepareText();
				tv.setText(s.getText());
				grp.addView(tv);
			}
			ll.addView(grp);

			tv = prepareText();
			tv.setText(getResources().getString(R.string.live));
			tv.setTextSize(BIG_FONT);
			ll.addView(tv);

			grp = prepareGroup();

			for (Live l : items.get(i).getLive()) {
				tv = prepareText();
				tv.setText(l.getText());
				grp.addView(tv);
			}
			ll.addView(grp);

			tv = prepareText();
			tv.setText(getResources().getString(R.string.pasta));
			tv.setTextSize(BIG_FONT);
			ll.addView(tv);

			grp = prepareGroup();

			for (Pasta p : items.get(i).getPasta()) {
				tv = prepareText();
				tv.setText(p.getText());
				grp.addView(tv);
			}
			ll.addView(grp);

			ll.requestLayout();
			fl.addView(ll);
			internalWrapper.addView(fl);
//			fl.setOnTouchListener(new View.OnTouchListener() {
//				public boolean onTouch(View v, MotionEvent event) {
//					// If the user swipes
//					if (mGestureDetector.onTouchEvent(event)) {
//						return true;
//					} else if (event.getAction() == MotionEvent.ACTION_UP
//							|| event.getAction() == MotionEvent.ACTION_CANCEL) {
//						int scrollY = getScrollY();
//						int featureWidth = v.getMeasuredWidth();
//						mActiveFeature = ((scrollY + (featureWidth / 2)) / featureWidth);
//						int scrollTo = mActiveFeature * featureWidth;
//						smoothScrollTo(scrollTo, 0);
//						return true;
//					} else {
//						return false;
//					}
//				}
//			});
		}
		setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// If the user swipes
				if (mGestureDetector.onTouchEvent(event)) {
					return true;
				} else if (event.getAction() == MotionEvent.ACTION_UP
						|| event.getAction() == MotionEvent.ACTION_CANCEL) {
					int scrollX = getScrollX();
					int featureWidth = v.getMeasuredWidth();
					mActiveFeature = ((scrollX + (featureWidth / 2)) / featureWidth);
					int scrollTo = mActiveFeature * featureWidth;
					smoothScrollTo(scrollTo, 0);
					return true;
				} else {
					return false;
				}
			}
		});
		mGestureDetector = new GestureDetector(new MyGestureDetector());
	}

	private LinearLayout prepareGroup() {
		LinearLayout grp;
		grp = new LinearLayout(getContext());
		grp.setOrientation(LinearLayout.VERTICAL);
		grp.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		grp.setPadding(10, 5, 0, 10);
		grp.setClipToPadding(true);
		grp.setClipChildren(true);
		return grp;
	}

	private TextView prepareText() {
		TextView tv;
		tv = new TextView(getContext());

		tv.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT));
		tv.setSingleLine(false);
		tv.setHorizontallyScrolling(false);
		return tv;
	}

	class MyGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				// right to left
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					int featureWidth = getMeasuredWidth();
					mActiveFeature = (mActiveFeature < (mItems.size() - 1)) ? mActiveFeature + 1
							: mItems.size() - 1;
					smoothScrollTo(mActiveFeature * featureWidth, 0);
					return true;
				}
				// left to right
				else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					int featureWidth = getMeasuredWidth();
					mActiveFeature = (mActiveFeature > 0) ? mActiveFeature - 1
							: 0;
					smoothScrollTo(mActiveFeature * featureWidth, 0);
					return true;
				}
			} catch (Exception e) {
				Log.e("Fling", "There was an error processing the Fling event:"
						+ e.getMessage());
			}
			return false;
		}
	}
}