package net.suteren.layout;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.SortedSet;

import net.suteren.R;
import net.suteren.domain.DayMenu;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ExpandableListView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HomeFeatureLayout extends HorizontalScrollView {
	private static final int SWIPE_MIN_DISTANCE = 5;
	private static final int SWIPE_THRESHOLD_VELOCITY = 300;

	private SortedSet<DayMenu> mItems = null;
	private GestureDetector mGestureDetector;
	private int mActiveFeature = 0;
	private SortedSet<DayMenu> items;

	public HomeFeatureLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public HomeFeatureLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public HomeFeatureLayout(Context context) {
		super(context);
	}

	public void setFeatureItems(SortedSet<DayMenu> items) {
		this.items = items;
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
		int x = 0;
		Calendar now = Calendar.getInstance();
		for (DayMenu item : items) {

			LinearLayout fl = (LinearLayout) LinearLayout.inflate(getContext(),
					R.layout.main, null);
			fl.setLayoutParams(new LayoutParams(width,
					LayoutParams.MATCH_PARENT));

			ExpandableListView ll = (ExpandableListView) fl
					.findViewById(R.id.dayContent);
			ll.setAdapter(new SectionAdapter(getContext(), getResources()
					.getStringArray(R.array.foodTypes), item));
			for (int i = 0; i < getResources()
					.getStringArray(R.array.foodTypes).length; i++) {
				ll.expandGroup(i);
			}
			View tv = fl.findViewById(R.id.dayDate);
			setDate(item.getDate(), (TextView) tv);

			internalWrapper.addView(fl);

			int c = now.compareTo(item.getDate());
			Log.d("HomeFeatureLayout", "comp: " + c);
			if (c < 0 && mActiveFeature == 0) {
				mActiveFeature = x;
				Log.d("HomeFeatureLayout", "Match: " + x);
			}
			x++;
		}
		Log.d("HomeFeatureLayout", "Scroll to: " + mActiveFeature);

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

	public boolean goToToday() {
		Calendar now = Calendar.getInstance();
		int x = 0;
		boolean result = false;
		for (DayMenu item : items) {
			int c = now.compareTo(item.getDate());
			Log.d("HomeFeatureLayout", "comp: " + c);
			if (c < 0) {
				mActiveFeature = x;
				Log.d("HomeFeatureLayout", "Match: " + x);
				result = true;
				break;
			}
			x++;
		}
		Log.d("HomeFeatureLayout", "Scroll To: " + mActiveFeature);
		smoothScrollTo(getMeasuredWidth() * mActiveFeature, 0);
		return result;
	}

	private void setDate(Calendar item, TextView tv) {
		DateFormat fo = android.text.format.DateFormat
				.getLongDateFormat(getContext());
		tv.setText(fo.format(item.getTime()));
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