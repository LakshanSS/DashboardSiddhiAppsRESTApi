/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.siddhi.apps.api.rest.internal;

/**
 * Rest API service which is used to access service stub for calling another worker.
 */
public class WorkerServiceFactory {
    
    public static WorkerServiceStub getWorkerHttpsClient(String url, String username, String password) {
        return MonitoringDataHolder.getInstance().getClientBuilderService().build(username, password,
                MonitoringDataHolder.getInstance().getStatusDashboardDeploymentConfigs()
                        .getWorkerConnectionConfigurations().getConnectionTimeOut(), MonitoringDataHolder.getInstance()
                        .getStatusDashboardDeploymentConfigs().getWorkerConnectionConfigurations().getReadTimeOut(),
                WorkerServiceStub.class, url);
    }
}
