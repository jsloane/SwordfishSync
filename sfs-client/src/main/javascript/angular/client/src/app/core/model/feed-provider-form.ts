import {
    DynamicFormControlModel,
    DynamicCheckboxModel,
    DynamicInputModel,
    DynamicRadioGroupModel,
    DynamicFormGroupModel
} from '@ng-dynamic-forms/core';

export const FEED_PROVIDER_FORM_MODEL: DynamicFormControlModel[] = [
    /*new DynamicFormGroupModel({
        id: 'test',
        legend: 'test',
        group: [*/
            new DynamicInputModel({
                id: 'name',
                maxLength: 64,
                placeholder: 'Name',
                required: true
            }),

            new DynamicInputModel({
                id: 'feedUrl',
                maxLength: 768,
                placeholder: 'URL',
                required: true
            })
        // ]})
        ,

    new DynamicInputModel({
        id: 'notifyEmail',
        maxLength: 128,
        placeholder: 'Notify Email'
    }),

    new DynamicCheckboxModel({
        id: 'active',
        label: 'Active',
        value: false,
        required: true
    }),

    new DynamicRadioGroupModel<string>({
        id: 'action',
        label: 'Action',
        options: [
            {
                label: 'Download',
                value: 'DOWNLOAD',
            },
            {
                label: 'Notify',
                value: 'NOTIFY',
            },
            {
                label: 'Skip',
                value: 'SKIP',
            }
        ],
        value: 'SKIP',
        required: true
    }),

    new DynamicInputModel({
        id: 'syncInterval',
        inputType: 'number',
        placeholder: 'Sync Interval',
        value: 0,
        required: true
    }),

    new DynamicInputModel({
        id: 'deleteInterval',
        inputType: 'number',
        placeholder: 'Delete Interval',
        value: 0,
        required: true
    }),

    new DynamicCheckboxModel({
        id: 'skipDuplicates',
        label: 'Skip Duplicates',
        required: true,
        value: true
    }),
    new DynamicCheckboxModel({
        id: 'skipPropersRepacksReals',
        label: 'Skip Propers/Repacks/Reals',
        required: true
    }),

    new DynamicInputModel({
        id: 'uploadLimit',
        inputType: 'number',
        placeholder: 'Upload Limit',
        value: 0, // TODO not set
        required: true
    }),
    new DynamicInputModel({
        id: 'downloadDirectory',
        maxLength: 256,
        placeholder: 'Download Directory',
        hint: 'test'
    }),

    new DynamicCheckboxModel({
        id: 'determineSubDirectory',
        label: 'Determine Sub Directory',
        value: true
    }),
    new DynamicCheckboxModel({
        id: 'extractRars',
        label: 'Extract Rars',
        value: true
    }),
    new DynamicCheckboxModel({
        id: 'removeTorrentOnComplete',
        label: 'Remove Torrent On Complete',
        required: true
    }),
    new DynamicCheckboxModel({
        id: 'removeTorrentDataOnComplete',
        label: 'Remove Torrent Data On Complete',
        required: true
    }),
    new DynamicInputModel({
        id: 'systemCommand',
        maxLength: 256,
        placeholder: 'System Command'
    }),

    new DynamicInputModel({
        id: 'detailsUrlValueFromRegex',
        maxLength: 768,
        placeholder: 'Details ID Value From URL Regex'
    }),
    new DynamicInputModel({
        id: 'detailsUrlFormat',
        maxLength: 768,
        placeholder: 'Details URL Substitution'
    }),

    new DynamicCheckboxModel({
        id: 'filterEnabled',
        label: 'Enable Filter',
        value: true,
        required: true
    }),
    new DynamicCheckboxModel({
        id: 'removeAddFilterOnMatch',
        label: 'Remove Add Filter Entry On Match',
        required: true
    }),

    new DynamicRadioGroupModel<string>({
        id: 'filterAction',
        label: 'Filter Action',
        options: [
            {
                label: 'Add',
                value: 'ADD',
            },
            {
                label: 'Ignore',
                value: 'IGNORE',
            }
        ],
        value: 'IGNORE',
        required: true
    }),
    new DynamicRadioGroupModel<string>({
        id: 'filterPrecedence',
        label: 'Filter Precedence',
        options: [
            {
                label: 'Add',
                value: 'ADD',
            },
            {
                label: 'Ignore',
                value: 'IGNORE',
            }
        ],
        value: 'IGNORE',
        required: true
    }),

];
