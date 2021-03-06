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

package com.glaf.core.jdbc.connection;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.glaf.core.config.DBConfiguration;
import com.glaf.core.util.ClassUtils;

public final class ConnectionProviderFactory {

	private static final Logger log = LoggerFactory.getLogger(ConnectionProviderFactory.class);

	private static final ConcurrentMap<String, ConnectionProvider> providerCache = new ConcurrentHashMap<String, ConnectionProvider>();

	private static final Set<String> SPECIAL_PROPERTIES;

	static {
		SPECIAL_PROPERTIES = new HashSet<String>();
		SPECIAL_PROPERTIES.add(DBConfiguration.JDBC_DATASOURCE);
		SPECIAL_PROPERTIES.add(DBConfiguration.JDBC_URL);
		SPECIAL_PROPERTIES.add(DBConfiguration.JDBC_DRIVER);
		SPECIAL_PROPERTIES.add(DBConfiguration.JDBC_ISOLATION);
		SPECIAL_PROPERTIES.add(DBConfiguration.JDBC_DRIVER);
		SPECIAL_PROPERTIES.add(DBConfiguration.JDBC_USER);
	}

	private static boolean druidConfigDefined(Properties properties) {
		Iterator<?> iter = properties.keySet().iterator();
		while (iter.hasNext()) {
			String property = (String) iter.next();
			if (property.startsWith("druid")) {
				return true;
			}
		}
		return false;
	}

	private static boolean hikariConfigDefined(Properties properties) {
		Iterator<?> iter = properties.keySet().iterator();
		while (iter.hasNext()) {
			String property = (String) iter.next();
			if (property.startsWith("hikari")) {
				return true;
			}
		}
		return false;
	}

	protected static void closeAndCreate(Properties properties) {
		String jdbcUrl = properties.getProperty(DBConfiguration.JDBC_URL);
		String user = properties.getProperty(DBConfiguration.JDBC_USER);
		String cacheKey = DigestUtils.md5Hex(jdbcUrl + user);
		ConnectionProvider provider = null;
		if (providerCache.get(cacheKey) != null) {
			provider = providerCache.get(cacheKey);
			provider.close();
		}
		provider = createProvider(properties, null);
		providerCache.put(cacheKey, provider);
	}

	private static boolean druidProviderPresent() {
		try {
			ClassUtils.classForName("com.glaf.core.jdbc.connection.DruidConnectionProvider");
		} catch (Exception e) {
			log.warn(
					"druid properties is specificed, but could not find com.glaf.core.jdbc.connection.DruidConnectionProvider from the classpath, "
							+ "these properties are going to be ignored.");
			return false;
		}
		return true;
	}

	private static boolean hikariProviderPresent() {
		try {
			ClassUtils.classForName("com.glaf.core.jdbc.connection.HikariCPConnectionProvider");
		} catch (Exception e) {
			log.warn(
					"hikari properties is specificed, but could not find com.glaf.core.jdbc.connection.HikariCPConnectionProvider from the classpath, "
							+ "these properties are going to be ignored.");
			return false;
		}
		return true;
	}

	public static ConnectionProvider createProvider(Properties properties) {
		if (properties == null || properties.isEmpty()) {
			return null;
		}
		String jdbcUrl = properties.getProperty(DBConfiguration.JDBC_URL);
		String user = properties.getProperty(DBConfiguration.JDBC_USER);
		String cacheKey = DigestUtils.md5Hex(jdbcUrl + user);
		if (providerCache.get(cacheKey) != null) {
			return providerCache.get(cacheKey);
		}
		ConnectionProvider provider = createProvider(properties, null);
		providerCache.put(cacheKey, provider);
		return provider;
	}

