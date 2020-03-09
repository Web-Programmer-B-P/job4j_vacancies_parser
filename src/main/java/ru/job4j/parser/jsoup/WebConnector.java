package ru.job4j.parser.jsoup;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.parser.model.Vacansy;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

/**
 * Class WebConnector
 *
 * @author Petr B.
 * @since 18.11.2019, 11:00
 */
public class WebConnector {
    private final static String URL = "https://www.sql.ru/forum/job-offers";
    private final static String PARENT_CLASS_TAG = "postslisttopic";
    private final static String LINK_ATTRIBUTE = "href";
    private final static String ID_TAG_INNER_PAGE = "content-wrapper-forum";
    private final static int ZERO = 0;
    private final static int FIVE = 5;
    private final static int TWO = 2;
    private final static int ONE = 1;
    private Map<String, Vacansy> list = new HashMap<>();
    private long currentDate = getDateBeginerYear().getTime();
    private static final Logger LOG = LogManager.getLogger(WebConnector.class.getName());

    public WebConnector() {

    }

    public WebConnector(long dateForStartSearchVacansy) {
        currentDate = dateForStartSearchVacansy;
    }

    /**
     * Метод который запускает поиск, а так же формирует путь для последующих страниц.
     */
    public void parseVacansy() {
        try {
            Document doc = Jsoup.connect(URL).get();
            Elements findNumbersOfPage = doc.getElementsByAttributeValue("id", ID_TAG_INNER_PAGE);
            int number = findLastNumberOfPages(findNumbersOfPage);
            boolean stop = false;
            for (int page = 1; page < number; page++) {
                if (page == 1) {
                    stop = findNameOfVacansyAndLink(URL);
                } else {
                    stop = findNameOfVacansyAndLink(URL + "/" + page);
                }
                if (!stop) {
                    break;
                }
            }
        } catch (IOException io) {
            LOG.trace(io.getMessage());
        }
    }

    /**
     * Метод ищет колличество страниц по которым нужно пройтись
     *
     * @param parentElements элемент страницы где ищем
     * @return int колличество страниц
     */
    private int findLastNumberOfPages(Elements parentElements) {
        int result = 0;
        for (Element element : parentElements) {
            Element table = element.child(FIVE);
            Element tbody = table.child(ZERO);
            Element tr = tbody.child(ZERO);
            Element td = tr.child(ZERO);
            Elements all = td.getAllElements();
            Element last = all.last();
            result = Integer.parseInt(last.text());
        }
        return result;
    }

    /**
     * Метод коннектится к сайту и начинает обход по критерию
     *
     * @param url путь где искать
     * @return true если хотя бы одна дата попала под отбор иначе false
     */
    private boolean findNameOfVacansyAndLink(String url) {
        boolean result = false;
        try {
            Document doc = Jsoup.connect(url).get();
            Elements allElementsUnderThisClass = doc.getElementsByAttributeValue("class", PARENT_CLASS_TAG);
            for (Element element : allElementsUnderThisClass) {
                Element tdFirst = element.child(ZERO);
                Element tdLast = tdFirst.parent().parent().child(FIVE);
                String date = getStringOfDate(tdLast);
                Date dateTimestamp = getDateFromString(date).getTime();
                if (dateTimestamp.getTime() > currentDate) {
                    result = true;
                    String name = tdFirst.text();
                    String link = tdFirst.attr(LINK_ATTRIBUTE);
                    if (link.contains("java") && !(link.contains("javascript") || link.contains("javascrip"))) {
                        String text = null;
                        text = findDescriptionByLink(link);
                        list.put(name, new Vacansy(name, text, link));
                    }
                }
            }
        } catch (IOException io) {
            LOG.trace(io.getMessage());
        }
        return result;
    }

    /**
     * Метод генерирует дату с начала года, которая используется по умолчанию
     *
     * @return Date
     */
    private Date getDateBeginerYear() {
        Date result = null;
        int year = LocalDate.now().getYear();
        GregorianCalendar calendar = new GregorianCalendar(year, 1, 1);
        result = calendar.getTime();
        return result;
    }

    /**
     * Метод возвращает дату вакансии в текстовом формате
     *
     * @param element
     * @return строка дата
     */
    private String getStringOfDate(Element element) {
        String result = null;
        result = element.text();
        return result;
    }

    /**
     * Метод преобразует строковую дату, с нестандартного формата, в тип Date по заданному шаблону
     *
     * @param stringDate строка дата
     * @return готовая дата
     */
    private Date parseDate(String stringDate) {
        Locale locale = new Locale("ru");
        DateFormatSymbols dateFormatSymbols = DateFormatSymbols.getInstance(locale);
        String[] shortNameMonths = {
                "янв", "фев", "мар", "апр", "май", "июн",
                "июл", "авг", "сен", "окт", "ноя", "дек"};
        dateFormatSymbols.setShortMonths(shortNameMonths);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yy, HH:mm", locale);
        simpleDateFormat.setDateFormatSymbols(dateFormatSymbols);
        Date date = null;
        try {
            date = simpleDateFormat.parse(stringDate);
        } catch (ParseException pe) {
            LOG.trace(pe.getMessage());
        }
        return date;
    }

    /**
     * Основной метод по обработке строковой даты, также проверяет строковую дату на такие вхождения как "сегодгя", "вчера";
     *
     * @param stringDate строковая дата
     * @return Calendar
     */
    private Calendar getDateFromString(String stringDate) {
        Calendar calendar = Calendar.getInstance();
        if (stringDate.contains("вчера")) {
            calendar.setTime(parseDay(stringDate).getTime());
            calendar.add(Calendar.DATE, -1);
        } else if (stringDate.contains("сегодня")) {
            calendar.setTime(parseDay(stringDate).getTime());
        } else {
            calendar.setTime(parseDate(stringDate));
        }
        return calendar;
    }

    /**
     * Метод принимает на вход строку типа "вчера, 9:35" или "сегодня, 9:35" и формирует дату
     *
     * @param day строка типа "вчера, 9:35"
     * @return Calendar дата
     */
    private Calendar parseDay(String day) {
        Locale locale = new Locale("ru");
        DateFormatSymbols dateFormatSymbols = DateFormatSymbols.getInstance(locale);
        String[] days = {"", "вчера", "сегодня"};
        dateFormatSymbols.setWeekdays(days);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE, HH:mm", locale);
        simpleDateFormat.setDateFormatSymbols(dateFormatSymbols);
        Date date = null;
        try {
            date = simpleDateFormat.parse(day);
        } catch (ParseException pe) {
            LOG.trace(pe.getMessage());
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int min = cal.get(Calendar.MINUTE);
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        return cal;
    }

    /**
     * Метод заходит на внутреннию страницу вакансии и возвращает описание вакансии.
     *
     * @param link ссылка внутренней строницы
     * @return String описание вакансии
     */
    private String findDescriptionByLink(String link) {
        String result = null;
        try {
            Document findTextVacansy = Jsoup.connect(link).get();
            Element parent = findTextVacansy.getElementById(ID_TAG_INNER_PAGE);
            Element table = parent.child(TWO);
            if (table.childNodeSize() > ZERO) {
                Element tbody = table.child(ZERO);
                Element tr = tbody.child(ONE);
                result = tr.child(ONE).text();
            }
        } catch (IOException e) {
            LOG.trace(e.getMessage());
        }
        return result;
    }

    /**
     * Метод возвращает список найденых вакансий, для дальнейшей обработки
     *
     * @return Set<Vacansy> list
     */
    public Map<String, Vacansy> getList() {
        return list;
    }
}
