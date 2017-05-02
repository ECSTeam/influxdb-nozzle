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

package com.ecsteam.nozzle.influxdb.foundation;

import com.ecsteam.nozzle.influxdb.util.TestCloudFoundryClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class AppDataCacheTests {

	@Test
	public void testAppDataCache() {
		TestCloudFoundryClient testCfClient = new TestCloudFoundryClient();

		AppDataCache cache = new AppDataCache(testCfClient);
		cache.refreshCache();

		Map<String, String> data = cache.getAppData("app1234");

		assertEquals("app1234", data.get("applicationId"));
		assertEquals("App 1", data.get("applicationName"));
		assertEquals("spaceA", data.get("spaceId"));
		assertEquals("Space A", data.get("spaceName"));
		assertEquals("org1", data.get("organizationId"));
		assertEquals("Org 1", data.get("organizationName"));
	}

	@Test
	public void testMissingAppDataCache() {
		TestCloudFoundryClient testCfClient = new TestCloudFoundryClient();

		AppDataCache cache = new AppDataCache(testCfClient);
		cache.refreshCache();

		Map<String, String> data = cache.getAppData("badId");
		assertEquals("badId", data.get("applicationId"));
		assertNull(data.get("applicationName"));
		assertNull(data.get("spaceName"));
		assertNull(data.get("organizationName"));
	}
}
