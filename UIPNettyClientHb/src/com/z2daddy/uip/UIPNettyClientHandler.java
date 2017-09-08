package com.z2daddy.uip;

import java.io.UnsupportedEncodingException;
import java.util.Date;

import org.hibernate.Session;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.z2daddy.uip.pojo.TSceIntrointfbuffget;
import com.z2daddy.uip.pojo.TSceIntrointfbuffgetId;


public class UIPNettyClientHandler extends SimpleChannelHandler {

	public static final Logger logger = LoggerFactory.getLogger(UIPNettyClientHandler.class);
	
	
	
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws UnsupportedEncodingException {
		ChannelBuffer channelBuffer = (ChannelBuffer)e.getMessage();
		String recvMsg = getSocketResponseMsg(channelBuffer);
		TSceIntrointfbuffget key = getKeyData(recvMsg);
		if(!key.getId().getTouchid().equals(UIPNettyClient.heartBeatParams.get("TOUCHID"))){
			addResp(key);
		}
		//logger.info(key+"&"+value);
	}
	
	public String getSocketResponseMsg(ChannelBuffer channelBuffer) throws UnsupportedEncodingException{
		int readableBytes = channelBuffer.readableBytes();
		byte [] bufferedBytes = new byte [readableBytes];
		channelBuffer.readBytes(bufferedBytes);
		
		return  new String(bufferedBytes,"GBK");
	}
	
	public static void addResp(TSceIntrointfbuffget key){
		Session dbSession = HibernateUtils.getSession();
		dbSession.beginTransaction();
		dbSession.save(key);
		dbSession.getTransaction().commit();
		HibernateUtils.closeSession(dbSession);
	}
	
	public TSceIntrointfbuffget getKeyData(String recvMsg){
		TSceIntrointfbuffget result= new TSceIntrointfbuffget();
		String touchIdWithSeqTag = recvMsg.substring(UIPSocketProtocol.SEQ_START_INDEX,UIPSocketProtocol.SEQ_START_INDEX+UIPSocketProtocol.SEQ_LEN).trim();
		String touchId = touchIdWithSeqTag.substring(0,touchIdWithSeqTag.length()-1);
		String seqTag = touchIdWithSeqTag.substring(touchIdWithSeqTag.length()-1);
		String serialNumber = recvMsg.substring(UIPSocketProtocol.NO_START_INDEX,UIPSocketProtocol.NO_START_INDEX+UIPSocketProtocol.SERIAL_NUMBER_LEN).trim();
		String bizCode = recvMsg.substring(UIPSocketProtocol.BIZCODE_START_INDEX,UIPSocketProtocol.BIZCODE_START_INDEX+UIPSocketProtocol.BIZCODE_LEN).trim();
		result.setId(new TSceIntrointfbuffgetId(touchId,seqTag));
		result.setCallerno(serialNumber);
		result.setBizcode(bizCode);
		result.setTaskdate(new Date());
		result.setIntfresp(getValueData(recvMsg));
		return  result;
	}
	
	public String getValueData(String recvMsg){
		String value = recvMsg.substring(UIPSocketProtocol.UIP_HEADLEN).trim();
		return  formatValue(value);
	}
	
	public String formatValue(String value){
		String valueFormatted= new String(value);
		while(valueFormatted.indexOf(" "+UIPSocketProtocol.RECORD_SEPRATOR) >= 0){
			valueFormatted = valueFormatted.replaceAll(" "+UIPSocketProtocol.RECORD_SEPRATOR, UIPSocketProtocol.RECORD_SEPRATOR);
		}
		return valueFormatted.trim();
	}
	

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelConnected(ctx, e);
		logger.info("channelConnected");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		logger.info("exceptionCaught"+e.getCause().getMessage());
		e.getCause().printStackTrace();
		e.getChannel().close();
	}
	
	

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelClosed(ctx, e);
		logger.info("channelClosed:"+e);
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		super.channelDisconnected(ctx, e);
		logger.info("channelDisconnected:"+e);
	}
}
