package com.example.localchat;

import java.util.HashMap;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bean.test.Person;
import com.util.test.Constant;

public class MainActivity extends ActionBarActivity {
	int test;

	MainService mainService;
	ListView listView;
	HashMap<Integer, Person> personMap = null;
	List<HashMap<Integer, Person>> personList = null;
	List<Integer> personIdList = null;
	List<Person> myPersonList = null;

	ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub

			mainService = ((MainService.MyBinder) service).getService();

		}
	};
	private UserItemAdapter itemAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listView = (ListView) findViewById(R.id.myList);
		mainService = new MainService();
		Intent service = new Intent(MainActivity.this, MainService.class);
		bindService(service, connection, Service.BIND_AUTO_CREATE);
		startService(service);
		registerBroadcast();

	}

	private void registerBroadcast() {
		// TODO Auto-generated method stub

		MyBroadCastReceiver broadCastReceiver = new MyBroadCastReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constant.register_UpdateUserList);

		registerReceiver(broadCastReceiver, filter);
	}

	class MyBroadCastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String act = intent.getAction();
			if (act.equals(Constant.register_UpdateUserList)) {
				myPersonList = mainService.getMyPersonList();
				itemAdapter = new UserItemAdapter();
				listView.setAdapter(itemAdapter);
			}

		}
	}

	class UserItemAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return myPersonList.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return myPersonList.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null)
				convertView = LayoutInflater.from(getApplicationContext())
						.inflate(R.layout.useritem, null);

			TextView textView = (TextView) convertView
					.findViewById(R.id.userName);
			textView.setText(myPersonList.get(position).getUserName());

			return convertView;
		}
	}
}
