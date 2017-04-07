/*******************************************************************************
 *  Copyright 2017 ECS Team, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use
 *  this file except in compliance with the License. You may obtain a copy of the
 *  License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed
 *  under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *  CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations under the License.
 ******************************************************************************/

package com.ecsteam.nozzle.influxdb.nozzle;

import com.ecsteam.nozzle.influxdb.config.NozzleProperties;
import com.ecsteam.nozzle.influxdb.destination.MetricsDestination;
import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.doppler.CounterEvent;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.ValueMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Captures messages from the Cloud Foundry Firehose and batches them to be sent to InfluxDB
 */
@Service
@Slf4j
public class InfluxDBWriter {

	private final ResettableCountDownLatch latch;
	private final List<String> messages;
	private final List<String> tagFields;

	private String foundation;

	@Autowired
	public InfluxDBWriter(NozzleProperties properties, MetricsDestination destination, InfluxDBSender sender) {
		log.info("Initializing DB Writer with batch size {}", properties.getBatchSize());
		this.messages = Collections.synchronizedList(new ArrayList<>());
		this.latch = new ResettableCountDownLatch(properties.getBatchSize());
		this.tagFields = properties.getTagFields();

		this.foundation = properties.getFoundation();

		new Thread(new InfluxDBBatchListener(latch, messages, sender)).start();
	}

	/**
	 * Convert an envelope into an InfluxDB compatible message. In general, the format is
	 *
	 * <tt>message[,tag=value]* value timestamp</tt>
	 *
	 * Add each message String to a batch list and count down a latch. When the latch reaches 0,
	 * it will write to InfluxDB and reset.
	 *
	 * @param envelope The event from the Firehose
	 */
	@Async
	void writeMessage(Envelope envelope) {
		final StringBuilder messageBuilder = new StringBuilder();

		writeValueMetric(messageBuilder, envelope);
		writeCounterEventTotal(messageBuilder, envelope);

		if (isTaggableField("delta")) {
			writeCounterEventDelta(messageBuilder, envelope);
		}
	}

	private void writeCommonSeriesData(StringBuilder messageBuilder, Envelope envelope, String metricName) {
		messageBuilder.append(envelope.getOrigin()).append('.').append(metricName);
		getTags(envelope).forEach((k, v) -> messageBuilder.append(",").append(k).append("=").append(v));
	}

	private void finishMessage(StringBuilder messageBuilder, Envelope envelope) {
		messageBuilder.append(' ').append(envelope.getTimestamp());

		messages.add(messageBuilder.toString());
		latch.countDown();

		messageBuilder.delete(0, messageBuilder.length());
	}

	private void writeValueMetric(StringBuilder messageBuilder, Envelope envelope) {
		ValueMetric metric = envelope.getValueMetric();

		if (metric != null) {
			writeCommonSeriesData(messageBuilder, envelope, metric.getName());
			messageBuilder.append(",eventType=ValueMetric value=").append(metric.value());
			finishMessage(messageBuilder, envelope);
		}
	}

	private void writeCounterEventTotal(StringBuilder messageBuilder, Envelope envelope) {
		CounterEvent event = envelope.getCounterEvent();

		if (event != null) {
			writeCommonSeriesData(messageBuilder, envelope, event.getName());
			messageBuilder.append(",eventType=CounterEvent,valueType=total value=").append(event.getTotal());
			finishMessage(messageBuilder, envelope);
		}
	}

	private void writeCounterEventDelta(StringBuilder messageBuilder, Envelope envelope) {
		CounterEvent event = envelope.getCounterEvent();

		if (event != null) {
			writeCommonSeriesData(messageBuilder, envelope, event.getName());
			messageBuilder.append(",eventType=CounterEvent,valueType=delta value=").append(event.getDelta());
			finishMessage(messageBuilder, envelope);
		}
	}

	/**
	 * Get all the tags from the Envelope plus any EventType-specific fields into a single Map
	 *
	 * @param envelope the Event
	 * @return the tag map
	 */
	private Map<String, String> getTags(Envelope envelope) {
		final Map<String, String> tags = new HashMap<>();

		if (StringUtils.hasText(foundation)) {
			tags.put("foundation", foundation);
		}

		if (isTaggableField("tags") && !CollectionUtils.isEmpty(envelope.getTags())) {
			envelope.getTags().forEach((k, v) -> {
				if (StringUtils.hasText(v)) {
					tags.put(k, v);
				}
			});
		}


		if (isTaggableField("ip") && StringUtils.hasText(envelope.getIp())) {
			tags.put("ip", envelope.getIp());
		}

		if (isTaggableField("deployment") && StringUtils.hasText(envelope.getDeployment())) {
			tags.put("deployment", envelope.getDeployment());
		}

		if (isTaggableField("job") && StringUtils.hasText(envelope.getJob())) {
			tags.put("job", envelope.getJob());
		}

		if (isTaggableField("index") && StringUtils.hasText(envelope.getIndex())) {
			tags.put("index", envelope.getIndex());
		}

		if (envelope.getValueMetric() != null) {
			if (isTaggableField("unit") && StringUtils.hasText(envelope.getValueMetric().getUnit())) {
				tags.put("unit", envelope.getValueMetric().getUnit());
			}
		}

		return tags;
	}

	private boolean isTaggableField(String field) {
		return StringUtils.hasText(field) && tagFields.contains(field);
	}
}
