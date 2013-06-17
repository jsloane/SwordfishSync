package mymedia.db.dao;

import java.util.List;

import mymedia.db.form.FeedInfo;
 
public interface FeedInfoDAO {
	public FeedInfo initFeedInfo(FeedInfo feedInfo);
    public void saveFeedInfo(FeedInfo feedInfo);
    public void removeFeedInfo(FeedInfo feedInfo);
    public List<FeedInfo> getFeedInfos();
	public FeedInfo getFeedInfo(Integer id);
    public List<FeedInfo> getMissingFeedInfos(Integer[] gotIds);
}