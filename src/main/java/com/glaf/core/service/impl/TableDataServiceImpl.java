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

package com.glaf.core.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.glaf.core.base.ColumnModel;
import com.glaf.core.base.TableModel;
import com.glaf.core.dao.EntityDAO;
import com.glaf.core.domain.ColumnDefinition;
import com.glaf.core.domain.SysDataLog;
import com.glaf.core.domain.TableDefinition;
import com.glaf.core.entity.SqlExecutor;
import com.glaf.core.expression.ExpressionFactory;
import com.glaf.core.factory.SysLogFactory;
import com.glaf.core.id.Dbid;
import com.glaf.core.id.IdGenerator;
import com.glaf.core.mapper.IdMapper;
import com.glaf.core.mapper.TableDataMapper;
import com.glaf.core.mapper.TablePageMapper;
import com.glaf.core.query.TablePageQuery;
import com.glaf.core.service.ITableDataService;
import com.glaf.core.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service("tableDataService")
@Transactional
public class TableDataServiceImpl implements ITableDataService {
	private final static Log logger = LogFactory.getLog(TableDataServiceImpl.class);

	private EntityDAO entityDAO;

	private IdMapper idMapper;

	private IdGenerator idGenerator;

	private TableDataMapper tableDataMapper;

	private TablePageMapper tablePageMapper;

	/**
	 * 删除数据
	 *
	 * @param model
	 */
	@Transactional
	public void deleteTableData(TableModel model) {
		if (StringUtils.isNotEmpty(model.getTableName()) && model.getColumns() != null
				&& !model.getColumns().isEmpty()) {
			if (model.getTableName() != null) {
				model.setTableName(model.getTableName().toUpperCase());
			}
			tableDataMapper.deleteTableData(model);
		}
	}

	/**
	 * 删除数据
	 *
	 * @param rows
	 */
	@Transactional
	public void deleteTableDataList(List<TableModel> rows) {
		if (rows != null && !rows.isEmpty()) {
			for (TableModel tableData : rows) {
				this.deleteTableData(tableData);
			}
		}
	}

	public List<Dbid> getAllDbids() {
		return idMapper.getAllDbids();
	}

	/**
	 * 获取一页数据
	 *
	 * @param pageNo
	 * @param pageSize
	 * @param model
	 * @return
	 */
	public Paging getPageData(int pageNo, int pageSize, TableModel model) {
		if (model.getTableName() != null) {
			model.setTableName(model.getTableName().toUpperCase());
		}
		Paging page = new Paging();
		TablePageQuery query = new TablePageQuery();
		query.tableName(model.getTableName());

		int count = tablePageMapper.getTableCount(query);
		if (count > 0) {
			page.setTotal(count);
			SqlExecutor queryExecutor = new SqlExecutor();
			queryExecutor.setParameter(model);
			queryExecutor.setStatementId("getTableData");
			List<Object> rows = entityDAO.getList(pageNo, pageSize, queryExecutor);
			page.setCurrentPage(pageNo);
			page.setPageSize(pageSize);
			page.setRows(rows);
		}
		return page;
	}

	public Map<String, Object> getTableDataByPrimaryKey(TableModel model) {
		if (model.getTableName() != null) {
			model.setTableName(model.getTableName().toUpperCase());
		}
		return tableDataMapper.getTableDataByPrimaryKey(model);
	}

	public List<Map<String, Object>> getTableKeyMap(TableModel model) {
		return tableDataMapper.getTableKeyMap(model);
	}

	private List<Map<String, Object>> getTablePrimaryKeyMap(String tableName, String columnName) {
		TableModel tableModel = new TableModel();
		ColumnModel idColumn = new ColumnModel();
		idColumn.setColumnName(columnName);
		tableModel.setTableName(tableName.toUpperCase());
		tableModel.setIdColumn(idColumn);
		return tableDataMapper.getTablePrimaryKeyMap(tableModel);
	}

