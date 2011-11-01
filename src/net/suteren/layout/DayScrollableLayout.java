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

import static android.text.format.DateFormat.DAY;

public class DayScrollableLayout extends HorizontalScrollView {
	private static final int SWIPE_MIN_DISTANCE = 5;
	private static final int SWIPE_THRESHOLD_VELOCITY = 300;

	private SortedSet<DayMenu> mItems = null;
	private GestureDetector mGestureDetector;
	private int mActiveFeature = 0;
	private SortedSet<DayMenu> items;

	public DayScrollableLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public DayScrollableLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DayScrollableLayout(Context context) {
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
		Calendar now = Calendar.getInstance();
		for (DayMenu item : items) {

			LinearLayout fl = (LinearLayout) LinearLayout.inflate(getContext(),
					R.layout.day, null);
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
			setDate(item.getDate(), fl);

			internalWrapper.addView(fl);

			int c = now.compareTo(item.getDate());
			Log.d("HomeFeatureLayout", "comp: " + c);
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

	Integer getTodayPosition() {
		Calendar now = Calendar.getInstance();
		int x = 0;
		int pos;
		for (DayMenu item : items) {
			Calendar d = item.getDate();
			d = Calendar.getInstance();
			d.setTime(item.getDate().getTime());
			d.add(Calendar.HOUR, -9);
			int c = now.compareTo(d);
			Log.d("HomeFeatureLayout", "comp: " + c);
			if (c < 0) {
				pos = x - 1;
				if (pos < 0)
					pos = 0;
				Log.d("HomeFeatureLayout", "Match: " + x);
				return pos;
			}
			x++;
		}
		return null;
	}

	public boolean isToday() {
		return getTodayPosition() == mActiveFeature;
	}

	public boolean goToToday() {
		Integer x = getTodayPosition();
		if (x == null)
			return false;
		mActiveFeature = x;
		Log.d("HomeFeatureLayout", "Scroll To: " + mActiveFeature);
		smoothScrollTo(getMeasuredWidth() * mActiveFeature, 0);
		return true;
	}

	private void setDate(Calendar item, View fl) {
		TextView tv = (TextView) fl.findViewById(R.id.dayDate);
		DateFormat fo = android.text.format.DateFormat
				.getLongDateFormat(getContext());
		tv.setText(fo.format(item.getTime()));

		tv = (TextView) fl.findViewById(R.id.dayDOW);

		tv.setText(android.text.format.DateFormat.format("" + DAY + DAY + DAY
				+ DAY, item.getTime()));

		// android.R.drawable.ic_menu_day;
		// android.R.drawable.ic_menu_revert;
		// android.R.drawable.ic_menu_today;

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