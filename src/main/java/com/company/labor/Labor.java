package com.company.labor;

import com.company.util.HttpConnect;
import com.company.util.PropertiesUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Labor {
	private static final int threadCount = Integer.parseInt(PropertiesUtil.getProperties("threadCount"));
	private static final int expectedIPCount = Integer.parseInt(PropertiesUtil.getProperties("expectedIPCount"));

	private static final StringBuffer sb = new StringBuffer();
	private static ExecutorService executorService = Executors.newFixedThreadPool(threadCount, new ThreadFactory() {
		public Thread newThread(Runnable r) {
			return new Thread(r, "LaborThreadFactory");
		}
	});

	private static LinkedBlockingQueue<String> ipQ = new LinkedBlockingQueue(300);
	private static HashSet<String> qedIp = new HashSet();

	public static void main(String[] args) throws IOException, InterruptedException {
		final AtomicInteger count = new AtomicInteger(0);

		new Thread(new Runnable() {
			public void run() {
				putIpsQ();
			}
		}).start();

		System.out.println("threadCount is :" + threadCount);
		System.out.println("you are querying for " + expectedIPCount + " ips.");
		System.out.println("you can terminate it by Ctrl+C anytime,the result is in your clipboard,just paste them!");

		final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		for (int i = 0; i < threadCount; i++)
			executorService.submit(new Runnable() {
				public void run() {
					try {
						while (true) {
							String ip = (String) Labor.ipQ.poll(3L, TimeUnit.SECONDS);
							if (ip == null) {
								System.out.println("all done.");
								System.exit(0);
							}
							HttpGet httpReq = new HttpGet("https://" + ip + "/?" + UUID.randomUUID().toString());
							HttpResponse response;
							try {
								response = HttpConnect.execute(httpReq);
							} catch (IOException e) {
								continue;
							}

							if (response.getStatusLine().getStatusCode() == 200) {
								String serverName = response.getLastHeader("Server").getValue();
								if (serverName.equals("gws")) {
									System.out.print(ip + "|");
									Labor.sb.append(ip);
									Labor.sb.append("|");
									clipboard.setContents(new StringSelection(Labor.sb.toString()), null);
									if (count.addAndGet(1) == Labor.expectedIPCount) {
										System.out.println();
										System.out.println(Labor.expectedIPCount + " ips found," +
												"that's enough for your use :)");
										System.out.println("shutting down...");
										Labor.sb.deleteCharAt(Labor.sb.length() - 1);
										clipboard.setContents(new StringSelection(Labor.sb.toString()), null);
										Toolkit.getDefaultToolkit().beep();
										System.exit(0);
									}
								}
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
	}

	private static void putIpsQ() {
		Random r = new Random(System.currentTimeMillis());
		while (true) {
			String ipRange = com.company.constant.Constant.IPTABLE[r.nextInt(com.company.constant.Constant
					.IPTABLELENGTH)];
			String[] array = ipRange.split("\\.");
			String prefix = array[0] + "." + array[1] + "." + array[2];
			String[] range = array[3].split("-");
			int value;
			if (range.length != 1) {
				int min = Integer.parseInt(range[0]);
				int max = 0;
				try {
					max = Integer.parseInt(range[1]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				value = min + r.nextInt(max - min + 1);
			} else {
				value = Integer.parseInt(range[0]);
			}
			try {
				String ip = prefix + "." + value;
				if (!qedIp.contains(ip)) {
					ipQ.put(ip);
					qedIp.add(ip);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}