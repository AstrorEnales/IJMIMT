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
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class SettingsDialog extends BaseDialog implements ListSelectionListener {
    public SettingsDialog() {
        super("IJMIMT");
        setSize(400, 400);

        JPanel selectionPanel = new JPanel();
        GridLayout selectionLayout = new GridLayout();
        selectionLayout.setColumns(1);
        selectionPanel.setLayout(selectionLayout);

        int[] ids = WindowManager.getIDList();
        ArrayList<ImageSelectionPanel> panels = new ArrayList<ImageSelectionPanel>();
        for (int i = 0; ids != null && i < ids.length; i++) {
            ImagePlus img = WindowManager.getImage(ids[i]);
            if (img == null)
                continue;
            ImageSelectionPanel panel = new ImageSelectionPanel(img.getTitle(), ids[i]);
            panels.add(panel);
            selectionPanel.add(panel);
        }
        images = new ImageSelectionPanel[panels.size()];
        selectionLayout.setRows(panels.size());
        panels.toArray(images);

        JScrollPane scrollPane = new JScrollPane(selectionPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBounds(50, 30, 300, 50);

        addComponent(new JLabel("Select the images for the montage:"));

        allSameCheckbox = new JCheckBox();
        allSameCheckbox.setText("All images show the same area");
        allSameCheckbox.setSelected(true);
        addComponent(allSameCheckbox);

        addZProjectionCheckbox = new JCheckBox();
        addZProjectionCheckbox.setText("Add Z-Projection (Only suitable for fluorescent images)");
        addComponent(addZProjectionCheckbox);

        constraints.gridwidth = GridBagConstraints.LINE_START;
        constraints.weightx = 1;
        JButton setLabelsTitleButton = new JButton("Labels as Title");
        setLabelsTitleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for (ImageSelectionPanel panel : images)
                    panel.setLabel(panel.getTitle());
            }
        });
        addComponent(setLabelsTitleButton);

        constraints.gridwidth = GridBagConstraints.CENTER;
        JButton setLabelsNumbersButton = new JButton("Labels as 1,2..N");
        setLabelsNumbersButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for (int i = 0; i < images.length; i++)
                    images[i].setLabel("" + (i + 1));
            }
        });
        addComponent(setLabelsNumbersButton);

        constraints.gridwidth = GridBagConstraints.REMAINDER;
        JButton setLabelsCharsButton = new JButton("Labels as A,B..Z");
        setLabelsCharsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                for (int i = 0; i < images.length; i++)
                    images[i].setLabel("" + (char) (i + 'A'));
            }
        });
        addComponent(setLabelsCharsButton);

        constraints.weightx = 0;
        constraints.weighty = 1;
        addComponent(scrollPane);
        constraints.weighty = 0;
        addOkAndCancelButtons(ids != null, true);
    }

    private final ImageSelectionPanel[] images;
    private final JCheckBox allSameCheckbox;
    private final JCheckBox addZProjectionCheckbox;

    public boolean getAllImagesSame() {
        return allSameCheckbox.isSelected();
    }

    public boolean getAddZProjection() {
        return addZProjectionCheckbox.isSelected();
    }

    public int[] getSelectedIds() {
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for (ImageSelectionPanel image : images) {
            if (image.isSelected())
                ids.add(image.getId());
        }
        int[] result = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++)
            result[i] = ids.get(i);
        return result;
    }

    public ArrayList<String> getSelectedLabels() {
        ArrayList<String> labels = new ArrayList<String>();
        for (ImageSelectionPanel image : images) {
            if (image.isSelected())
                labels.add(image.getLabel());
        }
        return labels;
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
    }
}
