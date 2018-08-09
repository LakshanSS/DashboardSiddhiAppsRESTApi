/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.siddhi.apps.api.rest.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import feign.RetryableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.siddhi.apps.api.rest.NotFoundException;
import org.wso2.carbon.siddhi.apps.api.rest.ApiResponseMessage;
import org.wso2.carbon.siddhi.apps.api.rest.SiddhiAppsDataHolder;
import org.wso2.carbon.siddhi.apps.api.rest.AppsApiService;
import org.wso2.carbon.siddhi.apps.api.rest.bean.NodeConfigurationDetails;
import org.wso2.carbon.siddhi.apps.api.rest.bean.ResourceClusterInfo;
import org.wso2.carbon.siddhi.apps.api.rest.bean.SiddhiAppStatus;
import org.wso2.carbon.siddhi.apps.api.rest.bean.SiddhiAppSummaryInfo;
import org.wso2.carbon.siddhi.apps.api.rest.dbhandler.DeploymentConfigs;
import org.wso2.carbon.siddhi.apps.api.rest.dbhandler.StatusDashboardDBHandler;
import org.wso2.carbon.siddhi.apps.api.rest.impl.utils.Constants;
import org.wso2.carbon.siddhi.apps.api.rest.internal.WorkerServiceFactory;
import org.wso2.carbon.siddhi.apps.api.rest.model.ModelApiResponse;
import org.wso2.carbon.siddhi.apps.api.rest.model.Query;
import org.wso2.carbon.siddhi.apps.api.rest.model.Record;
import org.wso2.carbon.siddhi.apps.api.rest.model.ServerHADetails;
import org.wso2.carbon.stream.processor.core.SiddhiAppRuntimeService;
import org.wso2.msf4j.Request;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.event.Event;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.siddhi.apps.api.rest.impl.utils.Constants.PROTOCOL;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-11-01T11:26:25.925Z")
public class AppsApiServiceImpl extends AppsApiService {

    private static StatusDashboardDBHandler dashboardStore;
    private Gson gson = new Gson();
    private DeploymentConfigs dashboardConfigurations;




