package jmc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import jmc.beans.ThreadInfoData;
import jmc.service.MBeanService;
import jmc.service.impl.MBeanServiceImpl;

public class JMC
{
	//private static final String basePath =  "D:\\temp\\";
	private static final String basePath =  "";
	private static final String result_file =  "result.txt";
	
  public JMC() {}
  
  /* clear cache
  public static void main(String[] args) throws Exception
  {
    try{
    	List<String> serverList = new ArrayList();
    	
    	if (args != null) {
	        if (args.length > 0) {
	          for (int i = 0; i < args.length; i++) {
	            serverList.add(args[i]);
	          }
	        } else {
	          System.out.println("no servers.");
	          System.exit(0);
	        }
    	}else {
    		System.out.println("no servers.");
    		System.exit(0);
    	}
      
    	clearCache(serverList);
    	    
    }catch (Exception e){
    	e.printStackTrace();
    	System.exit(0);
    }
  }
  */
  
  /* thread list */
  public static void main(String[] args) throws Exception
  {
	  Properties configProperties = getConfigProperties();
	  final String timeInterval = configProperties.getProperty("time.interval");
	  
	  try{
		  
		  long timeIntervalL = (Long.parseLong(timeInterval))*1000*60;
		  
		  if(timeIntervalL == 0L){
			  
			  exportThreadToFile(configProperties);
			  
		  }else{
			  
			  while(true){
					try{
						
						exportThreadToFile(configProperties);
						
						long nextExecutionTime = (new Date()).getTime()+timeIntervalL;
						System.out.println("Next execution time: "+new Date(nextExecutionTime));
						
						Thread.sleep(timeIntervalL);
						
						
					}catch(Exception e){
						e.printStackTrace();
						break;
					}  
				  }
		  }
		  
		  
	  }catch(Exception e){
		  e.printStackTrace();
	  }
	  
	  
	  
	  //test();
  }
  
