import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';

import { HttpParams } from '@angular/common/http';

import { ServerService } from '../server/server.service';
import { Configuration, Setting } from '../model/configuration';
import { Message } from '../model/message';

@Injectable()
export class AdminService {

  private restUrl = '/sfs-server/api/admin';

  constructor(private serverService: ServerService) { }

  getConfiguration(): Observable<Configuration> {
    return this.serverService.getRequest(this.restUrl + '/configuration', null);
  }

  saveSettings(settings: Setting[]) {
    return this.serverService.putRequest(this.restUrl + '/configuration/settings', settings, null);
  }

  getMessages(): Observable<Message[]> {
    return this.serverService.getRequest(this.restUrl + '/messages', null);
  }

  deleteMessage(id: number) {
    return this.serverService.deleteRequest(this.restUrl + '/messages/' + id, null);
  }

  purgeInprogressTorents(): Observable<string[]> {
    return this.serverService.putRequest(this.restUrl + '/purgeInprogressTorrents', null, null);
  }

    let httpParams = new HttpParams();
  }

}
