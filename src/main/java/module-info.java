module com.example.mvtsimulation {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.mvtsimulation to javafx.fxml;
    exports com.example.mvtsimulation;
}