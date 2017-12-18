
export interface Configuration {
    title: string;
    childConfiguration: Configuration[];
    setting: Setting;
}

export interface Setting {
    code: string;
    type: string;
    value: string;
}
