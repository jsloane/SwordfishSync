package mymedia.db.dao;

import java.util.List;

import mymedia.db.form.FeedInfo;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

@Repository
public class FeedInfoDAOImpl implements FeedInfoDAO {
	
    @Autowired
    @Qualifier("sessionFactoryMyMedia")
    private SessionFactory sessionFactory;
    
	public FeedInfo initFeedInfo(FeedInfo feedInfo) {
		List<FeedInfo> feedInfoList = sessionFactory.getCurrentSession().createCriteria(FeedInfo.class)
				.add(Restrictions.eq("url", feedInfo.getUrl())).list();
		
		FeedInfo existingFeedInfo = null;
		
		for (FeedInfo feedInfoFromDb : feedInfoList) {
			if (feedInfo.getUrl().equals(feedInfoFromDb.getUrl())) {
				existingFeedInfo = feedInfoFromDb;
			}
		}
		
		if (existingFeedInfo == null) {
			saveFeedInfo(feedInfo);
		} else {
			feedInfo = existingFeedInfo;
		}
		
		return feedInfo;
	}
	
	public void saveFeedInfo(FeedInfo feedInfo) {
	    sessionFactory.getCurrentSession().saveOrUpdate(feedInfo);
	}
	
	public void removeFeedInfo(FeedInfo feedInfo) {
        sessionFactory.getCurrentSession().delete(feedInfo);
	}
	
	public List<FeedInfo> getFeedInfos() {
        return sessionFactory.getCurrentSession().createCriteria(FeedInfo.class)
        		.list();
	}
	
	public FeedInfo getFeedInfo(Integer id) {
		return (FeedInfo) sessionFactory.getCurrentSession().createCriteria(FeedInfo.class)
				.add(Restrictions.eq("id", id)).uniqueResult();
	}
	
	public List<FeedInfo> getMissingFeedInfos(Integer[] gotIds) {
		return (List<FeedInfo>) sessionFactory.getCurrentSession().createCriteria(FeedInfo.class)
				.add(Restrictions.not(Restrictions.in("id", gotIds))).list();
	}
	
}