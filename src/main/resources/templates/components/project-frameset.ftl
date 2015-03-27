[#macro renderBreadcrumb i18n group]
    <ol class="breadcrumb">
        <li><a href="/courses">${ i18n.translate("section.courses") }</a></li>
        <li><a href="/courses/${group.course.getCode()}">${group.course.getCode()} - ${group.course.getName()}</a></li>
        <li class="active">Group ${group.getGroupNumber()}</li>
    </ol>
[/#macro]

[#macro renderSidemenu currentTab i18n group repository]
    <div>
        <ul class="nav nav-pills nav-stacked">
            <li role="presentation" [#if currentTab == "commits"]class="active"[/#if]>
                <a href="/courses/${group.course.getCode()}/groups/${group.getGroupNumber()}">
                    Commits
                </a>
            </li>
            <li role="presentation" [#if currentTab == "pull-requests"]class="active"[/#if]>
                <a href="/courses/${group.course.getCode()}/groups/${group.getGroupNumber()}/pulls">
                    Pull requests
                </a>
            </li>
            <li role="presentation" [#if currentTab == "assignments"]class="active"[/#if]>
                <a href="/courses/${group.course.getCode()}/groups/${group.getGroupNumber()}/assignments">
                    Assignments
                </a>
            </li>
        </ul>

        <h4>Git clone URL</h4>
        [#if repository?? && repository?has_content]
            <input class="well well-sm" style="width: 100%;" value="git clone ${repository.getUrl()}" readonly></input>
        [#else]
            <input class="well well-sm" style="width: 100%;" value="Could not connect to the Git server!" readonly></input>
        [/#if]
        <a class="btn btn-link btn-xs" href="http://git-scm.com/downloads" target="_blank">Install Git</a>
        <a class="btn btn-link btn-xs" href="https://help.github.com/articles/generating-ssh-keys/" target="_blank">Generate SSH keys</a>
    </div>
[/#macro]