package com.z2daddy.uip;

import java.io.FileNotFoundException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UIPNettyClientSimulator {
	public static int touchId = 0;
	
	public static final Logger logger = LoggerFactory.getLogger(UIPNettyClientSimulator.class);
	
	public static void main(String[] args) throws InterruptedException, FileNotFoundException, DocumentException {
		ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
		int sleepInterval = Integer.parseInt(args[0]);
		for (;;) {
			cachedThreadPool.execute(new UIPNettyClientSimulatorTask(touchId));
			Thread.sleep(sleepInterval);
			touchId ++;
		}
	}


}

