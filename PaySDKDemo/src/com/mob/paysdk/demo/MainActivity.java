package com.mob.paysdk.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.mob.paysdk.AliPayAPI;
import com.mob.paysdk.MobPayAPI;
import com.mob.paysdk.OnPayListener;
import com.mob.paysdk.Order;
import com.mob.paysdk.PayOrder;
import com.mob.paysdk.PayResult;
import com.mob.paysdk.PaySDK;
import com.mob.paysdk.WXPayAPI;
import com.mob.paysdk.demo.util.DialogUtils;
import com.mob.paysdk.demo.util.OrderUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);
		initView();
	}

	private void initView() {
		initCheckBox();

		// 初始化金额为0元
		updateMoney(0);

		EditText et = (EditText) findViewById(R.id.ed_money);
		et.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				updateMoney(0);
			}
		});

		View.OnClickListener clickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switch (v.getId()) {
					case R.id.vg_alipay: {
						RadioButton rb = (RadioButton) findViewById(R.id.rb_paymethod_ali);
						rb.setChecked(true);
						rb = (RadioButton) findViewById(R.id.rb_paymethod_wx);
						rb.setChecked(false);
					} break;
					case R.id.vg_wxpay: {
						RadioButton rb = (RadioButton) findViewById(R.id.rb_paymethod_ali);
						rb.setChecked(false);
						rb = (RadioButton) findViewById(R.id.rb_paymethod_wx);
						rb.setChecked(true);
					} break;
					default: {
					} break;
				}
			}
		};
		ViewGroup vg = (ViewGroup) findViewById(R.id.vg_wxpay);
		vg.setOnClickListener(clickListener);
		vg = (ViewGroup) findViewById(R.id.vg_alipay);
		vg.setOnClickListener(clickListener);
		vg.performClick();

		View v = findViewById(R.id.btn_topay);
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				pay();
			}
		});

		v = findViewById(R.id.tv_order_list);
		v.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, OrderListActivity.class);
				startActivity(intent);
			}
		});
	}

	private void initCheckBox() {
		final int[] ids = new int[] {R.id.rb_money_1, R.id.rb_money_2, R.id.rb_money_3};
		View.OnClickListener l = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				CheckBox ctv = (CheckBox) v;
				boolean check = ctv.isChecked();
				for (int i = 0; check && i < ids.length; i++) {
					if (v.getId() != ids[i]) {
						ctv = (CheckBox)findViewById(ids[i]);
						ctv.setChecked(false);
					}
				}
				if (check) {
					String text = ((CheckBox)v).getText().toString();
					text = text.substring(1);
					int yuan = Integer.parseInt(text);
					updateMoney(yuan);
				} else {
					updateMoney(0);
				}
			}
		};

		for (int i = 0; i < ids.length; i++) {
			View v = findViewById(ids[i]);
			v.setOnClickListener(l);
		}
	}

	private int getPayYuan() {
		TextView tv = (TextView) findViewById(R.id.tv_pay_total);
		String text = tv.getText().toString();
		text = text.substring(4, text.length() - 1);
		float fyuan = Float.parseFloat(text);
		int yuan = (int)(fyuan * 100);
		return yuan;
	}

	/**
	 * 0, 支付宝; 1, 微信支付
	 * @return
	 */
	private int getPayChannel() {
		RadioButton rb = (RadioButton) findViewById(R.id.rb_paymethod_ali);
		if (rb.isChecked()) {
			return 0;
		}
		return 1;
	}

	/**
	 * 发起Mob支付功能
	 */
	private void pay() {
		int yuan = getPayYuan();
		if (yuan <= 0) {
			Toast.makeText(MainActivity.this, R.string.main_topay_fail, Toast.LENGTH_SHORT).show();
			return;
		}
		final PayOrder order = new PayOrder();
		order.setOrderNo(getOutTradeNo());
		order.setAmount(getPayYuan());
		order.setSubject(getString(R.string.main_order_subject));
		order.setBody(getString(R.string.main_order_body));

		int payChannel = getPayChannel();
		final MobPayAPI payApi;
		if (0 == payChannel) {
			payApi = PaySDK.createMobPayAPI(AliPayAPI.class);
		} else if (1 == payChannel) {
			payApi = PaySDK.createMobPayAPI(WXPayAPI.class);
		} else {
			payApi = null;
		}

		DialogUtils.showLoading(this);
		payApi.pay(order, new OnPayListener<PayOrder>() {
			@Override
			public boolean onWillPay(String ticketId, PayOrder order, MobPayAPI api) {
				// 保存ticketId
				return false;
			}

			@Override
			public void onPayEnd(PayResult payResult, PayOrder order, MobPayAPI api) {
				MainActivity.this.onPayEnd(order, payResult);
			}
		});
	}

	/**
	 * 要求外部订单号必须唯一。
	 * @return 订单号
	 */
	public static String getOutTradeNo() {
		SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss", Locale.getDefault());
		Date date = new Date();
		String key = format.format(date);
		Random r = new Random();
		key = key + r.nextInt();
		key = key.substring(0, 15);
		return key;
	}

	private void onPayEnd(Order order, PayResult result) {
		if (PayResult.PAYRESULT_OK == result) {
			Toast.makeText(MainActivity.this, R.string.main_pay_success, Toast.LENGTH_SHORT).show();
		} else if (PayResult.PAYRESULT_CANCEL == result) {
			Toast.makeText(MainActivity.this, R.string.main_pay_cancel, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(MainActivity.this, R.string.main_pay_fail, Toast.LENGTH_SHORT).show();
		}

		OrderRecord or = new OrderRecord();
		or.order = (PayOrder)order;
		or.result = result;
		or.payAt = System.currentTimeMillis();

		OrderUtil.addOrder(this, or);
		DialogUtils.hideLoading(this);

		if (PayResult.PAYRESULT_OK == result) {
			DialogUtils.showPayResult(this, true);
		} else {
			DialogUtils.showPayResult(this, false);
		}
	}

	private void updateMoney(int yuan) {
		float fyuan = yuan;
		if (0 == yuan) {
			EditText et = (EditText) findViewById(R.id.ed_money);
			String text = et.getText().toString();
			try {
				fyuan = Float.parseFloat(text);
			} catch (NumberFormatException ex) {
				ex.printStackTrace();
			}
		}
		yuan = (int)(fyuan * 100);
		if (0 != yuan % 100) {
			String text = getString(R.string.main_pay_total1, fyuan);
			TextView tv = (TextView) findViewById(R.id.tv_pay_total);
			tv.setText(Html.fromHtml(text));
		} else {
			String text = getString(R.string.main_pay_total, fyuan);
			TextView tv = (TextView) findViewById(R.id.tv_pay_total);
			tv.setText(Html.fromHtml(text));
		}
	}
}
