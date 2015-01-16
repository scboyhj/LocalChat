package com.util.test;

import java.util.Random;

public class Constant {

	public final static int BUFFER_SIZE = 256;
	public final static byte[] AND = "AND".getBytes();

	public final static int CMD_REGIST = 80;
	public final static int CMD_MESSAGE = 81;

	public final static int MSG_TYPE_CALL = 1;
	public final static int MSG_TYPE_RSP = 2;
	public final static int MSG_TYPE_CALLEL = 3;

	public final static int COMMAND_REQUEST = 1;
	public final static int COMMAND_AGREEN = 2;
	public final static int COMMAND_REFUSE = 3;
	public final static int COMMAND_BUSYTIME = 4;
	public final static int COMMAND_FILENAME = 5;

	public final static String MUL_IP = "239.9.9.1";
	public final static int MUL_PORT = 6688;

	public final static String PreName = "chatfile";
	public final static String USERNAME = "username";
	public final static String USERID = "userid";
	public final static String USERIP = "userip";

	public final static String register_UpdateUserList = "com.localchat.updateuserlist";

	public final static int getMyId() {
		int i = (int) (Math.random() * 1000000);
		return i;
	}

	public static String intToIp(int i) {
		String ip = ((i >> 24) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
				+ ((i >> 8) & 0xFF) + "." + (i & 0xFF);

		return ip;
	}

}
