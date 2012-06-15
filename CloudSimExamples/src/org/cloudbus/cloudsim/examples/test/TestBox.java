package org.cloudbus.cloudsim.examples.test;

import java.util.ArrayDeque;

import org.junit.Assert;
import org.junit.Test;

public class TestBox {
	@Test
	public void testQueue(){
		ArrayDeque<Double> queue = new ArrayDeque<Double>(10);
		queue.add(1.0);
		queue.add(2.0);
		Assert.assertEquals(1, queue.pop().intValue());
		Assert.assertEquals(2, queue.pop().intValue());
	}
}
