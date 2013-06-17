package mymedia.db.service;

import java.util.List;

import mymedia.db.dao.FeedInfoDAO;
import mymedia.db.form.FeedInfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedInfoServiceImpl implements FeedInfoService {

    @Autowired
    private FeedInfoDAO feedInfoDAO;
    
    @Transactional("mymedia")
    public FeedInfo initFeedInfo(FeedInfo feedInfo) {
    	return feedInfoDAO.initFeedInfo(feedInfo);
    }
    
    @Transactional("mymedia")
    public void saveFeedInfo(FeedInfo feedInfo) {
    	feedInfoDAO.saveFeedInfo(feedInfo);
    }
 
    @Transactional("mymedia")
    public List<FeedInfo> getFeedInfos() {
        return feedInfoDAO.getFeedInfos();
    }
 
    @Transactional("mymedia")
    public void removeFeedInfo(FeedInfo feedInfo) {
    	feedInfoDAO.removeFeedInfo(feedInfo);
    }

    
    @Transactional("mymedia")
    public FeedInfo getFeedInfo(Integer id) {
    	return feedInfoDAO.getFeedInfo(id);
    }
    
    @Transactional("mymedia")
    public List<FeedInfo> getMissingFeedInfos(Integer[] gotIds) {
    	return feedInfoDAO.getMissingFeedInfos(gotIds);
    }
    
}