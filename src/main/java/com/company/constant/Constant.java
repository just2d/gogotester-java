package com.company.constant;

import com.company.util.PropertiesUtil;

public class Constant {
	public static final String[] IPTABLE = PropertiesUtil.getProperties("ipRanges").split(",");
	public static final int IPTABLELENGTH = IPTABLE.length;
}