    private static final Logger log = LoggerFactory.getLogger(AppsApiServiceImpl.class);
    @Override
    public Response query(Query body) throws NotFoundException {
        if (body.getQuery() == null || body.getQuery().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ApiResponseMessage(ApiResponseMessage
                    .ERROR, "Query cannot be empty or null")).build();
        }
        if (body.getAppName() == null || body.getAppName().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ApiResponseMessage(ApiResponseMessage
                    .ERROR, "Siddhi app name cannot be empty or null")).build();
        }

        SiddhiAppRuntimeService siddhiAppRuntimeService =
                SiddhiAppsDataHolder.getInstance().getSiddhiAppRuntimeService();
        Map<String, SiddhiAppRuntime> siddhiAppRuntimes = siddhiAppRuntimeService.getActiveSiddhiAppRuntimes();
        SiddhiAppRuntime siddhiAppRuntime = siddhiAppRuntimes.get(body.getAppName());
        if (siddhiAppRuntime == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage
                    .ERROR, "Cannot find an active SiddhiApp with name: " + body.getAppName())).build();
        } else {
            try {
                Event[] events = siddhiAppRuntime.query(body.getQuery());
                List<Record> records = getRecords(events);
                ModelApiResponse response = new ModelApiResponse();
                response.setRecords(records);
                return Response.ok().entity(response).build();
            } catch (Exception e) {
                log.error("Error while querying for siddhiApp: " + removeCRLFCharacters(body.getAppName()) +
                        ", with query: " + removeCRLFCharacters(body.getQuery()) + " Error: " +
                        removeCRLFCharacters(e.getMessage()), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                                                       "Cannot query: " + e.getMessage())).build();
            }
        }
    }

    @Override
    public Response getSiddhiApps(Request request) throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "lakshan!")).build();
    }

    @Override
    public Response getSingleDeploymentSiddhiApps() throws NotFoundException, SQLException {

            List<NodeConfigurationDetails> registeredWorkers = dashboardStore.selectAllWorkers();
            List<NodeConfigurationDetails> registeredManagers = dashboardStore.getAllManagerConfigDetails();
            List<SiddhiAppSummaryInfo> siddhiAppSummaryInfos = new ArrayList<>();

            List<String> ResourceClusteredWorkerNode = new ArrayList<>();
            if (!registeredManagers.isEmpty()) {
                registeredManagers.parallelStream().forEach(manager -> {
                    try {
                        feign.Response resourceResponse = WorkerServiceFactory.getWorkerHttpsClient(
                                PROTOCOL + generateURLHostPort(manager.getHost(), String.valueOf(manager
                                        .getPort())), this.getUsername(), this.getPassword()).getClusterNodeDetails();
                        if (resourceResponse != null && resourceResponse.status() == 200) {
                            Reader inputStream = resourceResponse.body().asReader();
                            List<ResourceClusterInfo> clusterInfos = gson.fromJson(
                                    inputStream, new TypeToken<List<ResourceClusterInfo>>() {
                                    }.getType());
                            for (ResourceClusterInfo clusterInfo : clusterInfos) {
                                String workerId = generateWorkerKey(clusterInfo.getHttps_host(), clusterInfo
                                        .getHttps_port());
                                ResourceClusteredWorkerNode.add(workerId);
                            }
                        }
                    } catch (RetryableException e) {
                        if (log.isDebugEnabled()) {
                            log.debug(removeCRLFCharacters(manager.getWorkerId()) + " Unnable to reach manager.", e);
                        }
                        log.warn(removeCRLFCharacters(manager.getWorkerId()) + " Unnable to reach manager.");

                    } catch (IOException e) {
                        log.warn("Error occured while getting the response " + e.getMessage());
                    }
                });
            }
            if (!registeredWorkers.isEmpty()) {
                registeredWorkers.parallelStream().forEach(worker -> {
                    ServerHADetails serverHADetails = new ServerHADetails();
                    if (!ResourceClusteredWorkerNode.contains(worker.getWorkerId())) {
                        try {
                            feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL +
                                    generateURLHostPort(worker.getHost(), String.valueOf(
                                            worker.getPort())), getUsername(), getPassword()).getWorker();
                            String responseBody = workerResponse.body().toString();
                            serverHADetails = gson.fromJson(responseBody, ServerHADetails.class);
                            if (serverHADetails.getClusterId().equals(Constants.NON_CLUSTERS_ID)) {
                                feign.Response registeredWorkerSiddhiAppsResponse = WorkerServiceFactory
                                        .getWorkerHttpsClient

                                                (PROTOCOL + generateURLHostPort(worker.getHost(), String.valueOf(
                                                        worker.getPort())), this.getUsername(), this.getPassword())
                                        .getAllAppDetails();
                                if (registeredWorkerSiddhiAppsResponse.status() == 200) {
                                    Reader inputReader = registeredWorkerSiddhiAppsResponse.body().asReader();
                                    List<SiddhiAppStatus> totalApps = gson.fromJson(inputReader, new
                                            TypeToken<List<SiddhiAppStatus>>() {
                                            }.getType());

                                    for (SiddhiAppStatus siddhiapp : totalApps) {
                                        SiddhiAppSummaryInfo siddhiAppSummaryInfo = new SiddhiAppSummaryInfo();
                                        siddhiAppSummaryInfo.setAppName(siddhiapp.getAppName());
                                        siddhiAppSummaryInfo.setStatus(siddhiapp.getStatus());
                                        siddhiAppSummaryInfo.setLastUpdate(siddhiapp.getTimeAgo());
                                        siddhiAppSummaryInfo.setStatEnabled(siddhiapp.isStatEnabled());
                                        siddhiAppSummaryInfo.setDeployedNodeType("Worker");
                                        siddhiAppSummaryInfo.setDeployedNodeHost(worker.getHost());
                                        siddhiAppSummaryInfo.setDeployedNodePort(String.valueOf(worker.getPort()));
                                        siddhiAppSummaryInfos.add(siddhiAppSummaryInfo);
                                    }
                                }
                            }
                        } catch (RetryableException ex) {
                            if (log.isDebugEnabled()) {
                                log.debug(removeCRLFCharacters(worker.getWorkerId()) + " Unnable to reach manager" +
                                        ".", ex);
                            }
                            log.warn(removeCRLFCharacters(worker.getWorkerId()) + " Unnable to reach manager.");
                        } catch (IOException e) {
                            log.error("error occurred while retrieving response ");
                        }
                    }
                });
            }
            return Response.ok().entity(siddhiAppSummaryInfos).build();

    }

    private List<Record> getRecords(Event[] events) {
        List<Record> records = new ArrayList<>();
        if (events != null) {
            for (Event event : events) {
                Record record = new Record();
                record.addAll(Arrays.asList(event.getData()));
                records.add(record);
            }
        }
        return records;
    }

    private static String removeCRLFCharacters(String str) {
        if (str != null) {
            str = str.replace('\n', '_').replace('\r', '_');
        }
        return str;
    }
    private String generateURLHostPort(String host, String port) {

        return host + Constants.URL_HOST_PORT_SEPERATOR + port;
    }

    /**
     * Get worker asscess username.
     *
     * @return
     */

    private String getUsername() {

        return dashboardConfigurations.getUsername();
    }

    /**
     * GetGet worker asscess password.
     *
     * @return
     */
    private String getPassword() {

        return dashboardConfigurations.getPassword();
    }

    private String generateWorkerKey(String host, String port) {

        return host + Constants.WORKER_KEY_GENERATOR + port;
    }
}
