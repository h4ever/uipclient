package com.z2daddy.uip;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.z2daddy.uip.pojo.TSceIntrointfbuffget;
import com.z2daddy.uip.pojo.TSceIntrointfbuffgetId;
import com.z2daddy.uip.pojo.TSceIntrointfbuffset;
import com.z2daddy.uip.pojo.TSceIntrointfbuffsetId;

public class UIPNettyClientSimulatorTask implements Runnable {

	public static final Logger logger = LoggerFactory.getLogger(UIPNettyClientSimulatorTask.class);
	private static final String taskTag = "001";

	private int touchId;
	private Timer timer;

	public static List<UIPSocketSendTask> sendTasks;

	public UIPNettyClientSimulatorTask() {
	}

	public UIPNettyClientSimulatorTask(int touchId) throws FileNotFoundException, DocumentException {
		if (sendTasks == null) {
			getSendTasksFromXml();
		}

		timer = new Timer();
		this.touchId = touchId;
	}

	@Override
	public void run() {
		setTask();

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				try {
					timer.cancel();
					getTask();
				} catch (NotHitsExcepiton e) {
					logger.info(e.getMessage());
				}
			}

			public void getTask() throws NotHitsExcepiton {
				logger.info("getTask " + touchId + "...");
				Session dbSession = HibernateUtils.getSession();
				TSceIntrointfbuffgetId id = new TSceIntrointfbuffgetId();
				TSceIntrointfbuffget intfRespData;

				for (int i = 0; i < sendTasks.size(); i++) {

					id.setTouchid(String.valueOf(touchId));
					id.setSeqtag(sendTasks.get(i).getSeqTag());
					
					intfRespData = (TSceIntrointfbuffget) dbSession.get(TSceIntrointfbuffget.class, id);
					
					if (intfRespData == null) {
						throw new NotHitsExcepiton(
								"Not Hits!!!TouchId:" + touchId + " SeqTag:" + sendTasks.get(i).getSeqTag());
					} else {
						logger.info("Hits... TouchId:" + intfRespData.getId().getTouchid() + " SeqTag:"
								+ intfRespData.getId().getSeqtag() + " IntfResp:" + intfRespData.getIntfresp());
					}
				}
				
				HibernateUtils.closeSession(dbSession);
			}
		}, 3000);
	}

	public void setTask() {
		Session dbSession = HibernateUtils.getSession();
		dbSession.beginTransaction();

		TSceIntrointfbuffsetId id = new TSceIntrointfbuffsetId();
		id.setTasktag("001");
		id.setTouchid(String.valueOf(touchId));

		TSceIntrointfbuffset key = new TSceIntrointfbuffset();
		key.setCallerno("18729262982");
		key.setId(id);
		key.setIntfparams("nil:nil:nil:nil:029~1:nil:99418095:99418134");
		key.setTaskdate(new Date());

		dbSession.save(key);
		dbSession.getTransaction().commit();
		HibernateUtils.closeSession(dbSession);

		logger.info("setTask " + touchId + "...");
	}


	@SuppressWarnings("unchecked")
	public static void getSendTasksFromXml() throws FileNotFoundException, DocumentException {
		sendTasks = new ArrayList<UIPSocketSendTask>();

		SAXReader reader = new SAXReader();
		Document document = reader.read(new FileInputStream("configuration.xml"));
		Element rootNode = document.getRootElement();
		List<Element> todoTasks = rootNode.elements("TODOTASK");

		for (Element todoTask : todoTasks) {
			if (taskTag.equals(todoTask.attribute("value").getText())) {

				List<Element> tasks = todoTask.elements("TASK");
				for (Element task : tasks) {
					getSendTaskFromXml(task);
				}
			} else {
				continue;
			}
		}
	}

	public static void getSendTaskFromXml(Element task) {
		UIPSocketSendTask sendTask = new UIPSocketSendTask();
		sendTask.setTaskTag(taskTag);
		sendTask.setBizCode(task.element("BizCode").getText());
		sendTask.setTransCode(task.element("TransCode").getText());
		sendTask.setSeqTag(task.element("SeqTag").getText());
		sendTasks.add(sendTask);

	}

}

class NotHitsExcepiton extends Exception {

	private static final long serialVersionUID = 1L;

	public NotHitsExcepiton(String msg) {
		super(msg);
	}

	public NotHitsExcepiton() {
		super();
	}

}
