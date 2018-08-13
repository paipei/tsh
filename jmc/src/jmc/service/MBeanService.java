package jmc.service;

import java.lang.Thread.State;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import jmc.beans.ThreadInfoData;

public interface MBeanService {
	
	public MBeanServerConnection getMBeanServerConnection(String server, String port);
	
	public ThreadMXBean getThreadMXBean(MBeanServerConnection serverConnection);
	
	public List<ThreadInfoData> getThreadInfos(ThreadMXBean threadMXBean, Set<State> threadStates);
	
	public Map<String, List<ThreadInfoData>> getThreadInfos(List<ThreadInfoData> threadInfoDataList);
	
	public void closeJMXConnection();
	
	

}
