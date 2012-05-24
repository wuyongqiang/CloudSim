package org.cloudbus.cloudsim.message;

import java.util.Date;

public class VmTradeMessage {
	static final public int VM_TRADE_MESSAGE_SELL = 0;
	static final public int VM_TRADE_MESSAGE_OFFER = 1;
	static final public int VM_TRADE_MESSAGE_BID = 10;
	static final public int VM_TRADE_MESSAGE_ACCEPT = 11;
	
	static final public int VM_TRADE_MESSAGE_LIFE = 10000; // 10 seconds
	
	private int msgType;
	private int urgentLevel;
	private int sender;
	private int receiver;
	private long expireTime; 
	
	private double vmCPU;
	private double vmMem;
	private double curCPU;
	private double curMem;
	
	public VmTradeMessage(){
		setMsgType(0);
		setSender(-1);
		setReceiver(-1);
		setExpireTime(getCurrentTime() + VM_TRADE_MESSAGE_LIFE);
	}
	
	private long getCurrentTime(){
		return new Date().getTime();
	}

	public int getMsgType() {
		return msgType;
	}

	public void setMsgType(int msgType) {
		this.msgType = msgType;
	}

	public int getUrgentLevel() {
		return urgentLevel;
	}

	public void setUrgentLevel(int urgentLevel) {
		this.urgentLevel = urgentLevel;
	}

	public int getSender() {
		return sender;
	}

	public void setSender(int sender) {
		this.sender = sender;
	}

	public int getReceiver() {
		return receiver;
	}

	public void setReceiver(int receiver) {
		this.receiver = receiver;
	}

	public long getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}

	public double getVmCPU() {
		return vmCPU;
	}

	public void setVmCPU(double vmCPU) {
		this.vmCPU = vmCPU;
	}

	public double getVmMem() {
		return vmMem;
	}

	public void setVmMem(double vmMem) {
		this.vmMem = vmMem;
	}

	public double getCurCPU() {
		return curCPU;
	}

	public void setCurCPU(double curCPU) {
		this.curCPU = curCPU;
	}

	public double getCurMem() {
		return curMem;
	}

	public void setCurMem(double curMem) {
		this.curMem = curMem;
	}
	
}
