package net.suteren;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;

import net.suteren.fg.FGManager;
import net.suteren.fg.FileLocker;
import net.suteren.fg.Locker;

import org.w3c.dom.Node;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class FGDroidService extends IntentService {

	private static final int HELLO_ID = 1;
	private static final String LOG_TAG = "FGDroid";

	private ContextWrapper context;
	private Resources res;

	private Locker locker;
	private FGManager manager;

	public FGDroidService() {
		super("FGDroidService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		locker = new FileLocker(getFilesDir());
		manager = new FGManager(locker);

		res = getResources();
		context = new ContextWrapper(getApplicationContext());
		try {

			Node n = null;

			Log.d(LOG_TAG, "3G: " + is3G());
			// Log.d("FGDroid", "WiFi: " + getMacAddress());
			Log.d(LOG_TAG, "Connected: " + isConnected());
			if (isConnected()) {
				File fd = getFilesDir();
				boolean refresh = true;
				for (int i = 0; i < 2; i++) {
					final String fn = res.getStringArray(R.array.week_file)[i];
					File[] files = fd.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							if (name != null && name.equals(fn))
								return true;
							return false;
						}
					});
					for (int j = 0; j < files.length; j++) {
						long lm = files[j].lastModified();
						Calendar now = Calendar.getInstance();
						int dow = now.get(Calendar.DAY_OF_WEEK);
						now.add(Calendar.DATE, -dow);
						if (lm < now.getTimeInMillis())
							refresh = true;

					}
					if (refresh) {
						n = manager
								.retrieve(
										new URL(
												res.getStringArray(R.array.week_url)[i]),
										getTemplate());
						File cd = getCacheDir();
						cd = new File(cd,
								res.getStringArray(R.array.week_file)[i]);
						FileOutputStream fos = new FileOutputStream(cd);
						// FileOutputStream fos = openFileOutput(
						// res.getStringArray(R.array.week_file)[i],
						// Context.MODE_PRIVATE);
						manager.save(fos, n);
					}
				}
				if (refresh)
					notification(res.getString(R.string.succesfull_fetch));
				// Intent i = new Intent(this, FGDroidActivity.class);
				// i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				// | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				// i.setAction("redraw");
				// startActivity(i);
				// sendBroadcast(i);
			} else {
				Log.w(LOG_TAG, "Not connected: fetch skipped.");
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Retrieving of FG menu failed.", e);
		} finally {
			locker.unlock();
		}
	}

	private void notification(String text) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.fg;
		CharSequence tickerText = text;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = (notification.flags | Notification.FLAG_AUTO_CANCEL)
				& ~Notification.FLAG_NO_CLEAR;
		Context context = getApplicationContext();
		CharSequence contentTitle = text;
		CharSequence contentText = text;
		Intent notificationIntent = new Intent(getBaseContext(),
				FGDroidActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NO_HISTORY
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		mNotificationManager.notify(HELLO_ID, notification);
	}

	private InputStream getTemplate() throws IOException {
		AssetManager assets = getAssets();
		InputStream inXsl = assets.open("fg.xsl");
		return inXsl;
	}

	public boolean is3G() {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		return !(connectivityManager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED || connectivityManager
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED);
	}

	public String getMacAddress() {
		WifiManager wm = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		return wm.getConnectionInfo().getMacAddress();
	}

	public boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}

}
