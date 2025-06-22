package com.openbravo.pos.manuals;

import com.openbravo.basic.BasicException;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.JPanelView;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * Panel that lists bundled PDF manuals and renders them for viewing.
 */
public class JPanelManualViewer extends JPanel implements JPanelView {

    private static final Logger LOGGER = Logger.getLogger(JPanelManualViewer.class.getName());
    private final JList<String> fileList = new JList<>();
    private final JPanel pagePanel = new JPanel();
    private final Map<String, String> fileMap = new LinkedHashMap<>();

    public JPanelManualViewer() {
        setLayout(new BorderLayout());
        pagePanel.setLayout(new BoxLayout(pagePanel, BoxLayout.Y_AXIS));
        JScrollPane listScroll = new JScrollPane(fileList);
        JScrollPane pageScroll = new JScrollPane(pagePanel);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, pageScroll);
        split.setDividerLocation(200);
        add(split, BorderLayout.CENTER);
        loadManualList();
        fileList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String key = fileList.getSelectedValue();
                    if (key != null) {
                        showManual(fileMap.get(key));
                    }
                }
            }
        });
    }

    private void loadManualList() {
        try (InputStream in = getClass().getResourceAsStream("/com/openbravo/manuals/index.txt")) {
            if (in == null) {
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            DefaultListModel<String> model = new DefaultListModel<>();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int idx = line.indexOf('=');
                if (idx > 0) {
                    String file = line.substring(0, idx).trim();
                    String name = line.substring(idx + 1).trim();
                    fileMap.put(name, file);
                    model.addElement(name);
                }
            }
            fileList.setModel(model);
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    private void showManual(String file) {
        pagePanel.removeAll();
        if (file == null) {
            return;
        }
        try (InputStream in = getClass().getResourceAsStream("/com/openbravo/manuals/" + file)) {
            if (in == null) {
                return;
            }
            PDDocument doc = PDDocument.load(in);
            PDFRenderer renderer = new PDFRenderer(doc);
            for (int i = 0; i < doc.getNumberOfPages(); i++) {
                BufferedImage img = renderer.renderImageWithDPI(i, 150);
                JLabel lbl = new JLabel(new ImageIcon(img));
                lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
                pagePanel.add(lbl);
            }
            doc.close();
            pagePanel.revalidate();
            pagePanel.repaint();
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, null, ex);
        }
    }

    @Override
    public String getTitle() {
        return AppLocal.getIntString("Menu.UserManuals");
    }

    @Override
    public void activate() throws BasicException {
    }

    @Override
    public boolean deactivate() {
        return true;
    }

    @Override
    public JComponent getComponent() {
        return this;
    }
}
