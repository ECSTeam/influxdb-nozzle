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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.ListApplicationsRequest;
import org.cloudfoundry.client.v2.organizations.ListOrganizationsRequest;
import org.cloudfoundry.client.v2.spaces.ListSpacesRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RequiredArgsConstructor
public class AppDataCache {

	private static final long CACHE_REFRESH = 1800L; // 30 min
	private static final Pattern PAGE_PARSER = Pattern.compile("[?&]page=(\\d+)");

	private final CloudFoundryClient cfClient;

	private final Map<String, String> appIdToNameMap = new HashMap<>();
	private final Map<String, String> spaceIdToNameMap = new HashMap<>();
	private final Map<String, String> orgIdToNameMap = new HashMap<>();

	private final Map<String, String> appToSpaceMap = new HashMap<>();
	private final Map<String, String> spaceToOrgMap = new HashMap<>();

	@Scheduled(fixedDelay = CACHE_REFRESH)
	public void refreshCache() {
		log.info("Refresh App Data Cache");

		if (cfClient != null) {
			getApps(1);
			getSpaces(1);
			getOrgs(1);
		}
	}

	private void getApps(int page) {
		cfClient.applicationsV2()
				.list(ListApplicationsRequest.builder()
						.page(page)
						.resultsPerPage(100)
						.build())
				.doOnError(Throwable::printStackTrace)
				.subscribe(response -> {
					response.getResources().forEach(app -> {
						appIdToNameMap.put(app.getMetadata().getId(), app.getEntity().getName());
						appToSpaceMap.put(app.getMetadata().getId(), app.getEntity().getSpaceId());
					});

					if (StringUtils.isEmpty(response.getNextUrl())) {
						Matcher matcher = PAGE_PARSER.matcher(response.getNextUrl());
						String pageNumber = matcher.group(1);

						getApps(Integer.valueOf(pageNumber));
					}
				});
	}

	private void getSpaces(int page) {
		cfClient.spaces()
				.list(ListSpacesRequest.builder()
						.page(page)
						.resultsPerPage(100)
						.build())
				.doOnError(Throwable::printStackTrace)
				.subscribe(response -> {
					response.getResources().forEach(space -> {
						spaceIdToNameMap.put(space.getMetadata().getId(), space.getEntity().getName());
						spaceToOrgMap.put(space.getMetadata().getId(), space.getEntity().getOrganizationId());
					});

					if (StringUtils.isEmpty(response.getNextUrl())) {
						Matcher matcher = PAGE_PARSER.matcher(response.getNextUrl());
						String pageNumber = matcher.group(1);

						getSpaces(Integer.valueOf(pageNumber));
					}
				});
	}

	private void getOrgs(int page) {
		cfClient.organizations()
				.list(ListOrganizationsRequest.builder()
						.page(page)
						.resultsPerPage(100)
						.build())
				.doOnError(Throwable::printStackTrace)
				.subscribe(response -> {
					response.getResources()
							.forEach(org -> orgIdToNameMap.put(org.getMetadata().getId(), org.getEntity().getName()));

					if (StringUtils.isEmpty(response.getNextUrl())) {
						Matcher matcher = PAGE_PARSER.matcher(response.getNextUrl());
						String pageNumber = matcher.group(1);

						getOrgs(Integer.valueOf(pageNumber));
					}
				});
	}

	public Map<String, String> getAppData(String applicationId) {
		String spaceId = appToSpaceMap.get(applicationId);
		String orgId = spaceToOrgMap.get(spaceId);

		Map<String, String> map = new HashMap<>();
		map.put("applicationId", applicationId);
		map.put("applicationName", appIdToNameMap.get(applicationId));
		map.put("spaceId", spaceId);
		map.put("spaceName", spaceIdToNameMap.get(spaceId));
		map.put("organizationId", orgId);
		map.put("organizationName", orgIdToNameMap.get(orgId));

		return map;
	}
}
