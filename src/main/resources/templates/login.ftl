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
							<h3>${i18n.translate("form.login.title")}</h3>
							<form role="form" method="POST" action="">
								<div class="form-group">
									<label for="netID">${i18n.translate("form.login.net-id.label")}</label>
									<input type="text" id="netID" name="netID" class="form-control" autofocus="autofocus" placeholder="${i18n.translate("form.login.net-id.label")}">
								</div>
								<div class="form-group">
									<label for="password">${i18n.translate("form.login.password.label")}</label>
									<input type="password" id="password" name="password" class="form-control" placeholder="${i18n.translate("form.login.password.label")}">
								</div>
								<div class="form-group">
									<input type="submit" class="btn btn-xl btn-primary pull-right" name="login" value="${i18n.translate("form.login.buttons.login.caption")}">
								</div>
							</form>
						</div>
					</div>
				</div>
			</div>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]
