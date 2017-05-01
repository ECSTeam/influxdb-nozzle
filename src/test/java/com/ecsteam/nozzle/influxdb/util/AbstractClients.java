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

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.client.v2.applications.*;
import org.cloudfoundry.client.v2.applicationusageevents.ApplicationUsageEvents;
import org.cloudfoundry.client.v2.buildpacks.Buildpacks;
import org.cloudfoundry.client.v2.domains.Domains;
import org.cloudfoundry.client.v2.environmentvariablegroups.EnvironmentVariableGroups;
import org.cloudfoundry.client.v2.events.Events;
import org.cloudfoundry.client.v2.featureflags.FeatureFlags;
import org.cloudfoundry.client.v2.info.Info;
import org.cloudfoundry.client.v2.jobs.Jobs;
import org.cloudfoundry.client.v2.organizationquotadefinitions.OrganizationQuotaDefinitions;
import org.cloudfoundry.client.v2.organizations.*;
import org.cloudfoundry.client.v2.privatedomains.PrivateDomains;
import org.cloudfoundry.client.v2.resourcematch.ResourceMatch;
import org.cloudfoundry.client.v2.routemappings.RouteMappings;
import org.cloudfoundry.client.v2.routes.Routes;
import org.cloudfoundry.client.v2.securitygroups.SecurityGroups;
import org.cloudfoundry.client.v2.servicebindings.ServiceBindingsV2;
import org.cloudfoundry.client.v2.servicebrokers.ServiceBrokers;
import org.cloudfoundry.client.v2.serviceinstances.ServiceInstances;
import org.cloudfoundry.client.v2.servicekeys.ServiceKeys;
import org.cloudfoundry.client.v2.serviceplans.ServicePlans;
import org.cloudfoundry.client.v2.serviceplanvisibilities.ServicePlanVisibilities;
import org.cloudfoundry.client.v2.services.Services;
import org.cloudfoundry.client.v2.serviceusageevents.ServiceUsageEvents;
import org.cloudfoundry.client.v2.shareddomains.SharedDomains;
import org.cloudfoundry.client.v2.spacequotadefinitions.SpaceQuotaDefinitions;
import org.cloudfoundry.client.v2.spaces.*;
import org.cloudfoundry.client.v2.stacks.Stacks;
import org.cloudfoundry.client.v2.userprovidedserviceinstances.UserProvidedServiceInstances;
import org.cloudfoundry.client.v2.users.Users;
import org.cloudfoundry.client.v3.applications.ApplicationsV3;
import org.cloudfoundry.client.v3.droplets.Droplets;
import org.cloudfoundry.client.v3.packages.Packages;
import org.cloudfoundry.client.v3.processes.Processes;
import org.cloudfoundry.client.v3.servicebindings.ServiceBindingsV3;
import org.cloudfoundry.client.v3.tasks.Tasks;
import org.cloudfoundry.doppler.ContainerMetricsRequest;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.doppler.Envelope;
import org.cloudfoundry.doppler.FirehoseRequest;
import org.cloudfoundry.doppler.RecentLogsRequest;
import org.cloudfoundry.doppler.StreamRequest;
import org.cloudfoundry.uaa.UaaClient;
import org.cloudfoundry.uaa.authorizations.Authorizations;
import org.cloudfoundry.uaa.clients.*;
import org.cloudfoundry.uaa.groups.Groups;
import org.cloudfoundry.uaa.identityproviders.IdentityProviders;
import org.cloudfoundry.uaa.identityzones.IdentityZones;
import org.cloudfoundry.uaa.tokens.Tokens;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Used to keep TestConfiguration classes clean
 */
public abstract class AbstractClients {

	public static class TestCloudFoundryClient implements CloudFoundryClient {

		@Override
		public ApplicationUsageEvents applicationUsageEvents() {
			return null;
		}

		@Override
		public ApplicationsV2 applicationsV2() {
			return null;
		}

		@Override
		public ApplicationsV3 applicationsV3() {
			return null;
		}

		@Override
		public Buildpacks buildpacks() {
			return null;
		}

		@Override
		public Domains domains() {
			return null;
		}

		@Override
		public Droplets droplets() {
			return null;
		}

		@Override
		public EnvironmentVariableGroups environmentVariableGroups() {
			return null;
		}

		@Override
		public Events events() {
			return null;
		}

		@Override
		public FeatureFlags featureFlags() {
			return null;
		}

		@Override
		public Info info() {
			return null;
		}

		@Override
		public Jobs jobs() {
			return null;
		}

		@Override
		public OrganizationQuotaDefinitions organizationQuotaDefinitions() {
			return null;
		}

