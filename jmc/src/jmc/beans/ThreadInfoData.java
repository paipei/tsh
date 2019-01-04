package jmc.beans;

import java.util.List;

public class ThreadInfoData { // abc
	
	private long threadId;
	private String threadName;
	private String threadState;
	private long blockedCount;
	private List<String> stackTraceList;
	
	
	public long getThreadId() {
		return threadId;
	}
	public void setThreadId(long threadId) {
		this.threadId = threadId;
	}
	public String getThreadName() {
		return threadName;
	}
	public void setThreadName(String threadName) {
		this.threadName = threadName;
	}
	public String getThreadState() {
		return threadState;
	}
	public void setThreadState(String threadState) {
		this.threadState = threadState;
	}
	public long getBlockedCount() {
		return blockedCount;
	}
	public void setBlockedCount(long blockedCount) {
		this.blockedCount = blockedCount;
	}
	public List<String> getStackTraceList() {
		return stackTraceList;
	}
	public void setStackTraceList(List<String> stackTraceList) {
		this.stackTraceList = stackTraceList;
	}

}
