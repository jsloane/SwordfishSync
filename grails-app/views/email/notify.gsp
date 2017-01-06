<%@ page contentType="text/html"%>
<html>
	<head></head>
	<body>
		<g:if test="${swordfishsync.NotificationService.Type.AVAILABLE.equals(type)}">
			<g:if test="${swordfishsync.TorrentContent.Type.TV.equals(torrentContent.type)}">
				<p>
					<b>${torrent.name}</b> is available to download.
					<g:if test="${torrentContent.detailsUrl}">
						Details: ${torrentContent.detailsUrl}
					</g:if>
				</p>
				<g:emailImage url="${torrentContent.backdropUrl}" />
				<p>${torrentContent.episodeTitle}</p>
				<p>${torrentContent.episodeDescription}</p>
				<g:emailImage url="${torrentContent.posterUrl}" />
				<p>${torrentContent.extraInfo}</p>
				<p style="color:#FFFFFF;">${tvdbNotice}</p>
			</g:if>
			<g:elseif test="${swordfishsync.TorrentContent.Type.MOVIE.equals(torrentContent.type)}">
				<p>
					<b>${torrent.name}</b> is available to download.
					<g:if test="${torrentContent.detailsUrl}">
						Details: ${torrentContent.detailsUrl}
					</g:if>
				</p>
				<g:emailImage url="${torrentContent.backdropUrl}" />
				<p>${torrentContent.extraInfo}</p>
				<g:emailImage url="${torrentContent.posterUrl}" />
				<p style="color:#FFFFFF;">${tmdbNotice}</p>
			</g:elseif>
			<g:else>
				<p>
					<b>${torrent.name}</b> is available to download.
					<g:if test="${torrentContent.detailsUrl}">
						Details: ${torrentContent.detailsUrl}
					</g:if>
				</p>
			</g:else>
		</g:if>
		<g:elseif test="${swordfishsync.NotificationService.Type.COMPLETED.equals(type)}">
			<g:if test="${swordfishsync.TorrentContent.Type.TV.equals(torrentContent.type)}">
				<p><b>${torrent.name}</b> has finished downloading to: ${torrentContent.downloadDirectory}</p>
				<g:emailImage url="${torrentContent.backdropUrl}" />
				<p>${torrentContent.episodeTitle}</p>
				<p>${torrentContent.episodeDescription}</p>
				<g:emailImage url="${torrentContent.posterUrl}" />
				<p>${torrentContent.extraInfo}</p>
				<p style="color:#FFFFFF;">${tvdbNotice}</p>
			</g:if>
			<g:elseif test="${swordfishsync.TorrentContent.Type.MOVIE.equals(torrentContent.type)}">
				<p><b>${torrent.name}</b> has finished downloading to: ${torrentContent.downloadDirectory}</p>
				<g:emailImage url="${torrentContent.backdropUrl}" />
				<p>${torrentContent.extraInfo}</p>
				<g:emailImage url="${torrentContent.posterUrl}" />
				<p style="color:#FFFFFF;">${tmdbNotice}</p>
			</g:elseif>
			<g:else>
				<p><b>${torrentContent.name}</b> has finished downloading to: ${torrentContent.downloadDirectory}</p>
			</g:else>
		</g:elseif>
	</body>
</html>