import { Component, OnInit, Input, ViewChild } from '@angular/core';
import { ControlContainer, NgForm, Validators } from '@angular/forms';

import { AdminService } from '../../core/services/admin.service';
import { Configuration } from '../../core/model/configuration';

@Component({
  selector: 'app-configuration',
  templateUrl: './configuration.component.html',
  styleUrls: ['./configuration.component.css']
})
export class ConfigurationComponent implements OnInit {

  mainConfig: Configuration;
  currentConfig: Configuration;
  mode: string;
  @ViewChild('settingsForm') settingsForm: NgForm;

  updateButtonDisabled = false;
  updateButtonText = 'Update';

  constructor(public adminService: AdminService) { }

  ngOnInit() {
    this.mode = 'view';

    this.adminService.getConfiguration().subscribe(config => {
        this.mainConfig = config;
        this.currentConfig = JSON.parse(JSON.stringify(config));
    }, error => {
          // this.getErrorMessage = <any>error;
          console.error(error);
          // console.error('getErrorMessage=' + this.getErrorMessage);
    });
  }

  updateMode(newMode: string) {
    this.mode = newMode;
    if (this.mode === 'view') {
        this.mainConfig = JSON.parse(JSON.stringify(this.currentConfig));
    }
  }

  onSubmit(settingsForm: NgForm) {
    this.updateButtonDisabled = true;
    this.updateButtonText = 'Updating...';

    this.adminService.saveSettings(settingsForm.value).subscribe(saveResponse => {
          this.updateButtonDisabled = false;
          this.updateButtonText = 'Update';
          this.currentConfig = JSON.parse(JSON.stringify(this.mainConfig));
          this.updateMode('view');
      },
      error => {
          this.updateButtonDisabled = false;
          this.updateButtonText = 'Update';
      }
    );
  }

}

@Component({
  selector: 'app-child-configuration',
  templateUrl: './child-configuration.component.html',
  viewProviders: [ { provide: ControlContainer, useExisting: NgForm } ]
})
export class ChildConfigurationComponent {

    @Input() config: Configuration;
    @Input() level: number;
    @Input() mode: string;

    getNextLevel(): number {
        return this.level + 1;
    }

    getBooleanValue(value: any) {
        if (value === 'true') {
            return true;
        } else if (value === 'false') {
            return false;
        }
        return value;
    }

}
