package ci553.happyshop.client.warehouse;

import java.io.IOException;
import java.sql.SQLException;

public class WarehouseController {
    private WarehouseModel model;

    public WarehouseController(WarehouseModel model) {
        this.model = model;
    }


    public void WarehouseClient(WarehouseModel model) {
        this.model = model;
    }

    void process(String action) throws SQLException, IOException {
        switch (action) {
            case "🔍":
                model.doSearch();
                break;
            case "Edit":
                model.doEdit();
                break;
            case "Delete":
                model.doDelete();
                break;
            case "➕":
                model.doChangeStockBy("add");
                break;
            case "➖":
                model.doChangeStockBy("sub");
                break;
            case "Submit":
                model.doSummit();
                break;
            case "Cancel":  // clear the editChild
                model.doCancel();
                break;
        }
    }
}
