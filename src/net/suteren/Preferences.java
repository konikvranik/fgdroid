package net.suteren;

import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.widget.TimePicker;

public class Preferences extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFormat(PixelFormat.RGBA_8888);

		addPreferencesFromResource(R.xml.main_prefs);
		Log.d(getClass().getName(), "OK");

		PreferenceScreen ps = getPreferenceScreen();
		PreferenceCategory pc = (PreferenceCategory) ps.getPreference(0);

		int count = pc.getPreferenceCount();
		Log.d(getClass().getName(), "Pref count: " + count);
		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this.getApplicationContext());

		for (int i = 0; i < count; i++) {
			Preference p = pc.getPreference(i);
			Log.d(getClass().getName(), "Pref: " + p);
			p.setOnPreferenceClickListener(new OnPreferenceClickListener() {

				public boolean onPreferenceClick(Preference preference) {

					final String key = preference.getKey();

					Log.d(getClass().getName(), "Key: " + key);

					final ColorPickerDialog d = new ColorPickerDialog(
							Preferences.this, prefs.getInt(key, getResources()
									.getColor(getColorByFoodType(key))));
					d.setAlphaSliderVisible(false);

					d.setButton("Ok", new DialogInterface.OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							SharedPreferences.Editor editor = prefs.edit();
							editor.putInt(key, d.getColor());
							editor.commit();
						}
					});

					d.setButton2("Cancel",
							new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog,
										int which) {

								}
							});

					d.show();

					return true;

				}
			});
		}

		pc = (PreferenceCategory) ps.getPreference(1);
		count = pc.getPreferenceCount();
		Log.d(getClass().getName(), "Count 2: " + count);
		for (int i = 0; i < count; i++) {
			Preference p = pc.getPreference(i);
			if ("triggerHour".equals(p.getKey()))
				p.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						TimePickerDialog d = new TimePickerDialog(
								Preferences.this,
								0,
								new OnTimeSetListener() {
									public void onTimeSet(
											TimePicker timepicker, int i, int j) {
										Log.d(getClass().getName(), "h:m: " + i
												+ ":" + j);
										Editor e = prefs.edit();
										e.putInt("triggerHour", i);
										e.putInt("triggerMinute", j);
										e.commit();
									}
								}, prefs.getInt("triggerHour", getResources()
										.getInteger(R.integer.triggerHour)),
								prefs.getInt("triggerMinute", getResources()
										.getInteger(R.integer.triggerMinute)),
								true);
						d.show();
						return false;
					}
				});
		}

		pc = (PreferenceCategory) ps.getPreference(2);
		count = pc.getPreferenceCount();
		Log.d(getClass().getName(), "Count 2: " + count);
		for (int i = 0; i < count; i++) {
			Preference p = pc.getPreference(i);
			if ("reset".equals(p.getKey()))
				p.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						Editor e = prefs.edit();
						e.clear();
						e.commit();
						Log.d(getClass().getName(), "reset!");
						return true;
					}
				});
		}
	}

	private int getColorByFoodType(String key) {
		if ("soup".equals(key)) {
			return R.color.soup;
		} else if ("food".equals(key)) {
			return R.color.food;
		} else if ("live".equals(key)) {
			return R.color.live;
		} else if ("superior".equals(key)) {
			return R.color.superior;
		} else if ("pasta".equals(key)) {
			return R.color.pasta;
		} else if ("cild".equals(key)) {
			return R.color.cold;
		} else if ("vegetarian".equals(key)) {
			return R.color.vegetarian;
		}
		// TODO Auto-generated method stub
		return 0;
	}

}
