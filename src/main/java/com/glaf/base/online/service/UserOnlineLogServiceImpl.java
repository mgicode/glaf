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

package com.glaf.base.online.service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.glaf.base.online.domain.UserOnlineLog;
import com.glaf.base.online.mapper.UserOnlineLogMapper;
import com.glaf.base.online.query.UserOnlineLogQuery;
import com.glaf.core.dao.EntityDAO;
import com.glaf.core.id.IdGenerator;
import com.glaf.core.util.DateUtils;

@Service("userOnlineLogService")
@Transactional(readOnly = true)
public class UserOnlineLogServiceImpl implements UserOnlineLogService {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected EntityDAO entityDAO;

	protected IdGenerator idGenerator;

	protected SqlSessionTemplate sqlSessionTemplate;

	protected UserOnlineLogMapper userOnlineLogMapper;

	public UserOnlineLogServiceImpl() {

	}

	public int count(UserOnlineLogQuery query) {
		return userOnlineLogMapper.getUserOnlineLogCount(query);
	}

	public int getLoginCount(String actorId) {
		UserOnlineLogQuery query = new UserOnlineLogQuery();
		query.actorId(actorId);
		query.day(DateUtils.getNowYearMonthDay());
		return userOnlineLogMapper.getUserOnlineLogCount(query);
	}

	public int getUserOnlineLogCountByQueryCriteria(UserOnlineLogQuery query) {
		return userOnlineLogMapper.getUserOnlineLogCount(query);
	}

	public List<UserOnlineLog> getUserOnlineLogs(String actorId) {
		UserOnlineLogQuery query = new UserOnlineLogQuery();
		query.actorId(actorId);
		List<UserOnlineLog> list = this.list(query);
		return list;
	}

	public List<UserOnlineLog> getUserOnlineLogsByQueryCriteria(int start, int pageSize, UserOnlineLogQuery query) {
		RowBounds rowBounds = new RowBounds(start, pageSize);
		List<UserOnlineLog> rows = sqlSessionTemplate.selectList("getUserOnlineLogs", query, rowBounds);
		return rows;
	}

	public List<UserOnlineLog> list(UserOnlineLogQuery query) {
		List<UserOnlineLog> list = userOnlineLogMapper.getUserOnlineLogs(query);
		return list;
	}

	@Transactional
	public void login(UserOnlineLog model) {
		model.setId(idGenerator.nextId());

		if (model.getLoginDate() == null) {
			model.setLoginDate(new Date());
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH) + 1;

		model.setYear(year);
		model.setMonth(month);
		model.setDay(DateUtils.getNowYearMonthDay());

		if (month <= 3) {
			model.setQuarter(1);
		} else if (month > 3 && month <= 6) {
			model.setQuarter(2);
		}
		if (month > 6 && month <= 9) {
			model.setQuarter(3);
		}
		if (month > 9) {
			model.setQuarter(4);
		}

		userOnlineLogMapper.insertUserOnlineLog(model);
	}

	/**
	 * 退出系统
	 * 
	 * @param actorId
	 */
	@Transactional
	public void logout(String actorId) {
		UserOnlineLogQuery query = new UserOnlineLogQuery();
		query.actorId(actorId);
		List<UserOnlineLog> list = this.list(query);
		if (list != null && !list.isEmpty()) {
			UserOnlineLog model = list.get(0);
			model.setLogoutDate(new Date());
			userOnlineLogMapper.updateUserOnlineLogLogoutDate(model);
		}
	}

	@Transactional
	public void saveAll(List<UserOnlineLog> rows) {
		for (UserOnlineLog model : rows) {
			model.setId(idGenerator.nextId());

			if (model.getLoginDate() == null) {
				model.setLoginDate(new Date());
			}

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			int year = calendar.get(Calendar.YEAR);
			int month = calendar.get(Calendar.MONTH) + 1;

			model.setYear(year);
			model.setMonth(month);
			model.setDay(DateUtils.getNowYearMonthDay());

			if (month <= 3) {
				model.setQuarter(1);
			} else if (month > 3 && month <= 6) {
				model.setQuarter(2);
			}
			if (month > 6 && month <= 9) {
				model.setQuarter(3);
			}
			if (month > 9) {
				model.setQuarter(4);
			}

			userOnlineLogMapper.insertUserOnlineLog(model);
		}
	}

	@javax.annotation.Resource
	public void setEntityDAO(EntityDAO entityDAO) {
		this.entityDAO = entityDAO;
	}

	@javax.annotation.Resource
	public void setIdGenerator(IdGenerator idGenerator) {
		this.idGenerator = idGenerator;
	}

	@javax.annotation.Resource
	public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate) {
		this.sqlSessionTemplate = sqlSessionTemplate;
	}

	@javax.annotation.Resource
	public void setUserOnlineLogMapper(UserOnlineLogMapper userOnlineLogMapper) {
		this.userOnlineLogMapper = userOnlineLogMapper;
	}

}
