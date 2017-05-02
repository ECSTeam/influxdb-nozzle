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
import com.ecsteam.nozzle.influxdb.util.TestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Slf4j
@RunWith(SpringRunner.class)
public class InfluxDBBatchSenderTests {

	private static final List<String> SAMPLE_BATCH = Arrays.asList("test line 1", "test line 2", "asdf , asdf , asdf", "foo=bar");

	@Test
	public void testSuccessfulSend() throws Exception {
		final CountDownLatch counter = new CountDownLatch(1);

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestNoRetryConfiguration.class);

		InfluxDBBatchSender bean = context.getBean(InfluxDBBatchSender.class);
		assertTrue(AopUtils.isAopProxy(bean));

		RestTemplate template = new RestTemplate();

		final AtomicInteger batchesSent = new AtomicInteger(0);
		final List<String> errorMessages = new ArrayList<>();
		template.setInterceptors(Collections.singletonList(new TestInterceptor(0, (body) -> {
			try {
				assertEquals("HTTP Body does not match", SAMPLE_BATCH.stream().collect(Collectors.joining("\n")), body);
			} catch (AssertionError e) {
				errorMessages.add(e.getMessage());
			}

			batchesSent.incrementAndGet();
			counter.countDown();
		})));

		bean.setHttpClient(template);

		bean.sendBatch(SAMPLE_BATCH);
		counter.await();

		assertEquals("Custom RestTemplate was not called properly", 1, batchesSent.get());
		assertEquals("Errors occured in the send", "", errorMessages.stream().collect(Collectors.joining("\n")));
	}

	@Test
	public void testSendAfterRetries() throws Exception {
		final CountDownLatch counter = new CountDownLatch(1);

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestNoRetryConfiguration.class);

		InfluxDBBatchSender bean = context.getBean(InfluxDBBatchSender.class);
		assertTrue(AopUtils.isAopProxy(bean));

		RestTemplate template = new RestTemplate();

		final AtomicInteger batchesSent = new AtomicInteger(0);
		final List<String> errorMessages = new ArrayList<>();

		TestInterceptor interceptor = new TestInterceptor(5, (body -> {
			try {
				assertEquals("HTTP Body does not match", SAMPLE_BATCH.stream().collect(Collectors.joining("\n")), body);
			} catch (AssertionError e) {
				errorMessages.add(e.getMessage());
			}

			batchesSent.incrementAndGet();
			counter.countDown();
		}));

		template.setInterceptors(Collections.singletonList(interceptor));

		bean.setHttpClient(template);

		bean.sendBatch(SAMPLE_BATCH);
		counter.await();

		assertEquals("Custom RestTemplate was not called properly", 1, batchesSent.get());
		assertEquals("Errors occured in the send", "", errorMessages.stream().collect(Collectors.joining("\n")));
		assertEquals("Didn't retry enough times", 5, interceptor.getCounter().get());
	}

	@Test
	public void testFailedSend() throws Exception {
		final CountDownLatch counter = new CountDownLatch(1);

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestNoRetryConfiguration.class);

		InfluxDBBatchSender bean = context.getBean(InfluxDBBatchSender.class);
		assertTrue(AopUtils.isAopProxy(bean));

		RestTemplate template = new RestTemplate();

		final List<String> errorMessages = new ArrayList<>();
		TestInterceptor interceptor = new TestInterceptor(11, (body -> {
			fail("Should be unreachable");
		}));

		template.setInterceptors(Collections.singletonList(interceptor));

		bean.setHttpClient(template);

		final AtomicInteger numRetries = new AtomicInteger(0);
		bean.setRecoverCallback((recoveryContext) -> {
			numRetries.set(recoveryContext.getRetryCount());
			counter.countDown();
		});

		bean.sendBatch(SAMPLE_BATCH);
		counter.await();

		assertEquals("Didn't retry enough times", 10, numRetries.get());
		assertEquals("Interceptor was wrong", interceptor.getCounter().get(), numRetries.get());
	}

	@Configuration
	@EnableAsync
	@EnableRetry
	protected static class TestNoRetryConfiguration {

		@Bean
		public InfluxDBBatchSender batchSender() {
			NozzleProperties properties = new NozzleProperties();
			properties.setBackoffPolicy(BackoffPolicy.linear);
			properties.setMinBackoff(10);
			properties.setMaxBackoff(10);

			return new InfluxDBBatchSender(properties, () -> "http://localhost");
		}

		@Bean
		public TaskExecutor taskExecutor() {
			ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
			executor.setMaxPoolSize(3);
			executor.setCorePoolSize(2);

			return executor;
		}
	}
}
