package swordfishsync.service.dto;

import swordfishsync.domain.FeedProvider;
import swordfishsync.domain.FilterAttribute;

public class FilterAttributeDto {

	Long							id;
	FeedProvider.FilterAction	filterType;
	String							filterRegex;

	public static FilterAttributeDto convertToFilterAttributeDto(final FilterAttribute filterAttribute) {
		FilterAttributeDto filterAttributeDto = new FilterAttributeDto();

		filterAttributeDto.setId(filterAttribute.getId());
		filterAttributeDto.setFilterType(filterAttribute.getFilterType());
		filterAttributeDto.setFilterRegex(filterAttribute.getFilterRegex());

		return filterAttributeDto;
	}

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

	@Override
	public String toString() {
		return "FilterAttributeDto [id=" + id + ", filterType=" + filterType + ", filterRegex=" + filterRegex + "]";
	}
	
}
