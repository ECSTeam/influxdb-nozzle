package com.ecsteam.nozzle.influxdb.util;

import org.cloudfoundry.client.v2.Metadata;
import org.cloudfoundry.client.v2.applications.ApplicationEntity;
import org.cloudfoundry.client.v2.applications.ApplicationResource;
import org.cloudfoundry.client.v2.applications.ApplicationsV2;
import org.cloudfoundry.client.v2.applications.ListApplicationsRequest;
import org.cloudfoundry.client.v2.applications.ListApplicationsResponse;
import org.cloudfoundry.client.v2.organizations.ListOrganizationsRequest;
import org.cloudfoundry.client.v2.organizations.ListOrganizationsResponse;
import org.cloudfoundry.client.v2.organizations.OrganizationEntity;
import org.cloudfoundry.client.v2.organizations.OrganizationResource;
import org.cloudfoundry.client.v2.organizations.Organizations;
import org.cloudfoundry.client.v2.spaces.ListSpacesRequest;
import org.cloudfoundry.client.v2.spaces.ListSpacesResponse;
import org.cloudfoundry.client.v2.spaces.SpaceEntity;
import org.cloudfoundry.client.v2.spaces.SpaceResource;
import org.cloudfoundry.client.v2.spaces.Spaces;
import reactor.core.publisher.Mono;

public class TestCloudFoundryClient extends AbstractClients.TestCloudFoundryClient {
	@Override
	public ApplicationsV2 applicationsV2() {
		return new AbstractClients.TestApplicationsV2() {
			@Override
			public Mono<ListApplicationsResponse> list(ListApplicationsRequest listApplicationsRequest) {
				return Mono.just(ListApplicationsResponse.builder().resource(
						ApplicationResource.builder().metadata(Metadata.builder().id("app1234").build()).entity(ApplicationEntity.builder().name("App 1").spaceId("spaceA").build()).build(),
						ApplicationResource.builder().metadata(Metadata.builder().id("app2341").build()).entity(ApplicationEntity.builder().name("App 2").spaceId("spaceA").build()).build(),
						ApplicationResource.builder().metadata(Metadata.builder().id("app3412").build()).entity(ApplicationEntity.builder().name("App 3").spaceId("spaceB").build()).build(),
						ApplicationResource.builder().metadata(Metadata.builder().id("app4123").build()).entity(ApplicationEntity.builder().name("App 4").spaceId("spaceB").build()).build()
				).build());
			}
		};
	}

	@Override
	public Organizations organizations() {
		return new AbstractClients.TestOrganizations() {
			@Override
			public Mono<ListOrganizationsResponse> list(ListOrganizationsRequest listOrganizationsRequest) {
				return Mono.just(ListOrganizationsResponse.builder().resource(
						OrganizationResource.builder().metadata(Metadata.builder().id("org1").build()).entity(OrganizationEntity.builder().name("Org 1").build()).build(),
						OrganizationResource.builder().metadata(Metadata.builder().id("org2").build()).entity(OrganizationEntity.builder().name("Org 2").build()).build()
				).build());
			}
		};
	}

	@Override
	public Spaces spaces() {
		return new AbstractClients.TestSpaces() {
			@Override
			public Mono<ListSpacesResponse> list(ListSpacesRequest listSpacesRequest) {
				return Mono.just(ListSpacesResponse.builder().resource(
						SpaceResource.builder().metadata(Metadata.builder().id("spaceA").build()).entity(SpaceEntity.builder().name("Space A").organizationId("org1").build()).build(),
						SpaceResource.builder().metadata(Metadata.builder().id("spaceB").build()).entity(SpaceEntity.builder().name("Space B").organizationId("org2").build()).build()
				).build());
			}
		};
	}
}