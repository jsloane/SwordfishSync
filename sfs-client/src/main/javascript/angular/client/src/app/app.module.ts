import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpModule } from '@angular/http'; // TODO remove and replace with HttpClientModule
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';

// dynamic forms
import { DynamicFormsCoreModule } from '@ng-dynamic-forms/core';
import { DynamicFormsMaterialUIModule } from '@ng-dynamic-forms/ui-material';

// progress bar
import { NgProgressModule } from '@ngx-progressbar/core';
import { NgProgressHttpClientModule } from '@ngx-progressbar/http-client';

// app
import { AppMaterialModule } from './shared/app-material.module';

import { AppRoutingModule } from './app-routing.module';

// app specific
import { ServerErrorComponent } from './core/server/server-error.component';
import { ServerService } from './core/server/server.service';
import { FeedProviderService } from './core/services/feed-provider.service';
import { TorrentService } from './core/services/torrent.service';
import { AdminService } from './core/services/admin.service';

import { SafeHtmlPipe } from './core/pipes/safe-html.pipe';

import { PageNotFoundComponent } from './core/components/page-not-found/page-not-found.component';
import { BreadcrumbsComponent } from './core/components/breadcrumbs/breadcrumbs.component';

import { ConfirmationDialogComponent } from './core/components/confirmation-dialog/confirmation-dialog.component';

import { HomeComponent } from './components/home/home.component';
import { ListFeedProvidersComponent } from './components/list-feed-providers/list-feed-providers.component';
import { EditFeedProviderComponent, AddTorrentsDialogComponent } from './components/edit-feed-provider/edit-feed-provider.component';
import { ListTorrentsComponent } from './components/list-torrents/list-torrents.component';
import { ManageFilterAttributesComponent } from './components/manage-filter-attributes/manage-filter-attributes.component';
import { ListTorrentsByStatusComponent } from './components/list-torrents-by-status/list-torrents-by-status.component';
import { ListClientTorrentsComponent } from './components/list-client-torrents/list-client-torrents.component';
import { AdminComponent } from './components/admin/admin.component';
import { ConfigurationComponent, ChildConfigurationComponent } from './components/configuration/configuration.component';
import { MessagesComponent } from './components/messages/messages.component';



@NgModule({
  declarations: [
    AppComponent,
    SafeHtmlPipe,
    PageNotFoundComponent,
    BreadcrumbsComponent,
    ServerErrorComponent,
    ConfirmationDialogComponent,
    HomeComponent,
    ListFeedProvidersComponent,
    EditFeedProviderComponent,
    AddTorrentsDialogComponent,
    ListTorrentsComponent,
    ManageFilterAttributesComponent,
    ListTorrentsByStatusComponent,
    ListClientTorrentsComponent,
    AdminComponent,
    ConfigurationComponent,
    ChildConfigurationComponent,
    MessagesComponent
  ],
  entryComponents: [
    ServerErrorComponent,
    ConfirmationDialogComponent,
    AddTorrentsDialogComponent
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
    NgProgressModule.forRoot(),
    NgProgressHttpClientModule,
    AppMaterialModule,
    AppRoutingModule
  ],
  providers: [ServerService, FeedProviderService, TorrentService, AdminService],
  bootstrap: [AppComponent]
})
export class AppModule { }