	@Transactional
	public Collection<TableModel> insertAll(TableDefinition tableDefinition, String seqNo,
			Collection<TableModel> rows) {
		logger.debug("tableDefinition=" + tableDefinition);
		logger.debug("idColumn=" + tableDefinition.getIdColumn().toString());
		if (tableDefinition.getTableName() != null) {
			tableDefinition.setTableName(tableDefinition.getTableName().toUpperCase());
		}
		Map<String, Object> colMap = new java.util.HashMap<String, Object>();

		Map<String, String> exprMap = new java.util.HashMap<String, String>();
		List<ColumnDefinition> exprColumns = new java.util.ArrayList<ColumnDefinition>();

		ColumnModel idColumn = new ColumnModel();

		ColumnDefinition idCol = tableDefinition.getIdColumn();
		if (idCol != null && idCol.getColumnName() != null) {
			idColumn.setColumnName(idCol.getColumnName());
			idColumn.setJavaType(idCol.getJavaType());
			idColumn.setValueExpression(idCol.getValueExpression());
			exprColumns.add(idCol);
			exprMap.put(idCol.getColumnName().toUpperCase(), idCol.getValueExpression());
		}

		Iterator<ColumnDefinition> iter = tableDefinition.getColumns().iterator();
		while (iter.hasNext()) {
			ColumnDefinition cell = iter.next();
			if (StringUtils.isNotEmpty(cell.getValueExpression())) {
				exprMap.put(cell.getColumnName().toUpperCase(), cell.getValueExpression());
				exprColumns.add(cell);
			}
		}

		logger.debug("expr map:" + exprMap);

		List<TableModel> inertRows = new java.util.ArrayList<TableModel>();

		logger.debug(" rows size = " + rows.size());
		// logger.debug(" key map: " + keyMap);
		Iterator<TableModel> iterator = rows.iterator();
		while (iterator.hasNext()) {
			TableModel tableData = iterator.next();
			ColumnModel myPK = tableData.getIdColumn();
			ColumnModel pkColumn = new ColumnModel();
			pkColumn.setColumnName(idColumn.getColumnName());
			pkColumn.setJavaType(idColumn.getJavaType());
			pkColumn.setValueExpression(idColumn.getValueExpression());

			for (ColumnModel column : tableData.getColumns()) {
				colMap.put(column.getColumnName(), column.getValue());
			}

			for (ColumnDefinition c : exprColumns) {
				ColumnModel x = new ColumnModel();
				x.setColumnName(c.getColumnName());
				x.setJavaType(c.getJavaType());
				x.setValueExpression(c.getValueExpression());
				tableData.addColumn(x);
			}

			for (ColumnModel cell : tableData.getColumns()) {
				String expr = exprMap.get(cell.getColumnName().toUpperCase());
				if (StringUtils.isNotEmpty(expr)) {
					if (ExpressionConstants.NOW_EXPRESSION.equals(expr)) {
						if (cell.getDateValue() == null) {
							cell.setDateValue(new Date());
							cell.setValue(cell.getDateValue());
						}
					}
					if (ExpressionConstants.ID_EXPRESSION.equals(expr)) {
						if (cell.getValue() == null) {
							if (StringUtils.equals(cell.getJavaType(), "Integer")) {
								cell.setValue(idGenerator.nextId());
							} else if (StringUtils.equals(cell.getJavaType(), "Long")) {
								cell.setValue(idGenerator.nextId());
							} else if (StringUtils.equals(cell.getValueExpression(),
									ExpressionConstants.UUID_EXPRESSION)) {
								cell.setValue(UUID32.getUUID());
							} else {
								cell.setValue(idGenerator.getNextId());
							}
						}
					}

					if (StringUtils.startsWith(expr, ExpressionConstants.ID_PREFIX_EXPRESSION)) {
						if (cell.getValue() == null) {
							String name = expr.substring(ExpressionConstants.ID_PREFIX_EXPRESSION.length(),
									expr.length() - 1);
							if (StringUtils.equals(cell.getJavaType(), "Integer")) {
								cell.setValue(idGenerator.nextId(name));
							} else if (StringUtils.equals(cell.getJavaType(), "Long")) {
								cell.setValue(idGenerator.nextId(name));
							} else {
								cell.setValue(idGenerator.getNextId(name));
							}
						}
					}

					if (ExpressionConstants.SEQNO_EXPRESSION.equals(expr)) {
						if (cell.getValue() == null) {
							cell.setValue(seqNo);
						}
					}
					Map<String, Object> context = new HashMap<String, Object>();

					if (cell.getValue() == null) {
						cell.setValue(ExpressionFactory.getInstance().evaluate(expr, context));
					}
				}
			}

			if (myPK != null && myPK.getValue() != null) {
				pkColumn.setValue(myPK.getValue());
			} else {
				if (StringUtils.equals(pkColumn.getJavaType(), "Integer")) {
					pkColumn.setValue(idGenerator.nextId());
				} else if (StringUtils.equals(pkColumn.getJavaType(), "Long")) {
					pkColumn.setValue(idGenerator.nextId());
				} else if (StringUtils.equals(pkColumn.getValueExpression(), ExpressionConstants.UUID_EXPRESSION)) {
					pkColumn.setValue(UUID32.getUUID());
				} else {
					pkColumn.setValue(idGenerator.getNextId());
				}
			}

			tableData.removeColumn(pkColumn);
			tableData.addColumn(pkColumn);
			tableData.setIdColumn(pkColumn);

			inertRows.add(tableData);
		}

		if (!inertRows.isEmpty()) {
			logger.debug("inert rows size:" + inertRows.size());
			for (TableModel tableData : inertRows) {
				tableData.setTableName(tableDefinition.getTableName());
				// logger.debug(tableData.toString());

				if (StringUtils.equals(tableDefinition.getDbType(), "hbase")) {
					tableData.setDbType("hbase");
				}
				tableDataMapper.insertTableData(tableData);
				SysDataLog log = new SysDataLog();
				log.setActorId(tableData.getActorId());
				if (tableData.getIdColumn() != null && tableData.getIdColumn().getValue() != null) {
					log.setBusinessKey(tableData.getIdColumn().getValue().toString());
				}
				log.setCreateTime(new Date());
				log.setOperate("insert");
				log.setModuleId(tableData.getTableName());
				log.setContent(tableData.toJsonObject().toJSONString());
				try {
					SysLogFactory.getInstance().addLog(log);
				} catch (Throwable ignored) {
				}
			}
		}

		return inertRows;
	}

