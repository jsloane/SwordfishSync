import { Component, OnInit } from '@angular/core';

import { Router, NavigationEnd, ActivatedRoute } from '@angular/router';
import { Title } from '@angular/platform-browser';

import 'rxjs/add/operator/filter';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/mergeMap';

// import { NgProgress } from '@ngx-progressbar/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']// ,
  // encapsulation: ViewEncapsulation.None,
  // preserveWhitespaces: false
})
export class AppComponent implements OnInit {
    constructor(private router: Router, private activatedRoute: ActivatedRoute, private titleService: Title/*, public progress: NgProgress*/) { }

    ngOnInit() {
        // this.progress.start();
        this.router.events
            // .filter((event) => event instanceof NavigationEnd)
            .map(() => this.activatedRoute)
            .map((route) => {
                while (route.firstChild) { route = route.firstChild; }
                return route;
            })
            .filter((route) => route.outlet === 'primary')
            .mergeMap((route) => route.data)
            .subscribe((event) => this.titleService.setTitle(event['title'])); // feed the title value from the router to the title service
    }

}
