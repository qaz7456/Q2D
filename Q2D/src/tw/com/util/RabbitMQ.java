package tw.com.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.MessageProperties;

public class RabbitMQ {

	private static final Logger logger = LogManager.getLogger(RabbitMQ.class);

	public static String Pull(String configPath) throws IOException, TimeoutException {
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder dombuilder = null;
		try {
			dombuilder = domfac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error(e);
		}
		File file = new File(configPath);
		Document configDoc = null;
		try {
			configDoc = dombuilder.parse(file);
		} catch (SAXException | IOException e) {
			logger.error(e);
		}
		Element configRoot = configDoc.getDocumentElement();

		NodeList connectionFactory = configRoot.getElementsByTagName("queueConnectionFactory");
		NodeList connectionInfo = connectionFactory.item(0).getChildNodes();

		String host = null, username = null, password = null;
		int port = 0;

		for (int i = 0; i < connectionInfo.getLength(); i++) {
			Node node = (Node) connectionInfo.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {

				String nodeName = node.getNodeName();
				String value = node.getTextContent();

				host = nodeName.equals("host") ? value : host;
				port = nodeName.equals("port") ? Integer.valueOf(value) : port;
				username = nodeName.equals("username") ? value : username;
				password = nodeName.equals("password") ? value : password;
			}
		}
		logger.debug("host: {} \\ port: {} \\ username: {} \\ password: {}", host, port,
				username,password);

		NodeList queueOrigin = configRoot.getElementsByTagName("queueOrigin");
		NodeList queueOriginInfo = queueOrigin.item(0).getChildNodes();

		String queue_name = null, message = null;
		boolean autoAck = false;

		for (int i = 0; i < queueOriginInfo.getLength(); i++) {

			Node node = (Node) queueOriginInfo.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE) {

				String nodeName = node.getNodeName();
				String value = node.getTextContent();

				queue_name = "queueName".equals(nodeName) ? value : queue_name;
			}
		}
		logger.debug("queue_name: {} ", queue_name);

		ConnectionFactory factory = new ConnectionFactory();

		factory.setHost(host);
		factory.setPort(port);
		factory.setUsername(username);
		factory.setPassword(password);

		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		GetResponse response = channel.basicGet(queue_name, autoAck);
		if (response == null) {
			// No message retrieved.
		} else {
			AMQP.BasicProperties props = response.getProps();
			byte[] body = response.getBody();
			long deliveryTag = response.getEnvelope().getDeliveryTag();

			message = new String(body, "UTF-8");
			channel.basicAck(deliveryTag, false);
		}

		return message;

	}
}
