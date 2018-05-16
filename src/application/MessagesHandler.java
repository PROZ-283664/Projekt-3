package application;

import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;

public class MessagesHandler {
	ConnectionFactory connectionFactory;
	JMSContext jmsContext;
	JMSConsumer jmsConsumer;
	Queue queue;

	MessagesHandler() {
		try {
			connectionFactory = new com.sun.messaging.ConnectionFactory();
			((com.sun.messaging.ConnectionFactory) connectionFactory)
					.setProperty(com.sun.messaging.ConnectionConfiguration.imqAddressList, "localhost:7676/jms");
			jmsContext = connectionFactory.createContext();
			queue = new com.sun.messaging.Queue("ATJQueue");
			jmsConsumer = jmsContext.createConsumer(queue, null);

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	public void setConsumer(String messageSelector) {
		jmsConsumer.close();
		jmsConsumer = jmsContext.createConsumer(queue, messageSelector);
	}

	public Message receiveQueueMessageSync() {
		return receiveQueueMessageSync(0);
	}

	public Message receiveQueueMessageSync(long time) {
		Message msg = jmsConsumer.receive(time);
		return msg;
	}

	public Message receiveQueueMessageSyncNoWait() {
		Message msg = jmsConsumer.receiveNoWait();
		return msg;
	}

	public void receiveQueueMessagesAsync(MessageListener listner) {
		jmsConsumer.setMessageListener(listner);
	}

	public void recieveAll() {
		JMSConsumer jmsConsumerAll = jmsContext.createConsumer(queue, null);
		while ((jmsConsumerAll.receive(10)) != null) {
		}
		jmsConsumerAll.close();
	}

	public void sendQueueMessage(Message message) {
		JMSProducer jmsProducer = jmsContext.createProducer();
		jmsProducer.send(queue, message);
	}

	public Message createMessage() {
		return jmsContext.createMessage();
	}

	public void close() {
		jmsConsumer.close();
		jmsContext.close();
	}
}
