package com.sushishop.shared.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionCallbacksTest {

    private final List<String> calls = new ArrayList<>();

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void shouldRunImmediatelyWithoutTransaction() {
        TransactionCallbacks.afterCommit(() -> calls.add("commit"));
        TransactionCallbacks.afterRollback(() -> calls.add("rollback"));

        assertThat(calls).containsExactly("commit");
    }

    @Test
    void shouldRunCommitCallbackOnlyAfterCommit() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionCallbacks.afterCommit(() -> calls.add("commit"));
        TransactionCallbacks.afterRollback(() -> calls.add("rollback"));
        assertThat(calls).isEmpty();

        TransactionSynchronizationManager.getSynchronizations().forEach(sync -> {
            sync.afterCommit();
            sync.afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        });

        assertThat(calls).containsExactly("commit");
    }

    @Test
    void shouldRunRollbackCallbackOnlyAfterRollback() {
        TransactionSynchronizationManager.initSynchronization();
        TransactionCallbacks.afterCommit(() -> calls.add("commit"));
        TransactionCallbacks.afterRollback(() -> calls.add("rollback"));

        TransactionSynchronizationManager.getSynchronizations()
                .forEach(sync -> sync.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        assertThat(calls).containsExactly("rollback");
    }
}
