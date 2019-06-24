package de.thl.intellijinfer.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.ui.components.JBLabel;
import de.thl.intellijinfer.config.GlobalSettings;
import de.thl.intellijinfer.model.InferInstallation;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;

public class SettingsForm {
    private JPanel mainPanel;
    private TextFieldWithBrowseButton pathChooser;
    private JBLabel getInferHereJBLabel;
    private JButton addAndCheckInstallationButton;

    private boolean modified = false;

    public SettingsForm() {
        pathChooser.addActionListener(e -> FileChooser.chooseFile(
                FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                ProjectUtil.guessCurrentProject(mainPanel),
                LocalFileSystem.getInstance().findFileByPath(pathChooser.getText()),
                (dir) -> pathChooser.setText(dir.getPath())));

        pathChooser.getTextField().getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) {modified = true;}
            @Override public void removeUpdate(DocumentEvent e) {modified = true;}
            @Override public void changedUpdate(DocumentEvent e) {modified = true;}
        });

        getInferHereJBLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    Desktop.getDesktop().browse(URI.create("https://fbinfer.com/docs/getting-started.html"));
                } catch (IOException ex) {}
            }
        });
        addAndCheckInstallationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GlobalSettings.getInstance().addInstallation(pathChooser.getText());
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }
    public boolean isModified() {
        return modified;
    }
    public void setModified(boolean modified) {
        this.modified = modified;
    }
    public String getPath() {
        return this.pathChooser.getText();
    }
    public void setPath(String path) {
        this.pathChooser.setText(path);
    }

}