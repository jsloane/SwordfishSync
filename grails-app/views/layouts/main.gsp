<!doctype html>
<html lang="en" class="no-js">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<meta http-equiv="X-UA-Compatible" content="IE=edge"/>
		<title>
			<g:layoutTitle default="${swordfishsync.Setting.valueFor('app.title')}"/>
		</title>
		<meta name="viewport" content="width=device-width, initial-scale=1"/>
		
		<asset:javascript src="application.js"/>
		<asset:stylesheet src="application.css"/>
		
		<g:layoutHead/>
	</head>
	<body data-base-url="${createLink(uri: '/')}">
	    <div class="navbar navbar-default navbar-static-top" role="navigation">
	        <div class="container">
	            <div class="navbar-header">
	                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
	                    <span class="sr-only">Toggle navigation</span>
	                    <span class="icon-bar"></span>
	                    <span class="icon-bar"></span>
	                    <span class="icon-bar"></span>
	                </button>
	                <a class="navbar-brand" href="/#">
	                    <i class="fa grails-icon">
	                        <asset:image src="grails-cupsonly-logo-white.svg"/>
	                    </i> Grails
	                </a>
	            </div>
	            <div class="navbar-collapse collapse" aria-expanded="false" style="height: 0.8px;">
	                <ul class="nav navbar-nav navbar-right">
	                    <g:pageProperty name="page.nav" />
	                </ul>
	            </div>
	        </div>
	    </div>
		
        <a href="#content" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <li><g:link class="list" controller="feedProvider" action="index">Feeds</g:link></li>
                <li><g:link class="list" controller="index" action="clientTorrents">Client Torrents</g:link></li>
                <li><g:link class="list" controller="configuration" action="index">Settings</g:link></li>
            </ul>
        </div>
        <div id="#content" class="content" role="main">
	        <div>
	        	<%-- todo: action message (save succes/failure, etc. or just use flash.message? just flash.message, but check if error/info) --%> 
	        	
	            <g:if test="${flash.message}">
	                <div class="message" role="status">${flash.message}</div>
	            </g:if>
	            <g:if test="${flash.error}">
	            	<ul class="errors">
	                	<li>${flash.error}</li>
	                </ul>
	            </g:if>
	            <g:if test="${errorMessages}">
	            	<ul class="errors">
			           	<g:each in="${errorMessages}" var="errorMessage">
	                		<li>${errorMessage}</li>
	                	</g:each>
	                </ul>
	            </g:if>
	            
	        	<div class="dismissable">
		            <g:if test="${messages}">
		            	<g:findAll in="${messages}" expr="it.type == swordfishsync.Message.Type.DANGER">
			           		<g:each in="${it}" var="message">
								<div class="alert alert-danger">
									<a href="#" class="close" data-id="${message.id}" data-dismiss="alert" aria-label="close">&times;</a>
									<strong>Error! [<g:formatDate date="${message.dateCreated}" type="datetime" style="MEDIUM"/>]</strong>
									<g:if test="${message.feedProvider}">
										<strong>
											[${message.feedProvider.name}<g:if test="${message.torrent}"> : ${message.torrent.name}</g:if>]:
										</strong>
									</g:if>
									${message.message}
								</div>
			          		</g:each>
		            	</g:findAll>
		            	<g:findAll in="${messages}" expr="it.type == swordfishsync.Message.Type.WARNING">
			           		<g:each in="${it}" var="message">
								<div class="alert alert-warning">
									<a href="#" class="close" data-id="${message.id}" data-dismiss="alert" aria-label="close">&times;</a>
									<strong>Warning! [<g:formatDate date="${message.dateCreated}" type="datetime" style="MEDIUM"/>]</strong>
									<g:if test="${message.feedProvider}">
										<strong>
											[${message.feedProvider.name}<g:if test="${message.torrent}"> : ${message.torrent.name}</g:if>]:
										</strong>
									</g:if>
									${message.message}
								</div>
			          		</g:each>
		            	</g:findAll>
		            	<g:findAll in="${messages}" expr="it.type == swordfishsync.Message.Type.SUCCESS">
			           		<g:each in="${it}" var="message">
								<div class="alert alert-success">
									<a href="#" class="close" data-id="${message.id}" data-dismiss="alert" aria-label="close">&times;</a>
									<strong>Success! [<g:formatDate date="${message.dateCreated}" type="datetime" style="MEDIUM"/>]</strong>
									<g:if test="${message.feedProvider}">
										<strong>
											[${message.feedProvider.name}<g:if test="${message.torrent}"> : ${message.torrent.name}</g:if>]:
										</strong>
									</g:if>
									${message.message}
								</div>
			          		</g:each>
		            	</g:findAll>
		            	<g:findAll in="${messages}" expr="it.type == swordfishsync.Message.Type.INFO">
			           		<g:each in="${it}" var="message">
								<div class="alert alert-info">
									<a href="#" class="close" data-id="${message.id}" data-dismiss="alert" aria-label="close">&times;</a>
									<strong>Info!</strong>
									<g:if test="${message.feedProvider}">
										<strong>
											[${message.feedProvider.name}<g:if test="${message.torrent}"> : ${message.torrent.name}</g:if>]:
										</strong>
									</g:if>
									${message.message}
								</div>
			          		</g:each>
		            	</g:findAll>
		            </g:if>
	            </div>
	            
	            <g:if test="${flash.errorMessages}">
					<div class="alert alert-danger">
						<strong>Error!</strong>
		           		<g:each in="${flash.errorMessages}" var="errorMessage">
							<p>${errorMessage}</p>
		          		</g:each>
					</div>
	            </g:if>
	            <g:if test="${flash.warningMessages}">
					<div class="alert alert-warning">
						<strong>Warning!</strong>
		           		<g:each in="${flash.warningMessages}" var="warningMessage">
							<p>${warningMessage}</p>
		          		</g:each>
					</div>
	            </g:if>
	            <g:if test="${flash.successMessages}">
					<div class="alert alert-success">
						<strong>Success!</strong>
		           		<g:each in="${flash.successMessages}" var="successMessage">
							<p>${successMessage}</p>
		          		</g:each>
					</div>
	            </g:if>
	            
            </div>
            
			<g:layoutBody/>
			
    	</div>
		
	    <div class="footer" role="contentinfo"></div>
	    
	    <div id="spinner" class="spinner" style="display:none;">
	        <g:message code="spinner.alt" default="Loading&hellip;"/>
	    </div>
	    
	</body>
</html>
