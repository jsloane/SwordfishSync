package mymedia.db.dao;

import mymedia.db.form.TorrentInfo;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class TorrentInfoDAOImpl implements TorrentInfoDAO {
	
    @Autowired
    @Qualifier("sessionFactoryMyMedia")
    private SessionFactory sessionFactory;
    
	public void saveTorrentInfo(TorrentInfo torrentInfo) {
        sessionFactory.getCurrentSession().saveOrUpdate(torrentInfo);
	}
	
	public void removeTorrentInfo(TorrentInfo torrentInfo) {
        sessionFactory.getCurrentSession().delete(torrentInfo);
	}
	
}
