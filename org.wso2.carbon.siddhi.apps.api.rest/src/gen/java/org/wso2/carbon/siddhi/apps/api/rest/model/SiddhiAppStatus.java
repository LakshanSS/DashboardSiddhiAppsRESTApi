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

package org.wso2.carbon.siddhi.apps.api.rest.model;

/**
 * This class holds the details of number of active and inactive app count.
 */
public class SiddhiAppStatus {
    private int activeAppCount = 0;
    private int inactiveAppCount = 0;
    
    public SiddhiAppStatus(int activeAppCount, int inactiveAppCount) {
        this.activeAppCount = activeAppCount;
        this.inactiveAppCount = inactiveAppCount;
    }
    
    public int getActiveAppCount() {
        return activeAppCount;
    }
    
    public void setActiveAppCount(int activeAppCount) {
        this.activeAppCount = activeAppCount;
    }
    
    public int getInactiveAppCount() {
        return inactiveAppCount;
    }
    
    public void setInactiveAppCount(int inactiveAppCount) {
        this.inactiveAppCount = inactiveAppCount;
    }
}