	@Transactional
	public void insertAllTableData(List<TableModel> rows) {
		if (rows != null && !rows.isEmpty()) {
			for (TableModel table : rows) {
				if (table.getTableName() != null) {
					table.setTableName(table.getTableName().toUpperCase());
				}
				tableDataMapper.insertTableData(table);
			}
		}
	}

	@Transactional
	public void insertTableData(TableDefinition tableDefinition, List<Map<String, Object>> rows) {
		if (tableDefinition.getTableName() != null) {
			tableDefinition.setTableName(tableDefinition.getTableName().toUpperCase());
		}
		List<ColumnDefinition> columns = tableDefinition.getColumns();
		if (columns != null && !columns.isEmpty()) {
			Iterator<Map<String, Object>> iterator = rows.iterator();
			while (iterator.hasNext()) {
				TableModel table = new TableModel();
				table.setTableName(tableDefinition.getTableName());
				Map<String, Object> dataMap = iterator.next();
				for (ColumnDefinition column : columns) {
					String javaType = column.getJavaType();
					String name = column.getColumnName();
					ColumnModel c = new ColumnModel();
					c.setColumnName(name);
					c.setJavaType(javaType);
					Object value = dataMap.get(name);
					if (value == null) {
						value = dataMap.get(name.toUpperCase());
					}
					if (value == null) {
						if (column.getName() != null) {
							value = dataMap.get(column.getName());
						}
					}
					if (value != null) {
						if ("Integer".equals(javaType)) {
							value = ParamUtils.getInt(dataMap, name);
						} else if ("Long".equals(javaType)) {
							value = ParamUtils.getLong(dataMap, name);
						} else if ("Double".equals(javaType)) {
							value = ParamUtils.getDouble(dataMap, name);
						} else if ("Date".equals(javaType)) {
							value = ParamUtils.getTimestamp(dataMap, name);
						} else if ("String".equals(javaType)) {
							value = ParamUtils.getString(dataMap, name);
						} else if ("Clob".equals(javaType)) {
							value = ParamUtils.getString(dataMap, name);
						}
						c.setValue(value);
						table.addColumn(c);
					}
				}
				tableDataMapper.insertTableData(table);
				SysDataLog log = new SysDataLog();
				log.setActorId(table.getActorId());
				if (table.getIdColumn() != null && table.getIdColumn().getValue() != null) {
					log.setBusinessKey(table.getIdColumn().getValue().toString());
				}
				log.setCreateTime(new Date());
				log.setOperate("insert");
				log.setModuleId(table.getTableName());
				log.setContent(table.toJsonObject().toJSONString());
				try {
					SysLogFactory.getInstance().addLog(log);
				} catch (Throwable ignored) {
				}
			}
		}
	}

	@Transactional
	public void insertTableData(TableModel tableModel) {
		if (tableModel.getTableName() != null) {
			tableModel.setTableName(tableModel.getTableName().toUpperCase());
		}
		tableDataMapper.insertTableData(tableModel);
	}

