package net.suteren.layout;

import static android.text.format.DateFormat.DAY;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.SortedSet;

import net.suteren.R;
import net.suteren.domain.DayMenu;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
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

public class DayScrollableLayout extends HorizontalScrollView {
	private static final int SWIPE_MIN_DISTANCE = 5;
	private static final int SWIPE_THRESHOLD_VELOCITY = 300;

	private SortedSet<DayMenu> mItems = null;
	private GestureDetector mGestureDetector;
	private int mActiveFeature = 0;
	private View todayIndicator;
	private int triggerHour;
	private int triggerMinute;

	public DayScrollableLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public DayScrollableLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public DayScrollableLayout(Context context) {
		super(context);
	}

	public void setTodayIndicator(View v) {
		todayIndicator = v;
		todayIndicator.setVisibility(View.INVISIBLE);
	}

	public void setFeatureItems(SortedSet<DayMenu> items) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getContext());
		triggerHour = prefs.getInt("triggerHour",
				getResources().getInteger(R.integer.triggerHour));
		triggerMinute = prefs.getInt("triggerMinute", getResources()
				.getInteger(R.integer.triggerMinute));
		Log.d(getClass().getName(), "Trigger hour: " + triggerHour + ":"
				+ triggerMinute);
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
			fl.setLayoutParams(new LayoutParams(width, LayoutParams.FILL_PARENT));

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
					toggleToday();
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
		for (DayMenu item : mItems) {
			Calendar d = item.getDate();
			d = Calendar.getInstance();
			d.setTime(item.getDate().getTime());
			d.set(Calendar.HOUR_OF_DAY, triggerHour);
			d.set(Calendar.MINUTE, triggerMinute);
			int c = now.compareTo(d);
			Log.d("HomeFeatureLayout", "comp: " + c);
			if (c < 0) {
				pos = x;
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
		Integer pos = getTodayPosition();
		if (pos == null)
			return false;
		return pos == mActiveFeature;
	}

	public boolean goToToday() {
		Integer x = getTodayPosition();
		if (x == null)
			return false;
		mActiveFeature = x;
		Log.d("HomeFeatureLayout", "Scroll To: " + mActiveFeature);
		smoothScrollTo(getMeasuredWidth() * mActiveFeature, 0);
		toggleToday();
		return true;
	}

	private void toggleToday() {
		if (isToday()) {
			if (todayIndicator instanceof TextView) {
				Calendar now = Calendar.getInstance();
				Calendar c = Calendar.getInstance();
				DateFormat df = android.text.format.DateFormat
						.getDateFormat(getContext());
				DateFormat tf = android.text.format.DateFormat
						.getTimeFormat(getContext());

				DayMenu item = mItems.toArray(new DayMenu[0])[getTodayPosition()];
				c.setTime(item.getDate().getTime());
				now.set(Calendar.HOUR_OF_DAY, 0);
				now.set(Calendar.MINUTE, 0);

				Log.d(getClass().getName(),
						"now: " + df.format(now.getTime()) + " "
								+ tf.format(now.getTime()) + " day: "
								+ df.format(c.getTime()) + " "
								+ tf.format(c.getTime()));

				int comp = now.compareTo(c);
				Log.d(getClass().getName(), "comp: " + comp);
				if (comp > 0) {
					((TextView) todayIndicator).setText(getResources()
							.getString(R.string.today));
				} else {
					((TextView) todayIndicator).setText(getResources()
							.getString(R.string.tomorrow));
				}
			}
			todayIndicator.setVisibility(View.VISIBLE);
		} else {
			todayIndicator.setVisibility(View.INVISIBLE);
		}
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
					if (isToday()) {
						todayIndicator.setVisibility(View.VISIBLE);
					} else {
						todayIndicator.setVisibility(View.INVISIBLE);
					}
					return true;
				}
				// left to right
				else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					int featureWidth = getMeasuredWidth();
					mActiveFeature = (mActiveFeature > 0) ? mActiveFeature - 1
							: 0;
					smoothScrollTo(mActiveFeature * featureWidth, 0);
					if (isToday()) {
						todayIndicator.setVisibility(View.VISIBLE);
					} else {
						todayIndicator.setVisibility(View.INVISIBLE);
					}
					return true;
				}
			} catch (Exception e) {
				Log.e("Fling", "There was an error processing the Fling event:"
						+ e.getMessage());
			}
			return false;
		}
	}

	public int getPosition() {
		return mActiveFeature;
	}

	public void scrollTo(int position) {
		mActiveFeature = position;
		smoothScrollTo(getMeasuredWidth() * mActiveFeature, 0);
		toggleToday();
	}

}