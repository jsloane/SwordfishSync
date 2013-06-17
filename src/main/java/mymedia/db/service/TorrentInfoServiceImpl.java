package mymedia.db.service;

import mymedia.db.dao.TorrentInfoDAO;
import mymedia.db.form.TorrentInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TorrentInfoServiceImpl implements TorrentInfoService {
	
    @Autowired
    private TorrentInfoDAO torrentInfoDAO;
    
    @Transactional("mymedia")
    public void saveTorrentInfo(TorrentInfo torrentInfo) {
    	torrentInfoDAO.saveTorrentInfo(torrentInfo);
    }
    
    @Transactional("mymedia")
    public void removeTorrentInfo(TorrentInfo torrentInfo) {
    	torrentInfoDAO.removeTorrentInfo(torrentInfo);
    }
    
}