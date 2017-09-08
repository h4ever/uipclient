package com.z2daddy.uip;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.z2daddy.uip.pojo.TSceIntrointfbuffset;

public class UIPNettyClient {

	public static final Logger logger = LoggerFactory.getLogger(UIPNettyClient.class);
	public static final int CALLID_START_INDX = 14;
	public static final String REDIS_SEPERATOR = ":";

	public static List<Channel> channels;
	public static List<UIPSocketServer> uipSocketServers;
	public static List<UIPSocketSendTask> sendTasks;
	public static String taskTag;

	public static Map<String, String> heartBeatParams;

	public static void main(String[] args)
			throws DocumentException, InterruptedException, IOException, NoAvailableChannelException {
		taskTag = new String(args[0]);
		readConfigFromXml();
		ClientBootstrap bootstrap = getBootstrap();
		channels = connectToUIPServer(bootstrap);
		loopSendMsgToServer();
	}

	public static ClientBootstrap getBootstrap() {
		ChannelFactory factory = new NioClientSocketChannelFactory(Executors.newCachedThreadPool(),
				Executors.newCachedThreadPool());

		ClientBootstrap bootstrap = new ClientBootstrap(factory);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				ChannelPipeline channelPipeline = Channels.pipeline();
				ChannelBuffer delimiter = ChannelBuffers.copiedBuffer(new byte[] { UIPSocketProtocol.PROTOCOL_ENDING });
				channelPipeline.addLast("decoder",
						new DelimiterBasedFrameDecoder(UIPSocketProtocol.READER_BUFFER_LENGTH, delimiter));
				channelPipeline.addLast("handler", new UIPNettyClientHandler());
				return channelPipeline;
			}
		});

		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		return bootstrap;
	}

	public static List<Channel> connectToUIPServer(ClientBootstrap bootstrap) throws InterruptedException {
		channels = new ArrayList<Channel>();
		for (UIPSocketServer uipSocketServer : uipSocketServers) {
			for (int i = 0; i < uipSocketServer.getPortRange(); i++) {
				ChannelFuture channelFuture = bootstrap
						.connect(new InetSocketAddress(uipSocketServer.getHost(), uipSocketServer.getStartPort() + i))
						.sync();
				Channel channel = channelFuture.getChannel();
				channels.add(channel);
			}
		}
		return channels;
	}

	public static void loopSendMsgToServer() throws InterruptedException, NoAvailableChannelException {
		int noTaskCount = 0;
		int interval = Integer.parseInt(heartBeatParams.get("INTERVAL"));
		int intervalUnit = Integer.parseInt(heartBeatParams.get("INTERVALUNIT"));
		int sleepCount = (interval * 1000) / intervalUnit;

		for (;;) {
			List<TSceIntrointfbuffset> todoTaskKeys = getTasksFromRedis();
			if (0 == todoTaskKeys.size()) {
				noTaskCount++;
				if (noTaskCount > sleepCount) {
					startHeartBeatTask();
					noTaskCount = 0;
				}
				Thread.sleep(intervalUnit);
				continue;
			} else {
				noTaskCount = 0;
				try {
					sendMsgsToServer(todoTaskKeys);
					removeTasks(todoTaskKeys);
				} catch (NotMatchWithXmlException | NoAvailableChannelException e) {
					logger.info(e.getMessage());
					break;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static List<TSceIntrointfbuffset> getTasksFromRedis() {
		Session dbSession = HibernateUtils.getSession();
		Query query = dbSession.createQuery("from TSceIntrointfbuffset where tasktag = ?");
		query.setParameter(0, taskTag);
		List<TSceIntrointfbuffset> tasks = query.list();
		HibernateUtils.closeSession(dbSession);
		return tasks;
	}

	public static void removeTasks(List<TSceIntrointfbuffset> todoTaskKeys) {
		Session dbSession = HibernateUtils.getSession();
		dbSession.beginTransaction();
		for (TSceIntrointfbuffset key : todoTaskKeys) {
			TSceIntrointfbuffset task = (TSceIntrointfbuffset) dbSession.load(TSceIntrointfbuffset.class, key.getId());
			dbSession.delete(task);
		}

		dbSession.getTransaction().commit();
		HibernateUtils.closeSession(dbSession);
	}

	public static void startHeartBeatTask() throws NoAvailableChannelException {
		String heartBeatMsg = generateSendMsg(heartBeatParams);
		for (int i = 0; i < channels.size(); i++) {
			sendMsgToServer(heartBeatMsg, channels.get(i));
		}
	}

	public static void sendMsgsToServer(List<TSceIntrointfbuffset> todoTaskKeys)
			throws NotMatchWithXmlException, NoAvailableChannelException {
		for (TSceIntrointfbuffset key : todoTaskKeys) {
			String[] sendMsgs = generateSendMsgs(key);
			int indexBase = getChannelIndexBaseNum(key);
			for (int i = 0; i < sendMsgs.length; i++) {
				Channel ch = getAvailableChannel(indexBase, i);
				sendMsgToServer(sendMsgs[i], ch);

			}
		}
	}

	public static int getChannelIndexBaseNum(TSceIntrointfbuffset pojo){
		return Integer.parseInt(pojo.getId().getTouchid().substring(CALLID_START_INDX)) ;
	}
	
	public static Channel getAvailableChannel(int indexBase, int offset) throws NoAvailableChannelException {
		int index = (indexBase+offset) % channels.size();
		Channel ch = channels.get(index);

		if (ch.isConnected()) {
			return ch;
		} else {
			channels.remove(index);

			for (int i = index+1; i < channels.size(); i++) {
				if (channels.get(i).isConnected()) {
					return channels.get(i);
				}
			}
			
			for(int i=0;i<index;i++){
				if (channels.get(i).isConnected()) {
					return channels.get(i);
				}
			}
		}

		throw new NoAvailableChannelException("no valid channel");

	}

	public static void sendMsgToServer(String toSendMsg, Channel channel) {
		ChannelBuffer channelBuffer = ChannelBuffers.copiedBuffer(toSendMsg, Charset.forName("utf-8"));
		channel.write(channelBuffer);
	}

	public static String[] generateSendMsgs(TSceIntrointfbuffset key) throws NotMatchWithXmlException {
		String[] requestBodies = getRequestBodies(key);

		String[] sendMsgs = new String[requestBodies.length];
		for (int i = 0; i < requestBodies.length; i++) {
			Map<String, String> mustInput = redisKeyProtocol(key);
			redisValueProtocol(mustInput, requestBodies, i);
			sendMsgs[i] = generateSendMsg(mustInput);
		}

		return sendMsgs;
	}

	public static String generateSendMsg(Map<String, String> mustInput) {
		UIPSocketProtocol socketHead = UIPSocketProtocol.getIstance();

		String requestBody = mustInput.get("REQUEST_BODY");
		int reqLen = UIPSocketProtocol.UIP_HEADLEN + requestBody.length() + 1;

		socketHead.setRequestDataSerialId(mustInput.get("TOUCHID") + mustInput.get("SEQ_TAG"));
		socketHead.setPhoneNo(mustInput.get("SERIAL_NUMBER"));
		socketHead.setRequestDatalength(reqLen);
		socketHead.setUipBizCode(mustInput.get("BIZ_CODE"));
		socketHead.setUipTranCode(mustInput.get("TRANS_CODE"));

		return socketHead.setSockHead() + requestBody + (char) UIPSocketProtocol.PROTOCOL_ENDING;
	}

	public static Map<String, String> redisKeyProtocol(TSceIntrointfbuffset key) {
		Map<String, String> mustInput = new HashMap<String, String>();
		mustInput.put("TOUCHID", key.getId().getTouchid());
		mustInput.put("SERIAL_NUMBER", key.getCallerno());

		return mustInput;
	}

	public static void redisValueProtocol(Map<String, String> mustInput, String[] requestBodies, int i) {
		mustInput.put("BIZ_CODE", sendTasks.get(i).getBizCode());
		mustInput.put("TRANS_CODE", sendTasks.get(i).getTransCode());
		mustInput.put("SEQ_TAG", sendTasks.get(i).getSeqTag());

		mustInput.put("REQUEST_BODY", requestBodyProtocol(requestBodies[i]));
	}

	public static String requestBodyProtocol(String requestBody) {
		return "nil".equals(requestBody) ? "" : requestBody;
	}

	public static String[] getRequestBodies(TSceIntrointfbuffset key) throws NotMatchWithXmlException {
		String[] requestBodies = key.getIntfparams().split(REDIS_SEPERATOR);
		if (requestBodies.length != sendTasks.size()) {
			throw new NotMatchWithXmlException("NotMatchWithXmlException:" + key.getIntfparams());
		}
		return requestBodies;
	}

	@SuppressWarnings("unchecked")
	public static void readConfigFromXml() throws FileNotFoundException, DocumentException {
		sendTasks = new ArrayList<UIPSocketSendTask>();

		SAXReader reader = new SAXReader();
		Document document = reader.read(new FileInputStream("configuration.xml"));
		Element rootNode = document.getRootElement();
		getUIPServerCfg(rootNode);
		List<Element> todoTasks = rootNode.elements("TODOTASK");

		for (Element todoTask : todoTasks) {
			if (taskTag.equals(todoTask.attribute("value").getText())) {
				getHeartBeatCfg(todoTask);
				List<Element> tasks = todoTask.elements("TASK");
				for (Element task : tasks) {
					getSendTaskCfg(task);
				}
			} else {
				continue;
			}
		}
	}

	public static void getSendTaskCfg(Element task) {
		UIPSocketSendTask sendTask = new UIPSocketSendTask();
		sendTask.setTaskTag(taskTag);
		sendTask.setBizCode(task.element("BizCode").getText());
		sendTask.setTransCode(task.element("TransCode").getText());
		sendTask.setSeqTag(task.element("SeqTag").getText());
		sendTasks.add(sendTask);

	}

	public static void getHeartBeatCfg(Element todoTask) {
		heartBeatParams = new HashMap<String, String>();

		Element heartBeat = todoTask.element("HeartBeat");

		heartBeatParams.put("SERIAL_NUMBER", heartBeat.element("SerialNumber").getText());
		heartBeatParams.put("TOUCHID", heartBeat.element("TouchId").getText());
		heartBeatParams.put("SEQ_TAG", heartBeat.element("SeqTag").getText());
		heartBeatParams.put("BIZ_CODE", heartBeat.element("BizCode").getText());
		heartBeatParams.put("TRANS_CODE", heartBeat.element("TransCode").getText());
		heartBeatParams.put("REQUEST_BODY", heartBeat.element("RequestBody").getText());
		heartBeatParams.put("INTERVAL", heartBeat.element("Interval").getText());
		heartBeatParams.put("INTERVALUNIT", heartBeat.element("IntervalUnit").getText());
	}

	@SuppressWarnings("unchecked")
	public static void getUIPServerCfg(Element rootNode) {
		List<Element> uipServers = rootNode.elements("UIPServer");
		uipSocketServers = new ArrayList<UIPSocketServer>();
		for (Element uipServer : uipServers) {
			UIPSocketServer uipSocketServer = new UIPSocketServer();
			uipSocketServer.setHost(uipServer.element("Host").getText());
			uipSocketServer.setStartPort(Integer.parseInt(uipServer.element("StartPort").getText()));
			uipSocketServer.setPortRange(Integer.parseInt(uipServer.element("PortRange").getText()));
			uipSocketServers.add(uipSocketServer);
		}
	}

}

@SuppressWarnings("serial")
class NotMatchWithXmlException extends Exception {
	public NotMatchWithXmlException() {
		super();
	}

	public NotMatchWithXmlException(String msg) {
		super(msg);
	}
}

@SuppressWarnings("serial")
class NoAvailableChannelException extends Exception {
	public NoAvailableChannelException() {
		super();
	}

	public NoAvailableChannelException(String msg) {
		super(msg);
	}
}
