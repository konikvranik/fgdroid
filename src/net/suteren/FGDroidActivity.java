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
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

public class FGDroidActivity extends Activity {

	Resources res;
	Locker locker;

	SortedSet<DayMenu> days = new TreeSet<DayMenu>();
	private FGManager manager;
	private DayScrollableLayout dayScrollableLayout;
	private boolean goToToday = false;

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

		goToToday = true;
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
		cal.add(Calendar.DATE, 5);
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
			dayScrollableLayout.goToToday();
			return true;
		case R.id.preferences:
			Intent intent = new Intent(this, Preferences.class);
			startActivity(intent);
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

	void redraw() {
		Log.d(LOG_TAG, "Redraw");
		View mainLayout = LinearLayout
				.inflate(this, R.layout.main_layout, null);
		final Integer pos;

		if (dayScrollableLayout != null) {
			Log.d(LOG_TAG, "HFL not null");
			pos = dayScrollableLayout.getPosition();
		} else
			pos = null;

		dayScrollableLayout = (DayScrollableLayout) mainLayout
				.findViewById(R.id.dayScrollableLayout1);

		Log.d(LOG_TAG, "hfl: " + dayScrollableLayout);
		Log.d(LOG_TAG, "main: " + mainLayout);

		dayScrollableLayout.setTodayIndicator(mainLayout.findViewById(R.id.today));
		// hfl = new DayScrollableLayout(this);

		// DayMenu dayMenu = days.get(showedDay.getTimeInMillis());

		dayScrollableLayout.setFeatureItems(days);
		if (pos != null) {
			Log.d(LOG_TAG, "Draw - Scrolling to: " + pos);

			dayScrollableLayout.post(new Runnable() {
				public void run() {
					dayScrollableLayout.scrollTo(pos);
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
		Log.d(LOG_TAG, "New intent1 hfl: " + dayScrollableLayout);
		Log.d(LOG_TAG, "New intent position: " + dayScrollableLayout.getPosition());
		super.onNewIntent(intent);
		Log.d(LOG_TAG, "New intent2 hfl: " + dayScrollableLayout);
		Log.d(LOG_TAG, "New intent position: " + dayScrollableLayout.getPosition());

		load();

		redraw();

	}

	@Override
	public void onBackPressed() {
		if (dayScrollableLayout.isToday())
			super.onBackPressed();
		else
			dayScrollableLayout.goToToday();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(getClass().getName(), "Resume");
		redraw();
		if (goToToday) {
			dayScrollableLayout.post(new Runnable() {
				public void run() {
					dayScrollableLayout.goToToday();
				}
			});

		}
		goToToday = false;
	}
}