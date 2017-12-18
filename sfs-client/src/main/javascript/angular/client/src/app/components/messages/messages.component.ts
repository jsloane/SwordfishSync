import { Component, OnInit } from '@angular/core';

import { AdminService } from '../../core/services/admin.service';
import { Message } from '../../core/model/message';
import { MatButton } from '@angular/material';

@Component({
  selector: 'app-messages',
  templateUrl: './messages.component.html',
  styleUrls: ['./messages.component.css']
})
export class MessagesComponent implements OnInit {

  messages: Message[];

  constructor(public adminService: AdminService) { }

  ngOnInit() {
    this.getMessages();
  }

  getMessages() {
    this.adminService.getMessages().subscribe(returnedMessages => {
      this.messages = returnedMessages;
    });
  }

  deleteMessage(id: number, clearButton: MatButton) {
    console.log('clearButton: ', clearButton);
    clearButton.disabled = true;
    this.adminService.deleteMessage(id).subscribe(response => {
      clearButton.disabled = false;
      console.log(response);
      this.getMessages();
    }, error => {
        clearButton.disabled = false;
    });
  }

}
