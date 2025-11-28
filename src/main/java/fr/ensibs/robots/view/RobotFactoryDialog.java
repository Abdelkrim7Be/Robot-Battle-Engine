package fr.ensibs.robots.view;

import fr.ensibs.robots.factories.RobotTaskFactory;
import fr.ensibs.robots.logic.Robot;
import fr.ensibs.robots.logic.RobotTask;
import fr.ensibs.robots.logic.TeamLeader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * Dialog opened when the LOAD button is clicked used to select a jar file,
 * load the classes it contains, and allow to choose a class, and a robot color
 *
 * 
 */
public class RobotFactoryDialog extends JDialog implements ActionListener
{
    // actions names
    private static final String OK = "OK", CANCEL = "Cancel", LOAD_JAR = "Load JAR", CHOOSE_COLOR = "Choose color";

    // the selected class if OK has been clicked (depending on their type)
    private Class<? extends RobotTask<Robot>> robotClass;
    private Class<? extends RobotTask<TeamLeader>> leaderClass;

    private Color color;                           // the color selected by the user
    private final RobotTaskFactory factory;        // the factory to create robot tasks
    private JList<Object> classesList;             // the list of classes loaded from the JAR file
    private DefaultListModel<Object> classesModel; // the model of the classList list
    private int nbLeaders, nbRobots;               // the number of robot classes loaded from the JAR file
    private JButton colorButton, okButton;         // the buttons that can be enabled/disabled

    /**
     * Constructor
     *
     * @param owner   the parent window
     * @param factory the factory to load classes from a jar file
     */
    public RobotFactoryDialog(Window owner, RobotTaskFactory factory)
    {
        super(owner);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setLocationRelativeTo(owner);

        this.factory = factory;
        initComponents();
    }

    /**
     * Give the selected {@link RobotTask<Robot>} class if the OK button has been clicked
     *
     * @return the selected class, if any
     */
    public Class<? extends RobotTask<Robot>> getRobotClass()
    {
        return robotClass;
    }

    /**
     * Give the selected {@link RobotTask<TeamLeader>} class if the OK button has been clicked
     *
     * @return the selected class, if any
     */
    public Class<? extends RobotTask<TeamLeader>> getLeaderClass()
    {
        return leaderClass;
    }

    /**
     * Give the selected color, if the OK button has been clicked
     *
     * @return the selected color
     */
    public Color getColor()
    {
        return color;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        switch (e.getActionCommand()) {
            case LOAD_JAR:
                loadJar();
                break;
            case CHOOSE_COLOR:
                chooseColor();
                break;
            case OK:
                okClicked();
            case CANCEL:
                dispose();
                break;
        }
    }

    /**
     * Action when the 'OK' button is clicked
     */
    private void okClicked()
    {
        Object elt = classesList.getSelectedValue();
        if (elt instanceof Class) {
            int idx = classesList.getSelectedIndex();
            if (nbRobots == 0 || idx > nbRobots + 1) {
                this.leaderClass = (Class<? extends RobotTask<TeamLeader>>) elt;
            } else {
                this.robotClass = (Class<? extends RobotTask<Robot>>) elt;
            }
        }
    }

    /**
     * Action when the 'choose color' button is clicked
     */
    private void chooseColor()
    {
        Color color = JColorChooser.showDialog(this, "Choose robots color", colorButton.getBackground());
        if (color != null) {
            colorButton.setBackground(color);
            double lightness = 0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue();
            colorButton.setForeground(lightness < 128 ? Color.WHITE : Color.BLACK);
            this.color = color;
            enableOkButton();
        }
    }

    /**
     * Action when the 'load JAR' button is clicked
     */
    private void loadJar()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileFilter(new FileNameExtensionFilter("JAR files", "jar"));
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            this.factory.loadJar(chooser.getSelectedFile());
            classesModel.clear();
            this.nbRobots = addClasses(factory.listRobotClasses(), "ROBOT");
            this.nbLeaders = addClasses(factory.listLeaderClasses(), "LEADER");
        }
    }

    /**
     * Add a line with the given title and the given classes in the classes list
     *
     * @param classes the classes to be added in the list
     * @param title   the title to be added before the classes
     * @return the number of classes that have been added
     */
    private int addClasses(List<?> classes, String title)
    {
        if (classes != null && !classes.isEmpty()) {
            classesModel.addElement(title + (classes.size() > 1 ? " CLASSES" : " CLASS"));
            classesModel.addAll(classes);
            return classes.size();
        }
        return 0;
    }

    /**
     * Enable/disable the OK button according to the selected values (color and class)
     */
    private void enableOkButton()
    {
        boolean ok = color != null && classesList.getSelectedValue() instanceof Class<?>;
        okButton.setEnabled(ok);
    }

    /**
     * Initialize the components in the dialog. Invoked in the constructor
     */
    private void initComponents()
    {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));
        add(panel);

        JPanel northButtons = new JPanel(new GridLayout(1, 2));
        northButtons.add(makeButton(LOAD_JAR));
        colorButton = makeButton(CHOOSE_COLOR);
        northButtons.add(colorButton);
        panel.add(northButtons, BorderLayout.NORTH);

        JPanel southButtons = new JPanel(new GridLayout(1, 2));
        southButtons.add(makeButton(CANCEL));
        okButton = makeButton(OK);
        okButton.setEnabled(false);
        southButtons.add(okButton);
        panel.add(southButtons, BorderLayout.SOUTH);

        panel.add(makeList(), BorderLayout.CENTER);
    }

    /**
     * Make a button with the given text and the current instance as listener
     *
     * @param text the text of the button
     * @return the new button
     */
    private JButton makeButton(String text)
    {
        JButton button = new JButton(text);
        button.addActionListener(this);
        return button;
    }

    /**
     * Make the list that contains the classes loaded from the JAR file
     *
     * @return component including the list
     */
    private Component makeList()
    {
        classesModel = new DefaultListModel<>();
        this.nbRobots = addClasses(factory.listRobotClasses(), "ROBOT");
        this.nbLeaders = addClasses(factory.listLeaderClasses(), "LEADER");
        classesList = new JList<>(classesModel);
        classesList.setCellRenderer(new ClassListCellRenderer());
        classesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        classesList.setPreferredSize(new Dimension(150, 150));
        classesList.addListSelectionListener((event) -> enableOkButton());

        return new JScrollPane(classesList);
    }

    /**
     * Class that manages how the elements in the classes list are displayed
     */
    static class ClassListCellRenderer extends JLabel implements ListCellRenderer<Object>
    {
        private final JLabel separator; // separator between the classes lists containing a title

        /**
         * Constructor
         */
        public ClassListCellRenderer()
        {
            // label properties (for the classes only)
            setOpaque(true);
            setPreferredSize(new Dimension(100, 20));
            setHorizontalAlignment(JLabel.LEFT);

            // create the separator
            separator = new JLabel();
            separator.setOpaque(true);
            separator.setHorizontalAlignment(JLabel.CENTER);
            separator.setBackground(Color.LIGHT_GRAY);
            separator.setPreferredSize(new Dimension(100, 10));
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
        {
            if (value instanceof Class) {
                setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
                setText(((Class<?>) value).getSimpleName());
                return this;
            }
            separator.setText(value.toString());
            return separator;
        }
    }
}
