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
package com.glaf.ui.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.glaf.ui.model.UserTheme;
import com.glaf.ui.query.UserThemeQuery;

/**
 * 
 * Mapper接口
 *
 */

@Component("com.glaf.ui.mapper.UserThemeMapper")
public interface UserThemeMapper {

	void deleteUserThemes(UserThemeQuery query);

	void deleteUserThemeById(Integer id);

	UserTheme getUserThemeById(Integer id);

	int getUserThemeCount(UserThemeQuery query);

	List<UserTheme> getUserThemes(UserThemeQuery query);

	void insertUserTheme(UserTheme model);

	void updateUserTheme(UserTheme model);

	void colseCourse(Integer id);

}