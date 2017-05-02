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

import lombok.Getter;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class TestInterceptor implements ClientHttpRequestInterceptor {
	@NonNull
	private final AtomicInteger countDown;

	@Getter
	private final AtomicInteger counter = new AtomicInteger(0);

	@NonNull
	private final Consumer<String> testMethod;

	private static final ClientHttpResponse SUCCESS = new ClientHttpResponse() {
		@Override
		public HttpStatus getStatusCode() throws IOException {
			return HttpStatus.NO_CONTENT;
		}

		@Override
		public int getRawStatusCode() throws IOException {
			return 204;
		}

		@Override
		public String getStatusText() throws IOException {
			return "No Content";
		}

		@Override
		public void close() {

		}

		@Override
		public InputStream getBody() throws IOException {
			return new ByteArrayInputStream(new byte[0]);
		}

		@Override
		public HttpHeaders getHeaders() {
			return null;
		}
	};

	public TestInterceptor(int numRetries, Consumer<String> testMethod) {
		this.countDown = new AtomicInteger(numRetries);
		this.testMethod = testMethod;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
		if (countDown.getAndDecrement() > 0) {
			counter.incrementAndGet();
			throw new IOException();
		}

		String bodyString = new String(body, "UTF-8");

		testMethod.accept(bodyString);

		return SUCCESS;
	}
}
