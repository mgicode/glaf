package ${packageName}.web.springmvc;

import java.io.IOException;
import java.util.*;
 
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import com.alibaba.fastjson.*;
 
import com.glaf.core.base.DataRequest;
import com.glaf.core.base.DataRequest.SortDescriptor;
import com.glaf.core.config.ViewProperties;
import com.glaf.core.identity.*;
import com.glaf.core.security.*;
import com.glaf.core.util.*;
 
import ${packageName}.model.*;
import ${packageName}.query.*;
import ${packageName}.service.*;
import ${packageName}.util.*;


public class ${entityName}BaseController {
	protected static final Log logger = LogFactory.getLog(${entityName}BaseController.class);

	protected ${entityName}Service ${modelName}Service;

	public ${entityName}BaseController() {

	}

        @javax.annotation.Resource
	public void set${entityName}Service(${entityName}Service ${modelName}Service) {
		this.${modelName}Service = ${modelName}Service;
	}


	@RequestMapping("/save")
	public ModelAndView save(HttpServletRequest request, ModelMap modelMap) {
		LoginContext loginContext = RequestUtils.getLoginContext(request);
		String actorId = loginContext.getActorId();
		Map<String, Object> params = RequestUtils.getParameterMap(request);
        params.remove("status");
		params.remove("wfStatus");

		${entityName} ${modelName} = new ${entityName}();

		try{
		    Tools.populate(${modelName}, params);

 <#if pojo_fields?exists>
    <#list  pojo_fields as field>	
      <#if field.type?exists && ( field.type== 'Integer')>
                ${modelName}.set${field.firstUpperName}(RequestUtils.getInt(request, "${field.name}"));
      <#elseif field.type?exists && ( field.type== 'Long')>
                ${modelName}.set${field.firstUpperName}(RequestUtils.getLong(request, "${field.name}"));
      <#elseif field.type?exists && ( field.type== 'Double')>
                ${modelName}.set${field.firstUpperName}(RequestUtils.getDouble(request, "${field.name}"));
      <#elseif field.type?exists && ( field.type== 'Date')>
                ${modelName}.set${field.firstUpperName}(RequestUtils.getDate(request, "${field.name}"));
      <#elseif field.type?exists && ( field.type== 'String')>
                ${modelName}.set${field.firstUpperName}(request.getParameter("${field.name}"));
      </#if>
    </#list>
</#if>
		    //${modelName}.setTenantId(loginContext.getTenantId());
		    //${modelName}.setCreateBy(actorId);
            //${modelName}.setUpdateBy(actorId);
		    ${modelName}Service.save(${modelName});   
        } catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex);
		}
		return this.list(request, modelMap);
	}

	@RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST)
	public @ResponseBody ${entityName} saveOrUpdate(HttpServletRequest request, @RequestBody Map<String, Object> model) {
                User user = RequestUtils.getUser(request);
		String actorId =  user.getActorId();
		${entityName} ${modelName} = new ${entityName}();
		try {
		    Tools.populate(${modelName}, model);
		<#if pojo_fields?exists>
		    <#list  pojo_fields as field>	
		    <#if field.type?exists && ( field.type== 'Integer')>
                    ${modelName}.set${field.firstUpperName}(ParamUtils.getInt(model, "${field.name}"));
		    <#elseif field.type?exists && ( field.type== 'Long')>
                    ${modelName}.set${field.firstUpperName}(ParamUtils.getLong(model, "${field.name}"));
                    <#elseif field.type?exists && ( field.type== 'Double')>
                    ${modelName}.set${field.firstUpperName}(ParamUtils.getDouble(model, "${field.name}"));
                    <#elseif field.type?exists && ( field.type== 'Date')>
                    ${modelName}.set${field.firstUpperName}(ParamUtils.getDate(model, "${field.name}"));
                    <#elseif field.type?exists && ( field.type== 'String')>
                    ${modelName}.set${field.firstUpperName}(ParamUtils.getString(model, "${field.name}"));
		    </#if>
		    </#list>
		</#if>
			//${modelName}.setTenantId(loginContext.getTenantId());
		    //${modelName}.setCreateBy(actorId);
            //${modelName}.setUpdateBy(actorId);
		    this.${modelName}Service.save(${modelName});
		} catch (Exception ex) {
		    ex.printStackTrace();
		    logger.error(ex);
		}
		return ${modelName};
	}

        @ResponseBody
	@RequestMapping("/save${entityName}")
	public byte[] save${entityName}(HttpServletRequest request) { 
	        LoginContext loginContext = RequestUtils.getLoginContext(request);
		    String actorId = loginContext.getActorId();
	        Map<String, Object> params = RequestUtils.getParameterMap(request);
		${entityName} ${modelName} = new ${entityName}();
		try {
		    Tools.populate(${modelName}, params);
 <#if pojo_fields?exists>
    <#list  pojo_fields as field>	
      <#if field.type?exists && ( field.type== 'Integer')>
                    ${modelName}.set${field.firstUpperName}(RequestUtils.getInt(request, "${field.name}"));
      <#elseif field.type?exists && ( field.type== 'Long')>
                    ${modelName}.set${field.firstUpperName}(RequestUtils.getLong(request, "${field.name}"));
      <#elseif field.type?exists && ( field.type== 'Double')>
                    ${modelName}.set${field.firstUpperName}(RequestUtils.getDouble(request, "${field.name}"));
      <#elseif field.type?exists && ( field.type== 'Date')>
                    ${modelName}.set${field.firstUpperName}(RequestUtils.getDate(request, "${field.name}"));
      <#elseif field.type?exists && ( field.type== 'String')>
                    ${modelName}.set${field.firstUpperName}(request.getParameter("${field.name}"));
      </#if>
    </#list>
</#if>
            //${modelName}.setTenantId(loginContext.getTenantId());
		    //${modelName}.setCreateBy(actorId);
            //${modelName}.setUpdateBy(actorId);
		    this.${modelName}Service.save(${modelName});

		    return ResponseUtils.responseJsonResult(true);
		} catch (Exception ex) {
		    ex.printStackTrace();
		    logger.error(ex);
		}
		return ResponseUtils.responseJsonResult(false);
	}

        @RequestMapping("/update")
	public ModelAndView update(HttpServletRequest request, ModelMap modelMap) {
		User user = RequestUtils.getUser(request);
		String actorId = user.getActorId();
		Map<String, Object> params = RequestUtils.getParameterMap(request);
                params.remove("status");
		params.remove("wfStatus");

		Tools.populate(${modelName}, params);

		<#if idField.type=='Integer' >
                ${entityName} ${modelName} = ${modelName}Service.get${entityName}(RequestUtils.getInt(request, "${idField.name}"));
		<#elseif idField.type== 'Long' >
                ${entityName} ${modelName} = ${modelName}Service.get${entityName}(RequestUtils.getLong(request, "${idField.name}"));
		<#else>
                ${entityName} ${modelName} = ${modelName}Service.get${entityName}(request.getParameter("${idField.name}"));
		</#if>

 <#if pojo_fields?exists>
    <#list  pojo_fields as field>	
      <#if field.type?exists && ( field.type== 'Integer')>
                ${modelName}.set${field.firstUpperName}(RequestUtils.getInt(request, "${field.name}"));
      <#elseif field.type?exists && ( field.type== 'Long')>
                ${modelName}.set${field.firstUpperName}(RequestUtils.getLong(request, "${field.name}"));
      <#elseif field.type?exists && ( field.type== 'Double')>
                ${modelName}.set${field.firstUpperName}(RequestUtils.getDouble(request, "${field.name}"));
      <#elseif field.type?exists && ( field.type== 'Date')>
                ${modelName}.set${field.firstUpperName}(RequestUtils.getDate(request, "${field.name}"));
      <#elseif field.type?exists && ( field.type== 'String')>
                ${modelName}.set${field.firstUpperName}(request.getParameter("${field.name}"));
      </#if>
    </#list>
</#if>
	
		${modelName}Service.save(${modelName});   

		return this.list(request, modelMap);
	}

        @ResponseBody
        @RequestMapping("/delete")
	public byte[] delete(HttpServletRequest request, ModelMap modelMap) {
		LoginContext loginContext = RequestUtils.getLoginContext(request);
		Map<String, Object> params = RequestUtils.getParameterMap(request);
		${idField.type} ${idField.name} = RequestUtils.get${idField.type}(request, "${idField.name}");
		String ${idField.name}s = request.getParameter("${idField.name}s");
		if (StringUtils.isNotEmpty(${idField.name}s)) {
			StringTokenizer token = new StringTokenizer(${idField.name}s, ",");
			while (token.hasMoreTokens()) {
				String x = token.nextToken();
				if (StringUtils.isNotEmpty(x)) {
					${entityName} ${modelName} = ${modelName}Service.get${entityName}(${idField.type}.valueOf(x));
					/**
		                         * 此处业务逻辑需自行调整
		                         */
					 //TODO
					if (${modelName} != null && (StringUtils.equals(${modelName}.getCreateBy(), loginContext.getActorId()) || loginContext.isSystemAdministrator())) {
						//${modelName}.setDeleteFlag(1);
						${modelName}Service.save(${modelName});
					}
				}
			}
		    return ResponseUtils.responseResult(true);
		} else if (${idField.name} != null) {
			${entityName} ${modelName} = ${modelName}Service
					.get${entityName}(${idField.type}.valueOf(${idField.name}));
			/**
		         * 此处业务逻辑需自行调整
		         */
		        //TODO
			if (${modelName} != null && ( StringUtils.equals(${modelName}.getCreateBy(), loginContext.getActorId()) || loginContext.isSystemAdministrator())) {
				//${modelName}.setDeleteFlag(1);
				${modelName}Service.save(${modelName});
				return ResponseUtils.responseResult(true);
			}
		}
	     return ResponseUtils.responseResult(false);
	}


        @ResponseBody
	@RequestMapping("/detail")
	public byte[] detail(HttpServletRequest request) throws IOException{
	    //RequestUtils.setRequestParameterToAttribute(request);
	    //Map<String, Object> params = RequestUtils.getParameterMap(request);
	    <#if idField.type=='Integer' >
            ${entityName} ${modelName} = ${modelName}Service.get${entityName}(RequestUtils.getInt(request, "${idField.name}"));
	    <#elseif idField.type== 'Long' >
            ${entityName} ${modelName} = ${modelName}Service.get${entityName}(RequestUtils.getLong(request, "${idField.name}"));
	    <#else>
            ${entityName} ${modelName} = ${modelName}Service.get${entityName}(request.getParameter("${idField.name}"));
	    </#if>
		 
	    JSONObject rowJSON =  ${modelName}.toJsonObject();
	    return rowJSON.toJSONString().getBytes("UTF-8");
        }
    

        @RequestMapping("/edit")
	public ModelAndView edit(HttpServletRequest request, ModelMap modelMap) {
		User user = RequestUtils.getUser(request);
		String actorId =  user.getActorId();
		RequestUtils.setRequestParameterToAttribute(request);
		request.removeAttribute("canSubmit");
		Map<String, Object> params = RequestUtils.getParameterMap(request);
		<#if idField.type=='Integer' >
                ${entityName} ${modelName} = ${modelName}Service.get${entityName}(RequestUtils.getInt(request, "${idField.name}"));
		<#elseif idField.type== 'Long' >
                ${entityName} ${modelName} = ${modelName}Service.get${entityName}(RequestUtils.getLong(request, "${idField.name}"));
		<#else>
                ${entityName} ${modelName} = ${modelName}Service.get${entityName}(request.getParameter("${idField.name}"));
		</#if>
		if(${modelName} != null) {
		    request.setAttribute("${modelName}", ${modelName});
		    JSONObject rowJSON =  ${modelName}.toJsonObject();
		    request.setAttribute("x_json", rowJSON.toJSONString());
		}

		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view, modelMap);
		}

		String x_view = ViewProperties.getString("${modelName}.edit");
		if (StringUtils.isNotEmpty(x_view)) {
			return new ModelAndView(x_view, modelMap);
		}

		return new ModelAndView("/${tableDefinition.moduleName}/${modelName}/edit", modelMap);
	}

        @RequestMapping("/view")
	public ModelAndView view(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);
		Map<String, Object> params = RequestUtils.getParameterMap(request);
		<#if idField.type=='Integer' >
                ${entityName} ${modelName} = ${modelName}Service.get${entityName}(RequestUtils.getInt(request, "${idField.name}"));
		<#elseif idField.type== 'Long' >
                ${entityName} ${modelName} = ${modelName}Service.get${entityName}(RequestUtils.getLong(request, "${idField.name}"));
		<#else>
                ${entityName} ${modelName} = ${modelName}Service.get${entityName}(request.getParameter("${idField.name}"));
		</#if>
		request.setAttribute("${modelName}", ${modelName});
		JSONObject rowJSON =  ${modelName}.toJsonObject();
		request.setAttribute("x_json", rowJSON.toJSONString());


		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view);
		}

		String x_view = ViewProperties.getString("${modelName}.view");
		if (StringUtils.isNotEmpty(x_view)) {
			return new ModelAndView(x_view);
		}

		return new ModelAndView("/${tableDefinition.moduleName}/${modelName}/view");
	}

        @RequestMapping("/query")
	public ModelAndView query(HttpServletRequest request, ModelMap modelMap) {
		RequestUtils.setRequestParameterToAttribute(request);
		String view = request.getParameter("view");
		if (StringUtils.isNotEmpty(view)) {
			return new ModelAndView(view, modelMap);
		}
		String x_view = ViewProperties.getString("${modelName}.query");
		if (StringUtils.isNotEmpty(x_view)) {
			return new ModelAndView(x_view, modelMap);
		}
		return new ModelAndView("/${tableDefinition.moduleName}/${modelName}/query", modelMap);
	}

	@RequestMapping("/json")
	@ResponseBody
	public byte[] json(HttpServletRequest request, ModelMap modelMap) throws IOException {
	    request.setCharacterEncoding("UTF-8");
		response.setCharacterEncoding("UTF-8");
	    LoginContext loginContext = RequestUtils.getLoginContext(request);
		Map<String, Object> params = RequestUtils.getParameterMap(request);
		${entityName}Query query = new ${entityName}Query();
		Tools.populate(query, params);
		query.deleteFlag(0);
		query.setActorId(loginContext.getActorId());
		query.setLoginContext(loginContext);
		
        if (!loginContext.isSystemAdministrator()) {
			query.tenantId(loginContext.getTenantId());
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
		int total = ${modelName}Service.get${entityName}CountByQueryCriteria(query);
		if (total > 0) {
		    result.put("code", 0);
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

			Map<String, User> userMap = IdentityFactory.getUserMap();
			List<${entityName}> list = ${modelName}Service.get${entityName}sByQueryCriteria(start, limit,
					query);

			if (list != null && !list.isEmpty()) {
				JSONArray rowsJSON = new JSONArray();
				 
				for (${entityName} ${modelName} : list) {
					JSONObject rowJSON = ${modelName}.toJsonObject();
					rowJSON.put("id", ${modelName}.getId());
					rowJSON.put("${modelName}Id", ${modelName}.getId());
                    rowJSON.put("startIndex", ++start);
 					rowsJSON.add(rowJSON);
				}

				result.put("rows", rowsJSON);

			}
		} else {
			JSONArray rowsJSON = new JSONArray();
			result.put("rows", rowsJSON);
			result.put("total", total);
			result.put("code", 0);
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
 

		return new ModelAndView("/${tableDefinition.moduleName}/${modelName}/list", modelMap);
	}

}
