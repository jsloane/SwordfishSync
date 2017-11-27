import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-page-not-found',
  templateUrl: './page-not-found.component.html',
  styleUrls: ['./page-not-found.component.css']
})

/**
 * Display an error when attemping to route
 * to a non existant page. The page will
 * also show the bad url.
 */
export class PageNotFoundComponent implements OnInit {

  constructor(public router: Router) { }

  ngOnInit() {
    console.error('page not found for route ' + this.router.url);
  }

}
