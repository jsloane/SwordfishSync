
export class Torrent {

    id: number;

    status: string;

    detailsUrl: string;
    feedProviderId: number;
    feedProviderName: string;

    torrentName: string;
    torrentUrl: string;

    torrentDateAdded: Date; // number?
    torrentDatePublished: Date; // number?
    torrentDateCompleted: Date; // number?

    torrentHashString: string;
    torrentClientTorrentId: number;
    torrentInCurrentFeed: boolean;
    torrentAddedToTorrentClient: boolean;

    clientActivityDate: Date; // number?
    clientPercentDone: number;

}
