package net.suteren;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

public class FGDroidActivity extends Activity {

	Resources res;

	ArrayList<DayMenu> days = new ArrayList<DayMenu>();

	private long modifiedTime = 0;

	private static final String LOG_TAG = "FGDroid";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(LOG_TAG, "Activity start");
		res = getResources();

		fetchData();

		load();

		redraw();
		// setContentView(R.layout.main);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(LOG_TAG, "Intent 1");
		super.onNewIntent(intent);
		Log.d(LOG_TAG, "Intent 2");
		load();
		redraw();
	}

	private void fetchData() {
		Log.d("FGDroid", "Starting service");
		Intent service = new Intent(this, FGDroidService.class);
		Log.d("FGDroid", "Service started");
		startService(service);
	}

	private void redraw() {
		Log.d(LOG_TAG, "Redraw");

		HomeFeatureLayout hfl = new HomeFeatureLayout(this);

		// DayMenu dayMenu = days.get(showedDay.getTimeInMillis());

		hfl.setFeatureItems(days);
		setContentView(hfl);
	}

	private boolean load() {
		Log.d(LOG_TAG, "Loading...");
		try {
			File fd = getFilesDir();
			File[] files = fd.listFiles();
			for (int i = 0; i < files.length; i++) {
				Log.d(LOG_TAG, "File " + i + ": " + files[i].getAbsolutePath());
			}
			files = fd.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					if (FGDroidService.LOCK_FILE_NAME.equals(name))
						return true;
					return false;
				}
			});

			if (files.length == 0) {
				Log.d(LOG_TAG, "Unlocked");
				files = fd.listFiles(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						if (FGDroidService.LOCK_FILE_NAME.equals(name))
							return false;
						return true;
					}
				});
				if (files.length == 0) {
					fetchData();
					return false;
				}
				Log.d(LOG_TAG, "Files: " + files.length);
				for (File f : files) {
					long lm = f.lastModified();
					if (lm > modifiedTime) {
						modifiedTime = lm;
						fetchData();
					}
				}
				String[] week_files = res.getStringArray(R.array.week_file);
				for (int i = 0; i < week_files.length; i++) {
					Log.d(LOG_TAG, "Week file " + i);
					try {
						FileInputStream s = openFileInput(week_files[0]);
						DocumentBuilder db = DocumentBuilderFactory
								.newInstance().newDocumentBuilder();
						Document n = db.parse(s);

						XPath xp = XPathFactory.newInstance().newXPath();
						XPathExpression xpDate = xp.compile("/menu/@time");
						String timeString = xpDate.evaluate(n);

						String[] dates = timeString.split("\\s*-\\s*");

						DateFormat df = new SimpleDateFormat("d.M.y");
						Date from = df.parse(dates[0]);
						XPathExpression xpDay = xp.compile("/menu/day");
						NodeList dayNodes = (NodeList) xpDay.evaluate(n,
								XPathConstants.NODESET);

						for (int j = 0; j < dayNodes.getLength(); j++) {
							Node dayNode = dayNodes.item(j);

							NamedNodeMap attrs = dayNode.getAttributes();
							String position = attrs.getNamedItem("position")
									.getNodeValue();

							Log.d(LOG_TAG, "DayNode " + position);
							Calendar cal = Calendar.getInstance();
							cal.setTime(from);
							cal.set(Calendar.HOUR, 0);
							cal.set(Calendar.MINUTE, 0);
							cal.set(Calendar.SECOND, 0);
							cal.set(Calendar.MILLISECOND, 0);
							cal.add(Calendar.DATE,
									Integer.parseInt(position) - 1);
							DayMenu dm = new DayMenu(cal);

							NodeList foodNodes = dayNode.getChildNodes();
							for (int k = 0; k < foodNodes.getLength(); k++) {

								Node fn = foodNodes.item(k);
								attrs = fn.getAttributes();
								Node type = attrs.getNamedItem("type");
								String value = type.getNodeValue();

								if ("soup".equals(value)) {
									dm.addSoup(new Soup(fn.getTextContent()));
								} else if ("normal".equals(value)) {
									dm.addFood(new Food(fn.getTextContent()));
								} else if ("live".equals(value)) {
									dm.addLive(new Live(fn.getTextContent()));
								} else if ("superior".equals(value)) {
									dm.addSuperior(new Superior(fn
											.getTextContent()));
								} else if ("pasta".equals(value)) {
									dm.addPasta(new Pasta(fn.getTextContent()));
								}

							}
							Log.d(LOG_TAG, dm.getDate().getTimeInMillis()
									+ ": " + dm.toString());

							days.add(dm);
						}
					} catch (Exception e1) {
						Log.e(LOG_TAG, "Parsing data failed.", e1);
					}
				}
				return true;
			} else {
				Log.d(LOG_TAG, "Locked");
			}
		} catch (Exception e) {
			Log.d(LOG_TAG, "Fail.", e);
		}
		return false;
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