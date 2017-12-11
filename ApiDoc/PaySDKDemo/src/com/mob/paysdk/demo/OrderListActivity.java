package com.mob.paysdk.demo;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.mob.paysdk.PayOrder;
import com.mob.paysdk.PayResult;
import com.mob.paysdk.demo.util.OrderUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/** Activity的基类, 做一些公共的处理. */
public class OrderListActivity extends Activity {

	private List<OrderRecord> orderList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.orderlist_activity);
		orderList = OrderUtil.getOrder(this);
		initView();
	}


	private void initView() {
		ListView lv = (ListView) findViewById(android.R.id.list);
		lv.setAdapter(new BaseAdapter() {
			@Override
			public int getCount() {
				return null != orderList ? orderList.size() : 0;
			}

			@Override
			public Object getItem(int position) {
				return orderList.get(position);
			}

			@Override
			public long getItemId(int position) {
				return position;
			}

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				if (null == convertView) {
					convertView = getLayoutInflater().inflate(R.layout.orderlist_listitem, parent, false);
				}
				OrderRecord item = (OrderRecord)getItem(position);
				bindView(convertView, item);
				return convertView;
			}
		});

		View v = findViewById(R.id.iv_back);
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}

	private void bindView(View convertView, OrderRecord item) {
		PayOrder order = item.order;
		TextView tv = (TextView) convertView.findViewById(R.id.tv_order_no);
		String text = null != order ? order.getOrderNo() : "";
		text = getString(R.string.orderlist_no, text);
		tv.setText(text);

		tv = (TextView) convertView.findViewById(R.id.tv_order_body);
		text = formatAmount(null != order ? order.getAmount() : 0 );
		text = getString(R.string.orderlist_amout, text);
		tv.setText(Html.fromHtml(text));

		tv = (TextView) convertView.findViewById(R.id.tv_order_time);
		tv.setText(formatTime(item.payAt));

		tv = (TextView) convertView.findViewById(R.id.tv_order_status);
		tv.setText(formatPayResult(item.result));

	}

	private String formatTime(long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return sdf.format(new Date(time));
	}

	private String formatAmount(int amout) {
		float a = amout;
		a /= 100;
		if (0 == amout % 100) {
			return String.format("%d", (int)a);
		} else {
			return String.format("%.2f", a);
		}
	}

	private String formatPayResult(PayResult result) {
		if (PayResult.PAYRESULT_OK == result) {
			return getString(R.string.main_pay_success);
		} else if (PayResult.PAYRESULT_CANCEL == result) {
			return getString(R.string.main_pay_cancel);
		} else if (PayResult.PAYRESULT_INVALID_CHANNEL == result) {
			return getString(R.string.main_pay_invalid_channel);
		} else {
			return getString(R.string.main_pay_fail);
		}
	}
}
