package com.dhval.ui;

import apache.tcpmon.JUtils;
import com.dhval.jpa.TransactionLog;
import com.dhval.jpa.TransactionTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionPanel extends JPanel {
    private static final Logger LOG = LoggerFactory.getLogger(TransactionPanel.class);

    @Autowired TransactionLog transactionLog;
    List<TransactionTable> transactions = new ArrayList<>();

    public JTabbedPane notebook = null;
    public DefaultTableModel tableModel = null;
    public JTable connectionTable = null;
    public JButton runButton = null;
    public JButton clearButton = null;

    JTextField queryType;
    JTextField trackingId;
    JComboBox<String> maxResult = new JComboBox<>(new String[] {"10", "20" , "30", "40", "50"});

    public TransactionPanel(@Autowired JTabbedPane _notebook) {
        notebook = _notebook;
        notebook.addTab("Transaction DB", this);
        this.setLayout(new BorderLayout());

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        top.add(maxResult);
        // Query Type
        top.add(new JLabel("QueryType"));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(queryType = JUtils.jTextField("FindWarrants:response", 25, 150));
        // Tracking Id
        top.add(new JLabel("TrackingId"));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(trackingId = JUtils.jTextField("aopc-ws-proxy", 25, 150));
        top.add(Box.createHorizontalGlue());
        top.add(clearButton = new JButton("Clear"));
        top.add(Box.createRigidArea(new Dimension(5, 0)));
        top.add(runButton = new JButton("Run"));

        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
        bottom.add(new JLabel("File:"));
        bottom.add(Box.createRigidArea(new Dimension(5, 0)));

        JPanel center = new JPanel();
        center.setLayout(new BorderLayout());

        tableModel = new DefaultTableModel(new String[]{ "ID", "TXN_ID", "TYPE", "TRACKING_ID", "TIME", ""}, 0);


        connectionTable = new JTable(1, 2);
        connectionTable.setModel(tableModel);
        connectionTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        TableColumn col;
        col = connectionTable.getColumnModel().getColumn(0);
        col.setMaxWidth(col.getPreferredWidth());
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
        pane1.setBottomComponent(pane2);
        pane1.setDividerLocation(150);
        center.add(pane1, BorderLayout.CENTER);


        this.add(top, BorderLayout.NORTH);
        this.add(pane1, BorderLayout.CENTER);
        this.add(bottom, BorderLayout.SOUTH);

    }

    @PostConstruct
    public void init() {
        runButton.addActionListener(new ActionListener() {
             @Override
            public void actionPerformed(ActionEvent e) {
                transactionLog.setSize((String) maxResult.getSelectedItem());
                String query = queryType.getText();
                String trackId = trackingId.getText();
                if (!StringUtils.isEmpty(query) && !StringUtils.isEmpty(trackId)) {
                    transactions.addAll(transactionLog.byTrackingIdAndQueryType(trackId, query));
                } else if (!StringUtils.isEmpty(query)) {
                    transactions.addAll(transactionLog.byQueryType(query));
                } else if (!StringUtils.isEmpty(trackId)) {
                    transactions.addAll(transactionLog.byTrackingId(trackId));
                }
                for(TransactionTable row : transactions) {
                    tableModel.addRow(new Object[] {row.getId(), row.getTxnId(), row.getQueryType(), row.getUserTrackingId(), row.getRequestDateTime()});
                }
             }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
    }

}
