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

package com.ecsteam.nozzle.influxdb;

import com.ecsteam.nozzle.influxdb.config.NozzleProperties;
import com.ecsteam.nozzle.influxdb.nozzle.FirehoseReader;
import com.ecsteam.nozzle.influxdb.util.TestCloudFoundryClient;
import com.ecsteam.nozzle.influxdb.util.TestDopplerClient;
import com.ecsteam.nozzle.influxdb.util.TestUaaClient;
import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.uaa.UaaClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.CountDownLatch;

@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class FirehoseReaderTests {

	@Autowired
	private FirehoseReader reader;


	@Test
	public void testRead() throws Exception {
		Thread failSafe = new Thread(() -> {
			try {
				Thread.sleep(15_000L);
				Assert.fail("We did not receive all the events in time!");
			} catch (InterruptedException e) {
				log.info("Firehose finished in time!");
			}
		});

		failSafe.start();

		NozzleTestConfiguration.counter.await();

		failSafe.interrupt();
	}

	@TestConfiguration
	static class CloudFoundryClientTestConfiguration {

		@Bean
		public CloudFoundryClient cfClient() {
			return new TestCloudFoundryClient();
		}

		@Bean
		public DopplerClient dopplerClient() {
			return new TestDopplerClient();
		}

		@Bean
		public UaaClient uaaClient() {
			return new TestUaaClient();
		}

		@Bean
		public NozzleProperties properties() {
			NozzleProperties properties = new NozzleProperties();

			properties.setAdminClientId("admin");
			properties.setAdminClientSecret("adminsecret");
			properties.setApiHost("localhost");
			properties.setClientId("abcd");
			properties.setClientSecret("1234");

			return properties;
		}
	}

	@TestConfiguration
	public static class NozzleTestConfiguration {
		static CountDownLatch counter = new CountDownLatch(1);

		@Bean
		public Runnable onComplete() {
			return () -> {
				counter.countDown();
			};
		}
	}
}
