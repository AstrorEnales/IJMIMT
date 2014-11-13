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

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.util.ArrayList;

public class SettingsDialog extends BaseDialog implements ListSelectionListener {
    public SettingsDialog() {
        super("IJMIMT (1/3)");
        setSize(400, 400);

        selection = new JList();
        selection.setSelectedIndex(0);
        selection.addListSelectionListener(this);
        int[] ids = WindowManager.getIDList();
        ArrayList<String> keys = new ArrayList<String>();
        for (int i = 0; ids != null && i < ids.length; i++) {
            ImagePlus img = WindowManager.getImage(ids[i]);
            if (img == null)
                continue;
            keys.add(img.getTitle() + " [id: " + ids[i] + "]");
        }
        imageKeys = new String[keys.size()];
        keys.toArray(imageKeys);
        selection.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        selection.setListData(imageKeys);
        if (ids != null)
            selection.setSelectedIndex(0);

        addComponent(new JLabel("Select the images for the montage:"));

        allSameCheckBox = new JCheckBox();
        allSameCheckBox.setText("All images show the same area");
        allSameCheckBox.setSelected(true);
        addComponent(allSameCheckBox);

        constraints.weighty = 1;
        addComponent(selection);
        constraints.weighty = 0;
        addOkAndCancelButtons(ids != null, true);
    }

    private final JList selection;
    private final String[] imageKeys;
    private final JCheckBox allSameCheckBox;

    public boolean getAllImagesSame() {
        return allSameCheckBox.isSelected();
    }

    public int[] getSelectedIds() {
        int[] selected = selection.getSelectedIndices();
        int[] ids = new int[selected.length];
        for (int i = 0; i < selected.length; i++) {
            String key = imageKeys[i];
            key = key.substring(key.lastIndexOf("[id: "));
            ids[i] = Integer.parseInt(key.substring(5, key.length() - 1));
        }
        return ids;
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
    }
}
