package ci553.happyshop.client.customer;

import ci553.happyshop.utility.UIStyle;
import ci553.happyshop.utility.WinPosManager;
import ci553.happyshop.utility.WindowBounds;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * The CustomerView is separated into two sections by a line :
 *
 * 1. Search Page – Always visible, allowing customers to browse and search for products.
 * 2. the second page – display either the Trolley Page or the Receipt Page
 *    depending on the current context. Only one of these is shown at a time.
 */

public class CustomerView  {
    public CustomerController cusController;

    private final int WIDTH = UIStyle.customerWinWidth;
    private final int HEIGHT = UIStyle.customerWinHeight;
    private final int COLUMN_WIDTH = WIDTH / 2 - 10;

    private HBox hbRoot; // Top-level layout manager
    private VBox vbTrolleyPage;  //vbTrolleyPage and vbReceiptPage will swap with each other when need
    private VBox vbReceiptPage;

    TextField tfId; //for user input on the search page. Made accessible so it can be accessed or modified by CustomerModel
    TextField tfName; //for user input on the search page. Made accessible so it can be accessed by CustomerModel

    //four controllers needs updating when program going on
    private ImageView ivProduct; //image area in searchPage
    private Label lbProductInfo;//product text info in searchPage
    private TextArea taTrolley; //in trolley Page
    private TextArea taReceipt;//in receipt page

    // Holds a reference to this CustomerView window for future access and management
    // (e.g., positioning the removeProductNotifier when needed).
    private Stage viewWindow;

    private ComboBox<String> cbSearchMode;  //search dropdown
    private TextField tfMinPrice; //Price range search
    private TextField tfMaxPrice;
    private HBox hbKeyword;
    private HBox hbPriceRange; // price container

    public void start(Stage window) {
        VBox vbSearchPage = createSearchPage();
        vbTrolleyPage = CreateTrolleyPage();
        vbReceiptPage = createReceiptPage();

        // Create a divider line
        Line line = new Line(0, 0, 0, HEIGHT);
        line.setStrokeWidth(4);
        line.setStroke(Color.PINK);
        VBox lineContainer = new VBox(line);
        lineContainer.setPrefWidth(4); // Give it some space
        lineContainer.setAlignment(Pos.CENTER);

        hbRoot = new HBox(10, vbSearchPage, lineContainer, vbTrolleyPage); //initialize to show trolleyPage
        hbRoot.setAlignment(Pos.CENTER);
        hbRoot.setStyle(UIStyle.rootStyle);

        Scene scene = new Scene(hbRoot, WIDTH, HEIGHT);
        window.setScene(scene);
        window.setTitle("🛒 HappyShop Customer Client");
        WinPosManager.registerWindow(window,WIDTH,HEIGHT); //calculate position x and y for this window
        window.show();
        viewWindow=window;// Sets viewWindow to this window for future reference and management.
    }

    private VBox createSearchPage() {
      Label laTitle = new Label("Search Products");
      laTitle.setStyle(UIStyle.labelTitleStyle);

      Label laSearchBy = new Label("SearchBy:");
      laSearchBy.setStyle(UIStyle.labelStyle);

      cbSearchMode = new ComboBox<>();
      cbSearchMode.getItems().addAll("By Product ID", "By Name/Description", "By Price Range");
      cbSearchMode.setValue("By Product ID");
      cbSearchMode.setStyle(UIStyle.comboBoxStyle);
      cbSearchMode.setOnAction(e -> switchSearchmode());

      HBox hbSearchMode = new HBox(10, laSearchBy, cbSearchMode);
      hbSearchMode.setAlignment(Pos.CENTER);

      Label laSearch = new Label("Product ID / Name:");
      laSearch.setStyle(UIStyle.labelStyle);

      Label laKeyword = new Label("Search:");
      laKeyword.setAlignment(Pos.CENTER);

      tfId = new TextField();
      tfId.setPromptText("Enter product ID");
      tfId.setPrefWidth(200);
      tfId.setStyle(UIStyle.textFiledStyle);
      tfId.setOnAction(actionEvent -> {
          try {
              cusController.doAction("Search");
          }catch (SQLException | IOException ex) {
              ex.printStackTrace();
          }
      });

      hbKeyword = new HBox(10, laKeyword, tfId);
      hbKeyword.setAlignment(Pos.CENTER);

      Label laMinPrice = new Label("Min Price £:");
      laMinPrice.setStyle(UIStyle.labelStyle);
      tfMinPrice = new TextField();
      tfMinPrice.setPromptText("0.00");
      tfMinPrice.setPrefWidth(80);
      tfMinPrice.setStyle("-fx-font-size: 14px");

      Label laMaxPrice = new Label("Max Price £:");
      laMaxPrice.setStyle(UIStyle.labelStyle);
      tfMaxPrice = new TextField();
      tfMaxPrice.setPromptText("999.99");
      tfMaxPrice.setPrefWidth(80);
      tfMaxPrice.setStyle("-fx-font-size: 14px");

      hbPriceRange = new HBox(10, laMinPrice, tfMinPrice, laMaxPrice, tfMaxPrice);
      hbPriceRange.setAlignment(Pos.CENTER);
      hbPriceRange.setVisible(false);
      hbPriceRange.setManaged(false);

      Button btnSearch = new Button("Search");
      btnSearch.setOnAction(this::buttonClicked);
      btnSearch.setStyle(UIStyle.buttonStyle);

      VBox vbSearchInputs = new VBox(10, hbSearchMode, hbKeyword, hbPriceRange, btnSearch);
      vbSearchInputs.setAlignment(Pos.CENTER);

      ivProduct = new ImageView(new Image("imageholder.jpg"));
      ivProduct.setFitHeight(150);
      ivProduct.setFitWidth(200);
      ivProduct.setPreserveRatio(true);

      lbProductInfo = new Label("No Product was searched");
      lbProductInfo.setStyle(UIStyle.labelStyle);
      lbProductInfo.setWrapText(true);
      lbProductInfo.setMaxWidth(COLUMN_WIDTH - 20);
      lbProductInfo.setMinHeight(80);

      Button btnAddToTrolley = new Button("Add to Trolley");
      btnAddToTrolley.setOnAction(this::buttonClicked);
      btnAddToTrolley.setStyle(UIStyle.buttonStyle);

      VBox vbSearchPage = new VBox(15, laTitle, vbSearchInputs, ivProduct, lbProductInfo, btnAddToTrolley);
      vbSearchPage.setPrefWidth(COLUMN_WIDTH);
      vbSearchPage.setAlignment(Pos.CENTER);
      vbSearchPage.setStyle("-fx-padding: 15px;");

        return vbSearchPage;
    }



