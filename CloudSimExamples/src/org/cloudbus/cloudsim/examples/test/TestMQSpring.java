package org.cloudbus.cloudsim.examples.test;

import static org.junit.Assert.*;

import javax.jms.JMSException;

import org.apache.activemq.spring.SpringConsumer;
import org.apache.activemq.spring.SpringProducer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/activeMq.xml"})
public class TestMQSpring{
	
	@Autowired
	private SpringConsumer consumer;
	
	@Autowired
	private SpringProducer producer;

	@Test
	public void test() throws JMSException, InterruptedException {
		//consumer.start();
		producer.start();	
		Thread.sleep(10000);
	}

}
