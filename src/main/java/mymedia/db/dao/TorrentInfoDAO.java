package mymedia.db.dao;

import mymedia.db.form.TorrentInfo;
 
public interface TorrentInfoDAO {
    public void saveTorrentInfo(TorrentInfo torrent);
    public void removeTorrentInfo(TorrentInfo torrentInfo);
}