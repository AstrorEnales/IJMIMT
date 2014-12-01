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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class BaseDialog extends JDialog {
    protected BaseDialog(String title) {
        setModal(true);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        setTitle(title);
        layout = new GridBagLayout();
        setLayout(layout);

        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 0;
    }

    private final GridBagLayout layout;
    protected final GridBagConstraints constraints;

    protected void addComponent(JComponent component) {
        layout.setConstraints(component, constraints);
        add(component);
    }

    protected void addOkAndCancelButtons(boolean okActive, boolean cancelActive) {
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        constraints.weightx = 1;
        constraints.weighty = 0;

        JButton okButton = new JButton("Ok");
        okButton.setEnabled(okActive);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                canceled = false;
                cleanupAndClose();
            }
        });
        addComponent(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(cancelActive);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                canceled = true;
                cleanupAndClose();
            }
        });
        addComponent(cancelButton);
    }

    protected void cleanupAndClose() {
        setVisible(false);
    }

    public boolean getCanceled() {
        return canceled;
    }

    private boolean canceled;

    public void showDialog() {
        canceled = false;
        setVisible(true);
    }
}