	@SuppressWarnings("rawtypes")
	private static ConnectionProvider createProvider(Properties properties, Map connectionProviderInjectionData) {
		if (properties == null || properties.isEmpty()) {
			return null;
		}
		log.debug("---------------------------ConnectionProvider create----------------");
		ConnectionProvider provider = null;
		String providerClass = properties.getProperty(DBConfiguration.JDBC_PROVIDER);
		if (providerClass != null) {
			provider = initializeConnectionProviderFromConfig(providerClass);
		} else if (druidConfigDefined(properties) && druidProviderPresent()) {
			provider = initializeConnectionProviderFromConfig("com.glaf.core.jdbc.connection.DruidConnectionProvider");
		} else if (hikariConfigDefined(properties) && hikariProviderPresent()) {
			provider = initializeConnectionProviderFromConfig(
					"com.glaf.core.jdbc.connection.HikariCPConnectionProvider");
		} else {
			Properties props = DBConfiguration.getDefaultDataSourceProperties();
			if (props != null) {
				providerClass = props.getProperty(DBConfiguration.JDBC_PROVIDER);
				if (StringUtils.isNotEmpty(providerClass)) {
					provider = initializeConnectionProviderFromConfig(providerClass);
				}
			}
		}

		if (provider == null) {
			provider = initializeConnectionProviderFromConfig("com.glaf.core.jdbc.connection.DruidConnectionProvider");
		}

		if (StringUtils.equals(properties.getProperty(DBConfiguration.JDBC_DRIVER), "org.sqlite.JDBC")) {
			provider = initializeConnectionProviderFromConfig(
					"com.glaf.core.jdbc.connection.HikariCPConnectionProvider");
		} else if (StringUtils.equals(properties.getProperty(DBConfiguration.JDBC_DRIVER), "org.voltdb.jdbc.Driver")) {
			provider = initializeConnectionProviderFromConfig("com.glaf.core.jdbc.connection.DruidConnectionProvider");
		}

		if (connectionProviderInjectionData != null && connectionProviderInjectionData.size() != 0) {
			try {
				BeanInfo info = Introspector.getBeanInfo(provider.getClass());
				PropertyDescriptor[] descritors = info.getPropertyDescriptors();
				int size = descritors.length;
				for (int index = 0; index < size; index++) {
					String propertyName = descritors[index].getName();
					if (connectionProviderInjectionData.containsKey(propertyName)) {
						Method method = descritors[index].getWriteMethod();
						method.invoke(provider, new Object[] { connectionProviderInjectionData.get(propertyName) });
					}
				}
			} catch (IntrospectionException e) {
				throw new RuntimeException("Unable to inject objects into the connection provider", e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Unable to inject objects into the connection provider", e);
			} catch (InvocationTargetException e) {
				throw new RuntimeException("Unable to inject objects into the connection provider", e);
			}
		}
		provider.configure(properties);
		log.debug("---------------------------ConnectionProvider end----------------");
		return provider;
	}

	public static ConnectionProvider createProvider(String systemName) {
		Properties props = DBConfiguration.getDataSourcePropertiesByName(systemName);
		// log.debug("ConnectionProvider@name=" + systemName);
		// log.debug("ConnectionProvider@props=" + props);
		ConnectionProvider model = createProvider(props);
		return model;
	}

	public static ConnectionProvider createProvider(String systemName, Properties properties) {
		ConnectionProvider model = createProvider(properties);
		return model;
	}

	protected static Properties getConnectionProperties(Properties properties) {
		Iterator<?> iter = properties.keySet().iterator();
		Properties result = new Properties();
		while (iter.hasNext()) {
			String prop = (String) iter.next();
			if (prop.startsWith(DBConfiguration.JDBC_DRIVER) && !SPECIAL_PROPERTIES.contains(prop)) {
				result.setProperty(prop.substring(DBConfiguration.JDBC_PREFIX.length() + 1),
						properties.getProperty(prop));
			}
		}
		String userName = properties.getProperty(DBConfiguration.JDBC_USER);
		if (userName != null) {
			result.setProperty("user", userName);
		}
		String pwd = properties.getProperty(DBConfiguration.JDBC_PASSWORD);
		if (pwd != null) {
			result.setProperty("password", pwd);
		}
		return result;
	}

	private static ConnectionProvider initializeConnectionProviderFromConfig(String providerClass) {
		ConnectionProvider connections;
		try {
			log.info("Initializing connection provider: " + providerClass);
			connections = (ConnectionProvider) ClassUtils.classForName(providerClass).newInstance();
		} catch (Exception ex) {
			log.error("Could not instantiate connection provider", ex);
			throw new RuntimeException("Could not instantiate connection provider: " + providerClass);
		}
		return connections;
	}

	private ConnectionProviderFactory() {

	}

}