		@Override
		public Organizations organizations() {
			return null;
		}

		@Override
		public Packages packages() {
			return null;
		}

		@Override
		public PrivateDomains privateDomains() {
			return null;
		}

		@Override
		public Processes processes() {
			return null;
		}

		@Override
		public ResourceMatch resourceMatch() {
			return null;
		}

		@Override
		public RouteMappings routeMappings() {
			return null;
		}

		@Override
		public Routes routes() {
			return null;
		}

		@Override
		public SecurityGroups securityGroups() {
			return null;
		}

		@Override
		public ServiceBindingsV2 serviceBindingsV2() {
			return null;
		}

		@Override
		public ServiceBindingsV3 serviceBindingsV3() {
			return null;
		}

		@Override
		public ServiceBrokers serviceBrokers() {
			return null;
		}

		@Override
		public ServiceInstances serviceInstances() {
			return null;
		}

		@Override
		public ServiceKeys serviceKeys() {
			return null;
		}

		@Override
		public ServicePlanVisibilities servicePlanVisibilities() {
			return null;
		}

		@Override
		public ServicePlans servicePlans() {
			return null;
		}

		@Override
		public ServiceUsageEvents serviceUsageEvents() {
			return null;
		}

		@Override
		public Services services() {
			return null;
		}

		@Override
		public SharedDomains sharedDomains() {
			return null;
		}

		@Override
		public SpaceQuotaDefinitions spaceQuotaDefinitions() {
			return null;
		}

		@Override
		public Spaces spaces() {
			return null;
		}

		@Override
		public Stacks stacks() {
			return null;
		}

		@Override
		public Tasks tasks() {
			return null;
		}

		@Override
		public UserProvidedServiceInstances userProvidedServiceInstances() {
			return null;
		}

		@Override
		public Users users() {
			return null;
		}
	}

	public static class TestDopplerClient implements DopplerClient {
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

	public static class TestUaaClient implements UaaClient {

		@Override
		public Authorizations authorizations() {
			return null;
		}

		@Override
		public Clients clients() {
			return null;
		}

		@Override
		public Mono<String> getUsername() {
			return null;
		}

		@Override
		public Groups groups() {
			return null;
		}

		@Override
		public IdentityProviders identityProviders() {
			return null;
		}

		@Override
		public IdentityZones identityZones() {
			return null;
		}

		@Override
		public Tokens tokens() {
			return null;
		}

		@Override
		public org.cloudfoundry.uaa.users.Users users() {
			return null;
		}
	}

	public static class TestApplicationsV2 implements ApplicationsV2 {

		@Override
		public Mono<AssociateApplicationRouteResponse> associateRoute(AssociateApplicationRouteRequest associateApplicationRouteRequest) {
			return null;
		}

		@Override
		public Mono<CopyApplicationResponse> copy(CopyApplicationRequest copyApplicationRequest) {
			return null;
		}

		@Override
		public Mono<CreateApplicationResponse> create(CreateApplicationRequest createApplicationRequest) {
			return null;
		}

		@Override
		public Mono<Void> delete(DeleteApplicationRequest deleteApplicationRequest) {
			return null;
		}

		@Override
		public Flux<byte[]> download(DownloadApplicationRequest downloadApplicationRequest) {
			return null;
		}

		@Override
		public Flux<byte[]> downloadDroplet(DownloadApplicationDropletRequest downloadApplicationDropletRequest) {
			return null;
		}

		@Override
		public Mono<ApplicationEnvironmentResponse> environment(ApplicationEnvironmentRequest applicationEnvironmentRequest) {
			return null;
		}

		@Override
		public Mono<GetApplicationResponse> get(GetApplicationRequest getApplicationRequest) {
			return null;
		}

		@Override
		public Mono<ApplicationInstancesResponse> instances(ApplicationInstancesRequest applicationInstancesRequest) {
			return null;
		}

		@Override
		public Mono<ListApplicationsResponse> list(ListApplicationsRequest listApplicationsRequest) {
			return null;
		}

		@Override
		public Mono<ListApplicationRoutesResponse> listRoutes(ListApplicationRoutesRequest listApplicationRoutesRequest) {
			return null;
		}

