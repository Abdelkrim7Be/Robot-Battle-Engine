package fr.ensibs.robots.view;

import fr.ensibs.robots.factories.BattleFactory;
import fr.ensibs.robots.factories.RobotTaskFactory;
import fr.ensibs.robots.logic.*;
import fr.ensibs.robots.logic.Robot;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static fr.ensibs.robots.logic.BattleSetup.NB_TEAMMATES;

/**
 * Panel that contains buttons to control battle actions (load and start/stop)
 * and information about the battle state
 *
 * @author Pascale Launay
 */
public class ControlsPanel extends JPanel implements ActionListener
{
    // actions names
    private static final String START = "Start", STOP = "Stop", LOAD = "Load robots";

    private final Battlefield battlefield;      // the battlefield instance
    private final BattlefieldEngine engine;     // the engine that runs the robots tasks periodically
    private final List<DroidView<? extends Droid>> views; // the views of the robots engaged in the battlefield
    private final BattleFactory factory;        // factory to make robots
    private final RobotTaskFactory taskFactory; // factory to make tasks

    private JButton loadButton, startButton;    // buttons that should be enabled/disabled
    private JTable robotsTable;                 // table that displays the robots states

    /**
     * Constructor
     *
     * @param views       the views of the robots engaged in the battlefield
     * @param factory     the factory to make robots
     * @param taskFactory the factory to make tasks
     */
    public ControlsPanel(List<DroidView<? extends Droid>> views, BattleFactory factory, RobotTaskFactory taskFactory)
    {
        super(new BorderLayout(5, 5));
        setBackground(Color.WHITE);

        this.views = views;
        this.factory = factory;
        this.taskFactory = taskFactory;
        this.battlefield = factory.makeBattlefield();
        this.engine = new BattlefieldEngine(battlefield, 100);

        initComponents();
    }

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        robotsTable.revalidate();
    }

    /**
     * Method invoked when a button is clicked
     *
     * @param e the button event
     */
    @Override
    public void actionPerformed(ActionEvent e)
    {
        switch (e.getActionCommand()) {
            case START:
            case STOP: // start/stop button clicked
                startStop();
                break;
            case LOAD: // load button clicked
                loadRobots();
                break;
        }
    }

    /**
     * Method invoked when the LOAD button is clicked. Open a RobotFactoryDialog,
     * and then add the new robots (if any) to the battlefield
     */
    private void loadRobots()
    {
        // open the factory dialog
        Window owner = SwingUtilities.getWindowAncestor(this);
        RobotFactoryDialog dialog = new RobotFactoryDialog(owner, taskFactory);
        dialog.pack();
        dialog.setVisible(true);
        // dialog closed: get the new robots
        Class<? extends RobotTask<Robot>> robotClass = dialog.getRobotClass();
        Class<? extends RobotTask<TeamLeader>> leaderClass = dialog.getLeaderClass();
        if (robotClass != null) {
            makeRobot(robotClass, dialog.getColor());
        } else if (leaderClass != null) {
            makeTeam(leaderClass, dialog.getColor());
        }
    }

    /**
     * Make a team from the given leader task class having the given color and
     * add the leader and the teammates to the battlefield
     *
     * @param clazz the class of the leader task
     * @param color the color of the team
     */
    private void makeTeam(Class<? extends RobotTask<TeamLeader>> clazz, Color color)
    {
        List<Droid> teammates = new ArrayList<>();
        for (int i = 0; i < NB_TEAMMATES; i++) {
            Droid mate = factory.makeDroid();
            teammates.add(mate);
            this.views.add(factory.makeRobotView(mate, clazz.getSimpleName() + " mate", color));
        }
        RobotTask<TeamLeader> task = taskFactory.makeLeaderTask(clazz);
        TeamLeader leader = factory.makeTeamLeader(teammates);
        task.setRobot(leader);
        this.engine.addTask(task);
        this.views.add(factory.makeRobotView(leader, clazz.getSimpleName() + " leader", color));
    }

    /**
     * Make a robot from the given task having the given color and add it to the
     * battlefield
     *
     * @param clazz the class of the robot task
     * @param color the color of the robot
     */
    private void makeRobot(Class<? extends RobotTask<Robot>> clazz, Color color)
    {
        RobotTask<Robot> task = taskFactory.makeRobotTask(clazz);
        Robot robot = factory.makeRobot();
        task.setRobot(robot);
        this.engine.addTask(task);
        this.views.add(factory.makeRobotView(robot, clazz.getSimpleName(), color));
    }

    /**
     * Method invoked when the START/STOP button is clicked. Start or stop the
     * battlefield engine and enable/disable buttons accordingly
     */
    private void startStop()
    {
        boolean start = startButton.getText().equals(START);
        startButton.setText(start ? STOP : START);
        loadButton.setEnabled(!start);
        if (start) {
            engine.start();
        } else {
            engine.stop();
        }
    }

    /**
     * Initialize the components in the panel. Invoked in the constructor
     */
    private void initComponents()
    {
        // LOAD and START/STOP buttons
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        loadButton = new JButton(LOAD);
        loadButton.addActionListener(this);
        buttonPanel.add(loadButton);
        startButton = new JButton(START);
        startButton.addActionListener(this);
        buttonPanel.add(startButton);
        add(buttonPanel, BorderLayout.NORTH);

        // table that displays the robots states
        robotsTable = new JTable(new RobotsTableModel(views));
        robotsTable.setRowSelectionAllowed(false);
        robotsTable.setCellSelectionEnabled(false);
        robotsTable.setDefaultRenderer(String.class, new RobotCellRenderer(views));
        TableColumnModel columnModel = robotsTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(150);
        columnModel.getColumn(1).setPreferredWidth(50);
        // insert the table in a scroll pane
        JScrollPane scrollPane = new JScrollPane(robotsTable);
        scrollPane.setPreferredSize(new Dimension(200, 500));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Class that manages the elements in the robot table. The table has 2 columns (robots
     * names and energy) and 1 row for each robot
     */
    static class RobotsTableModel extends AbstractTableModel
    {
        private final List<DroidView<? extends Droid>> views;  // the views of the robots engaged in the battle

        /**
         * Constructor
         *
         * @param views the views of the robots engaged in the battle
         */
        public RobotsTableModel(List<DroidView<? extends Droid>> views)
        {
            this.views = views;
        }

        @Override
        public int getColumnCount()
        {
            return 2;
        }

        @Override
        public int getRowCount()
        {
            return views.size();
        }

        @Override
        public String getColumnName(int columnIndex)
        {
            return columnIndex == 0 ? "ROBOT" : "ENERGY";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex)
        {
            return String.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex)
        {
            DroidView<? extends Droid> view = views.get(rowIndex);
            return columnIndex == 1 ? Integer.toString(view.getRobot().getEnergy()) : view.getName();
        }
    }

    /**
     * Class that manages how the elements in the robot table are displayed
     */
    static class RobotCellRenderer extends JLabel implements TableCellRenderer
    {
        private final List<DroidView<? extends Droid>> views;  // the views of the robots engaged in the battle

        /**
         * Constructor
         *
         * @param views the views of the robots engaged in the battle
         */
        public RobotCellRenderer(List<DroidView<? extends Droid>> views)
        {
            this.views = views;
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col)
        {
            setText((String) value);

            // define the cell background and foreground colors
            Color color = views.get(row).getColor();
            setBackground(color);
            double lightness = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue());
            setForeground(lightness < 128 ? Color.WHITE : Color.BLACK);

            // define the text alignment (left for names and center for energy values)
            setHorizontalAlignment(col == 1 ? JLabel.CENTER : JLabel.LEFT);
            return this;
        }
    }
}