	@Transactional
	public Collection<TableModel> saveAll(TableDefinition tableDefinition, String seqNo, Collection<TableModel> rows) {
		logger.debug("tableDefinition=" + tableDefinition);
		logger.debug("idColumn=" + tableDefinition.getIdColumn().toString());
		if (tableDefinition.getTableName() != null) {
			tableDefinition.setTableName(tableDefinition.getTableName().toUpperCase());
		}
		if (tableDefinition.isInsertOnly()) {
			return this.insertAll(tableDefinition, seqNo, rows);
		}

		Collection<String> aggregationKeys = new HashSet<String>();

		Map<String, Object> keyMap = new HashMap<String, Object>();
		Map<String, String> exprMap = new HashMap<String, String>();
		List<ColumnDefinition> exprColumns = new ArrayList<ColumnDefinition>();

		ColumnModel idColumn = new ColumnModel();

		ColumnDefinition idCol = tableDefinition.getIdColumn();
		if (idCol != null && idCol.getColumnName() != null) {
			idColumn.setColumnName(idCol.getColumnName());
			idColumn.setJavaType(idCol.getJavaType());
			idColumn.setValueExpression(idCol.getValueExpression());
		}

		Iterator<ColumnDefinition> iter = tableDefinition.getColumns().iterator();
		while (iter.hasNext()) {
			ColumnDefinition cell = iter.next();
			if (StringUtils.isNotEmpty(cell.getValueExpression())) {
				exprMap.put(cell.getColumnName(), cell.getValueExpression());
				exprColumns.add(cell);
			}
		}

		logger.debug(exprMap);
		logger.debug("aggregationKeys:" + tableDefinition.getAggregationKey());
		String keyCloumns = tableDefinition.getAggregationKey();
		if (StringUtils.isNotEmpty(keyCloumns)) {
			List<String> cols = StringTools.split(keyCloumns);
			if (cols != null && !cols.isEmpty()) {
				StringBuilder buffer = new StringBuilder(500);
				Map<String, Object> colMap = new java.util.HashMap<String, Object>();
				Iterator<TableModel> iterator = rows.iterator();
				while (iterator.hasNext()) {
					TableModel tableData = iterator.next();
					/**
					 * 使用聚合主键判断
					 */
					colMap.clear();
					buffer.delete(0, buffer.length());
					for (ColumnModel cell : tableData.getColumns()) {
						colMap.put(cell.getColumnName().toLowerCase(), cell.getValue());
						if (cell.getValue() == null) {
							// logger.debug(cell.getColumnName()+"->" +
							// cell.getValueExpression());
							if (StringUtils.equals(cell.getValueExpression(), ExpressionConstants.SEQNO_EXPRESSION)) {
								colMap.put(cell.getColumnName().toLowerCase(), seqNo);
							}
						}
					}

					if (tableData.getIdColumn() != null && tableData.getIdColumn().getValue() != null) {
						colMap.put(tableData.getIdColumn().getColumnName().toLowerCase(),
								tableData.getIdColumn().getValue());
					}

					Iterator<String> it = cols.iterator();
					while (it.hasNext()) {
						// Object val = colMap.get(it.next().toLowerCase());
						Object val = ParamUtils.get(colMap, it.next());
						if (val != null) {
							if (val instanceof Date) {
								buffer.append(DateUtils.getDateTime(DateUtils.FULL_DATE_FORMAT, (Date) val));
							} else {
								buffer.append(val.toString());
							}
						}
						if (it.hasNext()) {
							buffer.append("_");
						}
					}
					String aggregationKey = buffer.toString();
					aggregationKeys.add(aggregationKey);
					tableData.setAggregationKey(aggregationKey);// 设置聚合主键值

					if (aggregationKeys.size() > 0 && (aggregationKeys.size() % 500 == 0)) {
						TableModel model = new TableModel();
						model.setTableName(tableDefinition.getTableName());
						model.setIdColumn(idColumn);
						model.setAggregationKeys(aggregationKeys);
						List<Map<String, Object>> list = tableDataMapper.getTableKeyMap(model);
						if (list != null && !list.isEmpty()) {
							for (Map<String, Object> map : list) {
								Map<String, Object> dataMap = QueryUtils.lowerKeyMap(map);
								Object id = ParamUtils.getObject(dataMap, "id");
								String aggregationKey2 = ParamUtils.getString(dataMap, "aggregationkey");
								keyMap.put(aggregationKey2, id);
							}
						}
						aggregationKeys.clear();
					}
				}
			}

			if (aggregationKeys.size() > 0) {
				TableModel model = new TableModel();
				model.setTableName(tableDefinition.getTableName());
				model.setIdColumn(idColumn);
				model.setAggregationKeys(aggregationKeys);
				List<Map<String, Object>> list = tableDataMapper.getTableKeyMap(model);
				if (list != null && !list.isEmpty()) {
					for (Map<String, Object> map : list) {
						Map<String, Object> dataMap = QueryUtils.lowerKeyMap(map);
						Object id = ParamUtils.getObject(dataMap, "id");
						String aggregationKey = ParamUtils.getString(dataMap, "aggregationkey");
						keyMap.put(aggregationKey, id);
					}
				}
				aggregationKeys.clear();
			}

			List<TableModel> inertRows = new java.util.ArrayList<TableModel>();
			List<TableModel> updateRows = new java.util.ArrayList<TableModel>();
			Map<String, Object> colMap = new java.util.HashMap<String, Object>();
			logger.debug(" rows size = " + rows.size());
			Iterator<TableModel> iterator = rows.iterator();
			while (iterator.hasNext()) {
				TableModel tableData = iterator.next();
				colMap.clear();
				ColumnModel myPK = tableData.getIdColumn();
				ColumnModel pkColumn = new ColumnModel();
				pkColumn.setColumnName(idColumn.getColumnName());
				pkColumn.setJavaType(idColumn.getJavaType());
				pkColumn.setValueExpression(idColumn.getValueExpression());

				for (ColumnModel column : tableData.getColumns()) {
					colMap.put(column.getColumnName(), column.getValue());
				}

				if (tableData.getIdColumn() != null && tableData.getIdColumn().getValue() != null) {
					colMap.put(tableData.getIdColumn().getColumnName().toLowerCase(),
							tableData.getIdColumn().getValue());
				}

				if (keyMap.containsKey(tableData.getAggregationKey())) {
					Object id = keyMap.get(tableData.getAggregationKey());
					pkColumn.setValue(id);
					tableData.setIdColumn(pkColumn);
					tableData.removeColumn(pkColumn);
					updateRows.add(tableData);
				} else {
					ColumnModel col = new ColumnModel();
					col.setColumnName("AGGREGATIONKEY_");
					col.setJavaType("String");
					col.setValue(tableData.getAggregationKey());
					tableData.removeColumn(col);
					tableData.addColumn(col);

					for (ColumnDefinition c : exprColumns) {
						ColumnModel x = new ColumnModel();
						x.setColumnName(c.getColumnName());
						x.setJavaType(c.getJavaType());
						x.setValueExpression(c.getValueExpression());
						tableData.addColumn(x);
					}

					for (ColumnModel cell : tableData.getColumns()) {
						String expr = exprMap.get(cell.getColumnName());
						if (StringUtils.isNotEmpty(expr)) {
							if (ExpressionConstants.NOW_EXPRESSION.equals(expr)) {
								if (cell.getDateValue() == null) {
									cell.setDateValue(new Date());
									cell.setValue(cell.getDateValue());
								}
							}
							if (ExpressionConstants.ID_EXPRESSION.equals(expr)) {
								if (cell.getValue() == null) {
									if (StringUtils.equals(cell.getJavaType(), "Integer")) {
										cell.setValue(idGenerator.nextId());
									} else if (StringUtils.equals(cell.getJavaType(), "Long")) {
										cell.setValue(idGenerator.nextId());
									} else if (StringUtils.equals(cell.getValueExpression(),
											ExpressionConstants.UUID_EXPRESSION)) {
										cell.setValue(UUID32.getUUID());
									} else {
										cell.setValue(idGenerator.getNextId());
									}
								}
							}

							if (StringUtils.startsWith(expr, ExpressionConstants.ID_PREFIX_EXPRESSION)) {
								if (cell.getValue() == null) {
									String name = expr.substring(ExpressionConstants.ID_PREFIX_EXPRESSION.length(),
											expr.length() - 1);
									if (StringUtils.equals(cell.getJavaType(), "Integer")) {
										cell.setValue(idGenerator.nextId(name));
									} else if (StringUtils.equals(cell.getJavaType(), "Long")) {
										cell.setValue(idGenerator.nextId(name));
									} else {
										cell.setValue(idGenerator.getNextId(name));
									}
								}
							}

							if (ExpressionConstants.SEQNO_EXPRESSION.equals(expr)) {
								if (cell.getValue() == null) {
									cell.setValue(seqNo);
								}
							}
							Map<String, Object> context = new HashMap<String, Object>();

							if (cell.getValue() == null) {
								cell.setValue(ExpressionFactory.getInstance().evaluate(expr, context));
							}
						}
					}

					if (myPK != null && myPK.getValue() != null) {
						pkColumn.setValue(myPK.getValue());
					} else {
						if (StringUtils.equals(pkColumn.getJavaType(), "Integer")) {
							pkColumn.setValue(idGenerator.nextId());
						} else if (StringUtils.equals(pkColumn.getJavaType(), "Long")) {
							pkColumn.setValue(idGenerator.nextId());
						} else if (StringUtils.equals(pkColumn.getValueExpression(),
								ExpressionConstants.UUID_EXPRESSION)) {
							pkColumn.setValue(UUID32.getUUID());
						} else {
							pkColumn.setValue(idGenerator.getNextId());
						}
					}

					tableData.removeColumn(pkColumn);
					tableData.addColumn(pkColumn);
					tableData.setIdColumn(pkColumn);

					inertRows.add(tableData);
				}
			}

			if (!inertRows.isEmpty()) {
				logger.debug("inert rows size:" + inertRows.size());
				for (TableModel tableData : inertRows) {
					tableData.setTableName(tableDefinition.getTableName());
					// logger.debug(tableData.toString());
					if (StringUtils.equals(tableDefinition.getDbType(), "hbase")) {
						tableData.setDbType("hbase");
					}
					if (StringUtils.equals(tableData.getDbType(), "hbase")) {
						tableDataMapper.insertHBaseTableData(tableData);
					} else {
						tableDataMapper.insertTableData(tableData);
					}
				}
			}
			if (!updateRows.isEmpty()) {
				logger.debug("update rows size:" + updateRows.size());
				for (TableModel tableData : updateRows) {
					tableData.setTableName(tableDefinition.getTableName());
					if (StringUtils.equals(tableDefinition.getDbType(), "hbase")) {
						tableData.setDbType("hbase");
						tableDataMapper.insertHBaseTableData(tableData);
					} else {
						tableDataMapper.updateTableDataByPrimaryKey(tableData);
					}
				}
			}
			return rows;
		} else {
			throw new RuntimeException("aggregationKeys is required.");
		}
	}

