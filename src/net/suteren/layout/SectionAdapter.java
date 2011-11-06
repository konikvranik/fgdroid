package net.suteren.layout;

import java.util.HashMap;
import java.util.Map;

import net.suteren.R;
import net.suteren.domain.DayMenu;
import net.suteren.domain.Food;
import net.suteren.domain.Live;
import net.suteren.domain.Pasta;
import net.suteren.domain.Soup;
import net.suteren.domain.Superior;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SectionAdapter extends BaseExpandableListAdapter {

	Map<String, Food[]> tree = new HashMap<String, Food[]>();
	private String[] groups;
	private Context context;
	private SharedPreferences prefs;

	public SectionAdapter(Context context, String[] groups, DayMenu item) {
		this.groups = groups;
		this.context = context;
		tree.put(groups[0], item.getSoups().toArray(new Soup[0]));
		tree.put(groups[1], item.getFood().toArray(new Food[0]));
		tree.put(groups[2], item.getLive().toArray(new Live[0]));
		tree.put(groups[3], item.getSuperior().toArray(new Superior[0]));
		tree.put(groups[4], item.getPasta().toArray(new Pasta[0]));

		prefs = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

	}

	public Food getChild(int groupPosition, int childPosition) {
		return tree.get(groups[groupPosition])[childPosition];
	}

	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		LinearLayout ll = (LinearLayout) LinearLayout.inflate(context,
				R.layout.food, null);
		TextView tv = (TextView) ll.findViewById(R.id.text);
		Food food = getChild(groupPosition, childPosition);
		tv.setText(food.getText());
		View ribon = ll.findViewById(R.id.ribon);
		Resources res = context.getResources();
		if ("soup".equals(food.getType())) {
			ribon.setBackgroundColor(prefs.getInt("soup",
					res.getColor(R.color.soup)));
		} else if ("food".equals(food.getType())) {
			if (childPosition == 3)
				ribon.setBackgroundColor(prefs.getInt("vegetarian",
						res.getColor(R.color.vegetarian)));
			else if (childPosition == 4)
				ribon.setBackgroundColor(prefs.getInt("cold",
						res.getColor(R.color.cold)));
			else
				ribon.setBackgroundColor(prefs.getInt("food",
						res.getColor(R.color.food)));
		} else if ("superior".equals(food.getType())) {
			ribon.setBackgroundColor(prefs.getInt("superior",
					res.getColor(R.color.superior)));
		} else if ("live".equals(food.getType())) {
			ribon.setBackgroundColor(prefs.getInt("live",
					res.getColor(R.color.live)));
		} else if ("pasta".equals(food.getType())) {
			ribon.setBackgroundColor(prefs.getInt("pasta",
					res.getColor(R.color.pasta)));
		}
		return ll;
	}

	public int getChildrenCount(int groupPosition) {
		return tree.get(groups[groupPosition]).length;
	}

	public String getGroup(int groupPosition) {
		return groups[groupPosition];
	}

	public int getGroupCount() {
		return groups.length;
	}

	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		LinearLayout ll = (LinearLayout) LinearLayout.inflate(context,
				R.layout.section, null);
		TextView tv = (TextView) ll.findViewById(R.id.sectioLabel);
		tv.setText(getGroup(groupPosition));
		return ll;
	}

	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}

}
