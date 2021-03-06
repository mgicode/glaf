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

package com.glaf.matrix.transform.web.springmvc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.glaf.core.config.DatabaseConnectionConfig;
import com.glaf.core.config.ViewProperties;
import com.glaf.core.domain.ColumnDefinition;
import com.glaf.core.domain.Database;
import com.glaf.core.factory.TableFactory;
import com.glaf.core.identity.User;
import com.glaf.core.security.LoginContext;
import com.glaf.core.service.IDatabaseService;
import com.glaf.core.util.DBUtils;
import com.glaf.core.util.DateUtils;
import com.glaf.core.util.Paging;
import com.glaf.core.util.ParamUtils;
import com.glaf.core.util.RequestUtils;
import com.glaf.core.util.ResponseUtils;
import com.glaf.core.util.StringTools;
import com.glaf.core.util.Tools;
import com.glaf.matrix.data.util.ExceptionUtils;
import com.glaf.matrix.transform.domain.RowDenormaliser;
import com.glaf.matrix.transform.query.RowDenormaliserQuery;
import com.glaf.matrix.transform.service.RowDenormaliserService;
import com.glaf.matrix.transform.task.RowDenormaliserTask;
import com.glaf.matrix.util.SysParams;

/**
 * 
 * SpringMVC控制器
 *
 */

@Controller("/sys/rowDenormaliser")
@RequestMapping("/sys/rowDenormaliser")
public class RowDenormaliserController {
	protected static final Log logger = LogFactory.getLog(RowDenormaliserController.class);

	protected IDatabaseService databaseService;

	protected RowDenormaliserService rowDenormaliserService;

	public RowDenormaliserController() {

	}

