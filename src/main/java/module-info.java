module ch.vilki.secured {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;
    requires javafx.base;

    requires windpapi4j;
    requires org.slf4j;
    requires bcprov.jdk16;
    requires java.datatransfer;
    requires java.desktop;
    requires commons.cli;
    requires org.apache.commons.codec;
    requires com.google.common;
    requires kotlin.stdlib;


    opens ch.vilki.secured to javafx.fxml;
    exports ch.vilki.secured;


}