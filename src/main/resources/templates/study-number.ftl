[#import "macros.ftl" as macros]
[@macros.renderHeader i18n.translate("form.login.title") /]
		<div class="container">
			<div class="row login-panel">
				<div class="col-md-6 col-md-offset-3">
					<h2><img class="logo-image" src="/static/img/logo.png"> DEVHUB</h2>
[#if error?? && error?has_content]
					<div class="alert alert-danger">
						${i18n.translate(error)}
					</div>
[/#if]
					<div class="panel panel-default">
						<div class="panel-body">
							<h3>Provide your student number</h3>
							<p>In order to export the grades from Devhub to OSIRIS, we need your student number.
							Please fill in your student number below and request your partner to do so as well,
							so we have your student numbers.
							</p>
							<form role="form" id="login-form" method="POST" action="">
                                <div class="form-group">
                                    <label for="name">Name</label>
                                    <input type="text" value="${user.name}" id="name" name="name" class="form-control" disabled="disabled">
                                </div>
								<div class="form-group">
									<label for="studentNumber">Student Number</label>
									<input type="text" value="" id="studentNumber" name="studentNumber" class="form-control" autofocus="autofocus" placeholder="Student Number">
								</div>
								<div class="form-group">
									<label for="studentNumberConfirm">Confirm</label>
									<input type="text" value="" id="studentNumberConfirm" name="studentNumberConfirm" class="form-control" placeholder="Confirm">
								</div>
								<div class="form-group">
									<input type="submit" class="btn btn-xl btn-primary pull-right" name="continue" value="Continue">
								</div>
							</form>
						</div>
					</div>
				</div>
			</div>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
