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
import com.ecsteam.nozzle.influxdb.destination.MetricsDestination;
import com.ecsteam.nozzle.influxdb.foundation.AppDataCache;
import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.doppler.ContainerMetric;
import org.cloudfoundry.doppler.CounterEvent;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.HttpStartStop;
import org.cloudfoundry.doppler.ValueMetric;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.CounterService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	private final CounterService counterService;

	private String foundation;

	@Autowired
	public FirehoseEventSerializer(NozzleProperties properties, MetricsDestination destination, InfluxDBBatchSender sender, AppDataCache appDataCache, CounterService counterService) {
		log.info("Initializing DB Writer with batch size {}", properties.getBatchSize());
		this.messages = Collections.synchronizedList(new ArrayList<>());
		this.latch = new ResettableCountDownLatch(properties.getBatchSize());
		this.tagFields = properties.getTagFields();

		this.appDataCache = appDataCache;

		this.foundation = properties.getFoundation();

		this.counterService = counterService;

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
		this.counterService.increment("events.received." + envelope.getEventType().toString());

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
			tags.put("eventType", "ContainerMetric");

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

			List<String> fields = values.entrySet().stream().map((e) -> writeFieldValue(e.getKey(), e.getValue())).filter(StringUtils::hasText).collect(Collectors.toList());
			messageBuilder.append(" ").append(fields.stream().collect(Collectors.joining(",")));

			finishMessage(messageBuilder, envelope);
		}
	}

	private void writeValueMetric(StringBuilder messageBuilder, Envelope envelope) {
		ValueMetric metric = envelope.getValueMetric();

		if (metric != null) {
			Map<String, String> tags = getTags(envelope);
			tags.put("eventType", "ValueMetric");
			writeCommonSeriesData(messageBuilder, envelope, metric.getName(), tags);

			List<String> fields = Arrays.asList(writeFieldValue("value", metric.value()),
					writeFieldValue("unit", metric.getUnit()));

			messageBuilder.append(" ").append(fields.stream().filter(StringUtils::hasText).collect(Collectors.joining(",")));

			finishMessage(messageBuilder, envelope);
		}
	}

	private void writeCounterEvent(StringBuilder messageBuilder, Envelope envelope) {
		CounterEvent event = envelope.getCounterEvent();

		if (event != null) {
			Map<String, String> tags = getTags(envelope);
			tags.put("eventType", "CounterEvent");

			writeCommonSeriesData(messageBuilder, envelope, event.getName(), tags);

			List<String> fields = Arrays.asList(writeFieldValue("total", event.getTotal()),
					writeFieldValue("delta", event.getDelta()));

			messageBuilder.append(" ").append(fields.stream().filter(StringUtils::hasText).collect(Collectors.joining(",")));

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

			List<String> fields = new ArrayList<>();

			fields.add(writeFieldValue("contentLength", event.getContentLength()));
			fields.add(writeFieldValue("instanceIndex", event.getInstanceIndex()));
			fields.add(writeFieldValue("startTimestamp", event.getStartTimestamp()));
			fields.add(writeFieldValue("stopTimestamp", event.getStopTimestamp()));
			fields.add(writeFieldValue("statusCode", event.getStatusCode()));
			fields.add(writeFieldValue("instanceId", event.getInstanceId()));
			fields.add(writeFieldValue("method", event.getMethod().toString()));
			fields.add(writeFieldValue("peerType", event.getPeerType().toString()));
			fields.add(writeFieldValue("uri", event.getUri()));
			fields.add(writeFieldValue("userAgent", event.getUserAgent()));
			fields.add(writeFieldValue("remoteAddress", event.getRemoteAddress()));
			if (!CollectionUtils.isEmpty(event.getForwarded())) {
				fields.add(writeFieldValue("forwarded", event.getForwarded().stream().collect(Collectors.joining(","))));
			}

			builder.append(" ").append(fields.stream().filter(StringUtils::hasText).collect(Collectors.joining(",")));

			finishMessage(builder, envelope);
		}
	}

	private String writeFieldValue(String fieldName, Object fieldValue) {
		StringBuilder buffer = new StringBuilder();

		if (fieldValue != null && StringUtils.hasText(fieldName)) {
			if (fieldValue instanceof String) {
				if (StringUtils.hasText(fieldValue.toString().trim())) {
					buffer.append(fieldName).append("=\"").append(fieldValue).append('"');
				}
			} else {
				buffer.append(fieldName).append('=').append(fieldValue);
			}

			return buffer.toString();
		}

		return null;
	}

	/**
	 * Get all the tags from the Envelope plus any EventType-specific fields into a single Map
	 *
	 * @param envelope the Event
	 * @return the tag map
	 */
	private Map<String, String> getTags(Envelope envelope) {
		final Map<String, String> tags = new LinkedHashMap<>();

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

		return tags;
	}

	private boolean isTaggableField(String field) {
		return StringUtils.hasText(field) && tagFields.contains(field);
	}
}
