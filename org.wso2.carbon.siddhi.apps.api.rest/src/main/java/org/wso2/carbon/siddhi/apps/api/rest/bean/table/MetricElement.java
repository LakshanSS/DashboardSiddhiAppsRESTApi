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

package org.wso2.carbon.siddhi.apps.api.rest.bean.table;

import org.apache.commons.lang3.text.WordUtils;

/**
 * Siddhi app Component Bean Class.
 */
public class MetricElement {
    private String type;
    private Attribute attribute = new Attribute();
    
    public MetricElement() {
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = WordUtils.capitalize(type);
    }
    
    public Attribute getAttribute() {
        return attribute;
    }
    
    public void addAttributes(Attribute attribute) {
        this.attribute = attribute;
    }
    
    public void setAttributes(Attribute attributes) {
        this.attribute = attributes;
    }
}
