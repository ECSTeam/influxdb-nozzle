/*
 * Copyright 2017 ECS Team, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 */

package com.ecsteam.nozzle.influxdb.nozzle;

import com.ecsteam.nozzle.influxdb.config.NozzleProperties;
import com.ecsteam.nozzle.influxdb.util.TestDopplerClient;
import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.EventType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(SpringRunner.class)
@Slf4j
public class FirehoseReaderTests {

	@MockBean
	private FirehoseEventSerializer mockWriter;

	private static final Map<EventType, Integer> DEFAULT_COUNTS = new HashMap<EventType, Integer>() {{
		put(EventType.CONTAINER_METRIC, 0);
		put(EventType.HTTP_START_STOP, 0);
		put(EventType.COUNTER_EVENT, 0);
		put(EventType.VALUE_METRIC, 0);
	}};

	private final Map<EventType, Integer> eventCounts = new HashMap<>();

	@Before
	public void setup() {
		eventCounts.putAll(DEFAULT_COUNTS);

		doAnswer(invocation -> {
			Envelope arg = invocation.getArgumentAt(0, Envelope.class);
			EventType eventType = arg.getEventType();

			eventCounts.put(eventType, eventCounts.get(eventType) + 1);

			return null;
		}).when(mockWriter).writeMessage(any());
	}

	@Test
	public void testWithAllEventsCaptured() throws Exception {
		runTest(new NozzleProperties());


	}

	@Test
	public void testWithNoEventsCaptured() throws Exception {
		NozzleProperties properties = new NozzleProperties();
		properties.setCapturedEvents("[]");

		runTest(properties);
	}

	@Test
	public void testWithSomeEventsCaptured() throws Exception {
		NozzleProperties properties = new NozzleProperties();
		properties.setCapturedEvents("[\"COUNTER_EVENT\", \"VALUE_METRIC\"]");
	}

	private void runTest(NozzleProperties properties) throws Exception {
		CountDownLatch counter = new CountDownLatch(1);

		FirehoseReader testReader = new FirehoseReader(new TestDopplerClient(), properties, mockWriter);
		testReader.setOnCompleteCallback(counter::countDown);
		testReader.start();

		counter.await();

		List<EventType> types = properties.getCapturedEvents();

		eventCounts.forEach((k, v) ->
				Assert.assertEquals(String.format("%s messages should have been delivered for event type %s", types.contains(k) ? "100" : "No", k.toString()),
						types.contains(k) ? 100 : 0, v.intValue()));
	}
}
