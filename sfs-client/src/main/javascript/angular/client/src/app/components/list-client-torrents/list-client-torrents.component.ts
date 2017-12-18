import { Component, ViewChild, AfterViewInit } from '@angular/core';
import {MatPaginator, MatSort, MatTableDataSource} from '@angular/material';

import { TorrentService } from '../../core/services/torrent.service';

@Component({
  selector: 'app-list-client-torrents',
  templateUrl: './list-client-torrents.component.html',
  styleUrls: ['./list-client-torrents.component.css']
})
export class ListClientTorrentsComponent implements AfterViewInit {
  displayedColumns = ['name', 'status', 'activityDate', 'percentDone'];
  dataSource = new MatTableDataSource();

  isLoadingData = false;
  apiError = false;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  constructor(public torrentService: TorrentService) { }

  // TODO loading indicator

  /**
   * Set the paginator and sort after the view init since this component will
   * be able to query its view for the initialized paginator and sort.
   */
  ngAfterViewInit() {

    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;

    this.torrentService.getClientTorrents().subscribe(returnedClientTorrents => {
        this.isLoadingData = false;
        this.dataSource.data = returnedClientTorrents;
      },
      error => {
        this.isLoadingData = false;
        this.apiError = true;
          // this.getErrorMessage = <any>error;
          console.error(error);
          // console.error('getErrorMessage=' + this.getErrorMessage);
      }
    );

  }

  applyFilter(filterValue: string) {
    filterValue = filterValue.trim(); // Remove whitespace
    filterValue = filterValue.toLowerCase(); // Datasource defaults to lowercase matches
    this.dataSource.filter = filterValue;
  }
}
