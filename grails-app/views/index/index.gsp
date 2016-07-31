<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <title>--app title--</title>
    </head>
    <body>
    
        <h1>Downloading torrents</h1>
        <table class="do-data-table" data-searching="false">
          	<thead>
           		<tr>
           			<th>Feed</th>
           			<th>Torrent</th>
           			<th class="default-sort default-sort-order-desc">Date added</th>
           			<th>Progress</th>
           		</tr>
           	</thead>
           	<tbody>
           		<g:each in="${downloadingTorrents}" var="downloadingTorrent">
           			<tr>
           				<td>
           					<g:link controller="feedProvider" action="show" id="${downloadingTorrent.feedProvider.id}">${downloadingTorrent.feedProvider.name}</g:link>
           				</td>
           				<td>
           					<g:link controller="torrent" action="show" id="${downloadingTorrent.torrent.id}">${downloadingTorrent.torrent.name}</g:link>
           				</td>
           				<td>${downloadingTorrent.torrent.dateCreated}</td>
           				<td></td>
           			</tr>
           		</g:each>
           	</tbody>
        </table>
        
        <h1>Recently notified torrents</h1>
        <table class="do-data-table" data-searching="false">
           	<thead>
           		<tr>
           			<th>Feed</th>
           			<th>Torrent</th>
           			<th>Action</th>
           			<th class="default-sort default-sort-order-desc">Date added</th>
           		</tr>
           	</thead>
           	<tbody>
           		<g:each in="${notifiedTorrents}" var="notifiedTorrent">
           			<tr>
           				<td>
           					<g:link controller="feedProvider" action="show" id="${notifiedTorrent.feedProvider.id}">${notifiedTorrent.feedProvider.name}</g:link>
           				</td>
           				<td>
           					<g:link controller="torrent" action="show" id="${notifiedTorrent.torrent.id}">${notifiedTorrent.torrent.name}</g:link>
           				</td>
           				<td>${notifiedTorrent.torrent.dateCreated}</td>
           				<td></td>
           			</tr>
           		</g:each>
           	</tbody>
        </table>
        
        <h1>Recently completed torrents</h1>
        <table class="do-data-table" data-searching="false">
           	<thead>
           		<tr>
           			<th>Feed</th>
           			<th>Torrent</th>
           			<th>Action</th>
           			<th class="default-sort default-sort-order-desc">Date added</th>
           		</tr>
           	</thead>
           	<tbody>
           		<g:each in="${completedTorrents}" var="completedTorrent">
           			<tr>
           				<td>
           					<g:link controller="feedProvider" action="show" id="${completedTorrent.feedProvider.id}">${completedTorrent.feedProvider.name}</g:link>
           				</td>
           				<td>
           					<g:link controller="torrent" action="show" id="${completedTorrent.torrent.id}">${completedTorrent.torrent.name}</g:link>
           				</td>
           				<td></td>
           				<td>${completedTorrent.torrent.dateCreated}</td>
           			</tr>
           		</g:each>
           	</tbody>
        </table>
        
    </body>
</html>