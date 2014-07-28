package com.company.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class PropertiesUtil {
	private static ConcurrentHashMap<String, Properties> container = new ConcurrentHashMap();

	public static String getProperties(String key) {
		for (Iterator iterator = container.entrySet().iterator(); iterator.hasNext(); ) {
			Map.Entry entry = (Map.Entry) iterator.next();
			if (((Properties) entry.getValue()).containsKey(key)) {
				return ((Properties) entry.getValue()).getProperty(key);
			}
		}
		return "";
	}

	static {
		URL url = PropertiesUtil.class.getProtectionDomain().getCodeSource().getLocation();
		if (url != null) {
			String path = null;
			try {
				String pathS = url.getPath();
				String[] array = pathS.split("/");
				path = URLDecoder.decode(pathS.substring(0, pathS.length() - array[(array.length - 1)].length() - 1),
						"utf-8");
				if (!path.endsWith("/"))
					path = path.concat("/");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			File file = new File(path);
			String[] files = file.list();
			for (int i = 0; i < files.length; i++) {
				String fileName = files[i];
				if (fileName.endsWith(".properties")) {
					Properties properties = new Properties();
					try {
						String name = path + fileName;
						properties.load(new FileInputStream(name));
					} catch (IOException e) {
						e.printStackTrace();
					}
					container.put(fileName, properties);
				}
			}
		}
	}
}