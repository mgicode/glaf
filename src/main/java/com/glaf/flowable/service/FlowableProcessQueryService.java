/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.glaf.flowable.service;

import java.util.List;
import java.util.Map;

import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricActivityInstanceQuery;
import org.flowable.engine.history.HistoricDetail;
import org.flowable.engine.history.HistoricDetailQuery;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.springframework.transaction.annotation.Transactional;

import com.glaf.flowable.model.ProcessDefinitionInfo;
import com.glaf.flowable.model.ProcessInstanceInfo;

@Transactional(readOnly = true)
public interface FlowableProcessQueryService {

	List<ProcessDefinition> getAllLatestProcessDefinitions();

	HistoricActivityInstance getHistoricActivityInstance(
			String activityInstanceId);

	long getHistoricActivityInstanceCount(Map<String, Object> paramMap);

	long getHistoricActivityInstanceCount(HistoricActivityInstanceQuery query);

	List<HistoricActivityInstance> getHistoricActivityInstances(
			int firstResult, int maxResults, Map<String, Object> paramMap);

	List<HistoricActivityInstance> getHistoricActivityInstances(
			int firstResult, int maxResults, HistoricActivityInstanceQuery query);

	List<HistoricActivityInstance> getHistoricActivityInstances(
			String processInstanceId);

	long getHistoricDetailCount(Map<String, Object> paramMap);

	long getHistoricDetailCount(HistoricDetailQuery query);

	List<HistoricDetail> getHistoricDetails(int firstResult, int maxResults,
			Map<String, Object> paramMap);

	List<HistoricDetail> getHistoricDetails(int firstResult, int maxResults,
			HistoricDetailQuery query);

	List<HistoricDetail> getHistoricDetails(String taskId);

	HistoricProcessInstance getHistoricProcessInstance(String processInstanceId);

	long getHistoricProcessInstanceCount(Map<String, Object> paramMap);

	long getHistoricProcessInstanceCount(HistoricProcessInstanceQuery query);

	List<HistoricProcessInstance> getHistoricProcessInstances(int firstResult,
			int maxResults, Map<String, Object> paramMap);

	List<HistoricProcessInstance> getHistoricProcessInstances(int firstResult,
			int maxResults, HistoricProcessInstanceQuery query);

	ProcessDefinitionInfo getLatestProcessDefinitionByKey(
			String processDefinitionKey);

	List<ProcessDefinition> getProcessDefinitions(int firstResult,
			int maxResults, Map<String, Object> paramMap);

	List<ProcessDefinition> getLatestProcessDefinitions(int firstResult,
			int maxResults, Map<String, Object> paramMap);

	ProcessDefinition getProcessDefinition(String processDefinitionId);

	ProcessDefinition getProcessDefinitionByDeploymentId(String deploymentId);

	ProcessDefinition getProcessDefinitionByProcessInstanceId(
			String processInstanceId);

	long getProcessDefinitionCount(Map<String, Object> paramMap);

	long getProcessDefinitionCount(ProcessDefinitionQuery query);

	ProcessInstance getProcessInstance(String processInstanceId);

	long getProcessInstanceCount(Map<String, Object> paramMap);

	List<ProcessDefinition> getProcessDefinitions(int firstResult,
			int maxResults, ProcessDefinitionQuery query);

	List<ProcessDefinition> getProcessDefinitions(String processDefKey);

	long getProcessInstanceCount(ProcessInstanceQuery query);

	ProcessInstanceInfo getProcessInstanceInfo(String processInstanceId);

	List<ProcessInstance> getProcessInstances(int firstResult, int maxResults,
			Map<String, Object> paramMap);

	List<ProcessInstance> getProcessInstances(int firstResult, int maxResults,
			ProcessInstanceQuery query);

}