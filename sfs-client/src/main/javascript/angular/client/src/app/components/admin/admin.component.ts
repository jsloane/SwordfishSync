import { Component, OnInit } from '@angular/core';

import { AdminService } from '../../core/services/admin.service';

@Component({
  selector: 'app-admin',
  templateUrl: './admin.component.html',
  styleUrls: ['./admin.component.css']
})
export class AdminComponent implements OnInit {

  purgeMessages: string[] = [];
  purgingTorrents = false;

  testEmailAddress: string;
  sendingTestEmail = false;
  sentTestEmail = false;

  constructor(public adminService: AdminService) { }

  ngOnInit() { }

  onClickPurgeTorrents() {
    this.purgingTorrents = true;
    this.adminService.purgeInprogressTorents().subscribe(returnedPurgeMessages => {
      this.purgeMessages = returnedPurgeMessages;
      this.purgingTorrents = false;
    }, error => {
      this.purgingTorrents = false;
      console.error(error);
    });
  }

  onClickSendTestEmail() {
    this.sentTestEmail = false;
    this.sendingTestEmail = true;

    this.adminService.sendTestEmail(this.testEmailAddress).subscribe(result => {
      this.sentTestEmail = true;
      this.sendingTestEmail = false;
    }, error => {
      this.sentTestEmail = false;
      this.sendingTestEmail = false;
      console.error(error);
    });
  }

}
