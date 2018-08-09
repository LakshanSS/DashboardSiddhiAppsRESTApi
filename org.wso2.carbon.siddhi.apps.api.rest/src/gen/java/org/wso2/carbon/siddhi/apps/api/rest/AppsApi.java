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

package org.wso2.carbon.siddhi.apps.api.rest;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.siddhi.apps.api.rest.factories.AppsApiServiceFactory;
import org.wso2.carbon.siddhi.apps.api.rest.impl.AppsApiServiceImpl;
import org.wso2.carbon.siddhi.apps.api.rest.model.ModelApiResponse;
import org.wso2.carbon.siddhi.apps.api.rest.model.Query;
import org.wso2.carbon.stream.processor.core.SiddhiAppRuntimeService;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiParam;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

import java.sql.SQLException;

@Component(
        name = "siddhi-apps-service",
        service = Microservice.class,
        immediate = true
)

@Path("/lakshan")
@io.swagger.annotations.Api(description = "The Siddhi Apps API")
@RequestInterceptor(AuthenticationInterceptor.class)
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-11-01T11:26:25.925Z")
public class AppsApi implements Microservice {
    public static final String API_CONTEXT_PATH = "/lakshan";
    private Logger log = LoggerFactory.getLogger(AppsApi.class);
    private  AppsApiService delegate;
    //= new AppsApiServiceImpl();
    //= AppsApiServiceFactory.getSiddhiAppsApi();


    public AppsApi() {
    }

    public AppsApi(AppsApiService dashboardDataProvider) {
        this.delegate = dashboardDataProvider;
    }

    @POST
    @Path("/query")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Submit a Siddhi query and get the result records from a store",
            notes = "", response = ModelApiResponse.class, tags = {"store",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK, query was successfully submitted",
                    response = ModelApiResponse.class),

            @io.swagger.annotations.ApiResponse(code = 405, message = "Invalid input",
                    response = ModelApiResponse
                            .class)})
    public Response query(@ApiParam(value = "Query object which contains the query which returns the store records",
            required = true) Query body)
            throws NotFoundException {
        return delegate.query(body);
    }

    @OPTIONS
    @GET
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Gets currently deployed siddhi apps", response = Object.class, responseContainer = "List", tags={  })
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = Object.class, responseContainer = "List") })
    public Response getSiddhiApps( @Context Request request)
            throws NotFoundException, org.wso2.carbon.siddhi.apps.api.rest.NotFoundException {
        return delegate.getSiddhiApps(request);
    }


    /**
     * Get all the single deployment siddhi apps
     *
     * @param request
     * @return
     * @throws NotFoundException
     * @throws SQLException
     */
    @GET
    @Path("/lakshan/single-deployment-apps")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "List all the single Deployment siddhi applications that are " +
            "deployed in worker nodes.", notes
            = "Lists all siddhi applications.",
            response = void.class, tags = {"Nodes",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getSingleDeploymentSiddhiApps(@Context Request request)
            throws NotFoundException, SQLException {
        return delegate.getSingleDeploymentSiddhiApps();
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when its references are
     * satisfied.
     *
     * @param bundleContext the bundle context instance of this bundle.
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start(BundleContext bundleContext) throws Exception {
        log.debug("Siddhi Apps REST API activated.");
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.debug("Siddhi Apps REST API deactivated.");
    }

    @Reference(
            name = "siddhi.app.runtime.service.reference",
            service = SiddhiAppRuntimeService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSiddhiAppRuntimeService"
    )
    protected void setSiddhiAppRuntimeService(SiddhiAppRuntimeService siddhiAppRuntimeService) {
        SiddhiAppsDataHolder.getInstance().setSiddhiAppRuntimeService(siddhiAppRuntimeService);
    }

    protected void unsetSiddhiAppRuntimeService(SiddhiAppRuntimeService siddhiAppRuntimeService) {
        SiddhiAppsDataHolder.getInstance().setSiddhiAppRuntimeService(null);
    }
}
