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

package com.glaf.base.modules.update;

import java.util.*;

import com.glaf.base.modules.sys.model.SysTree;
import com.glaf.base.modules.sys.service.SysTreeService;
import com.glaf.core.base.TreeModel;
import com.glaf.core.context.ContextFactory;
import com.glaf.core.tree.component.TreeComponent;
import com.glaf.core.tree.component.TreeRepository;
import com.glaf.core.tree.helper.TreeRepositoryBuilder;

public class SysTreeUpdate {

	protected SysTreeService sysTreeService;

	public SysTreeService getSysTreeService() {
		if (sysTreeService == null) {
			sysTreeService = ContextFactory.getBean("sysTreeService");
		}
		return sysTreeService;
	}

	public void setSysTreeService(SysTreeService sysTreeService) {
		this.sysTreeService = sysTreeService;
	}

	public void updateTreeId() {
		List<SysTree> list = getSysTreeService().getAllSysTreeList();
		if (list != null && !list.isEmpty()) {
			TreeRepositoryBuilder builder = new TreeRepositoryBuilder();
			Map<String, SysTree> treeMap = new HashMap<String, SysTree>();
			List<TreeModel> treeModels = new ArrayList<TreeModel>();
			for (SysTree tree : list) {
				treeModels.add(tree);
				treeMap.put(String.valueOf(tree.getId()), tree);
			}
			TreeRepository repository = builder.build(treeModels);
			List<TreeComponent> topTrees = repository.getTopTrees();
			for (TreeComponent parent : topTrees) {
				List<TreeComponent> components = parent.getComponents();
                if(components != null && !components.isEmpty()){
                	
                }
			}
		}
	}

}