package site.easy.to.build.crm.cron;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import site.easy.to.build.crm.entity.Contract;

import java.time.LocalDate;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class ContractExpirationChecker {

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    @Scheduled(cron = "0 0 0 * * *")
    public void scheduleContractExpirationCheck(Contract contract) {
        Runnable checkExpirationTask = () -> {
            LocalDate currentDate = LocalDate.now();
            LocalDate endDate = LocalDate.parse(contract.getEndDate());
            if (currentDate.isAfter(endDate)) {
                // Contract has expired
                contract.setStatus("expired");

                System.out.println("Contract expired: " + contract.getContractId());
            } else {
                // Contract is still valid
                System.out.println("Contract is still valid: " + contract.getContractId());
            }
        };

        // Schedule the task to run periodically
        executorService.scheduleAtFixedRate(checkExpirationTask, 0, 1, TimeUnit.DAYS);
    }

    public void stopContractExpirationCheck() {
        executorService.shutdown();
    }
}
