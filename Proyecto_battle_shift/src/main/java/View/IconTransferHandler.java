/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package View;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 *
 * @author javie
 */
class IconTransferHandler extends TransferHandler {
    private final DataFlavor iconFlavor = new DataFlavor(Icon.class, "Icon");

    @Override
    protected Transferable createTransferable(JComponent c) {
        if (c instanceof JButton) {
            Icon icon = ((JButton) c).getIcon();
            return new Transferable() {
                @Override
                public DataFlavor[] getTransferDataFlavors() {
                    return new DataFlavor[]{iconFlavor};
                }

                @Override
                public boolean isDataFlavorSupported(DataFlavor flavor) {
                    return flavor.equals(iconFlavor);
                }

                @Override
                public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
                    if (flavor.equals(iconFlavor)) {
                        return icon;
                    } else {
                        throw new UnsupportedFlavorException(flavor);
                    }
                }
            };
        }
        return null;
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY;
    }
}