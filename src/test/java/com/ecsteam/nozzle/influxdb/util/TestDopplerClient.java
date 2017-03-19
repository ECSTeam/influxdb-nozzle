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

import org.cloudfoundry.doppler.*;
import reactor.core.publisher.Flux;

/**
 * Created by josh on 3/18/17.
 */
public class TestDopplerClient implements DopplerClient {
	@Override
	public Flux<Envelope> containerMetrics(ContainerMetricsRequest containerMetricsRequest) {
		return null;
	}

	@Override
	public Flux<Envelope> firehose(FirehoseRequest firehoseRequest) {
		return null;
	}

	@Override
	public Flux<Envelope> recentLogs(RecentLogsRequest recentLogsRequest) {
		return null;
	}

	@Override
	public Flux<Envelope> stream(StreamRequest streamRequest) {
		return null;
	}
}
