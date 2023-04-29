module com.hamza.modelsim {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jetbrains.annotations;

    opens com.hamza.modelsim to javafx.fxml;
    exports com.hamza.modelsim.components;
    exports com.hamza.modelsim.abstractcomponents;
    exports com.hamza.modelsim.constants;
    exports com.hamza.modelsim;
}