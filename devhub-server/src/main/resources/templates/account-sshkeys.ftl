[#import "macros.ftl" as macros]
[@macros.renderHeader "Profile" /]
[@macros.renderProjectMenu user "" /]
		<div class="content">
			<ol class="breadcrumb breadcrumb-nav">
				<li>
					<a href="/projects"><i class="glyphicon glyphicon-home"></i></a>
				</li>
				<li>
					<a href="/accounts/${user.studentNumber?c}">Profile of #${user.studentNumber?c}</a>
				</li>
				<li>
					<a href="/accounts/${user.studentNumber?c}/ssh-keys">SSH Keys</a>
				</li>
			</ol>
			<h1>SSH keys <button class="btn btn-primary pull-right"><i class="glyphicon glyphicon-plus"></i> Add new SSH key</button></h1>
			<table class="table table-striped">
				<thead>
					<tr>
						<th>SSH Key</th>
						<th class="minimal-width">Actions</th>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Apple iMac 2009 (thuis)</td>
						<td class="minimal-width"><a href=""><i class="glyphicon glyphicon-remove"></i></a></td>
					</tr>
					<tr>
						<td>Apple iMac 2013 (werk)</td>
						<td class="minimal-width"><a href=""><i class="glyphicon glyphicon-remove"></i></a></td>
					</tr>
					<tr>
						<td>Apple MacBook Air 2011 (prive)</td>
						<td class="minimal-width"><a href=""><i class="glyphicon glyphicon-remove"></i></a></td>
					</tr>
					<tr>
						<td>Apple MacBook Air 2013 (zaak)</td>
						<td class="minimal-width"><a href=""><i class="glyphicon glyphicon-remove"></i></a></td>
					</tr>
					<tr>
						<td colspan="2" class="no-data">You have not yet defined any SSH keys!</td>
					</tr>
				</tbody>
			</table>
		</div>
[@macros.renderScripts /]
[@macros.renderFooter /]