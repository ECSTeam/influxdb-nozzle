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

package com.ecsteam.nozzle.influxdb.config;

import com.ecsteam.nozzle.influxdb.foundation.AppDataCache;
import com.ecsteam.nozzle.influxdb.nozzle.FirehoseAuthenticationManager;
import com.ecsteam.nozzle.influxdb.nozzle.FirehoseReader;
import com.ecsteam.nozzle.influxdb.nozzle.FirehoseEventSerializer;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.ClientCredentialsGrantTokenProvider;
import org.cloudfoundry.reactor.uaa.ReactorUaaClient;
import org.cloudfoundry.uaa.UaaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
@EnableConfigurationProperties(NozzleProperties.class)
public class FirehoseConfig {
	private DefaultConnectionContext connectionContext(String apiHost, Boolean skipSslValidation) {
		return DefaultConnectionContext.builder()
				.apiHost(apiHost)
				.skipSslValidation(skipSslValidation)
				.build();
	}

	private TokenProvider tokenProvider(String clientId, String clientSecret) {
		return ClientCredentialsGrantTokenProvider.builder()
				.clientId(clientId)
				.clientSecret(clientSecret)
				.build();
	}

	@Bean
	@Autowired
	public UaaClient uaaClient(NozzleProperties properties) {
		if (StringUtils.hasText(properties.getAdminClientId()) &&
				StringUtils.hasText(properties.getAdminClientSecret())) {

			return ReactorUaaClient.builder()
					.connectionContext(connectionContext(getApiHost(properties), properties.isSkipSslValidation()))
					.tokenProvider(tokenProvider(properties.getAdminClientId(), properties.getAdminClientSecret())).build();
		}

		return null;
	}

	@Bean
	@Autowired
	public DopplerClient dopplerClient(NozzleProperties properties) {
		return ReactorDopplerClient.builder()
				.connectionContext(connectionContext(getApiHost(properties), properties.isSkipSslValidation()))
				.tokenProvider(tokenProvider(properties.getClientId(), properties.getClientSecret()))
				.build();
	}

	@Bean
	@Autowired
	public CloudFoundryClient cfClient(NozzleProperties properties) {
		return ReactorCloudFoundryClient.builder()
				.connectionContext(connectionContext(getApiHost(properties), properties.isSkipSslValidation()))
				.tokenProvider(tokenProvider(properties.getClientId(), properties.getClientSecret()))
				.build();
	}

	@Bean
	@Autowired
	FirehoseAuthenticationManager authManager(UaaClient uaaClient, NozzleProperties properties) {
		return new FirehoseAuthenticationManager(uaaClient, properties);
	}

	@Bean
	@Autowired
	FirehoseReader firehoseReader(DopplerClient dopplerClient, NozzleProperties properties, FirehoseEventSerializer writer) {
		return new FirehoseReader(dopplerClient, properties, writer);
	}

	@Bean
	@Autowired
	AppDataCache appDataCache(CloudFoundryClient cfClient) {
		return new AppDataCache(cfClient);
	}

	private String getApiHost(NozzleProperties properties) {
		String apiHost = properties.getApiHost();

		// in a tile context, this may get passed as a full URL, but we just need the hostname
		try {
			URL url = new URL(apiHost);
			apiHost = url.getHost();
		} catch (MalformedURLException e) {
			// this will happen if passed directly as "api.{SYSTEM_DOMAIN}"
		}

		return apiHost;
	}
}