		@Override
		public Mono<ListApplicationServiceBindingsResponse> listServiceBindings(ListApplicationServiceBindingsRequest listApplicationServiceBindingsRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeRoute(RemoveApplicationRouteRequest removeApplicationRouteRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeServiceBinding(RemoveApplicationServiceBindingRequest removeApplicationServiceBindingRequest) {
			return null;
		}

		@Override
		public Mono<RestageApplicationResponse> restage(RestageApplicationRequest restageApplicationRequest) {
			return null;
		}

		@Override
		public Mono<ApplicationStatisticsResponse> statistics(ApplicationStatisticsRequest applicationStatisticsRequest) {
			return null;
		}

		@Override
		public Mono<SummaryApplicationResponse> summary(SummaryApplicationRequest summaryApplicationRequest) {
			return null;
		}

		@Override
		public Mono<Void> terminateInstance(TerminateApplicationInstanceRequest terminateApplicationInstanceRequest) {
			return null;
		}

		@Override
		public Mono<UpdateApplicationResponse> update(UpdateApplicationRequest updateApplicationRequest) {
			return null;
		}

		@Override
		public Mono<UploadApplicationResponse> upload(UploadApplicationRequest uploadApplicationRequest) {
			return null;
		}
	}

	public static class TestOrganizations implements Organizations {

		@Override
		public Mono<AssociateOrganizationAuditorResponse> associateAuditor(AssociateOrganizationAuditorRequest associateOrganizationAuditorRequest) {
			return null;
		}

		@Override
		public Mono<AssociateOrganizationAuditorByUsernameResponse> associateAuditorByUsername(AssociateOrganizationAuditorByUsernameRequest associateOrganizationAuditorByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<AssociateOrganizationBillingManagerResponse> associateBillingManager(AssociateOrganizationBillingManagerRequest associateOrganizationBillingManagerRequest) {
			return null;
		}

		@Override
		public Mono<AssociateOrganizationBillingManagerByUsernameResponse> associateBillingManagerByUsername(AssociateOrganizationBillingManagerByUsernameRequest associateOrganizationBillingManagerByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<AssociateOrganizationManagerResponse> associateManager(AssociateOrganizationManagerRequest associateOrganizationManagerRequest) {
			return null;
		}

		@Override
		public Mono<AssociateOrganizationManagerByUsernameResponse> associateManagerByUsername(AssociateOrganizationManagerByUsernameRequest associateOrganizationManagerByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<AssociateOrganizationPrivateDomainResponse> associatePrivateDomain(AssociateOrganizationPrivateDomainRequest associateOrganizationPrivateDomainRequest) {
			return null;
		}

		@Override
		public Mono<AssociateOrganizationUserResponse> associateUser(AssociateOrganizationUserRequest associateOrganizationUserRequest) {
			return null;
		}

		@Override
		public Mono<AssociateOrganizationUserByUsernameResponse> associateUserByUsername(AssociateOrganizationUserByUsernameRequest associateOrganizationUserByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<CreateOrganizationResponse> create(CreateOrganizationRequest createOrganizationRequest) {
			return null;
		}

		@Override
		public Mono<DeleteOrganizationResponse> delete(DeleteOrganizationRequest deleteOrganizationRequest) {
			return null;
		}

		@Override
		public Mono<GetOrganizationResponse> get(GetOrganizationRequest getOrganizationRequest) {
			return null;
		}

		@Override
		public Mono<GetOrganizationInstanceUsageResponse> getInstanceUsage(GetOrganizationInstanceUsageRequest getOrganizationInstanceUsageRequest) {
			return null;
		}

		@Override
		public Mono<GetOrganizationMemoryUsageResponse> getMemoryUsage(GetOrganizationMemoryUsageRequest getOrganizationMemoryUsageRequest) {
			return null;
		}

		@Override
		public Mono<GetOrganizationUserRolesResponse> getUserRoles(GetOrganizationUserRolesRequest getOrganizationUserRolesRequest) {
			return null;
		}

		@Override
		public Mono<ListOrganizationsResponse> list(ListOrganizationsRequest listOrganizationsRequest) {
			return null;
		}

		@Override
		public Mono<ListOrganizationAuditorsResponse> listAuditors(ListOrganizationAuditorsRequest listOrganizationAuditorsRequest) {
			return null;
		}

		@Override
		public Mono<ListOrganizationBillingManagersResponse> listBillingManagers(ListOrganizationBillingManagersRequest listOrganizationBillingManagersRequest) {
			return null;
		}

		@Override
		public Mono<ListOrganizationDomainsResponse> listDomains(ListOrganizationDomainsRequest listOrganizationDomainsRequest) {
			return null;
		}

		@Override
		public Mono<ListOrganizationManagersResponse> listManagers(ListOrganizationManagersRequest listOrganizationManagersRequest) {
			return null;
		}

		@Override
		public Mono<ListOrganizationPrivateDomainsResponse> listPrivateDomains(ListOrganizationPrivateDomainsRequest listOrganizationPrivateDomainsRequest) {
			return null;
		}

		@Override
		public Mono<ListOrganizationServicesResponse> listServices(ListOrganizationServicesRequest listOrganizationServicesRequest) {
			return null;
		}

		@Override
		public Mono<ListOrganizationSpaceQuotaDefinitionsResponse> listSpaceQuotaDefinitions(ListOrganizationSpaceQuotaDefinitionsRequest listOrganizationSpaceQuotaDefinitionsRequest) {
			return null;
		}

		@Override
		public Mono<ListOrganizationSpacesResponse> listSpaces(ListOrganizationSpacesRequest listOrganizationSpacesRequest) {
			return null;
		}

		@Override
		public Mono<ListOrganizationUsersResponse> listUsers(ListOrganizationUsersRequest listOrganizationUsersRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeAuditor(RemoveOrganizationAuditorRequest removeOrganizationAuditorRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeAuditorByUsername(RemoveOrganizationAuditorByUsernameRequest removeOrganizationAuditorByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeBillingManager(RemoveOrganizationBillingManagerRequest removeOrganizationBillingManagerRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeBillingManagerByUsername(RemoveOrganizationBillingManagerByUsernameRequest removeOrganizationBillingManagerByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeManager(RemoveOrganizationManagerRequest removeOrganizationManagerRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeManagerByUsername(RemoveOrganizationManagerByUsernameRequest removeOrganizationManagerByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<Void> removePrivateDomain(RemoveOrganizationPrivateDomainRequest removeOrganizationPrivateDomainRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeUser(RemoveOrganizationUserRequest removeOrganizationUserRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeUserByUsername(RemoveOrganizationUserByUsernameRequest removeOrganizationUserByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<SummaryOrganizationResponse> summary(SummaryOrganizationRequest summaryOrganizationRequest) {
			return null;
		}

		@Override
		public Mono<UpdateOrganizationResponse> update(UpdateOrganizationRequest updateOrganizationRequest) {
			return null;
		}
	}

	public static class TestSpaces implements Spaces {

		@Override
		public Mono<AssociateSpaceAuditorResponse> associateAuditor(AssociateSpaceAuditorRequest associateSpaceAuditorRequest) {
			return null;
		}

		@Override
		public Mono<AssociateSpaceAuditorByUsernameResponse> associateAuditorByUsername(AssociateSpaceAuditorByUsernameRequest associateSpaceAuditorByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<AssociateSpaceDeveloperResponse> associateDeveloper(AssociateSpaceDeveloperRequest associateSpaceDeveloperRequest) {
			return null;
		}

		@Override
		public Mono<AssociateSpaceDeveloperByUsernameResponse> associateDeveloperByUsername(AssociateSpaceDeveloperByUsernameRequest associateSpaceDeveloperByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<AssociateSpaceManagerResponse> associateManager(AssociateSpaceManagerRequest associateSpaceManagerRequest) {
			return null;
		}

		@Override
		public Mono<AssociateSpaceManagerByUsernameResponse> associateManagerByUsername(AssociateSpaceManagerByUsernameRequest associateSpaceManagerByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<AssociateSpaceSecurityGroupResponse> associateSecurityGroup(AssociateSpaceSecurityGroupRequest associateSpaceSecurityGroupRequest) {
			return null;
		}

		@Override
		public Mono<CreateSpaceResponse> create(CreateSpaceRequest createSpaceRequest) {
			return null;
		}

		@Override
		public Mono<DeleteSpaceResponse> delete(DeleteSpaceRequest deleteSpaceRequest) {
			return null;
		}

		@Override
		public Mono<GetSpaceResponse> get(GetSpaceRequest getSpaceRequest) {
			return null;
		}

		@Override
		public Mono<GetSpaceSummaryResponse> getSummary(GetSpaceSummaryRequest getSpaceSummaryRequest) {
			return null;
		}

		@Override
		public Mono<ListSpacesResponse> list(ListSpacesRequest listSpacesRequest) {
			return null;
		}

		@Override
		public Mono<ListSpaceApplicationsResponse> listApplications(ListSpaceApplicationsRequest listSpaceApplicationsRequest) {
			return null;
		}

		@Override
		public Mono<ListSpaceAuditorsResponse> listAuditors(ListSpaceAuditorsRequest listSpaceAuditorsRequest) {
			return null;
		}

		@Override
		public Mono<ListSpaceDevelopersResponse> listDevelopers(ListSpaceDevelopersRequest listSpaceDevelopersRequest) {
			return null;
		}

		@Override
		public Mono<ListSpaceDomainsResponse> listDomains(ListSpaceDomainsRequest listSpaceDomainsRequest) {
			return null;
		}

		@Override
		public Mono<ListSpaceEventsResponse> listEvents(ListSpaceEventsRequest listSpaceEventsRequest) {
			return null;
		}

		@Override
		public Mono<ListSpaceManagersResponse> listManagers(ListSpaceManagersRequest listSpaceManagersRequest) {
			return null;
		}

		@Override
		public Mono<ListSpaceRoutesResponse> listRoutes(ListSpaceRoutesRequest listSpaceRoutesRequest) {
			return null;
		}

		@Override
		public Mono<ListSpaceSecurityGroupsResponse> listSecurityGroups(ListSpaceSecurityGroupsRequest listSpaceSecurityGroupsRequest) {
			return null;
		}

		@Override
		public Mono<ListSpaceServiceInstancesResponse> listServiceInstances(ListSpaceServiceInstancesRequest listSpaceServiceInstancesRequest) {
			return null;
		}

		@Override
		public Mono<ListSpaceServicesResponse> listServices(ListSpaceServicesRequest listSpaceServicesRequest) {
			return null;
		}

		@Override
		public Mono<ListSpaceUserRolesResponse> listUserRoles(ListSpaceUserRolesRequest listSpaceUserRolesRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeAuditor(RemoveSpaceAuditorRequest removeSpaceAuditorRequest) {
			return null;
		}

		@Override
		public Mono<RemoveSpaceAuditorByUsernameResponse> removeAuditorByUsername(RemoveSpaceAuditorByUsernameRequest removeSpaceAuditorByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeDeveloper(RemoveSpaceDeveloperRequest removeSpaceDeveloperRequest) {
			return null;
		}

		@Override
		public Mono<RemoveSpaceDeveloperByUsernameResponse> removeDeveloperByUsername(RemoveSpaceDeveloperByUsernameRequest removeSpaceDeveloperByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeManager(RemoveSpaceManagerRequest removeSpaceManagerRequest) {
			return null;
		}

		@Override
		public Mono<RemoveSpaceManagerByUsernameResponse> removeManagerByUsername(RemoveSpaceManagerByUsernameRequest removeSpaceManagerByUsernameRequest) {
			return null;
		}

		@Override
		public Mono<Void> removeSecurityGroup(RemoveSpaceSecurityGroupRequest removeSpaceSecurityGroupRequest) {
			return null;
		}

		@Override
		public Mono<UpdateSpaceResponse> update(UpdateSpaceRequest updateSpaceRequest) {
			return null;
		}
	}

	public static class TestClients implements Clients {

		@Override
		public Mono<BatchChangeSecretResponse> batchChangeSecret(BatchChangeSecretRequest batchChangeSecretRequest) {
			return null;
		}

		@Override
		public Mono<BatchCreateClientsResponse> batchCreate(BatchCreateClientsRequest batchCreateClientsRequest) {
			return null;
		}

		@Override
		public Mono<BatchDeleteClientsResponse> batchDelete(BatchDeleteClientsRequest batchDeleteClientsRequest) {
			return null;
		}

		@Override
		public Mono<BatchUpdateClientsResponse> batchUpdate(BatchUpdateClientsRequest batchUpdateClientsRequest) {
			return null;
		}

		@Override
		public Mono<ChangeSecretResponse> changeSecret(ChangeSecretRequest changeSecretRequest) {
			return null;
		}

		@Override
		public Mono<CreateClientResponse> create(CreateClientRequest createClientRequest) {
			return null;
		}

		@Override
		public Mono<DeleteClientResponse> delete(DeleteClientRequest deleteClientRequest) {
			return null;
		}

		@Override
		public Mono<GetClientResponse> get(GetClientRequest getClientRequest) {
			return null;
		}

		@Override
		public Mono<GetMetadataResponse> getMetadata(GetMetadataRequest getMetadataRequest) {
			return null;
		}

		@Override
		public Mono<ListClientsResponse> list(ListClientsRequest listClientsRequest) {
			return null;
		}

		@Override
		public Mono<ListMetadatasResponse> listMetadatas(ListMetadatasRequest listMetadatasRequest) {
			return null;
		}

		@Override
		public Mono<MixedActionsResponse> mixedActions(MixedActionsRequest mixedActionsRequest) {
			return null;
		}

		@Override
		public Mono<UpdateClientResponse> update(UpdateClientRequest updateClientRequest) {
			return null;
		}

		@Override
		public Mono<UpdateMetadataResponse> updateMetadata(UpdateMetadataRequest updateMetadataRequest) {
			return null;
		}
	}
}
