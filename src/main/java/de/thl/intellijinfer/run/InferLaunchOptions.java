package de.thl.intellijinfer.run;

import com.intellij.AppTopics;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.util.messages.MessageBus;
import com.intellij.util.messages.MessageBusConnection;
import de.thl.intellijinfer.config.GlobalSettings;
import de.thl.intellijinfer.model.BuildTool;
import de.thl.intellijinfer.model.Checker;
import de.thl.intellijinfer.model.InferInstallation;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InferLaunchOptions {
    private static final Logger log = Logger.getInstance(InferLaunchOptions.class);

    //Tracks changed files for use with the reactive mode
    private FileDocumentManagerListener changeListener;
    public List<String> changedFiles = new ArrayList<>();


    private Boolean fullAnalysis; //if a full analysis was already done after loading the current project/creating the run config

    //Saved Infer Configuration Options
    private InferInstallation selectedInstallation;
    private RunConfiguration usingRunConfig;
    private String additionalArgs;
    private List<Checker> selectedCheckers;
    private Boolean reactiveMode;


    InferLaunchOptions() {
        this.selectedInstallation = GlobalSettings.getInstance().getDefaultInstallation();
        this.additionalArgs = "";
        this.selectedCheckers = Checker.getDefaultCheckers();
        this.reactiveMode = false;
        this.fullAnalysis = false;
        createChangeFileListener();
    }

    /**
     * Constructs the final infer launch command
     * @return Infer Launch Command
     */
    @NotNull
    public String buildInferLaunchCmd() throws ExecutionException {
        if(this.usingRunConfig == null) throw new ExecutionException("Infer Execution failed: No Run Configuration selected");
        if(this.selectedInstallation == null) throw new ExecutionException("Infer Execution failed: No Installation selected");
        if(this.selectedCheckers == null || this.selectedCheckers.isEmpty()) throw new ExecutionException("Infer Execution failed: No Checkers selected");

        StringBuilder sb = new StringBuilder(this.selectedInstallation.getPath());

        //Additional Arguments
        sb.append(" run ").append(additionalArgs).append(" ");

        //Checkers
        for(Checker checker : selectedCheckers) {
            sb.append(checker.getActivationArgument()).append(" ");
        }
        for(Checker checker : Checker.getMissingCheckers(selectedCheckers)) {
            sb.append(checker.getDeactivationArgument()).append(" ");
        }

        //Reactive Mode
        if(this.reactiveMode && this.fullAnalysis) {
            if(!changedFiles.isEmpty()) {
                try {
                    final Path file = Paths.get(this.usingRunConfig.getProject().getBasePath() + "/changedfiles.txt");
                    Files.write(file, changedFiles, StandardCharsets.UTF_8);
                    sb.append("--reactive --changed-files-index changedfiles.txt ");
                } catch (IOException ioe) {
                    log.error(ioe);
                }
            }

        }
        this.fullAnalysis = true;
        changedFiles.clear();

        //Build Tool
        sb.append(BuildTool.getBuildCmd(this.usingRunConfig));

        return sb.toString();
    }

    /**
     * Creates a Listener (if one doesnt already exists), which collects all changed files, which are of a compilable type
     * @see BuildTool#FILE_EXTENSIONS
     */
    private void createChangeFileListener() {
        if(changeListener != null) return;
        changeListener = new FileDocumentManagerListener() {
            public void beforeDocumentSaving(@NotNull Document document) {
                Pattern r = Pattern.compile("(?<=DocumentImpl\\[file://).*?(?=\\])"); //Matches everything between "DocumentImpl[file://" and "]"
                Matcher m = r.matcher(document.toString());

                if (m.find()) {
                    if(m.group(0) != null && BuildTool.FILE_EXTENSIONS.stream().anyMatch((ext) -> m.group(0).endsWith(ext))) {
                        changedFiles.add(m.group(0));
                    }
                }
            }
        };
        MessageBus bus = ApplicationManager.getApplication().getMessageBus();
        MessageBusConnection connection = bus.connect();
        connection.subscribe(AppTopics.FILE_DOCUMENT_SYNC, changeListener);
    }

    public RunConfiguration getSelectedRunConfig() {
        return usingRunConfig;
    }
    public void setSelectedRunConfig(RunConfiguration rc) {
        this.usingRunConfig = rc;
    }
    public String getAdditionalArgs() {
        return additionalArgs;
    }
    public void setAdditionalArgs(String additionalArgs) {
        this.additionalArgs = additionalArgs;
    }
    public List<Checker> getSelectedCheckers() {
        return selectedCheckers;
    }
    public void setSelectedCheckers(List<Checker> selectedCheckers) {
        this.selectedCheckers = selectedCheckers;
    }
    public Boolean isReactiveMode() {
        return reactiveMode;
    }
    public void setReactiveMode(Boolean reactiveMode) {
        this.reactiveMode = reactiveMode;
    }
    public InferInstallation getSelectedInstallation() {
        return selectedInstallation;
    }
    public void setSelectedInstallation(InferInstallation selectedInstallation) {
        this.selectedInstallation = selectedInstallation;
    }
}
