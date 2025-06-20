package com.fu.swp391.schoolhealthmanagementsystem.scheduler;

import com.fu.swp391.schoolhealthmanagementsystem.service.ScheduledTaskCleanupService;
import com.fu.swp391.schoolhealthmanagementsystem.service.ScheduledTaskGenerationService;
import com.fu.swp391.schoolhealthmanagementsystem.service.StudentMedicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicationScheduler {

    private final ScheduledTaskGenerationService taskGenerationService;
    private final ScheduledTaskCleanupService taskCleanupService;
    private final StudentMedicationService studentMedicationService;

    @Scheduled(cron = "0 * * * * ?")
    public void generateDailyMedicationTasks() {
        log.info("Scheduler: Running daily medication task generation job.");
        try {
            taskGenerationService.generateScheduledTasks(null);
        } catch (Exception e) {
            log.error("Scheduler: Error during daily medication task generation job", e);
        }
        log.info("Scheduler: Finished daily medication task generation job.");
    }

    @Scheduled(cron = "0 0 16 * * ?")
    public void cleanupOverdueMedicationTasks() {
        log.info("Scheduler: Running job to cleanup overdue medication tasks.");
        try {
            taskCleanupService.markOverdueTasksAsSkipped();
        } catch (Exception e) {
            log.error("Scheduler: Error during overdue medication tasks cleanup job", e);
        }
        log.info("Scheduler: Finished overdue medication tasks cleanup job.");
    }

    @Scheduled(cron = "0 */3 * * * ?") // Run every 3 minutes
    public void processExpiredMedicationsJob() {
        log.info("Scheduler: Running job to process expired medications.");
        try {
            studentMedicationService.processExpiredMedications();
        } catch (Exception e) {
            log.error("Scheduler: Error during expired medications processing job", e);
        }
        log.info("Scheduler: Finished job to process expired medications.");
    }
}