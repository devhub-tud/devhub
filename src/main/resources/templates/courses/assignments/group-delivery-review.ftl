[#import "../../macros.ftl" as macros]
[#import "../../components/project-frameset.ftl" as projectFrameset]
[#import "../../components/delivery.ftl" as deliveryElement]

[@macros.renderHeader i18n.translate("section.projects") /]
[@macros.renderMenu i18n user /]
<div class="container">

    [@projectFrameset.renderBreadcrumb i18n group/]

    <div class="row">
        <div class="col-md-10 col-md-offset-2">
        [#if user.isAdmin() || user.isAssisting(course)]
            <a href="${course.getURI()}assignments/${assignment.assignmentId}" class="btn btn-default pull-right">
                <span class="glyphicon glyphicon-chevron-left"></span>
                ${i18n.translate("assignment.go-back-to-assignment")}
            </a>
        [/#if]
            <h4 style="line-height:34px; margin-top:0;">${i18n.translate("assignment.assignment-title", assignment.getAssignmentId(), assignment.getName())}</h4>
        </div>
    </div>

    <div class="row">
        <div class="col-md-2">
        [@projectFrameset.renderSidemenu "assignments" i18n group repository/]
        </div>
        <div class="col-md-10">
            <table class="table table-bordered">
                <tr>
                    <td>[@deliveryElement.render delivery builds/]</td>
                </tr>
            </table>

            [#if user.isAdmin() || user.isAssisting(course)]
            [#assign review = delivery.getReview()!]
                <div class="panel panel-default">
                    <div class="panel-heading">${i18n.translate("assignment.review.title")}</div>
                    <div class="panel-body">
                        <form action="" method="post" target="_self">
                            <div class="row">
                                <div class="form-group col-md-6">
                                    <label for="state">${i18n.translate("delivery.status")}</label>
                                    <select class="form-control" name="state" id="state">
                                        [#if deliveryStates?? && deliveryStates?has_content]
                                            [#list deliveryStates as deliveryState]
                                                <option value="${deliveryState?string}" [#if review?? && review?has_content && review.getState() == deliveryState]selected[/#if]>
                                                    ${i18n.translate(deliveryState.getTranslationKey())}
                                                </option>
                                            [/#list]
                                        [/#if]
                                    </select>
                                </div>

                                <div class="form-group col-md-6">
                                    <label for="grade">${i18n.translate("delivery.grade")}</label>
                                    <input type="number" class="form-control" name="grade" id="grade" min="1" max="10" step="0.1" [#if review?? && review?has_content ]value="${review.getGrade()!}"[/#if]>
                                </div>
                            </div>

                            <div class="form-group">
                                <label for="commentary">${i18n.translate("delivery.remarks")}</label>
                                <textarea class="form-control" name="commentary" id="commentary" rows="5">[#if review?? && review?has_content ]${review.getCommentary()!}[/#if]</textarea>
                            </div>


							[#assign assignment = delivery.assignment]
							<table class="table table-bordered">
								<col style="width: 30%"/>
								<col style="width: 50%"/>
								<col style="width: 10%"/>
								<col style="width: 10%"/>
								<thead>
									<tr>
										<th>Name</th>
										<th>Mastery</th>
										<th>Weight</th>
										<th>Points</th>
									</tr>
								</thead>
								[#list assignment.getTasks() as task]
								<tbody class="task" data-id="${task.id}">
									<tr class="active">
										<td colspan="2"><strong>${task.description}</strong></td>
										<td><strong>${task.getTotalWeight()}</strong></td>
										<td><strong id="lbl-total-task-${task.id}">0</strong></td>
									</tr>
									[#list task.getCharacteristics() as characteristic]
									<tr data-id="${characteristic.id}" data-weight="${characteristic.weight}" class="characteristic">
										<td>${characteristic.description}</td>
										<td>
											[#list characteristic.getLevels() as level]
											<label style="font-weight: normal; display: block;">
												<input type="radio" name="characteristic-${characteristic.id}" id="option-characteristic-${characteristic.id}" value="${level.id}"
													data-id="${level.id}" data-points="${level.points}" class="mastery" [#if delivery.getRubrics()?values?seq_contains(level)]checked[/#if]>
												${level.description} <em>(${level.points})</em>
                                            </label>
											[/#list]
										</td>
										<td>${characteristic.weight}</td>
										<td id="lbl-total-characteristic-${characteristic.id}">0</td>
									</tr>
									[/#list]
								</tbody>
								[/#list]
								<tfoot>
									<tr class="active">
										<td colspan="3"><strong>Total</strong></td>
										<td><strong><span  id="lbl-total">0</span>/${assignment.getNumberOfAchievablePoints()}</strong></td>
									</tr>
                                </tfoot>
							</table>

                            <button type="submit" class="btn btn-primary pull-right">${i18n.translate("button.label.submit")}</button>
                        </form>
                    </div>
                </div>
            [/#if]
        </div>
    </div>

</div>
[@macros.renderScripts ]
<script type="text/javascript">
	function computeTotals() {
		var total = 0;
		$('.task').each(function(i, taskElement) {
			var task = $(taskElement).data();
			task.total = 0;

			$(taskElement).find('.characteristic').each(function(i, characteristicElement) {
				var characteristic = $(characteristicElement).data();

                $(characteristicElement).find('[type="radio"]:checked').each(function(i, checkedMasteryElement) {
					var level = $(checkedMasteryElement).data();
					characteristic.points = level.points * characteristic.weight;
				});

				task.total += characteristic.points || 0;
				$(characteristicElement).find('#lbl-total-characteristic-' + characteristic.id).text(characteristic.points);
			});

			total += task.total || 0;
			$(taskElement).find('#lbl-total-task-' + task.id).text(task.total);
		});

        $('#lbl-total').text(total);
	}

	function persistMasteries() {
        $.ajax({
			type: 'POST',
			url: 'masteries',
			data: JSON.stringify(
				$('.mastery[type="radio"]:checked').map(function(i, e) {
					return $(e).data();
				}).toArray()
			),
			contentType: 'application/json',
			dataType: 'json'
		});
	}

	$(computeTotals)
	$('[type="radio"]').on('change', computeTotals)
    $('[type="radio"]').on('change', persistMasteries)
</script>

[/@macros.renderScripts]
[@macros.renderFooter /]
