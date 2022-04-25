import { Component, OnInit, AfterViewInit, ViewChild, Input } from '@angular/core';
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
import { TorrentService } from '../../core/services/torrent.service';
import { Api } from '../../core/model/api';
import { Torrent } from '../../core/model/torrent';

/** An example database that the data source uses to retrieve data for the table. */
export class TorrentDao {
  constructor(public torrentService: TorrentService) {}

  getTorrentsByStatuses(statuses: string[], sort: string, order: string, page: number, size: number): Observable<Api<Torrent>> {
      return this.torrentService.getTorrentsByStatuses(statuses, sort, order, page, size);
  }
}

@Component({
  selector: 'app-list-torrents-by-status',
  templateUrl: './list-torrents-by-status.component.html',
  styleUrls: ['./list-torrents-by-status.component.css']
})
export class ListTorrentsByStatusComponent implements OnInit, AfterViewInit {
  displayedColumns = ['actions', 'feedProviderName', 'torrentName', 'torrentDateAdded', 'clientActivityDate', 'clientPercentDone'];
  additionalActions = {reprocess: false};
  torrentDatabase: TorrentDao | null;
  dataSource = new MatTableDataSource();

  resultsLength = 0;
  isLoadingData = true;
  apiError = false;

  @Input() type: string;
  @Input() statuses: string[];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  constructor(public feedProviderService: FeedProviderService, public torrentService: TorrentService) { }

  ngOnInit() {
      if (this.type === 'notified' || this.type === 'completed') {
          this.displayedColumns = ['actions', 'feedProviderName', 'torrentName', 'torrentDateAdded'];
      }
      if (this.type === 'completed') {
          this.additionalActions.reprocess = true;
      }
  }

  ngAfterViewInit() {
    this.torrentDatabase = new TorrentDao(this.torrentService);

    // If the user changes the sort order, reset back to the first page.
    this.sort.sortChange.subscribe(() => this.paginator.pageIndex = 0);

    Observable.merge(this.sort.sortChange, this.paginator.page)
        .startWith(null)
        .switchMap(() => {
          this.isLoadingData = true;
          return this.torrentDatabase.getTorrentsByStatuses(
            this.statuses, this.sort.active, this.sort.direction, this.paginator.pageIndex, this.paginator.pageSize);
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
    this.feedProviderService.downloadTorrent(torrent.feedProviderId, torrent.id).subscribe(result => {
        console.log(result);
        torrent.status = 'IN_PROGRESS'; // TODO use constant in Torrent file
      },
      error => {
          console.error(error);
      }
    );
  }

  recompleteTorrent(torrent: Torrent) {
    // TODO disable button/loading indicator
    this.feedProviderService.recompleteTorrent(torrent.feedProviderId, torrent.id).subscribe(result => {
        console.log(result);
      },
      error => {
          console.error(error);
      }
    );
  }

}
