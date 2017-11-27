import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { URLSearchParams } from '@angular/http';

import { ServerService } from '../server/server.service';
import { Api } from '../model/api';
import { Torrent } from '../model/torrent';
import { ClientTorrent } from '../model/client-torrent';

@Injectable()
export class TorrentService {

    private restUrl = '/sfs-server/api/torrents';

    constructor(private serverService: ServerService) { }

    getTorrentsByStatuses(statuses: string[], sort: string, order: string, page: number, size: number): Observable<Api<Torrent>> {
        const urlSearchParams = new URLSearchParams();
        if (sort && order) {
            urlSearchParams.append('sort', sort + ',' + order);
        }
        if (page != null && size != null) {
            urlSearchParams.append('page', String(page));
            urlSearchParams.append('size', String(size));
        }
        for (const status of statuses) {
            urlSearchParams.append('statuses', status);
        }
        return this.serverService.getRequest(this.restUrl + '/torrentStatesByStatus', urlSearchParams);
    }

    getClientTorrents(): Observable<Array<ClientTorrent>> {
        return this.serverService.getRequest(this.restUrl + '/clientTorrents', null);
    }

}
