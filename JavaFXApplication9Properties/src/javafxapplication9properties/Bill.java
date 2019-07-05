/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package javafxapplication9properties;
import javafx.beans.property.*;
/**
 *
 * @author silve
 */
class Bill {

    private DoubleProperty amountDue = new SimpleDoubleProperty();

    public final double getAmountDue() {
        return amountDue.get();
    }

    public final void setAmountDue(double value) {
        amountDue.set(value);
    }
// Getter della property come oggetto

    public DoubleProperty amountDueProperty() {
        return amountDue;
    }
}
