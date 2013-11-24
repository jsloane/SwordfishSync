package mymedia.db.dao;

import java.util.List;

import mymedia.db.form.FeedInfo;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
//import javax.persistence.EntityManager;

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
	    //sessionFactory.getCurrentSession().merge(feedInfo);
	}
	
	public void removeFeedInfo(FeedInfo feedInfo) {
        sessionFactory.getCurrentSession().delete(feedInfo);
	}
	
	public List<FeedInfo> getFeedInfos() {
        return sessionFactory.getCurrentSession().createCriteria(FeedInfo.class)
        		.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
        		.list();
	}
	
	public FeedInfo getFeedInfo(Integer id) {
		return (FeedInfo) sessionFactory.getCurrentSession().createCriteria(FeedInfo.class)
				.add(Restrictions.eq("id", id)).uniqueResult();
	}
	
	public List<FeedInfo> getMissingFeedInfos(Integer[] gotIds) {
		if (gotIds.length > 0) {
			return (List<FeedInfo>) sessionFactory.getCurrentSession().createCriteria(FeedInfo.class)
					.add(
						Restrictions.not(
							Restrictions.in("id", gotIds)
						)
					).setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
					.list();
		}
		return getFeedInfos();
	}
	
}