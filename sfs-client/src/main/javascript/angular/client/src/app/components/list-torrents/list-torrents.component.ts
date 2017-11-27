import { Component, OnInit, OnDestroy, AfterViewInit, ViewChild } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import {HttpClient} from '@angular/common/http';
import { HttpClientModule } from '@angular/common/http';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/merge';
import 'rxjs/add/observable/of';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/switchMap';

import { FeedProviderService } from '../../core/services/feed-provider.service';
import { Api } from '../../core/model/api';
import { Torrent } from '../../core/model/torrent';


/** An example database that the data source uses to retrieve data for the table. */
export class FeedProviderDao {
    constructor(public feedProviderService: FeedProviderService) {}

    getTorrents(id: number, sort: string, order: string, page: number, size: number): Observable<Api<Torrent>> {
        return this.feedProviderService.getTorrents(id, sort, order, page, size);
    }
}

@Component({
  selector: 'app-list-torrents',
  templateUrl: './list-torrents.component.html',
  styleUrls: ['./list-torrents.component.css']
})
export class ListTorrentsComponent implements OnInit, AfterViewInit, OnDestroy  {
  displayedColumns = ['actions', 'torrentName', 'status', 'torrentDatePublished', 'torrentDateAdded',
      'torrentDateCompleted', 'torrentInCurrentFeed', 'torrentClientTorrentId', 'torrentHashString'];
  feedProviderDatabase: FeedProviderDao | null;
  dataSource = new MatTableDataSource();

  resultsLength = 0;
  isLoadingData = false;
  apiError = false;

  feedProviderId: number;

  private routeSub: any;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  // constructor(private http: HttpClient, public feedProviderService: FeedProviderService, private route: ActivatedRoute) { }
  constructor(public feedProviderService: FeedProviderService, private route: ActivatedRoute) { }

  ngOnInit() {
      this.routeSub = this.route.params.subscribe(params => {
        this.feedProviderId = Number(params['id']);

        if (isFinite(this.feedProviderId)) {

        }
      });
  }

  ngOnDestroy() {
    if (this.routeSub) {
     this.routeSub.unsubscribe();
    }
  }

  ngAfterViewInit() {
    this.feedProviderDatabase = new FeedProviderDao(this.feedProviderService);

    // If the user changes the sort order, reset back to the first page.
    this.sort.sortChange.subscribe(() => this.paginator.pageIndex = 0);

    Observable.merge(this.sort.sortChange, this.paginator.page)
        .startWith(null)
        .switchMap(() => {
          this.isLoadingData = true;
          return this.feedProviderDatabase.getTorrents(
              this.feedProviderId, this.sort.active, this.sort.direction, this.paginator.pageIndex, this.paginator.pageSize);
        })
        .map(data => {
          // Flip flag to show that loading has finished.
          this.isLoadingData = false;
          this.apiError = false;
          // this.resultsLength = data.total_count;
          this.resultsLength = data.totalElements;

          return data.content;
        })
        .catch(() => {
          this.isLoadingData = false;
          // Catch if an error occurred.
          this.apiError = true;
          return Observable.of([]);
        })
        .subscribe(data => this.dataSource.data = data);
  }

  applyFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // Datasource defaults to lowercase matches
    this.dataSource.filter = filterValue;
  }

  downloadTorrent(torrent: Torrent) {
      // TODO disable button/loading indicator
      this.feedProviderService.downloadTorrent(this.feedProviderId, torrent.id).subscribe(result => {
          console.log(result);
          torrent.status = 'IN_PROGRESS'; // TODO use constant in Torrent file
      },
      error => {
          // this.getErrorMessage = <any>error;
          console.error(error);
          // console.error('getErrorMessage=' + this.getErrorMessage);
      }
    );
  }


}

