package ru.job4j.parser.model;

import java.util.Objects;

/**
 * Class Vacansy
 *
 * @author Petr B.
 * @since 18.11.2019, 8:41
 */
public class Vacansy {
    private final String name;
    private final String text;
    private final String link;

    public Vacansy(String name, String text, String link) {
        this.name = name;
        this.text = text;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public String getText() {
        return text;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString() {
        return "Vacansy{"
                + "name='" + name
                + '\''
                + ", text='" + text
                + '\''
                + ", link='" + link
                + '\''
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Vacansy vacansy = (Vacansy) o;
        return Objects.equals(name, vacansy.name)
                && Objects.equals(text, vacansy.text)
                && Objects.equals(link, vacansy.link);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, text, link);
    }
}
