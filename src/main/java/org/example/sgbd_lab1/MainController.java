package org.example.sgbd_lab1;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.example.sgbd_lab1.DAO.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainController {

    // --- Dirty Read ---
    @FXML private Label drOriginal, drDirtyRead, drFinal, drOccurred, drRestored;
    @FXML private TextArea drLog;

    // --- Unrepeatable Read ---
    @FXML private Label urFirst, urSecond, urOccurred;
    @FXML private TextArea urLog;

    // --- Phantom Read ---
    @FXML private Label prFirst, prSecond, prOccurred;
    @FXML private TextArea prLog;

    // --- Lost Update ---
    @FXML private Label luInitial, luT1, luT2, luExpected, luActual, luOccurred;
    @FXML private TextArea luLog;

    // --- Deadlock ---
    @FXML private Label dlOccurred, dlVictim;
    @FXML private TextArea dlLog;

    // --- Batch Performance ---
    @FXML private Label bpAutoCommit, bpBatchCommit, bpSingleBatch, bpFastest;
    @FXML private TextArea bpLog;

    private final Transactions transactions = new Transactions();

    @FXML
    private void runDirtyRead() {
        drLog.clear();
        redirectOutput(drLog);
        new Thread(() -> {
            try {
                TransactionDTO result = transactions.dirtyRead();
                Platform.runLater(() -> {
                    drOriginal.setText(result.getOriginalValue());
                    drDirtyRead.setText(result.getDirtyReadValue());
                    drFinal.setText(result.getFinalValue());
                    drOccurred.setText(String.valueOf(result.dirtyReadOccurred()));
                    drRestored.setText(String.valueOf(result.wasRestoredCorrectly()));
                });
            } catch (Exception e) {
                Platform.runLater(() -> drLog.appendText("\nERROR: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void runUnrepeatableRead() {
        urLog.clear();
        redirectOutput(urLog);
        new Thread(() -> {
            try {
                UnrepeatableReadDTO result = transactions.unrepeatableRead();
                Platform.runLater(() -> {
                    urFirst.setText(result.getFirstReadValue());
                    urSecond.setText(result.getSecondReadValue());
                    urOccurred.setText(String.valueOf(result.unrepeatableReadOccurred()));
                });
            } catch (Exception e) {
                Platform.runLater(() -> urLog.appendText("\nERROR: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void runPhantomRead() {
        prLog.clear();
        redirectOutput(prLog);
        new Thread(() -> {
            try {
                PhantomReadDTO result = transactions.phantomRead();
                Platform.runLater(() -> {
                    prFirst.setText(String.valueOf(result.getFirstCount()));
                    prSecond.setText(String.valueOf(result.getSecondCount()));
                    prOccurred.setText(String.valueOf(result.phantomReadOccurred()));
                });
            } catch (Exception e) {
                Platform.runLater(() -> prLog.appendText("\nERROR: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void runLostUpdate() {
        luLog.clear();
        redirectOutput(luLog);
        new Thread(() -> {
            try {
                LostUpdateDTO result = transactions.lostUpdate();
                Platform.runLater(() -> populateLostUpdate(result));
            } catch (Exception e) {
                Platform.runLater(() -> luLog.appendText("\nERROR: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void runLostUpdatePessimistic() {
        luLog.clear();
        redirectOutput(luLog);
        new Thread(() -> {
            try {
                LostUpdateDTO result = transactions.lostUpdateWithPessimisticLock();
                Platform.runLater(() -> populateLostUpdate(result));
            } catch (Exception e) {
                Platform.runLater(() -> luLog.appendText("\nERROR: " + e.getMessage()));
            }
        }).start();
    }

    private void populateLostUpdate(LostUpdateDTO result) {
        luInitial.setText(String.valueOf(result.getInitialValue()));
        luT1.setText("+" + result.getT1Addition());
        luT2.setText("+" + result.getT2Addition());
        luExpected.setText(String.valueOf(result.getExpectedValue()));
        luActual.setText(String.valueOf(result.getActualValue()));
        luOccurred.setText(String.valueOf(result.lostUpdateOccurred()));
    }

    @FXML
    private void runDeadlock() {
        dlLog.clear();
        redirectOutput(dlLog);
        new Thread(() -> {
            try {
                DeadlockDTO result = transactions.deadlock();
                Platform.runLater(() -> {
                    dlOccurred.setText(String.valueOf(result.isDeadlockOccurred()));
                    dlVictim.setText(result.getDeadlockVictim());
                });
            } catch (Exception e) {
                Platform.runLater(() -> dlLog.appendText("\nERROR: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void runBatchPerformance() {
        bpLog.clear();
        redirectOutput(bpLog);
        new Thread(() -> {
            try {
                BatchPerformanceDTO result = transactions.batchPerformance();
                Platform.runLater(() -> {
                    bpAutoCommit.setText(result.getAutoCommitMs() + "ms");
                    bpBatchCommit.setText(result.getBatchCommitMs() + "ms");
                    bpSingleBatch.setText(result.getSingleBatchMs() + "ms");
                    bpFastest.setText(result.getFastest());
                });
            } catch (Exception e) {
                Platform.runLater(() -> bpLog.appendText("\nERROR: " + e.getMessage()));
            }
        }).start();
    }

    // Redirects System.out to the given TextArea for the duration of the run
    private void redirectOutput(TextArea target) {
        OutputStream os = new OutputStream() {
            private final ByteArrayOutputStream buf = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                buf.write(b);
                if (b == '\n') {
                    String line = buf.toString(StandardCharsets.UTF_8);
                    buf.reset();
                    Platform.runLater(() -> target.appendText(line));
                }
            }
        };
        System.setOut(new PrintStream(os, true, StandardCharsets.UTF_8));
    }
}