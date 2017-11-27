
export class FeedProvider {

    id: number;

    name: string;
    feedUrl: string;
    downloadDirectory: string;
    determineSubDirectory: boolean;
    extractRars: boolean;
    systemCommand: string;
    syncInterval: number;
    feedAction: string;
    uploadLimit: number;
    deleteInterval: number;
    notifyEmail: string;
    detailsUrlValueFromRegex: string;
    detailsUrlFormat: string;
    skipDuplicates: boolean;
    skipPropersRepacksReals: boolean;
    removeTorrentOnComplete: boolean;
    removeTorrentDataOnComplete: boolean;
    filterEnabled: boolean;
    removeAddFilterOnMatch: boolean;
    filterAction: string;
    filterPrecedence: string;

    dateCreated: Date; // number?
    lastUpdated: Date; // number?
    lastProcessed: Date; // number?

    feedLastFetched: Date; // number?
    feedLastPurged: Date; // number?

    feedTtl: number;
    feedIsCurrent: boolean;


}
