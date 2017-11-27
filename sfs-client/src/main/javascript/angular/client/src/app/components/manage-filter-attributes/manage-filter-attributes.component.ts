import { Component, OnInit, Input } from '@angular/core';
import { FilterAttribute } from '../../core/model/filter-attribute';

import { FeedProviderService } from '../../core/services/feed-provider.service';

@Component({
  selector: 'app-manage-filter-attributes',
  templateUrl: './manage-filter-attributes.component.html',
  styleUrls: ['./manage-filter-attributes.component.css']
})
export class ManageFilterAttributesComponent implements OnInit {

    @Input() feedProviderId: number;

    filterAttributesRegexAdd: Array<string>;
    filterAttributesRegexIgnore: Array<string>;

    mode: string;

    textareaAdd: string;
    textareaIgnore: string;

    simpleValue: string;
    simpleFilter: string;

    constructor(public feedProviderService: FeedProviderService) { }

    ngOnInit() {
        this.filterAttributesRegexAdd = [];
        this.filterAttributesRegexIgnore = [];

        this.mode = 'view';

        this.simpleFilter = 'add';

        this.feedProviderService.getFeedProviderFilterAttributes(this.feedProviderId).subscribe(filterAttributes => {
                this.processFilterAttributes(filterAttributes);
            },
            error => {
                // this.getErrorMessage = <any>error;
                console.error(error);
                // console.error('getErrorMessage=' + this.getErrorMessage);
            }
        );

    }

    updateMode(newMode: string) {
        this.mode = newMode;
    }

    addSimpleValue() {
        let regexString = this.simpleValue;
        if (regexString) {
            regexString = '\n(?i).*' + regexString.replace(/ /g, '.*') + '.*';
            if (this.simpleFilter === 'add') {
                this.textareaAdd = this.textareaAdd + regexString;
                this.simpleValue = '';
            } else if (this.simpleFilter === 'ignore') {
                this.textareaIgnore = this.textareaIgnore + regexString;
                this.simpleValue = '';
            }
        }
    }

    processFilterAttributes(filterAttributes: Array<FilterAttribute>) {
        this.textareaAdd = '';
        this.textareaIgnore = '';
        filterAttributes.forEach(filterAttribute => {
            if (filterAttribute.filterType === 'ADD') {
                if (this.textareaAdd) {
                    this.textareaAdd = this.textareaAdd + filterAttribute.filterRegex + '\n';
                } else {
                    this.textareaAdd = filterAttribute.filterRegex + '\n';
                }
            } else if (filterAttribute.filterType === 'IGNORE') {
                if (this.textareaIgnore) {
                    this.textareaIgnore = this.textareaIgnore + filterAttribute.filterRegex + '\n';
                } else {
                    this.textareaIgnore = filterAttribute.filterRegex + '\n';
                }
            }
        });
    }

    updateFilterAttributes() {
        const newfilterAttributes = Array<FilterAttribute>();

        if (this.textareaAdd) {
            const regexAdd = this.textareaAdd.split('\n');
            regexAdd.forEach(value => {
                if (value) {
                    const filterAttribute = new FilterAttribute();
                    filterAttribute.filterType = 'ADD';
                    filterAttribute.filterRegex = value.trim();
                    newfilterAttributes.push(filterAttribute);
                }
            });
        }

        if (this.textareaIgnore) {
            const regexIgnore = this.textareaIgnore.split('\n');
            regexIgnore.forEach(value => {
                if (value) {
                    const filterAttribute = new FilterAttribute();
                    filterAttribute.filterType = 'IGNORE';
                    filterAttribute.filterRegex = value.trim();
                    newfilterAttributes.push(filterAttribute);
                }
            });
        }

        // TODO snackbars for all save actions
        // TODO loading indicator on application http activity

        this.feedProviderService.replaceFeedProviderAttributes(this.feedProviderId, newfilterAttributes).subscribe(savedFilterAttributes => {
                this.processFilterAttributes(savedFilterAttributes);
                this.mode = 'view';
            },
            error => {
                // this.getErrorMessage = <any>error;
                console.error(error);
                // console.error('getErrorMessage=' + this.getErrorMessage);
            }
        );
    }

}
