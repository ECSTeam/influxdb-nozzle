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
import com.ecsteam.nozzle.influxdb.nozzle.InfluxDBSender;
import com.ecsteam.nozzle.influxdb.util.TestMetricsDestination;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

/**
 * Created by josh on 3/18/17.
 */
@RunWith(SpringRunner.class)
public class InfluxDBSenderTests {

	private InfluxDBSender sender;

	@Before
	public void before() {
		TestRestTemplate testRestTemplate = new TestRestTemplate(new RestTemplate());

		sender = new InfluxDBSender(testRestTemplate.getRestTemplate(), new NozzleProperties(), new TestMetricsDestination());
	}
}
