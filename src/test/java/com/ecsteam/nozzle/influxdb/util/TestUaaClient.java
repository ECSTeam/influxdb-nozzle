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

import org.cloudfoundry.uaa.clients.Clients;
import org.cloudfoundry.uaa.clients.CreateClientRequest;
import org.cloudfoundry.uaa.clients.CreateClientResponse;
import org.cloudfoundry.uaa.clients.GetClientRequest;
import org.cloudfoundry.uaa.clients.GetClientResponse;
import org.cloudfoundry.uaa.clients.UpdateClientRequest;
import org.cloudfoundry.uaa.clients.UpdateClientResponse;
import reactor.core.publisher.Mono;

public class TestUaaClient extends AbstractClients.TestUaaClient {
	@Override
	public Clients clients() {
		return new AbstractClients.TestClients() {
			@Override
			public Mono<GetClientResponse> get(GetClientRequest getClientRequest) {
				return Mono.just(GetClientResponse.builder().clientId("test-client-id").build());
			}

			@Override
			public Mono<CreateClientResponse> create(CreateClientRequest createClientRequest) {
				return Mono.just(CreateClientResponse.builder().clientId("1234").build());
			}

			@Override
			public Mono<UpdateClientResponse> update(UpdateClientRequest updateClientRequest) {
				return Mono.just(UpdateClientResponse.builder().clientId("1234").build());
			}
		};
	}
}