  private static void test(){
	  
	  JMXConnector connector = null;
	  try{
		  
		  String server = "10.32.183.228";
		  String serverUrl = "service:jmx:rmi:///jndi/rmi://" + server + ":9003/jmxrmi";
	      JMXServiceURL target = new JMXServiceURL(serverUrl);
	      connector = JMXConnectorFactory.connect(target);
	      MBeanServerConnection remote = connector.getMBeanServerConnection();
	        
	      System.out.println("Connected to " + serverUrl);
	        
	      //ObjectName iwaSearchResultValueDataCache = new ObjectName("net.sf.ehcache:type=Cache,CacheManager=iwacache,name=iwaSearchResultValueDataCache");
		  
	      ObjectName ajpExecutor = new ObjectName("Catalina:type=Executor,name=ajpExecutor");
	      
	      System.out.println("ac:"+remote.getAttribute(ajpExecutor, "activeCount"));
	      
	      
	      connector.close();
	  }catch(Exception e){
		  e.printStackTrace();
	  }finally{
		  if(connector != null){
			  try {
					connector.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		  }
			
	  }
  }
  
  private static void exportThreadToFile(Properties configProperties){

	    try{
	    	
	    	final String port = configProperties.getProperty("server.port");
	    	final String serverListStr = configProperties.getProperty("server.list");
	    	final String statesStr = configProperties.getProperty("thread.states");
	    	
	    	final Set<State> states = new HashSet<State>();
	    	final List<String> servers = new ArrayList<String>();
	    	
	    	if(serverListStr != null){
	    		String[] serverListArrs = serverListStr.split(",");
	    		for(String serverListArr:serverListArrs){
	    			servers.add(serverListArr.trim());
	    		}
	    	}
	    	
	    	if(statesStr != null){
	    		final String[] statesArrs = statesStr.split(",");
	    		for(String statesArr:statesArrs){
	    			State state = State.valueOf(statesArr.trim());
	    			if(state != null)
	    				states.add(state);
	    		}
	    	}
	    	
	    	StringBuffer result = new StringBuffer();
	    	String dateStr = "### Date Time: "+new Date()+" ###";
	    	System.out.println(dateStr);
	    	result.append(dateStr+"\n");
	    	for(String server:servers){
	    		StringBuffer sb = getServerThread(server, states, port);
	    		result.append(sb);
	    	}
	    	
	    	try {
	    		//BufferedWriter writer = new BufferedWriter( new FileWriter(basePath+result_file));
	    		//writer.append(result.toString());
	    		//writer.close();
	    		
	    		File resultFile = new File(basePath+result_file);
	    		if(!resultFile.exists())
	    			resultFile.createNewFile();
	    		
	    		Files.write(Paths.get(basePath+result_file), result.toString().getBytes("UTF-8"), StandardOpenOption.APPEND);
	    		
	    		System.out.println("*** server thread log is  exported to "+result_file+" ***");
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	      
	    }catch (Exception e){
	    	e.printStackTrace();
	    	System.exit(0);
	    }
  }
  
  private static Properties getConfigProperties(){
	  
	  Properties configProperties = new Properties();
	  
	  try {
		  
		InputStream inputFile = new FileInputStream(basePath+"config.properties");
		configProperties.load(inputFile);
		
		
		
	} catch (Exception e) {
		e.printStackTrace();
	}
	  
	  return configProperties;
  }
  
  private static StringBuffer getServerThread(String server, Set<State> states, String port){
	  
	  StringBuffer result = new StringBuffer();
	  try{
		  System.out.println("************************************checking host "+server+"***************************************");
		  result.append("******************************************************************************************************************************\n");
		  result.append("************************************************checking host "+server+" start **********************************************\n");
		  result.append("******************************************************************************************************************************\n");
		  
		  MBeanService mBeanService = new MBeanServiceImpl();
		  
		  MBeanServerConnection serverConnection = mBeanService.getMBeanServerConnection(server, port);
		  ThreadMXBean threadMXBean = mBeanService.getThreadMXBean(serverConnection);
		  
		  ObjectName ajpExecutor = new ObjectName("Catalina:type=Executor,name=ajpExecutor");
		  Object activeCountObj = serverConnection.getAttribute(ajpExecutor, "activeCount");
		  String activeCountStr = "Catalina/ajpExecutor.activeCount="+activeCountObj+"\n";
	      result.append(activeCountStr);
	      result.append("\n");
	      System.out.println(activeCountStr);
	      
		  final List<ThreadInfoData> threadInfoDataList = mBeanService.getThreadInfos(threadMXBean, states);
		  // new code start
		  final Map<String, List<ThreadInfoData>> threadInfoDataMapList = mBeanService.getThreadInfos(threadInfoDataList);
		  long blockedCount = 0;
		  for(Map.Entry<String, List<ThreadInfoData>> threadInfoDataEntry:threadInfoDataMapList.entrySet()){
			  
			  final List<ThreadInfoData> threadList = threadInfoDataEntry.getValue();
			  for(int t=0;t<threadList.size();t++){
				  ThreadInfoData threadInfoData = threadList.get(t);
				  
				  String threadString = threadInfoData.getThreadState()+" ["+threadInfoData.getThreadId()+"] [blocked count:"+threadInfoData.getBlockedCount()+"] - "+threadInfoData.getThreadName();
				  
				  if("BLOCKED".equals(threadInfoData.getThreadState())){
					  blockedCount ++;
				  }
				  
				  System.out.println(threadString);
				  result.append(threadString+"\n");
				  
				  if(t+1 == threadList.size()){
					  for(String stackTrace: threadInfoData.getStackTraceList()){
						  //System.out.println(stackTrace);
						  result.append(stackTrace+"\n");
					  }
				  }
				  
			  }
			  
		  }
		  // new code end
		  
		  /*
		  for(ThreadInfoData threadInfoData:threadInfoDataList){
			  String threadString = threadInfoData.getThreadState()+" ["+threadInfoData.getThreadId()+"] [blocked count:"+threadInfoData.getBlockedCount()+"] - "+threadInfoData.getThreadName();
			  
			  System.out.println(threadString);
			  result.append(threadString+"\n");
			  for(String stackTrace: threadInfoData.getStackTraceList()){
				  //System.out.println(stackTrace);
				  result.append(stackTrace+"\n");
			  }
		  }*/
		  
		  mBeanService.closeJMXConnection();
		  
		  result.append("\n");
		  Date today = new Date();
		  SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		  String todayString = sdf.format(today);
		  String logString = "JMCLOG|"+todayString+ "|server|" + server+"|activeCount|"+activeCountObj+"|blockedCount|"+ blockedCount;
		  System.out.println(logString);
		  result.append(logString);
		  
		  result.append("\n");
		  result.append("******************************************************************************************************************************\n");
		  result.append("************************************************checking host "+server+" end ************************************************\n");
		  result.append("******************************************************************************************************************************\n");
		  result.append("\n\n\n");
	  }catch(Exception e){
		  e.printStackTrace();
	  }
	  
	return result;
  }
  
  
  private static void clearCache(List<String> serverList){
	  
	  
	  for (String server : serverList){
	
		  try{
			  
			  String serverUrl = "service:jmx:rmi:///jndi/rmi://" + server + ":9003/jmxrmi";
		      JMXServiceURL target = new JMXServiceURL(serverUrl);
		      JMXConnector connector = JMXConnectorFactory.connect(target);
		      MBeanServerConnection remote = connector.getMBeanServerConnection();
		        
		      System.out.println("Connected to " + serverUrl);
		        
		      ObjectName iwaSearchResultValueDataCache = new ObjectName("net.sf.ehcache:type=Cache,CacheManager=iwacache,name=iwaSearchResultValueDataCache");
		      ObjectName pnsProductDataCache = new ObjectName("net.sf.ehcache:type=Cache,CacheManager=iwacache,name=pnsProductDataCache");
		        
		      remote.invoke(iwaSearchResultValueDataCache, "removeAll", null, null);
		      System.out.println("iwaSearchResultValueDataCache cleared");
		      remote.invoke(pnsProductDataCache, "removeAll", null, null);
		      System.out.println("pnsProductDataCache cleared");
		        
		      connector.close();
		        
		      System.out.println("*** " + server + " connection closed.");
		        
		  }catch(MalformedURLException e){
			  e.printStackTrace();
		  }catch(IOException e){
			  e.printStackTrace();
		  }catch(MalformedObjectNameException e){
			  e.printStackTrace();
		  }catch(ReflectionException e){
			  e.printStackTrace();
		  }catch(MBeanException e){
			  e.printStackTrace();
		  }catch(InstanceNotFoundException e){
			  e.printStackTrace();
		  }


      }
  }
}
