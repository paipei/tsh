package jmc.service.impl;

import java.io.IOException;
import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.MalformedURLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import jmc.beans.ThreadInfoData;
import jmc.service.MBeanService;

public class MBeanServiceImpl implements MBeanService{
	
	private JMXConnector CONNECTOR = null;

	@Override
	public MBeanServerConnection getMBeanServerConnection(String server, String port) {
		
		String serverUrl = "service:jmx:rmi:///jndi/rmi://" + server + ":"+port+"/jmxrmi";
		
	    JMXServiceURL target = null;
		try {
			target = new JMXServiceURL(serverUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		try {
			if(target != null)
				CONNECTOR = JMXConnectorFactory.connect(target);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
		MBeanServerConnection remote = null;
		try {
			if(CONNECTOR != null)
				remote = CONNECTOR.getMBeanServerConnection();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return remote;
	}

	@Override
	public ThreadMXBean getThreadMXBean(MBeanServerConnection serverConnection) {
		
		ThreadMXBean threadMXBean = null;
		try {
			threadMXBean = ManagementFactory.newPlatformMXBeanProxy(serverConnection, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return threadMXBean;
	}

	@Override
	public List<ThreadInfoData> getThreadInfos(ThreadMXBean threadMXBean, Set<State> threadStates) {
		
		final List<ThreadInfoData> resultList = new LinkedList<ThreadInfoData>();
		
		long[] ids = threadMXBean.getAllThreadIds();
		
        ThreadInfo[] threadInfos = threadMXBean.getThreadInfo(ids, 200);
        
        if(threadInfos != null && threadInfos.length >0){
        	
        	for(ThreadInfo threadInfo:threadInfos){
        		
        		if(threadInfo == null)
        			continue;
        		
        		State threadState = threadInfo.getThreadState();
        		
        		if(threadStates.contains(threadState)){
        			
        			long threadId = threadInfo.getThreadId();
	        		String threadName = threadInfo.getThreadName();
	        		StackTraceElement[] stackTraceEles = threadInfo.getStackTrace();
	        		long blockedCount = threadInfo.getBlockedCount();
	        		
	        		ThreadInfoData threadInfoData = new ThreadInfoData();
	        		threadInfoData.setBlockedCount(blockedCount);
	        		threadInfoData.setThreadId(threadId);
	        		threadInfoData.setThreadName(threadName);
	        		threadInfoData.setThreadState(threadState.toString());
	        		
	        		List<String> stackTraceList = new LinkedList<String>();
	        		for(StackTraceElement stackTraceEle:stackTraceEles){
	        			stackTraceList.add(" - "+stackTraceEle.toString());
	        		}
	        		
	        		threadInfoData.setStackTraceList(stackTraceList);
	        		
	        		
	        		resultList.add(threadInfoData);
	        		
        			/*System.out.println(threadState.toString()+" ["+threadId+"] [blocked count:"+blockedCount+"] - "+threadName);
        			for(StackTraceElement stackTraceEle:stackTraceEles){
	        			System.out.println(" - "+stackTraceEle.toString());
	        		}*/
        		}
        		
        	}
        }
        
		return resultList;
	}

	@Override
	public Map<String, List<ThreadInfoData>> getThreadInfos(List<ThreadInfoData> threadInfoDataList) {
		
		final Map<String, List<ThreadInfoData>> result =  new LinkedHashMap<String, List<ThreadInfoData>>();
		
		for(ThreadInfoData threadInfoData:threadInfoDataList){
			
			String key = getThreadInfoDataKey(threadInfoData);
			List<ThreadInfoData> threadList = result.get(key);
			if(threadList != null){
				threadList.add(threadInfoData);
			}else{
				threadList = new LinkedList<ThreadInfoData>();
				threadList.add(threadInfoData);
			}
			
			result.put(key, threadList);
		}
		
		return result;
	}
	
	
	private String getThreadInfoDataKey(ThreadInfoData threadInfoData){
		
		final StringBuffer sb = new StringBuffer();
		
		final List<String> stackTraceList = threadInfoData.getStackTraceList();
		
		for(String stackTrace:stackTraceList){
			sb.append(stackTrace);
		}
		
		return sb.toString();
	}

	@Override
	public void closeJMXConnection() {
		
		try {
			if(CONNECTOR != null)
				CONNECTOR.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	
	

}
