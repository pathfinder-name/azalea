<#--

-->
<!DOCTYPE html>
<html>
<head>
<title>${pageTitle}</title>
<meta http-equiv="Content-type" content="text/html; charset=UTF-8" />
<link href="${staticPath}/css/styles.css" rel="stylesheet" type="text/css" />
</head>
<body class="composite" marginwidth="0" marginheight="0" leftmargin="0" topmargin="0">

<div id="breadcrumbs">
	<table border="0" cellspacing="0" cellpadding="4" width="100%">
		<tr>
			<td> 
				Struts Configuration Browser > ${pageTitle}
			</td>
		</tr>
	</table>
</div>  
  
<table border="0" cellspacing="0" cellpadding="4" width="100%" id="main">
	<tr valign="top">
	    <#if !hideNav?exists>		<td id="leftcol" width="20%">
			<div id="navcolumn">
				<#-- Quick hack to show menu features :)
-->				<#-- This should be done via contribution from the actions
-->				<#-- themselves. E.g via a collection of MenuItems with url and name
-->				<div class="toolgroup">
					<div class="label"><strong>Configuration</strong></div>
					<div class="body">
						<div><@s.url id="constantsLink" action="showConstants" includeParams="none" />
							<a href="${constantsLink}">Constants</a>
						</div>
						<div><@s.url id="beansLink" action="showBeans" includeParams="none" />
							<a href="${beansLink}">Beans</a>
						</div>
						<div><@s.url id="jarsLink" action="showJars" includeParams="none" />
							<a href="${jarsLink}">Jars (requires Maven 2 data)</a>
						</div>
					</div>
				</div>
				<div id="projecttools" class="toolgroup">
					<#if namespaces?exists>					<div class="label"><strong>Namespaces</strong></div>
					<div class="body">
						<#foreach namespace in namespaces>						<div><@s.url id="namespaceLink" action="actionNames" includeParams="none"><@s.param name="namespace">${namespace}</@s.param></@s.url><a href="${namespaceLink}"><#if namespace == ""> default <#else> ${namespace} </#if></a></div>
						</#foreach>					</div>
					</#if>				</div>				
				<div class="toolgroup">
					<#if actionNames?exists>					<div class="label"><strong>Actions in <#if namespace == ""> default <#else> ${namespace} </#if></strong></div>
                        <#foreach name in actionNames>                        <div><@s.url id="actionLink" action="showConfig" includeParams="none"><@s.param name="namespace">${namespace}</@s.param><@s.param name="actionName">${name}</@s.param></@s.url><a href="${actionLink}">${name}</a></div>
						</#foreach>					</#if>				</div>
			</div>
		</td>
		</#if>		<td>
			<div id="bodycol">
				<div id="apphead">
					<h2>${pageTitle}</h2>
				</div>
				<div id="content" class="app">				
