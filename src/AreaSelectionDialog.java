/*
The MIT License (MIT)

Copyright (c) 2014 Marcel Friedrichs

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.ArrayList;

public final class AreaSelectionDialog extends BaseDialog implements ListSelectionListener {
    public AreaSelectionDialog(ArrayList<Rectangle> areas) {
        super("IJMIMT (2/3)");
        this.areas = areas;
        setSize(200, 400);

        growPixel = new JSpinner();
        growPixel.setModel(new SpinnerNumberModel());
        growPixel.setValue(0);
        selection = new JList();
        selection.setSelectedIndex(0);
        selection.addListSelectionListener(this);
        String[] ids = new String[areas.size()];
        for (int i = 0; i < areas.size(); i++) {
            Rectangle r = areas.get(i);
            ids[i] = (i + 1) + ": " + r.x + ", " + r.y + "; " + r.width + ", " + r.height;
        }
        selection.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        selection.setListData(ids);
        selection.setSelectedIndex(0);

        addComponent(new JLabel("Select one or more areas to be used in the montage"));
        addComponent(new JLabel("Areas:"));
        constraints.weighty = 1;
        addComponent(selection);
        constraints.weighty = 0;
        constraints.gridwidth = GridBagConstraints.RELATIVE;

        addComponent(new JLabel("Area increase [px]: "));
        growPixel.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent changeEvent) {
                valueChanged(null);
            }
        });
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        addComponent(growPixel);

        addOkAndCancelButtons(true, true);
    }

    private final JList selection;
    private final JSpinner growPixel;
    private final ArrayList<Rectangle> areas;

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        Overlay overlay = new Overlay();
        for (int i = 0; i < areas.size(); i++) {
            Rectangle area = areas.get(i);
            Roi r = new Roi(area);
            r.setStrokeColor(Color.RED);
            overlay.add(r);

            Font font = new Font("Arial", Font.PLAIN, 28);
            TextRoi textRoi = new TextRoi(area.getCenterX(), area.getCenterY(), "" + (i + 1), font);
            textRoi.setStrokeColor(Color.white);
            overlay.add(textRoi);
        }

        int[] selected = selection.getSelectedIndices();
        result = areas.get(selected[0]).getBounds();
        for (int i = 1; i < selected.length; i++)
            result.add(areas.get(selected[i]).getBounds());
        int grow = (Integer) growPixel.getValue();
        result = new Rectangle(Math.max(0, result.x - grow), Math.max(0, result.y - grow),
                result.width + grow * 2, result.height + grow * 2);

        Roi r = new Roi(result);
        r.setStrokeColor(Color.YELLOW);
        overlay.add(r);

        currentImage = WindowManager.getCurrentImage();
        currentImage.setOverlay(overlay);
    }

    private ImagePlus currentImage;
    private Rectangle result;

    @Override
    protected void cleanupAndClose() {
        currentImage.getOverlay().clear();
        currentImage.setHideOverlay(true);
        super.cleanupAndClose();
    }

    public Rectangle getResult() {
        return result;
    }
}
