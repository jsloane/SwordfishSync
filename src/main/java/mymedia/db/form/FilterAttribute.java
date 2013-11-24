package mymedia.db.form;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class FilterAttribute {

    @Column//(name="filterType")
    private String filterType;

    @Column//(name="filterRegex")
    private String filterRegex;

	public FilterAttribute() {
	}
	public FilterAttribute(String filterType, String filterRegex) {
		this.filterType = filterType;
		this.filterRegex = filterRegex;
	}
   	public String getFilterType() {
   		return filterType;
   	}
   	public String getFilterRegex() {
   		return filterRegex;
   	}
   	
   	public void setFilterType(String filterType) {
   		this.filterType = filterType;
   	}
   	public void setFilterRegex(String filterRegex) {
   		this.filterRegex = filterRegex;
   	}
   	
   	
   	@Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        
        FilterAttribute filterAttribute = (FilterAttribute) obj;
        return (
        		filterType != null &&
        		filterType.equals(filterAttribute.getFilterType())
    		) && (
            	filterRegex != null &&
            	filterRegex.equals(filterAttribute.getFilterRegex())
            );
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((filterType == null) ? 0 : filterType.hashCode());
        result = prime * result
                + ((filterRegex == null) ? 0 : filterRegex.hashCode());
        return result;
    }

   	

   	public String toString() {
        return "FilterAttribute: filterType [" + filterType + "], filterRegex [" + filterRegex + "]";
   	}
}
