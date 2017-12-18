import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { HttpParams } from '@angular/common/http';

import { ServerService } from '../server/server.service';
import { Api } from '../model/api';
import { Torrent } from '../model/torrent';
import { ClientTorrent } from '../model/client-torrent';

@Injectable()
export class TorrentService {

    private restUrl = '/sfs-server/api/torrents';

    constructor(private serverService: ServerService) { }

    getTorrentsByStatuses(statuses: string[], sort: string, order: string, page: number, size: number): Observable<Api<Torrent>> {
        let httpParams = new HttpParams();
        if (sort && order) {
            httpParams = httpParams.append('sort', sort + ',' + order);
        }
        if (page != null && size != null) {
            httpParams = httpParams.append('page', String(page));
            httpParams = httpParams.append('size', String(size));
        }
        for (const status of statuses) {
            httpParams = httpParams.append('statuses', status);
        }

        return this.serverService.getRequest(this.restUrl + '/torrentStatesByStatus', httpParams);
    }

    getClientTorrents(): Observable<Array<ClientTorrent>> {
        return this.serverService.getRequest(this.restUrl + '/clientTorrents', null);
    }

}
