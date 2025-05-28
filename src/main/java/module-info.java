module com.example.demov3 {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.sae.moutonloup.view to javafx.fxml;

    exports com.sae.moutonloup;
    exports com.sae.moutonloup.model;
}
