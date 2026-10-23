package com.example.cloud.care.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs the follow-up reminder sweep hourly, and once shortly after startup so
 * a local demo does not have to wait for the top of the hour to see a reminder.
 *
 * <p>The service call is on a separate bean on purpose: calling a
 * {@code @Transactional} method from inside the same bean bypasses Spring's
 * proxy, so the reminder reads would run without a transaction.
 */
@Component
public class PrescriptionReminderJob {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionReminderJob.class);

    private final PrescriptionService prescriptionService;

    public PrescriptionReminderJob(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @Scheduled(cron = "${cloudcare.prescription-reminder-cron:0 0 * * * *}")
    public void scheduledSweep() {
        runSweep("scheduled");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        runSweep("startup");
    }

    private void runSweep(String trigger) {
        try {
            int sent = prescriptionService.sendDueReminders();
            if (sent > 0) {
                log.info("Prescription reminder sweep ({}): {} notification(s).", trigger, sent);
            }
        } catch (Exception e) {
            // A reminder failure must never stop the application or the
            // scheduled executor. The next sweep retries.
            log.warn("Prescription reminder sweep ({}) failed: {}", trigger, e.getMessage());
        }
    }
}
