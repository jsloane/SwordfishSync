import { Component, OnInit, OnDestroy } from '@angular/core';
import { FormGroup, FormControl, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { MatDialog, MatDialogRef, MatDialogConfig } from '@angular/material';

import { DynamicFormControlModel, DynamicFormService } from '@ng-dynamic-forms/core';

import { FeedProviderService } from '../../core/services/feed-provider.service';
import { FeedProvider } from '../../core/model/feed-provider';
// import { MY_FORM_MODEL } from '../../core/model/feed-provider-form';
import { FEED_PROVIDER_FORM_MODEL } from '../../core/model/feed-provider-form';

import { ManageFilterAttributesComponent } from '../manage-filter-attributes/manage-filter-attributes.component';
import { ConfirmationDialogComponent } from '../../core/components/confirmation-dialog/confirmation-dialog.component';

@Component({
  selector: 'app-edit-feed-provider',
  templateUrl: './edit-feed-provider.component.html',
  styleUrls: ['./edit-feed-provider.component.css']
})
export class EditFeedProviderComponent implements OnInit, OnDestroy {

    formModel: DynamicFormControlModel[] = FEED_PROVIDER_FORM_MODEL;
    formGroup: FormGroup;

    mode: string; // edit, view
    id: number;
    private routeSub: any;

    constructor(
        private formService: DynamicFormService,
        public feedProviderService: FeedProviderService,
        public dialog: MatDialog,
        private route: ActivatedRoute,
        private router: Router
        ) {
        this.mode = route.snapshot.data['mode'];
    }

    ngOnInit() {
        console.log('EditFeedProviderComponent ngOnInit');
        this.formGroup = this.formService.createFormGroup(this.formModel);
        if (this.mode === 'create') {
            this.formGroup.reset(); // ?

            // this.id = null;
        }

        if (this.mode === 'view') {
            this.formGroup.disable();
        }

        if (this.mode !== 'create') {
            this.routeSub = this.route.params.subscribe(params => {
                this.id = Number(params['id']);

                if (isFinite(this.id)) {

                this.feedProviderService.getFeedProvider(this.id).subscribe(feedProvider => {
                        this.formGroup.patchValue(feedProvider);
                    },
                    error => {
                        // this.getErrorMessage = <any>error;
                        console.error(error);
                        // console.error('getErrorMessage=' + this.getErrorMessage);
                    }
                );
                }
            });
        }
    }

    ngOnDestroy() {
      if (this.routeSub) {
       this.routeSub.unsubscribe();
      }
    }

    updateMode(newMode: string) {
        this.mode = newMode;
        if (this.mode === 'edit' || this.mode === 'create') {
            this.formGroup.enable();
        }
        if (this.mode === 'view') {
            this.formGroup.disable();
        }
    }

    onSubmit() {
        // this.submitted = true;
        // TODO check if form is valid
        const formObj = this.formGroup.getRawValue();
        const serializedForm = JSON.stringify(formObj);
        if (this.mode === 'edit' && this.id) {
            this.feedProviderService.updateFeedProvider(this.id, formObj).subscribe(savedFeedProvider => {
                    console.log(savedFeedProvider);
                    this.formGroup.reset();
                    this.formGroup.patchValue(savedFeedProvider);
                    this.updateMode('view');
                },
                error => {
                    // this.getErrorMessage = <any>error;
                    console.error(error);
                    // console.error('getErrorMessage=' + this.getErrorMessage);
                }
            );
        } else if (this.mode === 'create') {
            this.feedProviderService.createFeedProvider(formObj).subscribe(savedFeedProvider => {
                    console.log(savedFeedProvider);
                    this.formGroup.reset();
                    this.formGroup.patchValue(savedFeedProvider);
                    this.id = savedFeedProvider.id;
                    this.updateMode('view');
                },
                error => {
                    // this.getErrorMessage = <any>error;
                    console.error(error);
                    // console.error('getErrorMessage=' + this.getErrorMessage);
                }
            );
        }
    }

    deleteFeedProvider() {
        let deleteConfirmationDialogRef: MatDialogRef<ConfirmationDialogComponent>;
        deleteConfirmationDialogRef = this.dialog.open(ConfirmationDialogComponent, {
            data: {
                title: 'Delete Feed',
                warning: 'Are you sure you want to delete this feed?',
                cancelLabel: 'Cancel',
                confirmLabel: 'Delete',
                confirmColour: 'warn',
                confirmEventEmit: true,
                confirmEventLabel: 'Deleting...'
            }
        });

        const sub = deleteConfirmationDialogRef.componentInstance.onConfirm.subscribe(() => {
            this.feedProviderService.deleteFeedProvider(this.id).subscribe(response => {
                    console.log(response);
                    // this.formGroup.reset();
                    // this.updateMode('create');
                    this.router.navigateByUrl('/feedProviders');
                },
                error => {
                    // this.getErrorMessage = <any>error;
                    console.error(error);
                    // console.error('getErrorMessage=' + this.getErrorMessage);
                }
            );
        });
    }

}
