package net.suteren;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DayMenu {

	List<Soup> soups = new ArrayList<Soup>();

	List<Food> food = new ArrayList<Food>();

	List<Superior> superior = new ArrayList<Superior>();

	List<Live> live = new ArrayList<Live>();

	List<Pasta> pasta = new ArrayList<Pasta>();

	Calendar date;

	public DayMenu(Calendar date) {
		this.date = date;
	}

	public List<Soup> getSoups() {
		return soups;
	}

	public void setSoups(List<Soup> soups) {
		this.soups = soups;
	}

	public List<Food> getFood() {
		return food;
	}

	public void setFood(List<Food> food) {
		this.food = food;
	}

	public List<Superior> getSuperior() {
		return superior;
	}

	public void setSuperior(List<Superior> superior) {
		this.superior = superior;
	}

	public List<Live> getLive() {
		return live;
	}

	public void setLive(List<Live> live) {
		this.live = live;
	}

	public List<Pasta> getPasta() {
		return pasta;
	}

	public void setPasta(List<Pasta> pasta) {
		this.pasta = pasta;
	}

	public void addSoup(Soup soup) {
		soups.add(soup);
	}

	public void addFood(Food food) {
		this.food.add(food);
	}

	public void addLive(Live live) {
		this.live.add(live);
	}

	public void addSuperior(Superior superior) {
		this.superior.add(superior);
	}

	public void addPasta(Pasta pasta) {
		this.pasta.add(pasta);
	}

	public Calendar getDate() {
		return date;
	}

	public void setDate(Calendar date) {
		this.date = date;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Day menu for ");
		if (getDate() == null)
			sb.append("No Date");
		else
			sb.append(new SimpleDateFormat("d.M.y").format(getDate().getTime()));
		return sb.toString();
	}
}
