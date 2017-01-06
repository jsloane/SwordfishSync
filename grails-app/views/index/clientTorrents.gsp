<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
    </head>
    <body>
    
        <h1>Client torrents</h1>
        <table class="do-data-table" data-searching="false">
          	<thead>
           		<tr>
           			<th class="default-sort default-sort-order-desc">Name</th>
           			<th>Status</th>
           			<th>Activity Date</th>
           			<th>Progress</th>
           		</tr>
           	</thead>
           	<tbody>
           		<g:each in="${clientTorrents}" var="clientTorrent">
           			<tr>
           				<td>
           					${clientTorrent.name}
           				</td>
           				<td>
           					${clientTorrent.status}
           				</td>
           				<td>
           					${clientTorrent.activityDate}
           				</td>
           				<td>
           					<progress value="${clientTorrent.percentDone}" max="1"></progress>
           				</td>
           			</tr>
           		</g:each>
           	</tbody>
        </table>
        
    </body>
</html>