import { Component, OnInit, AfterViewInit, ViewChild } from '@angular/core';

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
import { FeedProvider } from '../../core/model/feed-provider';


/** An example database that the data source uses to retrieve data for the table. */
export class FeedProviderDao {
    constructor(public feedProviderService: FeedProviderService) {}

    getFeedProviders(sort: string, order: string, page: number, size: number): Observable<Api<FeedProvider>> {
        return this.feedProviderService.getFeedProviders(sort, order, page, size);
    }
}

@Component({
  selector: 'app-list-feed-providers',
  templateUrl: './list-feed-providers.component.html',
  styleUrls: ['./list-feed-providers.component.css']
})
export class ListFeedProvidersComponent implements OnInit, AfterViewInit  {
  displayedColumns = ['enabled', 'actions', 'name', 'action', 'status', 'feedTtl', 'feedLastFetched', 'lastProcessed', 'downloadDirectory'];
  feedProviderDatabase: FeedProviderDao | null;
  dataSource = new MatTableDataSource();

  resultsLength = 0;
  isLoadingData = false;
  apiError = false;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  constructor(private http: HttpClient, public feedProviderService: FeedProviderService) { }

  ngOnInit() { }

  ngAfterViewInit() {
    this.feedProviderDatabase = new FeedProviderDao(this.feedProviderService);

    // If the user changes the sort order, reset back to the first page.
    this.sort.sortChange.subscribe(() => this.paginator.pageIndex = 0);

    Observable.merge(this.sort.sortChange, this.paginator.page)
        .startWith(null)
        .switchMap(() => {
          this.isLoadingData = true;
          return this.feedProviderDatabase.getFeedProviders(
              this.sort.active, this.sort.direction, this.paginator.pageIndex, this.paginator.pageSize);
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

}

