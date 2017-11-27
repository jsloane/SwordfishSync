import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpModule } from '@angular/http';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';

// dynamic forms
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import { DynamicFormsMaterialUIModule } from '@ng-dynamic-forms/ui-material';

// app
import { AppMaterialModule } from './shared/app-material.module';

import { AppRoutingModule } from './app-routing.module';

// app specific
import { ServerErrorComponent } from './core/server/server-error.component';
import { ServerService } from './core/server/server.service';
import { FeedProviderService } from './core/services/feed-provider.service';
import { TorrentService } from './core/services/torrent.service';

import { PageNotFoundComponent } from './core/components/page-not-found/page-not-found.component';
import { BreadcrumbsComponent } from './core/components/breadcrumbs/breadcrumbs.component';

import { ConfirmationDialogComponent } from './core/components/confirmation-dialog/confirmation-dialog.component';

import { HomeComponent } from './components/home/home.component';
import { ListFeedProvidersComponent } from './components/list-feed-providers/list-feed-providers.component';
import { EditFeedProviderComponent } from './components/edit-feed-provider/edit-feed-provider.component';
import { ListTorrentsComponent } from './components/list-torrents/list-torrents.component';
import { ManageFilterAttributesComponent } from './components/manage-filter-attributes/manage-filter-attributes.component';
import { ListTorrentsByStatusComponent } from './components/list-torrents-by-status/list-torrents-by-status.component';
import { ListClientTorrentsComponent } from './components/list-client-torrents/list-client-torrents.component';
import { AdminComponent } from './components/admin/admin.component';



@NgModule({
  declarations: [
    AppComponent,
    PageNotFoundComponent,
    BreadcrumbsComponent,
    ServerErrorComponent,
    ConfirmationDialogComponent,
    HomeComponent,
    ListFeedProvidersComponent,
    EditFeedProviderComponent,
    ListTorrentsComponent,
    ManageFilterAttributesComponent,
    ListTorrentsByStatusComponent,
    ListClientTorrentsComponent,
    AdminComponent
  ],
  entryComponents: [
    ServerErrorComponent,
    ConfirmationDialogComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpModule,
    HttpClientModule,
    ReactiveFormsModule,
    FormsModule,
    DynamicFormsCoreModule.forRoot(),
    DynamicFormsMaterialUIModule,
    AppMaterialModule,
    AppRoutingModule
  ],
  providers: [ServerService, FeedProviderService, TorrentService],
  bootstrap: [AppComponent]
})
export class AppModule { }
