package com.example.localchat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import com.bean.test.Person;
import com.util.test.ByteAndInt;
import com.util.test.Constant;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;

public class MainService extends Service {

	byte[] RegistBuffer = new byte[Constant.BUFFER_SIZE];
	byte[] MessageBuffer = new byte[Constant.BUFFER_SIZE];
	WifiManager wifiManager;
	MyBinder myBinder = new MyBinder();
	public InetAddress localInetAddress;
	public String localIp;
	public byte[] localIpBytes;
	Person me;
	ConnectBridge connectBridge;
	HashMap<Integer, Person> personMap = new HashMap<Integer, Person>();
	List<HashMap<Integer, Person>> personList = new ArrayList<HashMap<Integer, Person>>();
	List<Integer> personIdList = new ArrayList<Integer>();

	List<Person> myPersonList = new ArrayList<Person>();

	public List<Person> getMyPersonList() {
		return myPersonList;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
//		initCMD();
//		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//
//		new CheckWifiStates().start();
//
//		connectBridge = new ConnectBridge();
//		connectBridge.start();
//		getMyAboutInfo();
//		new UpdateMe().start();
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	@Deprecated
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		initCMD();
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		new CheckWifiStates().start();

		connectBridge = new ConnectBridge();
		connectBridge.start();
		getMyAboutInfo();
		new UpdateMe().start();
	}

	private void getMyAboutInfo() {
		// TODO Auto-generated method stub
		SharedPreferences preferences = getSharedPreferences(Constant.PreName,
				MODE_PRIVATE);
		String name = preferences.getString(Constant.USERNAME, "dudu");
		int id = preferences.getInt(Constant.USERID, Constant.getMyId());

		me = new Person();
		me.setUserId(id);
		me.setUserName(name);
		System.arraycopy(ByteAndInt.int2ByteArray(id), 0, RegistBuffer, 6, 4);
		System.arraycopy(name.getBytes(), 0, RegistBuffer, 14,
				name.getBytes().length);

	}

	boolean isUpdateMe = true;

	class UpdateMe extends Thread {
		@Override
		public void run() {
			while (isUpdateMe) {
				connectBridge.sendHeartPack();
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	class ConnectBridge extends Thread {

		MulticastSocket multicastSocket;
		byte[] ReceiveBuffer = new byte[Constant.BUFFER_SIZE];

		public ConnectBridge() {
		}

		public void sendHeartPack() {
			// TODO Auto-generated method stub
			RegistBuffer[4] = 1;
			try {
				DatagramPacket datagramPacket = new DatagramPacket(
						RegistBuffer, 0, RegistBuffer.length,
						InetAddress.getByName(Constant.MUL_IP),
						Constant.MUL_PORT);
				multicastSocket.send(datagramPacket);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		private void parsePkg(byte[] receiveBuffer) {
			// TODO Auto-generated method stub
			byte CMD = receiveBuffer[3];
			byte CMD_TYPE = receiveBuffer[4];
			byte CMD_ACT = receiveBuffer[5];

			byte[] IdBs = new byte[4];
			System.arraycopy(receiveBuffer, 6, IdBs, 0, 4);

			int recId = ByteAndInt.byteArray2Int(IdBs);
			switch (CMD) {
			case Constant.CMD_REGIST:

				switch (CMD_TYPE) {
				case Constant.MSG_TYPE_CALL:
					if (recId != me.getUserId()) {
						upDateUserList(recId, receiveBuffer);

						byte[] ipBs = new byte[4];
						System.arraycopy(receiveBuffer, 44, ipBs, 0, 4);

						try {
							InetAddress address = InetAddress
									.getByAddress(ipBs);

							RegistBuffer[4] = Constant.MSG_TYPE_RSP;
							DatagramPacket datagramPacket = new DatagramPacket(
									RegistBuffer, 0, RegistBuffer.length,
									address, Constant.MUL_PORT);
							multicastSocket.send(datagramPacket);

						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
					break;
				case Constant.MSG_TYPE_RSP:
					upDateUserList(recId, receiveBuffer);
				case Constant.MSG_TYPE_CALLEL:
					break;

				default:
					break;
				}

				break;

			default:
				break;
			}

		}

		private void upDateUserList(int recId, byte[] receive) {
			// TODO Auto-generated method stub
			Person person = new Person();
			parsePerson(person, receive);
			personMap.put(recId, person);

			if (!personIdList.contains(recId))
				personIdList.add(recId);
			if (!personList.contains(recId))
				personList.add(personMap);
			if (!myPersonList.contains(person))
				myPersonList.add(person);

			sendBroadcast(new Intent(Constant.register_UpdateUserList));
		}

		private void parsePerson(Person person, byte[] receive) {
			// TODO Auto-generated method stub

			byte name[] = new byte[30];
			byte id[] = new byte[4];
			byte ip[] = new byte[4];

			System.arraycopy(receive, 14, name, 0, 30);
			System.arraycopy(receive, 6, id, 0, 4);
			System.arraycopy(receive, 44, ip, 0, 4);

			String uName = new String(name);
			int uId = ByteAndInt.byteArray2Int(id);
			String uIp = Constant.intToIp(ByteAndInt.byteArray2Int(ip));
			person.setUserId(uId);
			person.setUserIp(uIp);
			person.setUserName(uName);

		}

		@Override
		public void run() {
			// TODO Auto-generated constructor stub

			try {
				multicastSocket = new MulticastSocket(Constant.MUL_PORT);
				multicastSocket.joinGroup(InetAddress
						.getByName(Constant.MUL_IP));
				while (!multicastSocket.isClosed()) {
					for (int i = 0; i < Constant.BUFFER_SIZE; i++)
						ReceiveBuffer[i] = 0;
					DatagramPacket datagramPacket = new DatagramPacket(
							ReceiveBuffer, 0, Constant.BUFFER_SIZE);
					multicastSocket.receive(datagramPacket);
					parsePkg(ReceiveBuffer);

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	class CheckWifiStates extends Thread {
		@Override
		public void run() {
			try {
				if (!wifiManager.isWifiEnabled()) {
					wifiManager.setWifiEnabled(true);
				}

				for (Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							if (inetAddress.isReachable(1000)) {
								localInetAddress = inetAddress;
								localIp = inetAddress.getHostAddress()
										.toString();
								localIpBytes = inetAddress.getAddress();
								System.arraycopy(localIpBytes, 0, RegistBuffer,
										44, 4);

							}
						}
					}
				}

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private void initCMD() {
		// TODO Auto-generated method stub
		for (int i = 0; i < RegistBuffer.length; i++)
			RegistBuffer[i] = 0;
		System.arraycopy(Constant.AND, 0, RegistBuffer, 0, 3);
		RegistBuffer[3] = Constant.CMD_REGIST;
		RegistBuffer[4] = Constant.MSG_TYPE_CALL;
		RegistBuffer[5] = Constant.COMMAND_AGREEN;
		for (int i = 0; i < MessageBuffer.length; i++)
			MessageBuffer[i] = 0;
		System.arraycopy(Constant.AND, 0, MessageBuffer, 0, 3);
		MessageBuffer[3] = Constant.CMD_MESSAGE;
		MessageBuffer[4] = Constant.MSG_TYPE_CALL;
		MessageBuffer[5] = Constant.COMMAND_AGREEN;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return myBinder;
	}

	class MyBinder extends Binder {
		public MainService getService() {
			return MainService.this;
		}
	}

	public HashMap<Integer, Person> getPersonMap() {
		return personMap;
	}

	public List<HashMap<Integer, Person>> getPersonList() {
		return personList;
	}

	public List<Integer> getPersonIdList() {
		return personIdList;
	}

}
