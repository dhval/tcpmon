package com.dhval.jpa;

import apache.tcpmon.OpenFileAction;
import com.dhval.utils.JUtils;
import com.dhval.utils.Utils;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Profile("db")
public class TransactionPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionPanel.class);

    @Autowired TransactionLog transactionLog;
    Set<TransactionTable> transactions = new LinkedHashSet<>();

    public JTabbedPane notebook = null;
    public DefaultTableModel tableModel = null;
    public JTable connectionTable = null;
    public JButton runButton = null;
    public JButton clearButton = null;
    public JButton viewButton = null;
    public JButton runSQLButton = null;

    private JLabel fileLabel = new JLabel("Select SQL file.");
    JTextField queryType;
    JTextField trackingId;
    JComboBox<String> maxResult = new JComboBox<>(new String[] {"10", "20" , "30", "40", "50"});
    JComboBox<String> customQueries = new JComboBox<>(new String[] {"SELECT TOP 1"});

    private RSyntaxTextArea syntaxTextArea = new RSyntaxTextArea(20, 60);

    private boolean enableScheduler = false;
    public JCheckBox retryBox = null;

    public TransactionPanel(@Autowired JTabbedPane _notebook) {
        notebook = _notebook;
        notebook.addTab("Transaction DB", this);
        this.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        maxResult.setMaximumSize(new Dimension(2, Short.MAX_VALUE));
        top.add(maxResult);
        // Query Type
        top.add(new JLabel("QueryType"));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(queryType = JUtils.jTextField("ssl-proxy", 25, 150));
        // Tracking Id
        top.add(new JLabel("TrackingId"));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(trackingId = JUtils.jTextField("", 25, 150));
        top.add(Box.createHorizontalGlue());
        top.add(runButton = new JButton("Run"));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(viewButton = new JButton("View"));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(clearButton = new JButton("Clear"));

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(retryBox = new JCheckBox("Auto Refresh"));
        bottom.add(Box.createRigidArea(new Dimension(5, 0)));
        bottom.add(new JLabel("File:"));
        bottom.add(Box.createRigidArea(new Dimension(5, 0)));
        bottom.add(fileLabel);
        bottom.add(Box.createHorizontalGlue());
        bottom.add(new JButton(new OpenFileAction("Open", this, fileLabel)));
        bottom.add(Box.createRigidArea(new Dimension(5, 0)));
        bottom.add(runSQLButton = new JButton("Run SQL"));


        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{ "ID", "TXN_ID", "TYPE", "TRACKING_ID", "TIME", "REQ", "RSP"}, 0);

        connectionTable = new JTable(10, 7) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        connectionTable.setModel(tableModel);
        connectionTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Sort table using column 4
        /**
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(connectionTable.getModel());
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(4, SortOrder.DESCENDING));
        sorter.setSortKeys(sortKeys);
        connectionTable.setRowSorter(sorter);
        **/

        TableColumn col;
        col = connectionTable.getColumnModel().getColumn(5);
        col.setMaxWidth(col.getPreferredWidth()/2);
        col = connectionTable.getColumnModel().getColumn(6);
        col.setMaxWidth(col.getPreferredWidth()/2);
        col = connectionTable.getColumnModel().getColumn(3);
        col.setPreferredWidth(col.getPreferredWidth() * 2);
        ListSelectionModel sel = connectionTable.getSelectionModel();

        JPanel tablePane = new JPanel();
        tablePane.setLayout(new BorderLayout());
        JScrollPane tableScrollPane = new JScrollPane(connectionTable);
        tablePane.add(tableScrollPane, BorderLayout.CENTER);

        JPanel pane2 = new JPanel();
        pane2.setLayout(new BorderLayout());

        JSplitPane pane1 = new JSplitPane(0);
        pane1.setDividerSize(4);
        pane1.setTopComponent(tablePane);
        pane1.setBottomComponent(new RTextScrollPane(syntaxTextArea));
        pane1.setDividerLocation(150);
        center.add(pane1, BorderLayout.CENTER);


        this.add(top, BorderLayout.NORTH);
        this.add(pane1, BorderLayout.CENTER);
        this.add(bottom, BorderLayout.SOUTH);

    }

    @PostConstruct
    public void configureListeners() {
        maxResult.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                transactionLog.setSize((String) maxResult.getSelectedItem());
            }
        });
        connectionTable.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent mouseEvent) {
                JTable table =(JTable) mouseEvent.getSource();
                Point point = mouseEvent.getPoint();
                int row = table.rowAtPoint(point);
                int col = table.columnAtPoint(point);
                if (mouseEvent.getClickCount() == 2) {
                   LOG.info("Row: " + row + " " + col);
                   fetchData(row, col);
                }
            }
        });
        runButton.addActionListener(new ActionListener() {
             @Override
            public void actionPerformed(ActionEvent e) {
                String query = queryType.getText();
                String trackId = trackingId.getText();
                if (!StringUtils.isEmpty(query) && !StringUtils.isEmpty(trackId)) {
                    transactions.addAll(transactionLog.byTrackingIdAndQueryType(trackId, query));
                } else if (!StringUtils.isEmpty(query)) {
                    transactions.addAll(transactionLog.byQueryType(query));
                } else if (!StringUtils.isEmpty(trackId)) {
                    transactions.addAll(transactionLog.byTrackingId(trackId));
                }
                updateTable();
             }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ListSelectionModel lsm = connectionTable.getSelectionModel();
                int min = lsm.getMinSelectionIndex();
                int max =  lsm.getMaxSelectionIndex();
                LOG.info("Remove: "  + lsm.getMinSelectionIndex()  + " - " + lsm.getMaxSelectionIndex() + "-of-" + tableModel.getRowCount());
                for(int i=max; i>= min; i--) {
                    LOG.info("Remove: "  + i + " " + tableModel.getValueAt(i, 0));
                    tableModel.removeRow(i);
                }

                lsm.clearSelection();
                lsm.setSelectionInterval(0, 0);

            }
        });
        viewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ListSelectionModel lsm = connectionTable.getSelectionModel();
                fetchData(lsm.getMinSelectionIndex(), 0);
            }
        });
        runSQLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
               execSQL();
            }
        });
        retryBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                try {
                    String echoFile = fileLabel.getText();
                    if (StringUtils.isEmpty(echoFile) || !Utils.isFilePresent(echoFile)) {
                        fileLabel.setText("File Not Found:");
                        return;
                    }
                    enableScheduler = e.getStateChange() == ItemEvent.SELECTED;
                } catch (Exception ex) {
                    LOG.warn(ex.getMessage(), ex);
                }
            }
        });
    }

    private void fetchData(int rowId, int colId) {
        if (rowId < 0 || rowId > tableModel.getRowCount()) return;
        int dbId = Integer.parseInt((String) tableModel.getValueAt(rowId, 0));
        LOG.info("Sel: " + rowId + "-db-"+ dbId);

        try {
            String result;
            if (colId == 6) {
                result = transactionLog.queryById2(dbId);
            } else {
                result = transactionLog.queryById1(dbId);
            }
            if (Utils.isXML(result)) {
                syntaxTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
                syntaxTextArea.setCodeFoldingEnabled(true);
                syntaxTextArea.setText(Utils.prettyXML(result));
            } else {
                syntaxTextArea.setText(result);
            }
        } catch (Exception ex) {
            LOG.warn(ex.getMessage(), ex);
            syntaxTextArea.setText(ex.getLocalizedMessage());
        }
    }

    private void updateTable() {
        List<String> list = IntStream.range(0, tableModel.getRowCount()).mapToObj(i -> tableModel.getValueAt(i,0).toString()).collect(Collectors.toList());
        for(TransactionTable row : transactions) {
            // Do not add duplicate rows.
            if (!list.contains(row.getId()))
                tableModel.insertRow(0, new Object[] {row.getId(), row.getTxnId(), row.getQueryType(), row.getUserTrackingId(), row.getRequestDateTime(), row.getRequestData(), row.getResponseData()});
        }
        transactions.clear();
    }

    private void execSQL() {
        try {
            String echoFile = fileLabel.getText();
            if (StringUtils.isEmpty(echoFile) || !Utils.isFilePresent(echoFile)) {
                fileLabel.setText("File Not Found:");
                return;
            }
            String query = new String(Files.readAllBytes(Paths.get(echoFile)));
            transactions.addAll(transactionLog.query(query.replace("\n", " ")));
            updateTable();
        } catch (Exception ex) {
            LOG.warn(ex.getMessage(), ex);
        }
    }

    @Scheduled(initialDelay = 30000, fixedDelay = 15000L)
    public void scheduler() {
        if (!enableScheduler) return;
        execSQL();
    }

}
