import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class ImageSelectionPanel extends JPanel {
    public ImageSelectionPanel(String title, int id) {
        super();
        this.title = title;
        this.id = id;
        layout = new GridBagLayout();
        setLayout(layout);
        constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        constraints.weightx = 1;
        addComponent(new JLabel(title));
        constraints.gridwidth = GridBagConstraints.RELATIVE;
        constraints.weightx = 0;
        addComponent(new JLabel("Montage Label: "));
        constraints.weightx = 1;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        labelTextfield = new JTextField(getTitle());
        addComponent(labelTextfield);
        selectedCheckbox = new JCheckBox();
        selectedCheckbox.setSelected(true);
        selectedCheckbox.setText("Use in Montage");
        addComponent(selectedCheckbox);
        setBorder(new BevelBorder(1, Color.GRAY, Color.LIGHT_GRAY));
    }

    private String title;
    private int id;
    private GridBagLayout layout;
    private GridBagConstraints constraints;
    private JTextField labelTextfield;
    private JCheckBox selectedCheckbox;

    private void addComponent(JComponent component) {
        layout.setConstraints(component, constraints);
        add(component);
    }

    public void setLabel(String label) {
        labelTextfield.setText(label);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title.substring(0, title.lastIndexOf("."));
    }

    public String getLabel() {
        return labelTextfield.getText();
    }

    public boolean isSelected() {
        return selectedCheckbox.isSelected();
    }
}
