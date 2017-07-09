[#macro renderBreadcrumb i18n group repositoryEntity]
	<ol class="breadcrumb hidden-xs">
		[#if group?? && group?has_content]
			<li><a href="/courses">${ i18n.translate("section.courses") }</a></li>
			<li><a href="${group.course.course.getURI()}">${group.course.course.code} - ${group.course.course.name}</a></li>
			<li><a href="${group.course.getURI()}">${group.course.timeSpan.start?string["yyyy"]}[#if group.course.timeSpan.end??] - ${group.course.timeSpan.end?string["yyyy"]}[/#if]</a></li>
			<li class="active">Group ${group.getGroupNumber()}</li>
		[#elseif repositoryEntity?? && repositoryEntity?has_content]
			<li><a href="/projects">${ i18n.translate("section.projects") }</a></li>
			<li><a href="${repositoryEntity.getURI()}">${repositoryEntity.getTitle()}</a></li>
		[/#if]
	</ol>
[/#macro]

[#macro renderSidemenu currentTab i18n group repository]
    <div>
        <ul class="nav nav-pills nav-stacked">
            <li role="presentation" [#if currentTab == "commits"]class="active"[/#if]>
                <a href="${repositoryEntity.getURI()}">
                    ${i18n.translate("section.commits")}
                </a>
            </li>
            <li role="presentation" [#if currentTab == "pull-requests"]class="active"[/#if]>
                <a href="${repositoryEntity.getURI()}pulls">
                    ${i18n.translate("section.pull-requests")}
                </a>
            </li>
            <li role="presentation" [#if currentTab == "issues"]class="active"[/#if]>
                <a href="${repositoryEntity.getURI()}issues">
                    ${i18n.translate("section.issues")}
                </a>
            </li>
    [#if group?? && group?has_content]
            <li role="presentation" [#if currentTab == "assignments"]class="active"[/#if]>
                <a href="${repositoryEntity.getURI()}assignments">
                    ${i18n.translate("section.assignments")}
                </a>
            </li>
    [/#if]
            <li role="presentation" [#if currentTab == "contributors"]class="active"[/#if]>
                <a href="${repositoryEntity.getURI()}contributors">
                    ${i18n.translate("section.contributors")}
                </a>
            </li>
            <li role="presentation" [#if currentTab == "insights"]class="active"[/#if]>
                <a href="${repositoryEntity.getURI()}insights">
                    ${i18n.translate("header.graphs")}
                </a>
            </li>
            <li role="presentation" [#if currentTab == "settings"]class="active"[/#if]>
                <a href="${repositoryEntity.getURI()}settings">
				Settings
                </a>
            </li>
        </ul>

        <h4>Git clone URL</h4>
        [#if repository?? && repository?has_content]
            <input class="well well-sm" style="width: 100%;" value="git clone ${repository.getUrl()}" readonly></input>
        [#else]
            <input class="well well-sm" style="width: 100%;" value="${i18n.translate("error.could-not-connect-git-server")}" readonly></input>
        [/#if]
    </div>
[/#macro]