<#include "macros.ftl"/>
<@startPage pageTitle="Jars and Struts Plugins"/>

<h3>Azalea Webconsole Index</h3>

<table width="100%">
	<tr>
		<th>SymbolicName</th>
		<th>Version</th>
	</tr>
	<#list bundleNames as bnd>
		<tr <#if bnd_index%2 gt 0>class="b"<#else>class="a"</#if> id="bnd_${bnd.id}">
		<td>${bnd.symbolicName}</td>
		<td>${bnd.version}</td>
		</tr>
	</#list>
</table>

<@endPage />
