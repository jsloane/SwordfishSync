package swordfishsync.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;

import swordfishsync.domain.FeedProvider;

@Entity
public class FilterAttribute {

	@Id
	@GeneratedValue
	Long							id;

	@Version
	Long version;

	@NotNull
	FeedProvider.FilterAction	filterType;

	@NotNull
	String							filterRegex;
	
	@ManyToOne
	@NotNull
	FeedProvider					feedProvider;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public FeedProvider.FilterAction getFilterType() {
		return filterType;
	}

	public void setFilterType(FeedProvider.FilterAction filterType) {
		this.filterType = filterType;
	}

	public String getFilterRegex() {
		return filterRegex;
	}

	public void setFilterRegex(String filterRegex) {
		this.filterRegex = filterRegex;
	}

	public FeedProvider getFeedProvider() {
		return feedProvider;
	}

	public void setFeedProvider(FeedProvider feedProvider) {
		this.feedProvider = feedProvider;
	}
	
}