	@RequestMapping("/chooseAaggregateColumns")
	public ModelAndView chooseAaggregateColumns(HttpServletRequest request) {
		RequestUtils.setRequestParameterToAttribute(request);
		List<Long> databaseIds = StringTools.splitToLong(request.getParameter("databaseIds"));
		if (databaseIds != null && !databaseIds.isEmpty()) {
			long databaseId = databaseIds.get(0);
			String tableName = request.getParameter("tableName");
			logger.debug("tableName:" + tableName);
			Database database = databaseService.getDatabaseById(databaseId);
			List<ColumnDefinition> columns = null;
			if (database != null) {
				logger.debug("database:" + database.getName());
				columns = DBUtils.getColumnDefinitions(database.getName(), tableName);
			} else {
				columns = DBUtils.getColumnDefinitions("default", tableName);
			}

			logger.debug("columns:" + columns);

			RowDenormaliser rowDenormaliser = rowDenormaliserService
					.getRowDenormaliser(RequestUtils.getString(request, "transformId"));
			if (rowDenormaliser != null) {
				request.setAttribute("rowDenormaliser", rowDenormaliser);
			}

			if (columns != null && !columns.isEmpty()) {
				List<String> selected = new ArrayList<String>();
				List<ColumnDefinition> unselected = new ArrayList<ColumnDefinition>();
				if (rowDenormaliser != null && StringUtils.isNotEmpty(rowDenormaliser.getAggregateColumns())) {
					List<String> cols = StringTools.split(rowDenormaliser.getAggregateColumns());
					for (ColumnDefinition col : columns) {
						if (cols.contains(col.getColumnName()) || cols.contains(col.getColumnName().toLowerCase())
								|| cols.contains(col.getColumnName().toUpperCase())) {
							selected.add(col.getColumnName().toLowerCase());
						} else {
							unselected.add(col);
						}
					}
					request.setAttribute("selected", selected);
					request.setAttribute("unselected", columns);
				} else {
					request.setAttribute("selected", selected);
					request.setAttribute("unselected", columns);
				}
				StringBuffer bufferx = new StringBuffer();
				StringBuffer buffery = new StringBuffer();

				if (columns != null && columns.size() > 0) {
					for (int j = 0; j < columns.size(); j++) {
						ColumnDefinition u = (ColumnDefinition) columns.get(j);
						if (selected != null && selected.contains(u.getColumnName())) {
							buffery.append("\n<option value=\"").append(u.getColumnName()).append("\">")
									.append(u.getColumnName()).append(" [").append(u.getColumnName()).append("]")
									.append("</option>");
						} else {
							bufferx.append("\n<option value=\"").append(u.getColumnName()).append("\">")
									.append(u.getColumnName()).append(" [").append(u.getColumnName()).append("]")
									.append("</option>");
						}
					}
				}
				request.setAttribute("bufferx", bufferx.toString());
				request.setAttribute("buffery", buffery.toString());
			}
		}

		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view);
		}

		String x_view = ViewProperties.getString("rowDenormaliser.chooseColumns");
		if (StringUtils.isNotEmpty(x_view)) {
			return new ModelAndView(x_view);
		}

		return new ModelAndView("/matrix/rowDenormaliser/chooseColumns");
	}

	@RequestMapping("/chooseColumns")
	public ModelAndView chooseColumns(HttpServletRequest request) {
		RequestUtils.setRequestParameterToAttribute(request);
		List<Long> databaseIds = StringTools.splitToLong(request.getParameter("databaseIds"));
		if (databaseIds != null && !databaseIds.isEmpty()) {
			long databaseId = databaseIds.get(0);
			String tableName = request.getParameter("tableName");
			logger.debug("tableName:" + tableName);
			Database database = databaseService.getDatabaseById(databaseId);
			List<ColumnDefinition> columns = null;
			if (database != null) {
				logger.debug("database:" + database.getName());
				columns = DBUtils.getColumnDefinitions(database.getName(), tableName);
			} else {
				columns = DBUtils.getColumnDefinitions("default", tableName);
			}

			logger.debug("columns:" + columns);

			RowDenormaliser rowDenormaliser = rowDenormaliserService
					.getRowDenormaliser(RequestUtils.getString(request, "transformId"));
			if (rowDenormaliser != null) {
				request.setAttribute("rowDenormaliser", rowDenormaliser);
			}

			if (columns != null && !columns.isEmpty()) {
				List<String> selected = new ArrayList<String>();
				List<ColumnDefinition> unselected = new ArrayList<ColumnDefinition>();

				if (rowDenormaliser != null && StringUtils.isNotEmpty(rowDenormaliser.getSyncColumns())) {
					List<String> cols = StringTools.split(rowDenormaliser.getSyncColumns());
					for (ColumnDefinition col : columns) {
						if (cols.contains(col.getColumnName()) || cols.contains(col.getColumnName().toLowerCase())
								|| cols.contains(col.getColumnName().toUpperCase())) {
							selected.add(col.getColumnName().toLowerCase());
						} else {
							unselected.add(col);
						}
					}
					request.setAttribute("selected", selected);
					request.setAttribute("unselected", columns);
				} else {
					request.setAttribute("selected", selected);
					request.setAttribute("unselected", columns);
				}

				StringBuffer bufferx = new StringBuffer();
				StringBuffer buffery = new StringBuffer();

				if (columns != null && columns.size() > 0) {
					for (int j = 0; j < columns.size(); j++) {
						ColumnDefinition u = (ColumnDefinition) columns.get(j);
						if (selected != null && selected.contains(u.getColumnName())) {
							buffery.append("\n<option value=\"").append(u.getColumnName()).append("\">")
									.append(u.getColumnName()).append(" [").append(u.getColumnName()).append("]")
									.append("</option>");
						} else {
							bufferx.append("\n<option value=\"").append(u.getColumnName()).append("\">")
									.append(u.getColumnName()).append(" [").append(u.getColumnName()).append("]")
									.append("</option>");
						}
					}
				}
				request.setAttribute("bufferx", bufferx.toString());
				request.setAttribute("buffery", buffery.toString());
			}
		}

		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view);
		}

		String x_view = ViewProperties.getString("rowDenormaliser.chooseColumns");
		if (StringUtils.isNotEmpty(x_view)) {
			return new ModelAndView(x_view);
		}

		return new ModelAndView("/matrix/rowDenormaliser/chooseColumns");
	}

	@ResponseBody
	@RequestMapping("/delete")
	public byte[] delete(HttpServletRequest request, ModelMap modelMap) {
		LoginContext loginContext = RequestUtils.getLoginContext(request);
		String id = RequestUtils.getString(request, "id");
		String ids = request.getParameter("ids");
		if (StringUtils.isNotEmpty(ids)) {
			StringTokenizer token = new StringTokenizer(ids, ",");
			while (token.hasMoreTokens()) {
				String x = token.nextToken();
				if (StringUtils.isNotEmpty(x)) {
					RowDenormaliser rowDenormaliser = rowDenormaliserService.getRowDenormaliser(String.valueOf(x));
					if (rowDenormaliser != null
							&& (StringUtils.equals(rowDenormaliser.getCreateBy(), loginContext.getActorId())
									|| loginContext.isSystemAdministrator())) {
						rowDenormaliserService.deleteById(rowDenormaliser.getId());
					}
				}
			}
			return ResponseUtils.responseResult(true);
		} else if (id != null) {
			RowDenormaliser rowDenormaliser = rowDenormaliserService.getRowDenormaliser(String.valueOf(id));
			if (rowDenormaliser != null && (StringUtils.equals(rowDenormaliser.getCreateBy(), loginContext.getActorId())
					|| loginContext.isSystemAdministrator())) {
				rowDenormaliserService.deleteById(rowDenormaliser.getId());
				return ResponseUtils.responseResult(true);
			}
		}
		return ResponseUtils.responseResult(false);
	}

	@ResponseBody
	@RequestMapping("/dropTable")
	public byte[] dropTable(HttpServletRequest request, ModelMap modelMap) {
		String id = RequestUtils.getString(request, "id");
		RowDenormaliser rowDenormaliser = rowDenormaliserService.getRowDenormaliser(id);
		if (rowDenormaliser != null && StringUtils.isNotEmpty(rowDenormaliser.getTargetTableName())) {
			TableFactory.clear();
			boolean error = false;
			List<Long> databaseIds = StringTools.splitToLong(rowDenormaliser.getDatabaseIds());
			for (Long databaseId : databaseIds) {
				try {
					Database targetDatabase = databaseService.getDatabaseById(databaseId);
					if (targetDatabase != null) {
						if (StringUtils.startsWithIgnoreCase(rowDenormaliser.getTargetTableName(), "etl_")
								|| StringUtils.startsWithIgnoreCase(rowDenormaliser.getTargetTableName(), "sync_")
								|| StringUtils.startsWithIgnoreCase(rowDenormaliser.getTargetTableName(), "tmp")
								|| StringUtils.startsWithIgnoreCase(rowDenormaliser.getTargetTableName(), "useradd_")
								|| StringUtils.startsWithIgnoreCase(rowDenormaliser.getTargetTableName(),
										"tree_table_")) {
							if (DBUtils.tableExists(targetDatabase.getName(), rowDenormaliser.getTargetTableName())) {
								String ddlStatements = " drop table " + rowDenormaliser.getTargetTableName();
								logger.warn(ddlStatements);
								DBUtils.executeSchemaResource(targetDatabase.getName(), ddlStatements);
							}
						}
					}
				} catch (Exception ex) {
					error = true;
					logger.error(ex);
				}
			}
			if (!error) {
				TableFactory.clear();
				return ResponseUtils.responseResult(true);
			}
		}
		return ResponseUtils.responseResult(false);
	}

	@RequestMapping("/edit")
	public ModelAndView edit(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);
		request.removeAttribute("canSubmit");

		RowDenormaliser rowDenormaliser = rowDenormaliserService.getRowDenormaliser(request.getParameter("id"));
		if (rowDenormaliser != null) {
			request.setAttribute("rowDenormaliser", rowDenormaliser);

			DatabaseConnectionConfig config = new DatabaseConnectionConfig();
			List<Database> databases = config.getDatabases();
			if (databases != null && !databases.isEmpty()) {
				StringBuilder buffer = new StringBuilder();
				if (StringUtils.isNotEmpty(rowDenormaliser.getDatabaseIds())) {
					List<Long> ids = StringTools.splitToLong(rowDenormaliser.getDatabaseIds());
					for (Database database : databases) {
						if (ids.contains(database.getId())) {
							buffer.append(database.getTitle()).append("[").append(database.getMapping()).append("]")
									.append(",");
						}
					}
				}
				request.setAttribute("databases", databases);
				request.setAttribute("selectedDB", buffer.toString());
			}
		}

		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view, modelMap);
		}

		String x_view = ViewProperties.getString("rowDenormaliser.edit");
		if (StringUtils.isNotEmpty(x_view)) {
			return new ModelAndView(x_view, modelMap);
		}

		return new ModelAndView("/matrix/rowDenormaliser/edit", modelMap);
	}

	@ResponseBody
	@RequestMapping("/execute")
	public byte[] execute(HttpServletRequest request) {
		Map<String, Object> params = RequestUtils.getParameterMap(request);
		RowDenormaliser rowDenormaliser = rowDenormaliserService.getRowDenormaliser(request.getParameter("id"));
		int errorCount = 0;
		String jobNo = null;
		Database database = null;
		String runDay = DateUtils.getNowYearMonthDayHHmmss();
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.putAll(params);
		SysParams.putInternalParams(parameter);
		StringBuilder buffer = new StringBuilder();
		if (rowDenormaliser != null && StringUtils.isNotEmpty(rowDenormaliser.getDatabaseIds())) {
			rowDenormaliser.setTransformTime(new Date());
			StringTokenizer token = new StringTokenizer(rowDenormaliser.getDatabaseIds(), ",");
			while (token.hasMoreTokens()) {
				String x = token.nextToken();
				if (StringUtils.isNotEmpty(x) && StringUtils.isNumeric(x)) {
					jobNo = "row_denormaliser_" + rowDenormaliser.getId() + "_" + x + "_" + runDay;
					try {
						database = databaseService.getDatabaseById(Long.parseLong(x));
						if (database != null) {
							RowDenormaliserTask task = new RowDenormaliserTask(database.getId(),
									rowDenormaliser.getId(), jobNo, parameter);
							if (task.execute()) {
								buffer.append(database.getTitle()).append("[").append(database.getMapping())
										.append("]执行成功。\n");
							} else {
								errorCount++;
								buffer.append(database.getTitle()).append("[").append(database.getMapping())
										.append("]执行失败。\n");
								StringBuffer sb = ExceptionUtils
										.getMsgBuffer("row_denormaliser_" + rowDenormaliser.getId());
								buffer.append(sb.toString());
							}
						}
					} catch (Exception ex) {
						errorCount++;
					} finally {
						ExceptionUtils.clear("row_denormaliser_" + rowDenormaliser.getId());
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

		return ResponseUtils.responseJsonResult(errorCount == 0 ? true : false, buffer.toString());
	}

	@ResponseBody
	@RequestMapping("/executeAll")
	public byte[] executeAll(HttpServletRequest request) {
		Map<String, Object> params = RequestUtils.getParameterMap(request);
		String rowIds = request.getParameter("syncIds");
		List<String> rowIdList = StringTools.split(rowIds);
		int errorCount = 0;
		int errorTotal = 0;
		String jobNo = null;
		Database database = null;
		RowDenormaliserTask task = null;
		RowDenormaliser rowDenormaliser = null;
		StringBuilder buffer = new StringBuilder();
		String runDay = DateUtils.getNowYearMonthDayHHmmss();
		Map<String, Object> parameter = new HashMap<String, Object>();
		parameter.putAll(params);
		SysParams.putInternalParams(parameter);
		for (String rowId : rowIdList) {
			rowDenormaliser = rowDenormaliserService.getRowDenormaliser(rowId);
			if (rowDenormaliser != null && StringUtils.isNotEmpty(rowDenormaliser.getDatabaseIds())) {
				rowDenormaliser.setTransformTime(new Date());
				StringTokenizer token = new StringTokenizer(rowDenormaliser.getDatabaseIds(), ",");
				while (token.hasMoreTokens()) {
					String x = token.nextToken();
					if (StringUtils.isNotEmpty(x) && StringUtils.isNumeric(x)) {
						jobNo = "row_denormaliser_" + rowDenormaliser.getId() + "_" + x + "_" + runDay;
						try {
							database = databaseService.getDatabaseById(Long.parseLong(x));
							if (database != null) {
								task = new RowDenormaliserTask(database.getId(), rowDenormaliser.getId(), jobNo,
										parameter);
								if (task.execute()) {
									buffer.append(database.getTitle()).append("[").append(database.getMapping())
											.append("]执行成功。\n");
								} else {
									errorCount++;
									buffer.append(database.getTitle()).append("[").append(database.getMapping())
											.append("]执行失败。\n");
									StringBuffer sb = ExceptionUtils
											.getMsgBuffer("row_denormaliser_" + rowDenormaliser.getId());
									buffer.append(sb.toString());
								}
							}
						} catch (Exception ex) {
							errorCount++;
							errorTotal++;
						} finally {
							ExceptionUtils.clear("row_denormaliser_" + rowDenormaliser.getId());
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

		return ResponseUtils.responseJsonResult(errorTotal == 0 ? true : false, buffer.toString());
	}

	@RequestMapping("/json")
	@ResponseBody
	public byte[] json(HttpServletRequest request, ModelMap modelMap) throws IOException {
		LoginContext loginContext = RequestUtils.getLoginContext(request);
		Map<String, Object> params = RequestUtils.getParameterMap(request);
		RowDenormaliserQuery query = new RowDenormaliserQuery();
		Tools.populate(query, params);
		query.deleteFlag(0);
		query.setActorId(loginContext.getActorId());
		query.setLoginContext(loginContext);

		if (!loginContext.isSystemAdministrator()) {
			String actorId = loginContext.getActorId();
			query.createBy(actorId);
		}

		int start = 0;
		int limit = 10;
		String orderName = null;
		String order = null;

		int pageNo = ParamUtils.getInt(params, "page");
		limit = ParamUtils.getInt(params, "rows");
		start = (pageNo - 1) * limit;
		orderName = ParamUtils.getString(params, "sortName");
		order = ParamUtils.getString(params, "sortOrder");

		if (start < 0) {
			start = 0;
		}

		if (limit <= 0) {
			limit = Paging.DEFAULT_PAGE_SIZE;
		}

		JSONObject result = new JSONObject();
		int total = rowDenormaliserService.getRowDenormaliserCountByQueryCriteria(query);
		if (total > 0) {
			result.put("total", total);
			result.put("totalCount", total);
			result.put("totalRecords", total);
			result.put("start", start);
			result.put("startIndex", start);
			result.put("limit", limit);
			result.put("pageSize", limit);

			if (StringUtils.isNotEmpty(orderName)) {
				query.setSortOrder(orderName);
				if (StringUtils.equals(order, "desc")) {
					query.setSortOrder(" desc ");
				}
			}

			List<RowDenormaliser> list = rowDenormaliserService.getRowDenormalisersByQueryCriteria(start, limit, query);

			if (list != null && !list.isEmpty()) {
				JSONArray rowsJSON = new JSONArray();

				result.put("rows", rowsJSON);

				for (RowDenormaliser rowDenormaliser : list) {
					JSONObject rowJSON = rowDenormaliser.toJsonObject();
					rowJSON.put("id", rowDenormaliser.getId());
					rowJSON.put("rowId", rowDenormaliser.getId());
					rowJSON.put("rowDenormaliserId", rowDenormaliser.getId());
					rowJSON.put("startIndex", ++start);
					rowsJSON.add(rowJSON);
				}

			}
		} else {
			JSONArray rowsJSON = new JSONArray();
			result.put("rows", rowsJSON);
			result.put("total", total);
		}
		return result.toJSONString().getBytes("UTF-8");
	}

	@RequestMapping
	public ModelAndView list(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);

		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view, modelMap);
		}

		return new ModelAndView("/matrix/rowDenormaliser/list", modelMap);
	}

	@ResponseBody
	@RequestMapping("/save")
	public byte[] save(HttpServletRequest request) {
		User user = RequestUtils.getUser(request);
		String actorId = user.getActorId();
		Map<String, Object> params = RequestUtils.getParameterMap(request);
		String targetTableName = request.getParameter("targetTableName");
		targetTableName = targetTableName.trim();
		if (!(StringUtils.startsWithIgnoreCase(targetTableName, "etl_")
				|| StringUtils.startsWithIgnoreCase(targetTableName, "sync_")
				|| StringUtils.startsWithIgnoreCase(targetTableName, "tmp")
				|| StringUtils.startsWithIgnoreCase(targetTableName, "useradd_")
				|| StringUtils.startsWithIgnoreCase(targetTableName, "tree_table_"))) {
			return ResponseUtils.responseJsonResult(false, "目标表必须是以etl_,sync_,useradd_,tmp开头");
		}
		RowDenormaliser rowDenormaliser = new RowDenormaliser();
		try {
			Tools.populate(rowDenormaliser, params);
			rowDenormaliser.setTitle(request.getParameter("title"));
			rowDenormaliser.setDatabaseIds(request.getParameter("databaseIds"));
			rowDenormaliser.setSourceTableName(request.getParameter("sourceTableName"));
			rowDenormaliser.setAggregateColumns(request.getParameter("aggregateColumns"));
			rowDenormaliser.setPrimaryKey(request.getParameter("primaryKey"));
			rowDenormaliser.setTransformColumn(request.getParameter("transformColumn"));
			rowDenormaliser.setSyncColumns(request.getParameter("syncColumns"));
			rowDenormaliser.setDateDimensionColumn(request.getParameter("dateDimensionColumn"));
			rowDenormaliser.setIncrementColumn(request.getParameter("incrementColumn"));
			rowDenormaliser.setSqlCriteria(request.getParameter("sqlCriteria"));
			rowDenormaliser.setSort(RequestUtils.getInt(request, "sort"));
			rowDenormaliser.setTransformStatus(RequestUtils.getInt(request, "transformStatus"));
			rowDenormaliser.setTransformTime(RequestUtils.getDate(request, "transformTime"));
			rowDenormaliser.setTransformFlag(request.getParameter("transformFlag"));
			rowDenormaliser.setTargetTableName(targetTableName);
			rowDenormaliser.setTargetColumn(request.getParameter("targetColumn"));
			rowDenormaliser.setTargetColumnType(request.getParameter("targetColumnType"));
			rowDenormaliser.setScheduleFlag(request.getParameter("scheduleFlag"));
			rowDenormaliser.setDeleteFetch(request.getParameter("deleteFetch"));
			rowDenormaliser.setLocked(RequestUtils.getInt(request, "locked"));
			rowDenormaliser.setCreateBy(actorId);
			this.rowDenormaliserService.save(rowDenormaliser);

			return ResponseUtils.responseJsonResult(true);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex);
		}
		return ResponseUtils.responseJsonResult(false);
	}

	@javax.annotation.Resource
	public void setDatabaseService(IDatabaseService databaseService) {
		this.databaseService = databaseService;
	}

	@javax.annotation.Resource(name = "com.glaf.matrix.transform.service.rowDenormaliserService")
	public void setRowDenormaliserService(RowDenormaliserService rowDenormaliserService) {
		this.rowDenormaliserService = rowDenormaliserService;
	}

}
