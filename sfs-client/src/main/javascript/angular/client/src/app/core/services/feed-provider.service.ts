import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { URLSearchParams } from '@angular/http';

import { ServerService } from '../server/server.service';
import { Api } from '../model/api';
import { FeedProvider } from '../model/feed-provider';
import { Torrent } from '../model/torrent';
import { FilterAttribute } from '../model/filter-attribute';

@Injectable()
export class FeedProviderService {

    private restUrl = '/sfs-server/api/feedProviders';

    constructor(private serverService: ServerService) { }

    createFeedProvider(feedProviderData: FeedProvider): Observable<FeedProvider> {
        return this.serverService.postRequest(this.restUrl, feedProviderData, null);
    }

    getFeedProviders(sort: string, order: string, page: number, size: number): Observable<Api<FeedProvider>> {
        const urlSearchParams = new URLSearchParams();
        if (sort && order) {
            urlSearchParams.append('sort', sort + ',' + order);
        }
        if (page != null && size != null) {
            urlSearchParams.append('page', String(page));
            urlSearchParams.append('size', String(size));
        }
        return this.serverService.getRequest(this.restUrl, urlSearchParams);
    }

    getFeedProvider(id: number): Observable<FeedProvider> {
        return this.serverService.getRequest(this.restUrl + '/' + id, null );
    }

    updateFeedProvider(id: number, feedProviderData: FeedProvider): Observable<FeedProvider> {
        return this.serverService.putRequest(this.restUrl + '/' + id, feedProviderData, null);
    }

    deleteFeedProvider(id: number): Observable<FeedProvider> {
        return this.serverService.deleteRequest(this.restUrl + '/' + id, null);
    }

    getTorrents(id: number, sort: string, order: string, page: number, size: number): Observable<Api<Torrent>> {
        const urlSearchParams = new URLSearchParams();
        if (sort && order) {
            urlSearchParams.append('sort', sort + ',' + order);
        }
        if (page != null && size != null) {
            urlSearchParams.append('page', String(page));
            urlSearchParams.append('size', String(size));
        }
        return this.serverService.getRequest(this.restUrl + '/' + id + '/torrents', urlSearchParams);
    }

    getFeedProviderFilterAttributes(id: number): Observable<Array<FilterAttribute>> {
        return this.serverService.getRequest(this.restUrl + '/' + id + '/filterAttributes', null);
    }

    replaceFeedProviderAttributes(id: number, filterAttributes: Array<FilterAttribute>): Observable<Array<FilterAttribute>> {
        return this.serverService.putRequest(this.restUrl + '/' + id + '/filterAttributes', filterAttributes, null);
    }

    downloadTorrent(id: number, torrentStateId: number) {
        return this.serverService.putRequest(this.restUrl + '/' + id + '/torrents/' + torrentStateId + '/download', null, null);
    }

}
