package tw.com.util;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import tw.com.heartbeat.clinet.serivce.HeartBeatService;
import tw.com.heartbeat.clinet.vo.HeartBeatClientVO;

public class Q2D extends Thread {
	private static final Logger logger = LogManager.getLogger(Q2D.class);

	public static String FILE_XML_PATH = null;
	private String configPath = null;
	private String heartBeatXmlFilePath = null;

	public Q2D(String configPath, String heartBeatXmlFilePath) {
		this.configPath = configPath;
		this.heartBeatXmlFilePath = heartBeatXmlFilePath;
	}

	@Override
	public void run() {
		String message = null;

		Document configDoc = XMLUtil.getDocument(configPath);
		Element configRoot = configDoc.getDocumentElement();
		NodeList heartBeatClient = configRoot.getElementsByTagName("HeartBeatClient");

		NodeList clientInfo = heartBeatClient.item(0).getChildNodes();

		String beatID = null;
		String fileName = null;
		long timeSeries = 0;
		LocalDateTime localDateTime = LocalDateTime.now();

		for (int i = 0; i < clientInfo.getLength(); i++) {
			Node node = (Node) clientInfo.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				String nodeName = node.getNodeName();
				String value = node.getTextContent();

				beatID = nodeName.equals("BeatID") ? value : beatID;
				fileName = nodeName.equals("FileName") ? value : fileName;
				timeSeries = nodeName.equals("TimeSeries") ? Long.parseLong(value) : timeSeries;
			}
		}

		HeartBeatClientVO heartBeatClientVO = new HeartBeatClientVO();

		heartBeatClientVO.setBeatID(beatID);
		heartBeatClientVO.setFileName(fileName);
		heartBeatClientVO.setLocalDateTime(localDateTime);
		heartBeatClientVO.setTimeSeries(timeSeries);

		HeartBeatService heartBeatService = new HeartBeatService(heartBeatXmlFilePath);
		heartBeatService.setHeartBeatClientVO(heartBeatClientVO);

		while (true) {
			try {

				heartBeatService.beat();
				message = RabbitMQ.Pull(configPath);

				logger.debug("提取: {}", message);
				if (message != null) {
					try {
						String result = message;
						logger.debug("執行資料庫新增動作");
						XMLUtil.insert(configPath, result);
						logger.debug("執行資料庫刪除動作");
						XMLUtil.delete(configPath, result);
						logger.debug("執行資料庫修改動作");
						XMLUtil.update(configPath, result);
					} catch (Exception e) {
						logger.error(e.getMessage());
						RabbitMQ.ErrorPush(configPath, message);
					}
				}

			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			if (message == null) {
				try {
					logger.debug("休息" + timeSeries + "毫秒");
					Thread.sleep(timeSeries);
				} catch (InterruptedException e) {
					logger.error(e.getMessage());
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {
		String configPath = args[0];
		String heartBeatXmlFilePath = args[1];
		// String configPath = "resources\\test_q2d-q2d-config.xml";
		// String heartBeatXmlFilePath =
		// "resources\\test_q2d-q2d-HeatBeatClinetBeans.xml";
		// String convertPath = "resources\\test.xml";
		// XMLUtil.update(configPath, convertPath);
		// XMLUtil.insert(configPath,convertPath);
		// XMLUtil.delete(configPath, convertPath);
		new Q2D(configPath, heartBeatXmlFilePath).start();
	}
}
