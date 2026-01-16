package ci553.happyshop.client.customer;

import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.client.customer.catalogue.exception.UnderMinimumPaymentException;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.utility.ProductListFormatter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TODO
 * You can either directly modify the CustomerModel class to implement the required tasks,
 * or create a subclass of CustomerModel and override specific methods where appropriate.
 */
public class CustomerModel {
    public CustomerView cusView;
    public DatabaseRW databaseRW; //Interface type, not specific implementation
    //Benefits: Flexibility: Easily change the database implementation.

    private Product theProduct = null; // product found from search
    private ArrayList<Product> trolley = new ArrayList<>(); // a list of products in trolley

    // Four UI elements to be passed to CustomerView for display updates.
    private String imageName = "imageHolder.jpg";                // Image to show in product preview (Search Page)
    private String displayLaSearchResult = "No Product was searched yet"; // Label showing search result message (Search Page)
    private String displayTaTrolley = "";                                // Text area content showing current trolley items (Trolley Page)
    private String displayTaReceipt = "";                                // Text area content showing receipt after checkout (Receipt Page)

    //SELECT productID, description, image, unitPrice,inStock quantity
    void search() throws SQLException {

        String searchMode = cusView.getSearchMode();
        System.out.println("DEBUG : searchMode = " + searchMode );


        if (searchMode.equals("By Product ID")) {
            System.out.println("DEBUG: Calling searchById()"); // to fix issues with search
            searchById();
        } else if (searchMode.equals("By Name/Description")) {
            System.out.println("DEBUG: Calling searchByNameOrDescription()");
           searchByNameOrDescription();
        } else if (searchMode.equals("By Price Range")) {
            System.out.println("DEBUG: Calling searchByPriceRange()");
            searchByPriceRange();
        }
        updateView();
    }

    private void searchById() throws SQLException {
        String productId = cusView.tfId.getText().trim();

        if (productId.isEmpty()){
            theProduct = null;
            displayLaSearchResult = "Please type ProductID";
            return;
        }
        theProduct = databaseRW.searchByProductId(productId);

        if (theProduct  != null && theProduct.getStockQuantity() > 0){
            double unitPrice = theProduct.getUnitPrice();
            String description = theProduct.getProductDescription();
            int stock = theProduct.getStockQuantity();


            String baseInfo = String.format("Product_Id: %s\n%s\nPrice: £%.2f", productId, description, unitPrice);
            String quantifyInfo = stock <100 ? String.format("\n%d units left.", stock):"";
            displayLaSearchResult = baseInfo + quantifyInfo;
        }
    }

    private void searchByNameOrDescription() throws SQLException {
        String keyword= cusView.tfId.getText().trim();

        if (keyword.isEmpty()){
            theProduct = null;
            displayLaSearchResult = "Please enter product name";
            return;
        }
        theProduct = databaseRW.searchByNameOrDescription(keyword);

        if (theProduct != null){
            displayLaSearchResult = String.format("product ID: %S\n%s\nPrice: %.2f",
            theProduct.getProductId(),
            theProduct.getProductDescription(),
            theProduct.getUnitPrice());
        } else {
            displayLaSearchResult = "No matching product found:" + keyword;
        }
    }

    private void searchByPriceRange() throws SQLException{
        String minStr = cusView.getMinPrice();
        String maxStr = cusView.getMaxPrice();

        if (minStr.isEmpty() || maxStr.isEmpty()){
            theProduct = null;
            displayLaSearchResult = "please enter both min and max prices";
            return;
        }
        try {
            double minPrice = Double.parseDouble(minStr);
            double maxPrice = Double.parseDouble(maxStr);

            if (minPrice < 0 || maxPrice < 0){
                displayLaSearchResult = "Price must be positive";
                return;
            }

            if (minPrice > maxPrice){
                displayLaSearchResult = "Min price can't be higher than max";
                return;
            }

            ArrayList<Product> products = databaseRW.searchByPriceRange(minPrice, maxPrice);

            if (!products.isEmpty()){
                theProduct = products.get(0);
                displayLaSearchResult = String.format("Found %d products\nShowing: %s\nprice: £%.2f",
                products.size(),
                theProduct.getProductDescription(),
                theProduct.getUnitPrice());
            } else {
                theProduct = null;
                displayLaSearchResult = String.format("No product found between $%.2f and $%.2f", minPrice, maxPrice);
            }
        } catch (NumberFormatException e) {
            displayLaSearchResult = "Please enter valid numbers for pricing";
        }
    }



