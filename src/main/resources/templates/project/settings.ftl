[#macro listTags repository commitId]
	[#list repository.getTags() as tag]
		[#if tag.getCommit().getCommit() == commitId]
        <span class="label label-primary">${tag.getSimpleName()}</span>
		[/#if]
	[/#list]
[/#macro]

[#import "../macros.ftl" as macros]
[#import "../components/project-frameset.ftl" as projectFrameset]
[#import "../components/commit-row.ftl" as commitRow]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user repositoryEntity/]
<div class="container">

[#if group?? && group?has_content]
	[@projectFrameset.renderBreadcrumb i18n group/]
[/#if]

    <div class="row">
        <div class="col-md-10 col-md-offset-2">
            <h4 style="line-height:34px; margin-top:0;">Settings</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
		[@projectFrameset.renderSidemenu "settings" i18n group![] repository![]/]
        </div>
        <div class="col-md-10">
            <div class="panel panel-default">
                <div class="panel-heading">Build configuration</div>
                <div class="panel-body">
                    <form>
                        <div class="checkbox">
                            <label>
                                <input type="checkbox" [#if buildInstruction.withDisplay]checked[/#if] disabled> Run tests with display buffer
                            </label>
                        </div>
                        <div class="checkbox">
                            <label>
                                <input type="checkbox" [#if buildInstruction.pmd]checked[/#if] disabled> Enable PMD
                            </label>
                        </div>
                        <div class="checkbox">
                            <label>
                                <input type="checkbox" [#if buildInstruction.checkstyle]checked[/#if] disabled> Enable Checkstyle
                            </label>
                        </div>
                        <div class="checkbox">
                            <label>
                                <input type="checkbox" [#if buildInstruction.findbugs]checked[/#if] disabled> Enable Findbugs
                            </label>
                        </div>
                    </form>
                </div>
            </div>
            <div class="panel panel-warning">
                <div class="panel-heading">Delete repository</div>
                <div class="panel-body">
                    <button type="button" id="btn-delete-repository" class="btn btn-danger" [#if courseEdition?? && !(user.isAdmin() || user.isAssisting(courseEdition))]disabled[/#if]>Delete repository</button>
                </div>
            </div>
        </div>
    </div>
</div>


[@macros.renderScripts]

<script type="text/javascript">
	$(function() {
		$('#btn-delete-repository').on('click', function() {
			if (prompt('Please confirm that you want to remove repository "${repository.name}" by confirming the repository name below:') === '${repository.name}' &&
				confirm('Are you sure you wish to remove "${repository.name}"? This action cannot be undone.')) {
				$.ajax({
					url: '${repositoryEntity.getURI()}',
					type: 'DELETE',
					success: function() {
						window.location.href = '/';
					}
				})
			} else {
				alert('Removal aborted due invalid confirmation.')
			}
		})
	})
</script>

[/@macros.renderScripts]
[@macros.renderFooter /]
