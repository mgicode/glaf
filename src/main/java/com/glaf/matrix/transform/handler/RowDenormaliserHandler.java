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

package com.glaf.matrix.transform.handler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.glaf.core.context.ContextFactory;
import com.glaf.core.domain.Database;
import com.glaf.core.service.IDatabaseService;
import com.glaf.core.util.DateUtils;
import com.glaf.matrix.data.util.ExecutionUtils;
import com.glaf.matrix.handler.DataExecutionHandler;
import com.glaf.matrix.transform.domain.RowDenormaliser;
import com.glaf.matrix.transform.service.RowDenormaliserService;
import com.glaf.matrix.transform.task.RowDenormaliserTask;
import com.glaf.matrix.util.SysParams;

public class RowDenormaliserHandler implements DataExecutionHandler {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	public void execute(Object id, Map<String, Object> context) {
		IDatabaseService databaseService = ContextFactory.getBean("databaseService");
		RowDenormaliserService rowDenormaliserService = ContextFactory
				.getBean("com.glaf.matrix.transform.service.rowDenormaliserService");
		RowDenormaliser rowDenormaliser = rowDenormaliserService.getRowDenormaliser(id.toString());
		if (rowDenormaliser != null && StringUtils.isNotEmpty(rowDenormaliser.getDatabaseIds())) {
			int errorCount = 0;
			String jobNo = null;
			Database database = null;
			RowDenormaliserTask task = null;
			Map<String, Object> parameter = new HashMap<String, Object>();
			SysParams.putInternalParams(parameter);
			rowDenormaliser.setTransformTime(new Date());
			String runDay = DateUtils.getNowYearMonthDayHour();
			StringTokenizer token = new StringTokenizer(rowDenormaliser.getDatabaseIds(), ",");
			while (token.hasMoreTokens()) {
				String x = token.nextToken();
				if (StringUtils.isNotEmpty(x) && StringUtils.isNumeric(x)) {
					jobNo = "row_denormaliser_" + rowDenormaliser.getId() + "_" + x + "_" + runDay;
					try {
						database = databaseService.getDatabaseById(Long.parseLong(x));
						if (database != null) {
							ExecutionUtils.put(jobNo, new Date());
							task = new RowDenormaliserTask(database.getId(), rowDenormaliser.getId(), jobNo, parameter);
							if (!task.execute()) {
								errorCount++;
							}
						}
					} catch (Exception ex) {
						errorCount++;
						logger.error("row denormaliser error", ex);
					} finally {
						ExecutionUtils.remove(jobNo);
					}
				}
			}

			if (errorCount == 0) {
				rowDenormaliser.setTransformStatus(1);
			} else {
				rowDenormaliser.setTransformStatus(-1);
			}
			rowDenormaliserService.save(rowDenormaliser);
		}
	}

}