    private VBox CreateTrolleyPage() {
        Label laPageTitle = new Label("🛒🛒  Trolley 🛒🛒");
        laPageTitle.setStyle(UIStyle.labelTitleStyle);

        taTrolley = new TextArea();
        taTrolley.setEditable(false);
        taTrolley.setPrefSize(WIDTH/2, HEIGHT-50);

        Button btnCancel = new Button("Cancel");
        btnCancel.setOnAction(this::buttonClicked);
        btnCancel.setStyle(UIStyle.buttonStyle);

        Button btnCheckout = new Button("Check Out");
        btnCheckout.setOnAction(this::buttonClicked);
        btnCheckout.setStyle(UIStyle.buttonStyle);

        HBox hbBtns = new HBox(10, btnCancel,btnCheckout);
        hbBtns.setStyle("-fx-padding: 15px;");
        hbBtns.setAlignment(Pos.CENTER);

        vbTrolleyPage = new VBox(15, laPageTitle, taTrolley, hbBtns);
        vbTrolleyPage.setPrefWidth(COLUMN_WIDTH);
        vbTrolleyPage.setAlignment(Pos.TOP_CENTER);
        vbTrolleyPage.setStyle("-fx-padding: 15px;");
        return vbTrolleyPage;
    }

    private VBox createReceiptPage() {
        Label laPageTitle = new Label("Receipt");
        laPageTitle.setStyle(UIStyle.labelTitleStyle);

        taReceipt = new TextArea();
        taReceipt.setEditable(false);
        taReceipt.setPrefSize(WIDTH/2, HEIGHT-50);

        Button btnCloseReceipt = new Button("OK & Close"); //btn for closing receipt and showing trolley page
        btnCloseReceipt.setStyle(UIStyle.buttonStyle);

        btnCloseReceipt.setOnAction(this::buttonClicked);

        vbReceiptPage = new VBox(15, laPageTitle, taReceipt, btnCloseReceipt);
        vbReceiptPage.setPrefWidth(COLUMN_WIDTH);
        vbReceiptPage.setAlignment(Pos.TOP_CENTER);
        vbReceiptPage.setStyle(UIStyle.rootStyleYellow);
        return vbReceiptPage;
    }

    private void switchSearchmode(){
        String mode = cbSearchMode.getValue();

        if (mode.equals("By Price Range")){
            hbKeyword.setVisible(false);
            hbKeyword.setManaged(false);
            hbPriceRange.setVisible(true);
            hbPriceRange.setManaged(true);
        }else {
            hbKeyword.setVisible(true);
            hbKeyword.setManaged(true);
            hbPriceRange.setVisible(false);
            hbPriceRange.setManaged(false);

            if (mode.equals("By Product ID")){
                tfId.setPromptText("Enter product ID (e.g. 0001)");
            }else {
                tfId.setPromptText("Enter product name");
            }

        }
    }
    public String getSearchMode(){
        return  cbSearchMode.getValue();
    }

    public String getMinPrice(){
        return tfMinPrice.getText().trim();
    }

    public String getMaxPrice(){
        return tfMaxPrice.getText().trim();
    }




    private void buttonClicked(ActionEvent event) {
        try{
            Button btn = (Button)event.getSource();
            String action = btn.getText();
            if(action.equals("Add to Trolley")){
                showTrolleyOrReceiptPage(vbTrolleyPage); //ensure trolleyPage shows if the last customer did not close their receiptPage
            }
            if(action.equals("OK & Close")){
                showTrolleyOrReceiptPage(vbTrolleyPage);
            }
            cusController.doAction(action);
        }
        catch(SQLException e){
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void update(String imageName, String searchResult, String trolley, String receipt) {

        ivProduct.setImage(new Image(imageName));
        lbProductInfo.setText(searchResult);
        taTrolley.setText(trolley);
        if (!receipt.equals("")) {
            showTrolleyOrReceiptPage(vbReceiptPage);
            taReceipt.setText(receipt);
        }
    }

    // Replaces the last child of hbRoot with the specified page.
    // the last child is either vbTrolleyPage or vbReceiptPage.
    private void showTrolleyOrReceiptPage(Node pageToShow) {
        int lastIndex = hbRoot.getChildren().size() - 1;
        if (lastIndex >= 0) {
            hbRoot.getChildren().set(lastIndex, pageToShow);
        }
    }

    WindowBounds getWindowBounds() {
        return new WindowBounds(viewWindow.getX(), viewWindow.getY(),
                  viewWindow.getWidth(), viewWindow.getHeight());
    }
}
