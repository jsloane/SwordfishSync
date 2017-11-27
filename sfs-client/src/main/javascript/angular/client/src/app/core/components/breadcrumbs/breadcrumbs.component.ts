import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { Router, ActivatedRoute, NavigationEnd } from '@angular/router';
import 'rxjs/add/operator/filter';

@Component({
  selector: 'app-breadcrumbs',
  templateUrl: './breadcrumbs.component.html',
  styleUrls: ['./breadcrumbs.component.css']// ,
  // encapsulation: ViewEncapsulation.None // needed to style generated mat-toolbar-row
})
export class BreadcrumbsComponent implements OnInit {

  breadcrumbs: Array<Object>;
  homeRouterLink: string;

  constructor(private router: Router, private route: ActivatedRoute) { }

  ngOnInit() {
    this.router.events
    .filter(event => event instanceof NavigationEnd)
    .subscribe(event => {
      let homeRouterLink = '/';
      this.breadcrumbs = [];
      let currentRoute = this.route.root, url = '';
      if (currentRoute.snapshot.data && currentRoute.snapshot.data['breadcrumbHomeRouterLink']) {
          homeRouterLink = currentRoute.snapshot.data['breadcrumbHomeRouterLink'];
      }
      do {
        const childrenRoutes = currentRoute.children;
        currentRoute = null;
        childrenRoutes.forEach(route => {
          if (route.outlet === 'primary') {
            const routeSnapshot = route.snapshot;
            url += '/' + routeSnapshot.url.map(segment => segment.path).join('/');
            const breadcrumb = {
              label: null,
              url: url
            };
            if (route.snapshot.data && route.snapshot.data['breadcrumb']) {
                breadcrumb.label = route.snapshot.data['breadcrumb'];
            }
            if (route.snapshot.data && route.snapshot.data['breadcrumbHomeRouterLink']) {
                homeRouterLink = route.snapshot.data['breadcrumbHomeRouterLink'];
            }
            this.breadcrumbs.push(breadcrumb);
            currentRoute = route;
          }
        });
      } while (currentRoute);
      this.homeRouterLink = homeRouterLink;
    });
  }

}