    void addToTrolley() {
        if (theProduct != null) {

            // trolley.add(theProduct) — Product is appended to the end of the trolley.
            // To keep the trolley organized, add code here or call a method that:
            //TODO
            // 1. Merges items with the same product ID (combining their quantities).
            // 2. Sorts the products in the trolley by product ID.

            // COMMENTED OUT - trolley.add(theProduct);
            makeOrganisedTrolley();
            displayTaTrolley = ProductListFormatter.buildString(trolley); //built a String for trolley so it's shown
        } else {
            displayLaSearchResult = "Please search for an available product before adding it to the trolley";
            System.out.println("must search and get an available product before add to trolley");
        }
        displayTaReceipt = ""; // Clear receipt to switch back to trolleyPage (receipt shows only when not empty)
        updateView();
    }

    public void makeOrganisedTrolley() {
        for (Product p : trolley) {
            if (p.getProductId().equals(theProduct.getProductId())) {
                p.setOrderedQuantity(p.getOrderedQuantity() + theProduct.getOrderedQuantity());
                return;
            }
        }
        Product pNew = new Product(theProduct.getProductId(), theProduct.getProductDescription(), theProduct.getProductImageName(), theProduct.getUnitPrice(), theProduct.getStockQuantity());
        trolley.add(pNew);
    }



    /**
     * Groups products by their productId to optimize database queries and updates.
     * By grouping products, we can check the stock for a given `productId` once, rather than repeatedly
     */
    private ArrayList<Product> groupProductsById(ArrayList<Product> proList) {
        Map<String, Product> grouped = new HashMap<>();
        for (Product p : proList) {
            String id = p.getProductId();
            if (grouped.containsKey(id)) {
                Product existing = grouped.get(id);
                existing.setOrderedQuantity(existing.getOrderedQuantity() + p.getOrderedQuantity());
            } else {
                // Make a shallow copy to avoid modifying the original
                grouped.put(id, new Product(p.getProductId(), p.getProductDescription(),
                        p.getProductImageName(), p.getUnitPrice(), p.getStockQuantity()));
            }
        }
        return new ArrayList<>(grouped.values());
    }

    void cancel() {
        trolley.clear();
        displayTaTrolley = "";
        updateView();
    }

    void closeReceipt() {
        displayTaReceipt = "";
    }

    void updateView() {
        if (theProduct != null) {
            imageName = theProduct.getProductImageName();
            String relativeImageUrl = StorageLocation.imageFolder + imageName; //relative file path, eg images/0001.jpg
            // Get the full absolute path to the image
            Path imageFullPath = Paths.get(relativeImageUrl).toAbsolutePath();
            imageName = imageFullPath.toUri().toString(); //get the image full Uri then convert to String
            System.out.println("Image absolute path: " + imageFullPath); // Debugging to ensure path is correct
        } else {
            imageName = "imageHolder.jpg";
        }
        cusView.update(imageName, displayLaSearchResult, displayTaTrolley, displayTaReceipt);
    }
    // extra notes:
    //Path.toUri(): Converts a Path object (a file or a directory path) to a URI object.
    //File.toURI(): Converts a File object (a file on the filesystem) to a URI object

    //for test only
    public ArrayList<Product> getTrolley() {
        return trolley;
    }

    public void setTheProduct(Product theProduct) {
        this.theProduct = theProduct;
    }

    private static class ValidationResult {
        boolean isValid = true;
        String paymentIssues; //UMPException
        List<String> quantityIssues = new ArrayList<>();//EOQException
    }


