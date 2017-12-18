import { FeedProvider } from './feed-provider';
import { Torrent } from './torrent';

export class Message {

    id: number;
    dateCreated: Date;
    dateUpdated: Date;
    type: string;
    category: string;
    message: string;
    reported: boolean;
    feedProvider: FeedProvider;
    torrent: Torrent;

}
