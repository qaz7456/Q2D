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

	public static void Push(String message) throws Exception {
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder dombuilder = null;
		try {
			dombuilder = domfac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error(e);
		}
		File file = new File(Q2D.FILE_XML_PATH);
		Document configDoc = null;
		try {
			configDoc = dombuilder.parse(file);
		} catch (SAXException | IOException e) {
			logger.error(e);
		}
		Element configRoot = configDoc.getDocumentElement();

		NodeList connectionFactory = configRoot.getElementsByTagName("connectionFactory");
		NodeList connectionInfo = connectionFactory.item(0).getChildNodes();

		String host = null;
		int port = 0;
		String username = null;
		String password = null;

		for (int i = 0; i < connectionInfo.getLength(); i++) {
			Element node = (Element) connectionInfo.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {

				NamedNodeMap namedNodeMap = node.getAttributes();
				for (int l = 0; l < namedNodeMap.getLength(); ++l) {
					Node attr = namedNodeMap.item(l);
					String attrName = attr.getNodeName();
					String attrVal = attr.getNodeValue();

					host = "host".equals(attrName) ? attrVal : host;
					port = "port".equals(attrName) ? Integer.valueOf(attrVal) : port;
					username = "username".equals(attrName) ? attrVal : username;
					password = "password".equals(attrName) ? attrVal : password;
				}
			}
		}
		logger.debug("host: {} \\ port: {} \\ username: {} \\ password: {}", host, port, username, password);

		NodeList queueDestination = configRoot.getElementsByTagName("queueDestination");
		NodeList queueDestinationInfo = queueDestination.item(0).getChildNodes();

		String queue_name = null;
		String routing_key = null;
		String exchange = null;

		for (int i = 0; i < queueDestinationInfo.getLength(); i++) {
			Node node = (Node) connectionInfo.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {

				NamedNodeMap namedNodeMap = node.getAttributes();
				for (int l = 0; l < namedNodeMap.getLength(); ++l) {
					Node attr = namedNodeMap.item(l);
					String attrName = attr.getNodeName();
					String attrVal = attr.getNodeValue();

					queue_name = "queue_name".equals(attrName) ? attrVal : queue_name;
					routing_key = "routing_key".equals(attrName) ? attrVal : routing_key;
					exchange = "exchange".equals(attrName) ? attrVal : exchange;
				}
			}
		}
		logger.debug("queue_name: {} \\ routing_key: {} \\ exchange: {}", queue_name, routing_key, exchange);

		ConnectionFactory factory = new ConnectionFactory();

		factory.setHost(host);
		factory.setPort(port);
		factory.setUsername(username);
		factory.setPassword(password);

		Connection connection = factory.newConnection();
		Channel channel = connection.createChannel();

		channel.queueDeclare(queue_name, true, false, false, null);

		channel.basicPublish(exchange, routing_key, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
		logger.debug("寄送: {}", message);

		channel.close();
		connection.close();
	}

	public static String Pull() throws IOException, TimeoutException {
		DocumentBuilderFactory domfac = DocumentBuilderFactory.newInstance();
		DocumentBuilder dombuilder = null;
		try {
			dombuilder = domfac.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error(e);
		}
		File file = new File(Q2D.FILE_XML_PATH);
		Document configDoc = null;
		try {
			configDoc = dombuilder.parse(file);
		} catch (SAXException | IOException e) {
			logger.error(e);
		}
		Element configRoot = configDoc.getDocumentElement();

		NodeList connectionFactory = configRoot.getElementsByTagName("connectionFactory");
		NodeList connectionInfo = connectionFactory.item(0).getChildNodes();

		String host = null, username = null, password = null;
		int port = 0;

		for (int i = 0; i < connectionInfo.getLength(); i++) {
			Element node = (Element) connectionInfo.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {

				NamedNodeMap namedNodeMap = node.getAttributes();
				for (int l = 0; l < namedNodeMap.getLength(); ++l) {
					Node attr = namedNodeMap.item(l);
					String attrName = attr.getNodeName();
					String attrVal = attr.getNodeValue();

					host = "host".equals(attrName) ? attrVal : host;
					port = "port".equals(attrName) ? Integer.valueOf(attrVal) : port;
					username = "username".equals(attrName) ? attrVal : username;
					password = "password".equals(attrName) ? attrVal : password;
				}
			}
		}
		logger.debug("host: {} \\ port: {} \\ username: {} \\ password: {}", host, port, username, password);

		NodeList queueOrigin = configRoot.getElementsByTagName("queueOrigin");
		NodeList queueOriginInfo = queueOrigin.item(0).getChildNodes();

		String queue_name = null, message = null;
		boolean autoAck = false;

		for (int i = 0; i < queueOriginInfo.getLength(); i++) {
			Element node = (Element) connectionInfo.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {

				NamedNodeMap namedNodeMap = node.getAttributes();
				for (int l = 0; l < namedNodeMap.getLength(); ++l) {
					Node attr = namedNodeMap.item(l);
					String attrName = attr.getNodeName();
					String attrVal = attr.getNodeValue();

					queue_name = "queue_name".equals(attrName) ? attrVal : queue_name;
				}
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
