package mymedia.db.service;

import mymedia.db.form.TorrentInfo;
 
public interface TorrentInfoService {
    public void saveTorrentInfo(TorrentInfo torrentInfo);
    public void removeTorrentInfo(TorrentInfo torrent);
}