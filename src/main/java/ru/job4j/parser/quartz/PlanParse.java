package ru.job4j.parser.quartz;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import ru.job4j.parser.db.DbParser;
import ru.job4j.parser.jsoup.WebConnector;

/**
 * Class PlanParse
 *
 * @author Petr B.
 * @since 20.11.2019, 19:28
 */
public class PlanParse implements Job {
    private WebConnector parser;
    private static final Logger LOG = LogManager.getLogger(PlanParse.class.getName());

    public PlanParse() {

    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        LOG.info("Время старта планировщика выставлено на " + jobExecutionContext.getNextFireTime());
        if (jobExecutionContext.getPreviousFireTime() == null) {
            LOG.info("Выполняю первый запуск, ищу все записи с начала года");
            parser = new WebConnector();
            parser.parseVacansy();
            new DbParser(parser.getList()).init();
        }

        if (jobExecutionContext.getPreviousFireTime() != null) {
            LOG.info("Выполняю поиск всех вакинсий которые свежее последнего запуска планировщика");
            parser = new WebConnector(jobExecutionContext.getPreviousFireTime().getTime());
            parser.parseVacansy();
            new DbParser(parser.getList()).init();
        }
    }
}
