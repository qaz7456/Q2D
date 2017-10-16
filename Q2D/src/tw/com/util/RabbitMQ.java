package tw.com.util;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.MessageProperties;

public class RabbitMQ {

	private static final Logger logger = LogManager.getLogger(RabbitMQ.class);

//	public static void Push(String message) throws Exception {
//
//		ApplicationContext context = new ClassPathXmlApplicationContext(Q2D.FILE_XML_PATH);
//		ConnectionFactory factory = new ConnectionFactory();
//
//		ConnectionBean connectionBean = (ConnectionBean) context.getBean("connectionFactory");
//		QueueBean queueBean = (QueueBean) context.getBean("queueDestination");
//
//		String host = connectionBean.getHost();
//		int port = connectionBean.getPort();
//		String username = connectionBean.getUsername();
//		String password = connectionBean.getPassword();
//
//		String queue_name = queueBean.getQueueName();
//		String routing_key = queueBean.getRoutingKey();
//		String exchange = queueBean.getExchangeName();
//
//		factory.setHost(host);
//		factory.setPort(port);
//		factory.setUsername(username);
//		factory.setPassword(password);
//
//		Connection connection = factory.newConnection();
//		Channel channel = connection.createChannel();
//
//		channel.queueDeclare(queue_name, true, false, false, null);
//
//		channel.basicPublish(exchange, routing_key, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
//		logger.debug("寄送: {}", message);
//
//		channel.close();
//		connection.close();
//	}
//
//	public static String Pull() throws IOException, TimeoutException {
//
//		String message = null;
//		boolean autoAck = false;
//
//		ApplicationContext context = new ClassPathXmlApplicationContext(Q2D.FILE_XML_PATH);
//		ConnectionFactory factory = new ConnectionFactory();
//
//		ConnectionBean connectionBean = (ConnectionBean) context.getBean("connectionFactory");
//		QueueBean queueBean = (QueueBean) context.getBean("queueOrigin");
//
//		String host = connectionBean.getHost();
//		int port = connectionBean.getPort();
//		String username = connectionBean.getUsername();
//		String password = connectionBean.getPassword();
//
//		String queue_name = queueBean.getQueueName();
//
//		factory.setHost(host);
//		factory.setPort(port);
//		factory.setUsername(username);
//		factory.setPassword(password);
//
//		Connection connection = factory.newConnection();
//		Channel channel = connection.createChannel();
//
//		GetResponse response = channel.basicGet(queue_name, autoAck);
//		if (response == null) {
//			// No message retrieved.
//		} else {
//			AMQP.BasicProperties props = response.getProps();
//			byte[] body = response.getBody();
//			long deliveryTag = response.getEnvelope().getDeliveryTag();
//
//			message = new String(body, "UTF-8");
//			channel.basicAck(deliveryTag, false);
//		}
//
//		return message;
//
//	}
}
