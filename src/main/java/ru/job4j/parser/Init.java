package ru.job4j.parser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import ru.job4j.parser.property.Property;
import ru.job4j.parser.quartz.PlanParse;

import java.time.LocalDate;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.*;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Class Init
 *
 * @author Petr B.
 * @since 20.11.2019, 19:29
 */
public class Init {
    private static final Logger LOG = LogManager.getLogger(Init.class.getName());

    public static void main(String[] args) {
        LOG.info("Время старта приложения " + LocalDate.now());
        Property prop = new Property();
        prop.init();
        try {
            SchedulerFactory schedFact = new StdSchedulerFactory();
            Scheduler sched = schedFact.getScheduler();
            sched.start();
            JobDetail job = newJob(PlanParse.class)
                    .withIdentity("myJob", "group1")
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity("myTrigger", "group1")
                    .startNow()
                    .withSchedule(cronSchedule(prop.getPropertyCronTime()))
                    .forJob("myJob", "group1")
                    .build();
            sched.scheduleJob(job, trigger);
        } catch (IllegalArgumentException | SchedulerException s) {
            LOG.trace(s.getMessage());
        }
    }
}
