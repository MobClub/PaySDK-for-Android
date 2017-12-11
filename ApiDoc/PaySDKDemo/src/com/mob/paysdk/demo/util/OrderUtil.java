package com.mob.paysdk.demo.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.mob.paysdk.demo.OrderRecord;
import com.mob.tools.utils.Data;
import com.mob.tools.utils.Hashon;
import com.mob.tools.utils.SharePrefrenceHelper;

import java.util.ArrayList;
import java.util.List;

public class OrderUtil extends Object {

	private static final String ORDER_FILE = "order_file";
	private static final String KEY_ORDER_DATA = "order_data";

	public static class SaveData {
		ArrayList<OrderRecord> orderRecords;
	}

	public static List<OrderRecord> getOrder(Context context) {
		SharePrefrenceHelper sp = new SharePrefrenceHelper(context);
		sp.open(ORDER_FILE);
		String json = sp.getString(KEY_ORDER_DATA);
		Hashon hashon = new Hashon();
		SaveData saveData = hashon.fromJson(json, SaveData.class);
		return null != saveData ? saveData.orderRecords : null;
	}

	public static void addOrder(Context context, OrderRecord or) {
		ArrayList<OrderRecord> list = (ArrayList<OrderRecord>)getOrder(context);
		if (null == list) {
			list = new ArrayList<OrderRecord>();
		}
		list.add(0, or);
		save(context, list);
	}

	private static void save(Context context, ArrayList<OrderRecord> list) {
		Hashon hashon = new Hashon();
		SaveData sd = new SaveData();
		sd.orderRecords = list;
		String text = hashon.fromObject(sd);
		SharePrefrenceHelper sp = new SharePrefrenceHelper(context);
		sp.open(ORDER_FILE);
		sp.putString(KEY_ORDER_DATA, text);
	}
}
