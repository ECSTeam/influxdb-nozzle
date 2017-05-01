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

import com.ecsteam.nozzle.influxdb.config.AppDataCache;
import com.ecsteam.nozzle.influxdb.config.NozzleProperties;
import com.ecsteam.nozzle.influxdb.destination.MetricsDestination;
import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.doppler.ContainerMetric;
import org.cloudfoundry.doppler.CounterEvent;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.HttpStartStop;
import org.cloudfoundry.doppler.ValueMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Captures messages from the Cloud Foundry Firehose and batches them to be sent to InfluxDB
 */
@Service
@Slf4j
public class FirehoseEventSerializer {

	private final ResettableCountDownLatch latch;
	private final List<String> messages;
	private final List<String> tagFields;
	private final AppDataCache appDataCache;

	private String foundation;

	@Autowired
	public FirehoseEventSerializer(NozzleProperties properties, MetricsDestination destination, InfluxDBBatchSender sender, AppDataCache appDataCache) {
		log.info("Initializing DB Writer with batch size {}", properties.getBatchSize());
		this.messages = Collections.synchronizedList(new ArrayList<>());
		this.latch = new ResettableCountDownLatch(properties.getBatchSize());
		this.tagFields = properties.getTagFields();

		this.appDataCache = appDataCache;

		this.foundation = properties.getFoundation();

		new Thread(new BatchedEventListener(latch, messages, sender)).start();
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

		switch (envelope.getEventType()) {
			case VALUE_METRIC:
				writeValueMetric(messageBuilder, envelope);
				break;
			case COUNTER_EVENT:
				writeCounterEvent(messageBuilder, envelope);
				break;
			case CONTAINER_METRIC:
				writeContainerMetric(messageBuilder, envelope);
				break;
			case HTTP_START_STOP:
				writeHttpStartStop(messageBuilder, envelope);
				break;
		}
	}

	private void writeCommonSeriesData(StringBuilder messageBuilder, Envelope envelope,
									   String metricName, Map<String, String> tags) {
		messageBuilder.append(envelope.getOrigin()).append('.').append(metricName);
		tags.forEach((k, v) -> messageBuilder.append(",").append(k).append("=").append(v));
	}

	private void finishMessage(StringBuilder messageBuilder, Envelope envelope) {
		messageBuilder.append(' ').append(envelope.getTimestamp());

		messages.add(messageBuilder.toString());
		latch.countDown();

		messageBuilder.delete(0, messageBuilder.length());
	}

	private void writeContainerMetric(StringBuilder messageBuilder, Envelope envelope) {
		ContainerMetric metric = envelope.getContainerMetric();

		if (metric != null) {
			Map<String, String> tags = getTags(envelope);

			if (metric.getApplicationId() != null) {
				tags.putAll(appDataCache.getAppData(metric.getApplicationId()));
			}

			writeCommonSeriesData(messageBuilder, envelope, "ContainerMetric", tags);

			Map<String, Number> values = new LinkedHashMap<>();
			values.put("instanceIndex", metric.getInstanceIndex());
			values.put("cpuPercentage", metric.getCpuPercentage());
			values.put("diskBytes", metric.getDiskBytes());
			values.put("diskBytesQuota", metric.getDiskBytesQuota());
			values.put("memoryBytes", metric.getMemoryBytes());
			values.put("memoryBytesQuota", metric.getMemoryBytesQuota());

			messageBuilder.append(" ").append(values.entrySet().stream()
					.map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
					.collect(Collectors.joining(",")));

			//values.forEach((k,v) -> messageBuilder.append(" ").append(k).append("=").append(v));
			finishMessage(messageBuilder, envelope);
		}
	}

	private void writeValueMetric(StringBuilder messageBuilder, Envelope envelope) {
		ValueMetric metric = envelope.getValueMetric();

		if (metric != null) {
			Map<String, String> tags = getTags(envelope);
			tags.put("eventType", "ValueMetric");
			writeCommonSeriesData(messageBuilder, envelope, metric.getName(), tags);

			messageBuilder.append(" value=").append(metric.value())
				.append(",unit=").append(metric.getUnit());

			finishMessage(messageBuilder, envelope);
		}
	}

	private void writeCounterEvent(StringBuilder messageBuilder, Envelope envelope) {
		CounterEvent event = envelope.getCounterEvent();

		if (event != null) {
			Map<String, String> tags = getTags(envelope);
			tags.put("eventType", "CounterEvent");

			writeCommonSeriesData(messageBuilder, envelope, event.getName(), tags);
			messageBuilder.append(",eventType=CounterEvent total=").append(event.getTotal())
				.append(",delta=").append(event.getDelta());

			finishMessage(messageBuilder, envelope);
		}
	}

	private void writeHttpStartStop(StringBuilder builder, Envelope envelope) {
		HttpStartStop event = envelope.getHttpStartStop();
		if (event != null) {
			Map<String, String> tags = getTags(envelope);
			tags.put("eventType", "HttpStartStop");

			if (event.getApplicationId() != null) {
				tags.putAll(appDataCache.getAppData(event.getApplicationId().toString()));
			}

			writeCommonSeriesData(builder, envelope, "HttpStartStop", tags);

			Map<String, Object> values = new LinkedHashMap<>();

			if (event.getContentLength() != null) {
				values.put("contentLength", event.getContentLength());
			}

			if (event.getInstanceIndex() != null) {
				values.put("instanceIndex", event.getInstanceIndex());
			}

			if (event.getStartTimestamp() != null) {
				values.put("startTimestamp", event.getStartTimestamp());
			}

			if (event.getStopTimestamp() != null) {
				values.put("stopTimestamp", event.getStopTimestamp());
			}

			if (event.getStatusCode() != null) {
				values.put("statusCode", event.getStatusCode());
			}

			if (!CollectionUtils.isEmpty(event.getForwarded())) {
				values.put("forwarded", event.getForwarded().stream().collect(Collectors.joining(",")));
			}

			if (StringUtils.hasText(event.getInstanceId())) {
				values.put("instanceId", event.getInstanceId());
			}

			if (event.getMethod() != null) {
				values.put("method", event.getMethod().toString());
			}

			if (event.getPeerType() != null) {
				values.put("peerType", event.getPeerType().toString());
			}

			if (StringUtils.hasText(event.getUri())) {
				values.put("uri", event.getUri());
			}

			if (StringUtils.hasText(event.getUserAgent())) {
				values.put("userAgent", event.getUserAgent());
			}

			if (StringUtils.hasText(event.getRemoteAddress())) {
				values.put("remoteAddress", event.getRemoteAddress());
			}

			builder.append(" ").append(values.entrySet().stream()
					.map(entry -> String.format("%s=%s", entry.getKey(), entry.getValue()))
					.collect(Collectors.joining(",")));
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
