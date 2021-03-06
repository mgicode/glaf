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
package com.glaf.core.job;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.glaf.core.domain.util.SysDataLogTableUtils;
import com.glaf.core.util.DateUtils;

public class SysDataLogCreateTableJob extends BaseJob {
	protected final static Log logger = LogFactory.getLog(SysDataLogCreateTableJob.class);

	protected static AtomicLong lastExecuteTime = new AtomicLong(System.currentTimeMillis());

	public void runJob(JobExecutionContext context) throws JobExecutionException {
		if ((System.currentTimeMillis() - lastExecuteTime.get()) < DateUtils.HOUR) {
			return;
		}
		String jobName = context.getJobDetail().getKey().getName();
		logger.info("Executing job: " + jobName + " executing at " + DateUtils.getDateTime(new Date()));
		Date date = DateUtils.getDateAfter(new Date(), 0);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int daysOfMonth = DateUtils.getYearMonthDays(year, month + 1);

		calendar.set(year, month, daysOfMonth);

		int begin = getYearMonthDay(date);
		int end = getYearMonthDay(calendar.getTime());

		for (int i = begin; i <= end; i++) {
			try {
				SysDataLogTableUtils.createTable("SYS_DATA_LOG_" + i);
			} catch (Exception ex) {
				
				logger.error(ex);
			}
		}
		lastExecuteTime.set(System.currentTimeMillis());
	}

	public static int getYearMonthDay(Date date) {
		String returnStr = null;
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
		returnStr = f.format(date);
		return Integer.parseInt(returnStr);
	}

	public static void main(String[] args) {
		Date date = DateUtils.getDateAfter(new Date(), 0);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int daysOfMonth = DateUtils.getYearMonthDays(year, month + 1);

		calendar.set(year, month, daysOfMonth);

		int begin = getYearMonthDay(date);
		int end = getYearMonthDay(calendar.getTime());

		for (int i = begin; i <= end; i++) {
			logger.debug(i);
			try {
				SysDataLogTableUtils.createTable("SYS_DATA_LOG_" + i);
			} catch (Exception ex) {
				
				logger.error(ex);
			}
		}

	}

}
