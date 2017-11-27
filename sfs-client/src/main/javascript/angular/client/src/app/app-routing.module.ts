
import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

// import { CreateOutlineComponent } from './outlines/create/create-outline.component';
// import { ViewOutlineComponent } from './outlines/view/view-outline/view-outline.component';
// import { DetailOutlineComponent } from './outlines/detail/detail-outline.component';
import { PageNotFoundComponent } from './core/components/page-not-found/page-not-found.component';
// import { DashboardsComponent } from './dashboards/dashboards.component';
// import { OutlineListComponent } from './dashboards/outline-list/outline-list.component';
import { HomeComponent } from './components/home/home.component';
import { AdminComponent } from './components/admin/admin.component';
import { ListFeedProvidersComponent } from './components/list-feed-providers/list-feed-providers.component';
import { ListTorrentsComponent } from './components/list-torrents/list-torrents.component';
import { ListClientTorrentsComponent } from './components/list-client-torrents/list-client-torrents.component';
// import { CreateFeedProviderComponent } from './components/create-feed-provider/create-feed-provider.component';
import { EditFeedProviderComponent } from './components/edit-feed-provider/edit-feed-provider.component';
// import { ViewFeedProviderComponent } from './components/view-feed-provider/view-feed-provider.component';

export const routes: Routes = [
    // { path: 'create', component: CreateOutlineComponent },
    // { path: 'view', component: ViewOutlineComponent },
    // { path: 'detail', component: DetailOutlineComponent },
    {
        path: 'home', component: HomeComponent, data: {
                    title: 'SwordfishSync - Home'
                }
        /*children: [
            { path: '', redirectTo: 'author', pathMatch: 'full' },
            {
                path: 'author',
                component: OutlineListComponent,
                data: {
                    title: 'Home - Subject Outline Sandbox'
                },
            },
            {
                path: 'qa',
                component: OutlineListComponent,
                data: {
                    title: 'Home - Subject Outline Sandbox',
                    breadcrumbHomeRouterLink: '/dashboard/qa'
                },
            }
        ]*/
    },
    {
        path: 'feedProviders', component: ListFeedProvidersComponent,
        children: [
            /*{ path: '', redirectTo: 'list', pathMatch: 'full' },
            {
                path: 'list',
                component: OutlineListComponent,
                data: {
                    title: 'Home - Subject Outline Sandbox'
                },
            },*/
            /*{
                path: 'create',
                component: CreateFeedProviderComponent,
                data: {
                    title: 'Create',
                    breadcrumbHomeRouterLink: '/feedProviders/create'
                },
            }*/
        ]
    },
    {
        path: 'feedProviders/create',
        component: EditFeedProviderComponent,
        data: {
            title: 'Create',
            breadcrumbHomeRouterLink: '/',
            mode: 'create'
        }
    },
    {
        path: 'feedProviders/edit/:id',
        component: EditFeedProviderComponent,
        data: {
            title: 'Edit',
            breadcrumbHomeRouterLink: '/',
            mode: 'edit'
        }
    },
    {
        path: 'feedProviders/view/:id',
        component: EditFeedProviderComponent,
        data: {
            title: 'View',
            breadcrumbHomeRouterLink: '/',
            mode: 'view'
        }
    },
    {
        path: 'feedProviders/torrents/:id',
        component: ListTorrentsComponent,
        data: {
            title: 'Torrents',
            breadcrumbHomeRouterLink: '/'
        }
    },
    {
        path: 'clientTorrents',
        component: ListClientTorrentsComponent,
        data: {
            title: 'Client Torrents',
            breadcrumbHomeRouterLink: '/'
        }
    },
    {
        path: 'admin',
        component: AdminComponent,
        data: {
            title: 'Admin',
            breadcrumbHomeRouterLink: '/'
        }
    },
    // { path: '', redirectTo: 'home', pathMatch: 'full' },
    { path: '', redirectTo: 'home', pathMatch: 'full' },
    { path: '**', component: PageNotFoundComponent }
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule { }
