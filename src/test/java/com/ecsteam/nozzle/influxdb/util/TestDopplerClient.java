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

package com.ecsteam.nozzle.influxdb.util;

import org.cloudfoundry.doppler.ContainerMetric;
import org.cloudfoundry.doppler.CounterEvent;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.EventType;
import org.cloudfoundry.doppler.FirehoseRequest;
import org.cloudfoundry.doppler.HttpStartStop;
import org.cloudfoundry.doppler.Method;
import org.cloudfoundry.doppler.PeerType;
import org.cloudfoundry.doppler.ValueMetric;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TestDopplerClient extends AbstractClients.TestDopplerClient {
	private static final Random RNG = new Random();

	private static final String[] UNITS = new String[]{"nanos", "s", "bytes", "b"};

	private static final String[] APPS = new String[]{"app1234", "app2341", "app3412", "app4123"};

	private static final int ONE_GIG = 1024 * 1024 * 1024;

	private static final PeerType[] PEERS = new PeerType[] { PeerType.CLIENT, PeerType.SERVER };

	private static final Method[] METHODS = new Method[] { Method.GET, Method.POST, Method.PUT, Method.DELETE, Method.HEAD, Method.OPTIONS };

	@Override
	public Flux<Envelope> firehose(FirehoseRequest firehoseRequest) {
		return Flux.fromIterable(envelopes()).delayElements(Duration.ofMillis(5));
	}

	private List<Envelope> envelopes() {
		List<Envelope> envelopes = new ArrayList<>(500);

		for (int i = 0; i < 100; ++i) {
			envelopes.add(generateContainerMetric());
			envelopes.add(generateCounterEvent());
			envelopes.add(generateHttpStartStop());
			envelopes.add(generateValueMetric());
		}

		Collections.shuffle(envelopes);

		return envelopes;
	}

	private Envelope.Builder startEnvelope() {
		int idx = 10 + RNG.nextInt(10);

		return Envelope.builder()
				.ip(String.format("10.0.0.%d", idx))
				.job("test-job")
				.index(String.format("idx-%d", idx))
				.origin("test");
	}

	private Envelope generateValueMetric() {
		Envelope.Builder builder = startEnvelope();

		ValueMetric valueMetric = ValueMetric.builder().name("valueMetric." + UUID.randomUUID().toString()).value(1024 * RNG.nextDouble()).unit(UNITS[RNG.nextInt(UNITS.length)]).build();
		return builder.eventType(EventType.VALUE_METRIC).valueMetric(valueMetric).build();
	}

	private Envelope generateCounterEvent() {
		Envelope.Builder builder = startEnvelope();

		CounterEvent event = CounterEvent.builder().name("counterEvent." + UUID.randomUUID().toString()).total(RNG.nextLong()).delta(RNG.nextLong()).build();
		return builder.eventType(EventType.COUNTER_EVENT).counterEvent(event).build();
	}

	private Envelope generateContainerMetric() {
		Envelope.Builder builder = startEnvelope();

		ContainerMetric metric = ContainerMetric.builder()
				.applicationId(APPS[RNG.nextInt(APPS.length)])
				.cpuPercentage(RNG.nextDouble())
				.diskBytes((long) RNG.nextInt(ONE_GIG))
				.diskBytesQuota(2L * ONE_GIG)
				.memoryBytes((long) RNG.nextInt(ONE_GIG))
				.memoryBytesQuota((long) ONE_GIG)
				.instanceIndex(RNG.nextInt(3))
				.build();

		return builder.eventType(EventType.CONTAINER_METRIC).containerMetric(metric).build();
	}

	private Envelope generateHttpStartStop() {
		Envelope.Builder builder = startEnvelope();

		HttpStartStop metric = HttpStartStop.builder()
				.forwarded("1.2.3.4", "192.168.1.10", "10.0.0.1")
				.contentLength((long) RNG.nextInt(ONE_GIG))
				.peerType(PEERS[RNG.nextInt(PEERS.length)])
				.method(METHODS[RNG.nextInt(METHODS.length)])
				.requestId(UUID.randomUUID())
				.startTimestamp(System.currentTimeMillis() - 100 - RNG.nextInt(50))
				.stopTimestamp(System.currentTimeMillis() - 50 - RNG.nextInt(25))
				.uri(String.format("/some/resource/%s", UUID.randomUUID().toString()))
				.userAgent("test-unit")
				.statusCode(200 + RNG.nextInt(310))
				.remoteAddress("1.2.3.4")
				.build();

		return builder.eventType(EventType.HTTP_START_STOP).httpStartStop(metric).build();
	}
}
