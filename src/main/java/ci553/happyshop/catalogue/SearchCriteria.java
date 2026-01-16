package ci553.happyshop.catalogue;


/**
*This record holds the information needed to run a product search
*
 *
 *
 **/

public record SearchCriteria(
        String searchMode,
        String keyword,
        double minPrice,
        double maxPrice
) {
    public SearchCriteria {

        if (minPrice < 0 || maxPrice < 0){
            throw new IllegalArgumentException("Prices cannot be negative");
        }
        if (minPrice > maxPrice){
            throw new IllegalArgumentException("Min price can't excced max price");
        }
        if (searchMode == null || searchMode.trim().isEmpty()){
            throw new IllegalArgumentException("search can't be empty");
        }

    }
    public boolean isPriceRangeSearch(){ //check if price range search
        return "By Price Range".equals(searchMode);
    }

    public boolean isProductIdSearch(){
        return "By Product ID".equals(searchMode);
    }

    public boolean isNameSearch(){
        return "By Name/Description".equals(searchMode);
    }

    public boolean hasKeyword() {
        return  keyword != null && !keyword.trim().isEmpty();
    }


}
