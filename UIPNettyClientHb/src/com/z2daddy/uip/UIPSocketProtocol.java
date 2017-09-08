package com.z2daddy.uip;

public class UIPSocketProtocol {
	public static final int RESP_HEAD_LEN = 7;
	public static final int READER_BUFFER_LENGTH = 5120;
	public static final int UIP_HEADLEN = 174;
	public static final int TIMEOUT = 6;
	public static final byte PROTOCOL_ENDING = 26;
	public static final int PACKLEN_START_INDEX = 2;
	public static final int SEQ_START_INDEX = 144;
	public static final int NO_START_INDEX = 80;
	public static final int BIZCODE_START_INDEX = 8;
	public static final int TRANSCODE_START_INDEX = 15;

	public static final int VERSION_LEN = 2;
	public static final int REQPAKG_LEN = 5;
	public static final int BIZCODE_LEN = 7;
	public static final int TRANSCODE_LEN = 7;
	public static final int PROVINCE_CODE_LEN = 4;
	public static final int INMODE_CODE_LEN = 1;
	public static final int TRADE_EPARCHY_CODE_LEN = 4;
	public static final int TRADE_CITY_CODE_LEN = 4;
	public static final int TRADE_DEPARTID_LEN = 5;
	public static final int TRADE_STAFFID_LEN = 8;
	public static final int TRADE_DEPARTPWD_LEN = 32;
	public static final int SERIAL_NUMBER_LEN = 15;
	public static final int TERMINALID_LEN = 32;
	public static final int CURRPACK_LEN = 4;
	public static final int SEQ_LEN = 30;
	public static final String NIL = "";

	public static final String VERSION = "10";
	public static final String PROVINCE_CODE = "SHXI";
	public static final String IN_MODE_CODE = "1";
	public static final String TRADE_EPARCHY_CODE = "0919";
	public static final String TRADE_CITY_CODE = "INTF";
	public static final String TRADE_DEPART_ID = "00302";
	public static final String TRADE_STAFF_ID = "ITFCC000";
	public static final String TRADE_DEPART_PASSWD = "000000";
	public static final String TERMINALID = "10.131.67.234";

	public static final String RECORD_SEPRATOR = "~";

	public int requestDatalength;
	public String phoneNo;
	public String requestDataSerialId;
	public String uipTranCode;
	public String uipBizCode;
	public String currentDataIndex;
	
	public static String protocolHead;

	private UIPSocketProtocol() {

	}

	private static volatile UIPSocketProtocol instance;

	public static UIPSocketProtocol getIstance() {
		if (instance == null) {
			synchronized (UIPSocketProtocol.class) {
				if (instance == null) {
					instance = new UIPSocketProtocol();
					getSockHead();
				}
			}
		}
		return instance;
	}
	
	public static void getSockHead(){
		protocolHead = rpad(VERSION, VERSION_LEN);
		protocolHead += rpad(NIL, REQPAKG_LEN);
		protocolHead += "1";
		protocolHead += rpad(NIL, TRANSCODE_LEN);
		protocolHead += rpad(NIL, BIZCODE_LEN);
		protocolHead += rpad(PROVINCE_CODE, PROVINCE_CODE_LEN);
		protocolHead += rpad(IN_MODE_CODE, INMODE_CODE_LEN);
		protocolHead += rpad(TRADE_EPARCHY_CODE, TRADE_EPARCHY_CODE_LEN);
		protocolHead += rpad(TRADE_CITY_CODE, TRADE_CITY_CODE_LEN);
		protocolHead += rpad(TRADE_DEPART_ID, TRADE_DEPARTID_LEN);
		protocolHead += rpad(TRADE_STAFF_ID, TRADE_STAFFID_LEN);
		protocolHead += rpad(TRADE_DEPART_PASSWD, TRADE_DEPARTPWD_LEN);
		protocolHead += rpad(NIL, SERIAL_NUMBER_LEN);
		protocolHead += rpad(TERMINALID, TERMINALID_LEN);
		protocolHead += rpad("1", CURRPACK_LEN);
		protocolHead += "1   0        ";
		protocolHead += rpad(NIL, SEQ_LEN);
	}

	public String setSockHead() {
		StringBuilder sockHeadInstance = new StringBuilder(protocolHead);
		
		String packLen = String.valueOf(requestDatalength);
		sockHeadInstance.replace(PACKLEN_START_INDEX, PACKLEN_START_INDEX+packLen.length(), packLen);
		
		sockHeadInstance.replace(BIZCODE_START_INDEX, BIZCODE_START_INDEX+uipBizCode.length(), uipBizCode);
		sockHeadInstance.replace(TRANSCODE_START_INDEX, TRANSCODE_START_INDEX+uipTranCode.length(), uipTranCode);
		sockHeadInstance.replace(NO_START_INDEX, NO_START_INDEX+phoneNo.length(), phoneNo);
		sockHeadInstance.replace(SEQ_START_INDEX, SEQ_START_INDEX+requestDataSerialId.length(), requestDataSerialId);
		
		return sockHeadInstance.toString();
	}
	
	
	public int getRequestDatalength() {
		return requestDatalength;
	}

	public void setRequestDatalength(int requestDatalength) {
		this.requestDatalength = requestDatalength;
	}

	public String getPhoneNo() {
		return phoneNo;
	}

	public void setPhoneNo(String phoneNo) {
		this.phoneNo = phoneNo;
	}

	public String getRequestDataSerialId() {
		return requestDataSerialId;
	}

	public void setRequestDataSerialId(String requestDataSerialId) {
		this.requestDataSerialId = requestDataSerialId;
	}

	public String getUipTranCode() {
		return uipTranCode;
	}

	public void setUipTranCode(String uipTranCode) {
		this.uipTranCode = uipTranCode;
	}

	public String getUipBizCode() {
		return uipBizCode;
	}

	public void setUipBizCode(String uipBizCode) {
		this.uipBizCode = uipBizCode;
	}

	public String getCurrentDataIndex() {
		return currentDataIndex;
	}

	public void setCurrentDataIndex(String currentDataIndex) {
		this.currentDataIndex = currentDataIndex;
	}

	public static String rpad(String s, int len) {
		if (s.length() >= len)
			return s;
		StringBuilder sb = new StringBuilder(s);
		for (int i = 0; i < len - s.length(); i++) {
			sb.append(' ');
		}
		return sb.toString();
	}
}