    private ValidationResult validateTrolley() {
        ValidationResult result = new ValidationResult();

        double totalPayment = 0;
        ArrayList<Product> items = getTrolley();

        for (Product p: items){
            // totalPayment += theProduct.getUnitPrice() * theProduct.getOrderedQuantity();
            //to calc payment using merged quantities

            int qty = p.getOrderedQuantity(); 

            int resolvedQty = qty;

                if (qty > 50) {
                    resolvedQty = 50;
                    p.setOrderedQuantity(50);
                    result.quantityIssues.add("Quantity for " + p.getProductId() + " exceeded 50. Note: Reduced back to 50.");
                    result.isValid = false ;
                }
                totalPayment += p.getUnitPrice() * resolvedQty;


        }
        if (totalPayment < 5.00) {
            result.paymentIssues = "Total payment (£" + String.format("%.2f", totalPayment) + ") is less than £5. Checkout aborted.";
            result.isValid = false;
        }
        return result;
    }
    void checkOut() throws IOException, SQLException {
        ValidationResult vr = validateTrolley();
        // Validated trolley for payment and quantity issues, and throws exceptions
        try {
            if (!vr.isValid) {
                if (vr.paymentIssues != null) {
                    throw new UnderMinimumPaymentException(vr.paymentIssues);// throw UMPException
                }
                if (!vr.quantityIssues.isEmpty()) {
                    StringBuilder sb = new StringBuilder("Quantity issues detected: ");
                    for (String issue : vr.quantityIssues) {
                        sb.append(issue).append(" ");
                    }
                    //throw new ExcessiveOrderQuantityException(sb.toString().trim());
                }
            }

            if (!trolley.isEmpty()) {
                // Group the products in the trolley by productId to optimize stock checking
                // Check the database for sufficient stock for all products in the trolley.
                // If any products are insufficient, the update will be rolled back.
                // If all products are sufficient, the database will be updated, and insufficientProducts will be empty.
                // Note: If the trolley is already organized (merged and sorted), grouping is unnecessary.
                ArrayList<Product> groupedTrolley = groupProductsById(trolley);
                ArrayList<Product> insufficientProducts = databaseRW.purchaseStocks(groupedTrolley);

                if (insufficientProducts.isEmpty()) { // If stock is sufficient for all products
                    //get OrderHub and tell it to make a new Order
                    OrderHub orderHub = OrderHub.getOrderHub();
                    Order theOrder = orderHub.newOrder(trolley);
                    trolley.clear();
                    displayTaTrolley = "";
                    displayTaReceipt = String.format(
                            "Order_ID: %s\nOrdered_Date_Time: %s\n%s",
                            theOrder.getOrderId(),
                            theOrder.getOrderedDateTime(),
                            ProductListFormatter.buildString(theOrder.getProductList())
                    );
                    System.out.println(displayTaReceipt);
                } else { // Some products have insufficient stock — build an error message to inform the customer
                    StringBuilder errorMsg = new StringBuilder();
                    for (Product p : insufficientProducts) {
                        errorMsg.append("\u2022 " + p.getProductId()).append(", ")
                                .append(p.getProductDescription()).append(" (Only ")
                                .append(p.getStockQuantity()).append(" available, ")
                                .append(p.getOrderedQuantity()).append(" requested)\n");
                    }
                    theProduct = null;

                    //TODO
                    // Add the following logic here:
                    // 1. Remove products with insufficient stock from the trolley.
                    // 2. Trigger a message window to notify the customer about the insufficient stock, rather than directly changing displayLaSearchResult.
                    //You can use the provided RemoveProductNotifier class and its showRemovalMsg method for this purpose.
                    //remember close the message window where appropriate (using method closeNotifierWindow() of RemoveProductNotifier class)
                    displayLaSearchResult = "Checkout failed due to insufficient stock for the following products:\n" + errorMsg.toString();
                    System.out.println("stock is not enough");
                }
            } else {
                displayTaTrolley = "Your trolley is empty";
                System.out.println("Your trolley is empty");
            }
            updateView();
        } catch (UnderMinimumPaymentException e) {
            // Payment too low,leave the cart unchanged, abort checkout
            displayLaSearchResult = e.getMessage();  // "Total payment (£X.XX) is less than £5. Checkout aborted."
            theProduct = null;

            System.out.println("Checkout failed: " + e.getMessage());


        }
        updateView();
    }
}
