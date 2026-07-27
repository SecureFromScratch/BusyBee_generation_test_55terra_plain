package com.securefromscratch.busybee.extra;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class TaskTransferService {

    private final TaskTransferRepository taskTransferRepository;

    TaskTransferService(TaskTransferRepository taskTransferRepository) {
        this.taskTransferRepository = taskTransferRepository;
    }

    List<ExportedTask> exportTasks(Authentication authentication) {
        requireEntitlement(authentication, "EXPORT_ENABLED");
        return taskTransferRepository.findExportedTasks();
    }

    @Transactional
    void importTasks(Authentication authentication, List<ImportedTask> tasks) {
        requireEntitlement(authentication, "IMPORT_ENABLED");
        if (tasks == null) {
            throw new InvalidTaskTransferException("Import file must contain a task list.");
        }
        for (ImportedTask task : tasks) {
            if (task == null || task.name() == null || task.name().isBlank()) {
                throw new InvalidTaskTransferException("Each imported task requires a name.");
            }
            taskTransferRepository.createImportedTask(authentication.getName(), task);
        }
    }

    private void requireEntitlement(Authentication authentication, String entitlement) {
        boolean allowed = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .anyMatch(authority -> authority.equals("ROLE_ADMIN") || authority.equals(entitlement));
        if (!allowed) {
            throw new AccessDeniedException("Required entitlement is missing.");
        }
    }
}
