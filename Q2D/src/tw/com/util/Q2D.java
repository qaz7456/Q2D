package tw.com.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Q2D {

	private static final Logger logger = LogManager.getLogger(Q2D.class);
	public static String FILE_XML_PATH = null;
	
//	private static Thread thread = new Thread() {
//		@Override
//		public void run() {
//			ApplicationContext context = new ClassPathXmlApplicationContext(FILE_XML_PATH);
//			HeartBeatService service = (HeartBeatService) context.getBean("heartBeatService");
//
//			String message = null;
//			while (true) {
//				try {
//					message = RabbitMQ.Pull();
//
//					logger.debug("提取: {}", message);
//					if (message != null) {
//						logger.debug("開始發送至WebService");
//						message = WebService.execute(message);
//						logger.debug("WebServic響應: {}", message);
//						logger.debug("開始推送到Queue上");
//						RabbitMQ.Push(message);
//					}
//					service.beat();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//				if (message == null) {
//					try {
//						long breakTime = service.getHeartBeatClientVO().getTimeSeries();
//						logger.debug("休息" + breakTime + "毫秒");
//						Thread.sleep(breakTime);
//					} catch (InterruptedException e) {
//						logger.error(e.getMessage());
//					}
//				}
//			}
//		}
//	};

	public static void main(String[] args) throws Exception {
		String configPath = "resources\\q2d-config.xml";
		String convertPath = "resources\\test.xml";
		XMLUtil.insert(configPath,convertPath);
//		XMLUtil.delete(configPath, convertPath);
//		FILE_XML_PATH = args[0];
//		FILE_XML_PATH = new File(FILE_XML_PATH).toURI().toString();
//
//		thread.start();
	}
}
