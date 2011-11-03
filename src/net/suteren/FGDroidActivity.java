package net.suteren;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.SortedSet;
import java.util.TreeSet;

import net.suteren.domain.DayMenu;
import net.suteren.fg.FGManager;
import net.suteren.fg.FileLocker;
import net.suteren.fg.Locker;
import net.suteren.layout.DayScrollableLayout;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

public class FGDroidActivity extends Activity {

	Resources res;
	Locker locker;

	SortedSet<DayMenu> days = new TreeSet<DayMenu>();
	private FGManager manager;
	private DayScrollableLayout hfl;

	private static final String LOG_TAG = "FGDroid";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		locker = new FileLocker(getFilesDir());
		manager = new FGManager(locker);

		Log.d(LOG_TAG, "Activity start");
		res = getResources();

		if (!load())
			fetchData();

		checkDataFreshness();

		redraw();

		hfl.post(new Runnable() {
			public void run() {
				hfl.goToToday();
			}
		});
	}

	private void checkDataFreshness() {
		if (days == null || days.size() < 1)
			load();
		if (days == null)
			return;
		Calendar cal = Calendar.getInstance();
		int dow = cal.get(Calendar.DAY_OF_WEEK);
		cal.add(Calendar.DATE, -dow);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DATE, 7);
		if (days.isEmpty() || cal.compareTo(days.last().getDate()) > 0) {
			fetchData();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			fetchData();
			return true;
		case R.id.today:
			hfl.goToToday();
			return true;
		case R.id.about:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void fetchData() {
		Log.d("FGDroid", "Starting service");
		Intent service = new Intent(this, FGDroidService.class);
		Log.d("FGDroid", "Service started");
		startService(service);
	}

	private void redraw() {
		Log.d(LOG_TAG, "Redraw");
		View mainLayout = LinearLayout.inflate(this, R.layout.main, null);
		final Integer pos;

		if (hfl != null) {
			Log.d(LOG_TAG, "HFL not null");
			pos = hfl.getPosition();
		} else
			pos = null;

		hfl = (DayScrollableLayout) mainLayout
				.findViewById(R.id.dayScrollableLayout);

		hfl.setTodayIndicator(mainLayout.findViewById(R.id.today));
		// hfl = new DayScrollableLayout(this);

		// DayMenu dayMenu = days.get(showedDay.getTimeInMillis());

		hfl.setFeatureItems(days);
		if (pos != null) {
			Log.d(LOG_TAG, "Draw - Scrolling to: " + pos);

			hfl.post(new Runnable() {
				public void run() {
					hfl.scrollTo(pos);
				}
			});
		}

		setContentView(mainLayout);
	}

	private boolean load() {
		Log.d(LOG_TAG, "Loading...");
		boolean result = false;
		try {
			if (!locker.isLocked()) {
				result = true;
				Log.d(LOG_TAG, "Unlocked");
				// fetchData();

				String[] week_files = res.getStringArray(R.array.week_file);
				for (int i = 0; i < week_files.length; i++) {
					Log.d(LOG_TAG, "Week file " + i);
					try {

						File cd = getCacheDir();
						cd = new File(cd,
								res.getStringArray(R.array.week_file)[i]);
						FileInputStream fis = new FileInputStream(cd);

						// FileInputStream fis = openFileInput(week_files[i]);
						Log.d(LOG_TAG, "Loading file " + week_files[i]);
						manager.load(fis, days);
					} catch (Exception e1) {
						result = false;
						Log.e(LOG_TAG, "Parsing data failed.", e1);
					}
				}
			} else {
				Log.d(LOG_TAG, "Locked");
			}
		} catch (Exception e) {
			Log.d(LOG_TAG, "Fail.", e);
		}
		return result;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		Log.d(LOG_TAG, "New intent1 hfl: " + hfl);
		Log.d(LOG_TAG, "New intent position: " + hfl.getPosition());
		super.onNewIntent(intent);
		Log.d(LOG_TAG, "New intent2 hfl: " + hfl);
		Log.d(LOG_TAG, "New intent position: " + hfl.getPosition());

		load();

		redraw();

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int a = event.getAction();
		Log.d(LOG_TAG, "Action: " + a);
		a = event.getActionIndex();
		Log.d(LOG_TAG, "ActionIndex: " + a);
		a = event.getActionMasked();
		Log.d(LOG_TAG, "ActionMasked: " + a);
		return super.onTouchEvent(event);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (hfl.isToday())
			super.onBackPressed();
		else
			hfl.goToToday();
	}
}