	@Transactional
	public void saveOrUpdate(TableDefinition tableDefinition, boolean updatable, List<Map<String, Object>> rows) {
		if (tableDefinition.getTableName() != null) {
			tableDefinition.setTableName(tableDefinition.getTableName().toUpperCase());
		}
		String tableName = tableDefinition.getTableName();
		String idColumnName = null;
		List<Map<String, Object>> rowIds = null;
		List<ColumnDefinition> columns = tableDefinition.getColumns();
		if (columns != null && !columns.isEmpty()) {
			for (ColumnDefinition column : columns) {
				if (column.isPrimaryKey()) {
					idColumnName = column.getColumnName();
					idColumnName = idColumnName.toUpperCase();
					rowIds = this.getTablePrimaryKeyMap(tableName, idColumnName);
					break;
				}
			}
		}

		Collection<String> keys = new HashSet<String>();

		if (rowIds != null && !rowIds.isEmpty()) {
			Iterator<Map<String, Object>> iter = rowIds.iterator();
			while (iter.hasNext()) {
				Map<String, Object> dataMap = iter.next();
				if (dataMap.get("id") != null) {
					keys.add(dataMap.get("id").toString());
				}
				if (dataMap.get("ID") != null) {
					keys.add(dataMap.get("ID").toString());
				}
			}
		}

		List<Map<String, Object>> inertRows = new java.util.ArrayList<Map<String, Object>>();
		List<Map<String, Object>> updateRows = new java.util.ArrayList<Map<String, Object>>();
		Iterator<Map<String, Object>> iterator = rows.iterator();
		while (iterator.hasNext()) {
			Map<String, Object> dataMap = iterator.next();
			Object id = dataMap.get(idColumnName);
			if (id == null) {
				assert idColumnName != null;
				id = dataMap.get(idColumnName.toUpperCase());
			}
			if (id != null) {
				if (keys.contains(id.toString())) {
					updateRows.add(dataMap);
				} else {
					inertRows.add(dataMap);
				}
			}
		}

		if (!inertRows.isEmpty()) {
			this.insertTableData(tableDefinition, inertRows);
		}

		if (!updateRows.isEmpty()) {
			this.updateTableData(tableDefinition, updateRows);
		}

	}

