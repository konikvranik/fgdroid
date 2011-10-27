package net.suteren;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.tidy.Tidy;

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
	public static final String LOCK_FILE_NAME = ".lock";
	private ContextWrapper context;
	private Resources res;

	public FGDroidService() {
		super("FGDroidService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		res = getResources();
		context = new ContextWrapper(getApplicationContext());
		try {

			Node n = null;

			Log.d(LOG_TAG, "3G: " + is3G());
			// Log.d("FGDroid", "WiFi: " + getMacAddress());
			Log.d(LOG_TAG, "Connected: " + isConnected());
			if (isConnected()) {
				File fd = getFilesDir();
				for (int i = 0; i < 2; i++) {
					final String fn = res.getStringArray(R.array.week_file)[i];
					File[] files = fd.listFiles(new FilenameFilter() {
						public boolean accept(File dir, String name) {
							if (name != null && name.equals(fn))
								return true;
							return false;
						}
					});
					boolean refresh = false;
					for (int j = 0; j < files.length; j++) {
						long lm = files[j].lastModified();
						Calendar now = Calendar.getInstance();
						int dow = now.get(Calendar.DAY_OF_WEEK);
						now.add(Calendar.DATE, -dow);
						if (lm < now.getTimeInMillis())
							refresh = true;

					}
					if (refresh) {
						n = retrieve(new URL(
								res.getStringArray(R.array.week_url)[i]));
						save(res.getStringArray(R.array.week_file)[i], n);
					}
				}
				notification(res.getString(R.string.succesfull_fetch));
				Intent i = new Intent(this, FGDroidActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
//				startActivity(i);
				// sendBroadcast(i);
			} else {
				Log.w(LOG_TAG, "Not connected: fetch skipped.");
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Retrieving of FG menu failed.", e);
		} finally {
			deleteFile(LOCK_FILE_NAME);
		}
	}

	private void save(String filename, Node n) throws FileNotFoundException,
			TransformerException, TransformerConfigurationException,
			TransformerFactoryConfigurationError, IOException {
		try {
			openFileOutput(LOCK_FILE_NAME, Context.MODE_PRIVATE);
			FileOutputStream fos = openFileOutput(filename,
					Context.MODE_PRIVATE);
			TransformerFactory.newInstance().newTransformer()
					.transform(new DOMSource(n), new StreamResult(fos));
			fos.close();
		} finally {
			deleteFile(LOCK_FILE_NAME);
		}
	}

	private void notification(String text) {
		String ns = Context.NOTIFICATION_SERVICE;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
		int icon = R.drawable.hpa;
		CharSequence tickerText = "Hello";
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);
		notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL
				| Notification.FLAG_ONGOING_EVENT;
		Context context = getApplicationContext();
		CharSequence contentTitle = text;
		CharSequence contentText = text;
		Intent notificationIntent = new Intent(getBaseContext(),
				FGDroidActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		mNotificationManager.notify(HELLO_ID, notification);
	}

	public Node retrieve(URL url) throws IOException, TransformerException,
			ParserConfigurationException {
		HttpURLConnection con = download(url);
		Document d = tidy(con.getInputStream(), con.getContentEncoding());
		DOMResult res = transform(d);
		return res.getNode();
	}

	private DOMResult transform(Document d) throws IOException,
			TransformerConfigurationException,
			TransformerFactoryConfigurationError, ParserConfigurationException,
			TransformerException {
		InputStream inXsl = getTemplate();
		Transformer tr = TransformerFactory.newInstance().newTransformer(
				new StreamSource(inXsl));
		DOMResult res = new DOMResult(DocumentBuilderFactory.newInstance()
				.newDocumentBuilder().newDocument());
		tr.transform(new DOMSource(d), res);
		return res;
	}

	private InputStream getTemplate() throws IOException {
		AssetManager assets = getAssets();
		InputStream inXsl = assets.open("fg.xsl");
		return inXsl;
	}

	private HttpURLConnection download(URL url) throws IOException {
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		Log.d(LOG_TAG, "response: " + con.getResponseCode());
		int len = con.getContentLength();
		Log.d(LOG_TAG, "Len: " + len);
		return con;
	}

	private Document tidy(InputStream is, String enc) throws IOException {
		Tidy t = new Tidy();
		t.setInputEncoding(enc == null ? "cp1250" : enc);
		t.setNumEntities(true);
		t.setXmlOut(true);
		t.setShowWarnings(false);
		t.setTrimEmptyElements(true);
		// t.setQuoteNbsp(true);
		Document d = t.parseDOM(is, null);
		is.close();
		return d;
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
