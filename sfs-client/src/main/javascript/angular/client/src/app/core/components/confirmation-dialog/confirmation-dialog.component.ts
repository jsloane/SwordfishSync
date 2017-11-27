import { Component, Inject, OnInit, EventEmitter } from '@angular/core';
import {MatDialog, MatDialogRef, MatDialogConfig, MAT_DIALOG_DATA} from '@angular/material';

@Component({
    selector: 'app-confirmation-dialog',
    templateUrl: './confirmation-dialog.component.html',
    styleUrls: ['./confirmation-dialog.component.css']
})
export class ConfirmationDialogComponent implements OnInit {

    isConfirmButtonDisabled: boolean;
    confirmButtonText: String;
    onConfirm = new EventEmitter();
    isConfirm2ButtonDisabled: boolean;
    confirm2ButtonText: String;
    onConfirm2 = new EventEmitter();

    constructor(
        public dialogRef: MatDialogRef<ConfirmationDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any
    ) { }

    ngOnInit() {
        this.confirmButtonText = this.data.confirmLabel;
        this.confirm2ButtonText = this.data.confirm2Label;
        this.isConfirmButtonDisabled = false;
    }

    /**
     * Notify that the confirm button has been clicked.
     */
    confirmEvent() {
        if (this.data.confirmEventEmit) {
            this.confirmButtonText = this.data.confirmEventLabel;
            this.isConfirmButtonDisabled = true;
            this.onConfirm.emit();
        }
    }

    /**
     * Notify that the second confirm button has been clicked.
     */
    confirm2Event() {
        if (this.data.confirm2EventEmit) {
            this.confirm2ButtonText = this.data.confirm2EventLabel;
            this.isConfirm2ButtonDisabled = true;
            this.onConfirm2.emit();
        }
    }

}