	/**
	 * 保存JSON数据到指定的表
	 *
	 * @param tableDefinition
	 * @param jsonObject
	 */
	@Transactional
	public void saveTableData(TableDefinition tableDefinition, JSONObject jsonObject) {

		if (tableDefinition != null && tableDefinition.getIdColumn() != null) {
			if (tableDefinition.getTableName() != null) {
				tableDefinition.setTableName(tableDefinition.getTableName().toUpperCase());
			}
			ColumnDefinition idColumn = tableDefinition.getIdColumn();

			boolean insertData = true;
			Object primaryKey = null;
			if (idColumn.getColumnName() != null) {
				primaryKey = jsonObject.get(idColumn.getColumnName());
			} else if (idColumn.getName() != null) {
				primaryKey = jsonObject.get(idColumn.getName());
			}
			if (primaryKey != null) {
				insertData = false;
			}

			TableModel tableModel = new TableModel();
			tableModel.setTableName(tableDefinition.getTableName());

			List<ColumnDefinition> columns = tableDefinition.getColumns();
			if (columns != null && !columns.isEmpty()) {
				for (ColumnDefinition col : columns) {
					if (StringUtils.equalsIgnoreCase(idColumn.getColumnName(), col.getColumnName())) {
						continue;
					}
					String javaType = col.getJavaType();
					String columnName = col.getColumnName();
					String name = col.getName();
					ColumnModel cm = new ColumnModel();
					cm.setJavaType(javaType);
					cm.setColumnName(col.getColumnName());
					Object value;

					if (jsonObject.containsKey(columnName)) {
						value = jsonObject.get(columnName);
						if (StringUtils.equalsIgnoreCase("Integer", javaType)) {
							if (value instanceof Integer) {
								cm.setValue(jsonObject.getInteger(columnName));
							} else {
								cm.setValue(Integer.parseInt(value.toString()));
							}
						} else if (StringUtils.equalsIgnoreCase("Long", javaType)) {
							if (value instanceof Long) {
								cm.setValue(jsonObject.getLong(columnName));
							} else {
								cm.setValue(Long.parseLong(value.toString()));
							}
						} else if (StringUtils.equalsIgnoreCase("Double", javaType)) {
							if (value instanceof Double) {
								cm.setValue(jsonObject.getDouble(columnName));
							} else {
								cm.setValue(Double.parseDouble(value.toString()));
							}
						} else if (StringUtils.equalsIgnoreCase("Date", javaType)) {
							if (value instanceof Date) {
								cm.setValue(jsonObject.getDate(columnName));
							} else if (value instanceof Long) {
								Long t = jsonObject.getLong(columnName);
								cm.setValue(new Date(t));
							} else {
								cm.setValue(DateUtils.toDate(value.toString()));
							}
						} else if (StringUtils.equalsIgnoreCase("String", javaType)) {
							if (value instanceof String) {
								cm.setValue(jsonObject.getString(columnName));
							} else {
								cm.setValue(value.toString());
							}
						} else {
							cm.setValue(value);
						}
					} else if (jsonObject.containsKey(name)) {
						value = jsonObject.get(name);
						if (StringUtils.equalsIgnoreCase("Integer", javaType)) {
							if (value instanceof Integer) {
								cm.setValue(jsonObject.getInteger(name));
							} else {
								cm.setValue(Integer.parseInt(value.toString()));
							}
						} else if (StringUtils.equalsIgnoreCase("Long", javaType)) {
							if (value instanceof Long) {
								cm.setValue(jsonObject.getLong(name));
							} else {
								cm.setValue(Long.parseLong(value.toString()));
							}
						} else if (StringUtils.equalsIgnoreCase("Double", javaType)) {
							if (value instanceof Double) {
								cm.setValue(jsonObject.getDouble(name));
							} else {
								cm.setValue(Double.parseDouble(value.toString()));
							}
						} else if (StringUtils.equalsIgnoreCase("Date", javaType)) {
							if (value instanceof Date) {
								cm.setValue(jsonObject.getDate(name));
							} else if (value instanceof Long) {
								Long t = jsonObject.getLong(name);
								cm.setValue(new Date(t));
							} else {
								cm.setValue(DateUtils.toDate(value.toString()));
							}
						} else if (StringUtils.equalsIgnoreCase("String", javaType)) {
							if (value instanceof String) {
								cm.setValue(jsonObject.getString(name));
							} else {
								cm.setValue(value.toString());
							}
						} else {
							cm.setValue(value);
						}
					}
					tableModel.addColumn(cm);
				}
			}

			if (insertData) {
				ColumnModel idCol = new ColumnModel();
				idCol.setJavaType(idColumn.getJavaType());
				if (StringUtils.equalsIgnoreCase("Integer", idColumn.getJavaType())) {
					idCol.setValue(idGenerator.nextId());
				} else if (StringUtils.equalsIgnoreCase("Long", idColumn.getJavaType())) {
					idCol.setValue(idGenerator.nextId());
				} else if (StringUtils.equalsIgnoreCase(ExpressionConstants.UUID_EXPRESSION,
						idColumn.getValueExpression())) {
					idCol.setValue(UUID32.getUUID());
				} else {
					idCol.setValue(idGenerator.getNextId());
				}
				tableModel.setIdColumn(idCol);

				tableDataMapper.insertTableData(tableModel);

				SysDataLog log = new SysDataLog();
				log.setActorId(tableModel.getActorId());
				if (tableModel.getIdColumn() != null && tableModel.getIdColumn().getValue() != null) {
					log.setBusinessKey(tableModel.getIdColumn().getValue().toString());
				}
				log.setCreateTime(new Date());
				log.setOperate("insert");
				log.setModuleId(tableModel.getTableName());
				log.setContent(tableModel.toJsonObject().toJSONString());
				SysLogFactory.getInstance().addLog(log);

			} else {
				ColumnModel idCol = new ColumnModel();
				idCol.setJavaType(idColumn.getJavaType());
				idCol.setValue(primaryKey);
				tableModel.setIdColumn(idCol);
				tableDataMapper.updateTableDataByPrimaryKey(tableModel);
				SysDataLog log = new SysDataLog();
				log.setActorId(tableModel.getActorId());
				if (tableModel.getIdColumn() != null && tableModel.getIdColumn().getValue() != null) {
					log.setBusinessKey(tableModel.getIdColumn().getValue().toString());
				}
				log.setCreateTime(new Date());
				log.setOperate("update");
				log.setModuleId(tableModel.getTableName());
				log.setContent(tableModel.toJsonObject().toJSONString());
				SysLogFactory.getInstance().addLog(log);
			}
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
	public void setIdMapper(IdMapper idMapper) {
		this.idMapper = idMapper;
	}

	@javax.annotation.Resource
	public void setTableDataMapper(TableDataMapper tableDataMapper) {
		this.tableDataMapper = tableDataMapper;
	}

	@javax.annotation.Resource
	public void setTablePageMapper(TablePageMapper tablePageMapper) {
		this.tablePageMapper = tablePageMapper;
	}

	@Transactional
	public void updateAllDbids(List<Dbid> rows) {
		if (rows != null && !rows.isEmpty()) {
			Map<String, Long> idMap = new java.util.HashMap<String, Long>();
			List<Dbid> list = this.getAllDbids();
			for (Dbid id : rows) {
				if (StringUtils.isNumeric(id.getValue())) {
					idMap.put(id.getName(), Long.parseLong(id.getValue()));
				}
			}
			if (list != null && !list.isEmpty()) {
				for (Dbid dbid : list) {
					if (idMap.containsKey(dbid.getName()) && StringUtils.isNumeric(dbid.getValue())) {
						if (idMap.get(dbid.getName()) > Long.parseLong(dbid.getValue())) {
							dbid.setValue(idMap.get(dbid.getName()).toString());
							dbid.setVersion(dbid.getVersion() + 1);
							idMapper.updateNextDbId(dbid);
						}
					}
				}
			}
		}
	}

	@Transactional
	public void updateAllTableData(List<TableModel> rows) {
		if (rows != null && !rows.isEmpty()) {
			for (TableModel table : rows) {
				if (table.getTableName() != null) {
					table.setTableName(table.getTableName().toUpperCase());
				}
				tableDataMapper.updateTableDataByPrimaryKey(table);
			}
		}
	}

	@Transactional
	public void updateTableData(TableDefinition tableDefinition, List<Map<String, Object>> rows) {
		if (tableDefinition.getTableName() != null) {
			tableDefinition.setTableName(tableDefinition.getTableName().toUpperCase());
		}
		List<ColumnDefinition> columns = tableDefinition.getColumns();
		if (columns != null && !columns.isEmpty()) {
			Iterator<Map<String, Object>> iterator = rows.iterator();
			while (iterator.hasNext()) {
				TableModel table = new TableModel();
				table.setTableName(tableDefinition.getTableName());
				Map<String, Object> dataMap = iterator.next();
				for (ColumnDefinition column : columns) {
					String javaType = column.getJavaType();
					String name = column.getColumnName();
					ColumnModel c = new ColumnModel();
					c.setColumnName(name);
					c.setJavaType(javaType);
					Object value = dataMap.get(name);
					if (value == null) {
						value = dataMap.get(name.toUpperCase());
					}
					if (value == null) {
						if (column.getName() != null) {
							value = dataMap.get(column.getName());
						}
					}
					if (value != null) {
						if ("Integer".equals(javaType)) {
							value = ParamUtils.getInt(dataMap, name);
						} else if ("Long".equals(javaType)) {
							value = ParamUtils.getLong(dataMap, name);
						} else if ("Double".equals(javaType)) {
							value = ParamUtils.getDouble(dataMap, name);
						} else if ("Date".equals(javaType)) {
							value = ParamUtils.getTimestamp(dataMap, name);
						} else if ("String".equals(javaType)) {
							value = ParamUtils.getString(dataMap, name);
						} else if ("Clob".equals(javaType)) {
							value = ParamUtils.getString(dataMap, name);
						}
						c.setValue(value);
						if (column.isPrimaryKey()) {
							table.setIdColumn(c);
						} else {
							table.addColumn(c);
						}
					}
				}
				tableDataMapper.updateTableDataByPrimaryKey(table);
				SysDataLog log = new SysDataLog();
				log.setActorId(table.getActorId());
				if (table.getIdColumn() != null && table.getIdColumn().getValue() != null) {
					log.setBusinessKey(table.getIdColumn().getValue().toString());
				}
				log.setCreateTime(new Date());
				log.setOperate("update");
				log.setModuleId(table.getTableName());
				log.setContent(table.toJsonObject().toJSONString());
				SysLogFactory.getInstance().addLog(log);
			}
		}
	}

	@Transactional
	public void updateTableData(TableModel model) {
		if (model.getTableName() != null) {
			model.setTableName(model.getTableName().toUpperCase());
		}
		tableDataMapper.updateTableDataByPrimaryKey(model);
		SysDataLog log = new SysDataLog();
		log.setActorId(model.getActorId());
		if (model.getIdColumn() != null && model.getIdColumn().getValue() != null) {
			log.setBusinessKey(model.getIdColumn().getValue().toString());
		}
		log.setCreateTime(new Date());
		log.setOperate("update");
		log.setModuleId(model.getTableName());
		log.setContent(model.toJsonObject().toJSONString());
		SysLogFactory.getInstance().addLog(log);
	}

	@Transactional
	public void updateTableDataByWhere(TableModel model) {
		if (model.getTableName() != null) {
			model.setTableName(model.getTableName().toUpperCase());
		}
		tableDataMapper.updateTableData(model);
		SysDataLog log = new SysDataLog();
		log.setActorId(model.getActorId());
		if (model.getIdColumn() != null && model.getIdColumn().getValue() != null) {
			log.setBusinessKey(model.getIdColumn().getValue().toString());
		}
		log.setCreateTime(new Date());
		log.setOperate("update");
		log.setModuleId(model.getTableName());
		log.setContent(model.toJsonObject().toJSONString());
		SysLogFactory.getInstance().addLog(log);
	}

}