//package tw.com.util;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//public class Thread {
//	private static final Logger logger = LogManager.getLogger(Thread.class);
//	private String configPath =null,convertPath =null;
//	 public Thread(String configPath,String convertPath) {
//		this.configPath = configPath;
//		this.convertPath = convertPath;
//	}
//	@Override
//	public void run() {
//		String message = null;
//		while (true) {
//			try {
//				message = RabbitMQ.Pull();
//
//				logger.debug("提取: {}", message);
//				if (message != null) {
//					XMLUtil.insert(configPath, convertPath);
//					logger.debug("開始發送至WebService");
////					message = WebService.execute(message);
//					logger.debug("WebServic響應: {}", message);
//					logger.debug("開始推送到Queue上");
//					RabbitMQ.Push(message);
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			if (message == null) {
//				try {
////					long breakTime = service.getHeartBeatClientVO().getTimeSeries();
//					logger.debug("休息" + breakTime + "毫秒");
//					Thread.sleep(breakTime);
//				} catch (InterruptedException e) {
//					logger.error(e.getMessage());
//				}
//			}
//		}
//	}
//}
