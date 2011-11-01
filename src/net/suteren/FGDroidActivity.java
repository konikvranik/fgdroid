package net.suteren;

import java.io.FileInputStream;
import java.util.SortedSet;
import java.util.TreeSet;

import net.suteren.domain.DayMenu;
import net.suteren.fg.FGManager;
import net.suteren.fg.FileLocker;
import net.suteren.fg.Locker;
import net.suteren.layout.HomeFeatureLayout;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;

public class FGDroidActivity extends Activity {

	Resources res;
	Locker locker;

	SortedSet<DayMenu> days = new TreeSet<DayMenu>();
	private FGManager manager;
	private HomeFeatureLayout hfl;

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

		redraw();
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
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// @Override
	// protected void onNewIntent(Intent intent) {
	// Log.d(LOG_TAG, "Intent 1");
	// super.onNewIntent(intent);
	// Log.d(LOG_TAG, "Intent 2");
	// if (!"redraw".equals(intent.getAction())) {
	// Log.d(LOG_TAG, "Fetching data again");
	// fetchData();
	// }
	// load();
	// redraw();
	// }

	private void fetchData() {
		Log.d("FGDroid", "Starting service");
		Intent service = new Intent(this, FGDroidService.class);
		Log.d("FGDroid", "Service started");
		startService(service);
	}

	private void redraw() {
		Log.d(LOG_TAG, "Redraw");

		hfl = new HomeFeatureLayout(this);

		// DayMenu dayMenu = days.get(showedDay.getTimeInMillis());

		hfl.setFeatureItems(days);
		setContentView(hfl);
		hfl.goToToday();
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
						FileInputStream fis = openFileInput(week_files[i]);
						Log.d(LOG_TAG, "Loading file " + week_files[i]);
						manager.load(fis, days);
					} catch (Exception e1) {
						result = false;
						Log.e(LOG_TAG, "Parsing data failed.", e1);
					}
				}
				return result;
			} else {
				Log.d(LOG_TAG, "Locked");
			}
		} catch (Exception e) {
			Log.d(LOG_TAG, "Fail.", e);
		}
		return result;
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
}