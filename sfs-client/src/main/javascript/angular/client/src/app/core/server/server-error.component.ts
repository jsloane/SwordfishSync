import { Component, Inject } from '@angular/core';
import {MatDialog, MatDialogRef, MatDialogConfig, MAT_DIALOG_DATA} from '@angular/material';

@Component({
  selector: 'app-server-error',
  templateUrl: './server-error.component.html',
  styleUrls: ['./server-error.component.css']
})
export class ServerErrorComponent {

  constructor(
    public dialogRef: MatDialogRef<ServerErrorComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) { }

}
