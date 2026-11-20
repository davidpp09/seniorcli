package com.postmanlite.ui;

import com.postmanlite.model.RequestSpec;
import com.postmanlite.model.ResponseResult;
import com.postmanlite.service.JsonPretty;
import com.postmanlite.service.RequestExecutor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Ventana principal: barra de request arriba, pestanas de request y response, historial a la izquierda. */
public class MainFrame extends JFrame {

    private static final String[] METHODS = {"GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS"};

    private final RequestExecutor executor = new RequestExecutor();

    private final JComboBox<String> methodBox = new JComboBox<>(METHODS);
    private final JTextField urlField = new JTextField("https://httpbin.org/get");
    private final JButton sendButton = new JButton("Send");

    private final DefaultTableModel headersModel = new DefaultTableModel(new String[]{"Key", "Value"}, 0);
    private final JTextArea bodyArea = new JTextArea();

    private final JLabel statusLabel = new JLabel("Sin respuesta todavia");
    private final JTextArea responseBody = new JTextArea();
    private final JTextArea responseHeaders = new JTextArea();

    private final DefaultListModel<RequestSpec> historyModel = new DefaultListModel<>();
    private final JList<RequestSpec> historyList = new JList<>(historyModel);

    public MainFrame() {
        super("PostmanLite");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);

        headersModel.addRow(new Object[]{"Accept", "application/json"});
        headersModel.addRow(new Object[]{"", ""});

        JSplitPane center = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildRequestPanel(), buildResponsePanel());
        center.setResizeWeight(0.4);

        JSplitPane root = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildHistoryPanel(), center);
        root.setDividerLocation(220);

        add(buildTopBar(), BorderLayout.NORTH);
        add(root, BorderLayout.CENTER);
        getRootPane().setDefaultButton(sendButton);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(6, 0));
        bar.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        bar.add(methodBox, BorderLayout.WEST);
        bar.add(urlField, BorderLayout.CENTER);
        bar.add(sendButton, BorderLayout.EAST);
        sendButton.addActionListener(e -> send());
        return bar;
    }

    private JComponent buildHistoryPanel() {
        historyList.setCellRenderer((list, value, index, selected, focus) -> {
            JLabel l = new JLabel(value.method() + "  " + value.url());
            l.setOpaque(true);
            l.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
            l.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
            l.setForeground(selected ? list.getSelectionForeground() : list.getForeground());
            return l;
        });
        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && historyList.getSelectedValue() != null) {
                load(historyList.getSelectedValue());
            }
        });
        JScrollPane scroll = new JScrollPane(historyList);
        scroll.setBorder(BorderFactory.createTitledBorder("Historial"));
        return scroll;
    }

    private JComponent buildRequestPanel() {
        JTable table = new JTable(headersModel);
        // Siempre hay una fila vacia al final para seguir agregando headers.
        headersModel.addTableModelListener(e -> ensureTrailingEmptyRow());

        JButton remove = new JButton("Quitar fila");
        remove.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0 && headersModel.getRowCount() > 1) {
                if (table.isEditing()) table.getCellEditor().stopCellEditing();
                headersModel.removeRow(row);
            }
        });
        JPanel headers = new JPanel(new BorderLayout());
        headers.add(new JScrollPane(table), BorderLayout.CENTER);
        headers.add(remove, BorderLayout.SOUTH);

        bodyArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Headers", headers);
        tabs.addTab("Body", new JScrollPane(bodyArea));
        return tabs;
    }

    private JComponent buildResponsePanel() {
        responseBody.setEditable(false);
        responseBody.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        responseHeaders.setEditable(false);
        responseHeaders.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Body", new JScrollPane(responseBody));
        tabs.addTab("Headers", new JScrollPane(responseHeaders));

        statusLabel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(statusLabel, BorderLayout.NORTH);
        panel.add(tabs, BorderLayout.CENTER);
        return panel;
    }

    private void ensureTrailingEmptyRow() {
        int last = headersModel.getRowCount() - 1;
        if (last < 0 || !isBlank(headersModel.getValueAt(last, 0)) || !isBlank(headersModel.getValueAt(last, 1))) {
            SwingUtilities.invokeLater(() -> headersModel.addRow(new Object[]{"", ""}));
        }
    }

    private static boolean isBlank(Object o) {
        return o == null || o.toString().isBlank();
    }

    private RequestSpec currentSpec() {
        Map<String, String> headers = new LinkedHashMap<>();
        for (int i = 0; i < headersModel.getRowCount(); i++) {
            Object k = headersModel.getValueAt(i, 0);
            Object v = headersModel.getValueAt(i, 1);
            if (!isBlank(k)) headers.put(k.toString().trim(), v == null ? "" : v.toString().trim());
        }
        return new RequestSpec((String) methodBox.getSelectedItem(), urlField.getText(), headers, bodyArea.getText());
    }

    private void load(RequestSpec spec) {
        methodBox.setSelectedItem(spec.method());
        urlField.setText(spec.url());
        bodyArea.setText(spec.body());
        headersModel.setRowCount(0);
        spec.headers().forEach((k, v) -> headersModel.addRow(new Object[]{k, v}));
        headersModel.addRow(new Object[]{"", ""});
    }

    private void send() {
        RequestSpec spec = currentSpec();
        historyModel.add(0, spec);

        sendButton.setEnabled(false);
        statusLabel.setForeground(UIManager.getColor("Label.foreground"));
        statusLabel.setText("Enviando...");

        // La llamada de red va fuera del hilo de la UI para que la ventana no se congele.
        new SwingWorker<ResponseResult, Void>() {
            @Override protected ResponseResult doInBackground() { return executor.execute(spec); }

            @Override protected void done() {
                try {
                    show(get());
                } catch (Exception e) {
                    show(ResponseResult.failure(String.valueOf(e), 0));
                } finally {
                    sendButton.setEnabled(true);
                }
            }
        }.execute();
    }

    private void show(ResponseResult r) {
        if (r.isError()) {
            statusLabel.setForeground(new Color(0xC62828));
            statusLabel.setText("Error: " + r.error());
            responseBody.setText("");
            responseHeaders.setText("");
            return;
        }
        Color color = r.status() < 300 ? new Color(0x2E7D32) : r.status() < 400 ? new Color(0xEF6C00) : new Color(0xC62828);
        statusLabel.setForeground(color);
        statusLabel.setText("Status: " + r.status() + "    Tiempo: " + r.elapsedMillis() + " ms    Tamano: " + r.sizeBytes() + " B");

        responseBody.setText(JsonPretty.format(r.body()));
        responseBody.setCaretPosition(0);

        StringBuilder sb = new StringBuilder();
        r.headers().entrySet().stream()
                .sorted(Map.Entry.comparingByKey(String.CASE_INSENSITIVE_ORDER))
                .forEach(e -> sb.append(e.getKey()).append(": ").append(String.join(", ", (List<String>) e.getValue())).append('\n'));
        responseHeaders.setText(sb.toString());
        responseHeaders.setCaretPosition(0);
    }
}
