// File: SearchFilter.java
package com.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * A class that provides search filtering functionality for items with date and text content.
 * @param <T> The type of item to filter
 */
public class SearchFilter<T> {

    /**
     * Interface for retrieving date information from an item.
     */
    public interface DateProvider<T> {
        int getYear(T item);
        int getMonth(T item);
    }

    /**
     * Interface for retrieving text content from an item.
     */
    public interface TextProvider<T> {
        String getText(T item);
    }

    private final DateProvider<T> dateProvider;
    private final TextProvider<T> textProvider;

    /**
     * Constructor for SearchFilter.
     *
     * @param dateProvider Provider for extracting date information from items
     * @param textProvider Provider for extracting text content from items
     */
    public SearchFilter(DateProvider<T> dateProvider, TextProvider<T> textProvider) {
        this.dateProvider = dateProvider;
        this.textProvider = textProvider;
    }

    /**
     * Filter a list of items based on specified criteria.
     *
     * @param items      The items to filter
     * @param searchText The text to search for (can be letters, phrases, or email addresses)
     * @param year       The year to filter by (null to ignore)
     * @param month      The month to filter by (null to ignore)
     * @param matchCase  Whether to perform case-sensitive text matching
     * @return A filtered list of items
     */
    public List<T> filter(List<T> items,
                          String searchText,
                          Integer year,
                          Integer month,
                          boolean matchCase) {
        List<T> result = new ArrayList<>();

        // Build the filter predicate
        Predicate<T> predicate = buildFilterPredicate(searchText, year, month, matchCase);

        // Apply the filter
        for (T item : items) {
            if (predicate.test(item)) {
                result.add(item);
            }
        }

        return result;
    }

    /**
     * Build a predicate for filtering items by date and text.
     */
    private Predicate<T> buildFilterPredicate(String searchText,
                                              Integer year,
                                              Integer month,
                                              boolean matchCase) {
        Predicate<T> predicate = item -> true;

        // Year filter
        if (year != null) {
        	//si year != null, on ne garde que les item dont l’année vaut year.
            predicate = predicate.and(item -> dateProvider.getYear(item) == year);
        }

        // Month filter
        if (month != null) {
        	//si month != null, on ne garde que les item dont le mois vaut month.
            predicate = predicate.and(item -> dateProvider.getMonth(item) == month);
        }

        // Text filter
        if (searchText != null && !searchText.isEmpty()) {
            predicate = predicate.and(buildTextPredicate(searchText, matchCase));
        }

        return predicate;
    }

    /**
     * Build a predicate for text filtering, handling case, phrases, and emails.
     */
    private Predicate<T> buildTextPredicate(String searchText, boolean matchCase) {
        // Precompute a final query string
        final String query = matchCase ? searchText : searchText.toLowerCase();
        // si matchCase=true, on garde la casse d’origine
         // si matchCase=false, on passe tout en minuscules

        // Email regex
        Pattern emailPattern = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
        );

        return item -> {
            String rawText = textProvider.getText(item);
            if (rawText == null) return false;

            // Apply casing rule
            String text = matchCase ? rawText : rawText.toLowerCase();

            // If the query looks like an email, do a plain contains
            if (emailPattern.matcher(query).matches()) {
                return text.contains(query);
            }

            // If it's a phrase (contains space)
            if (query.contains(" ")) {
                return text.contains(query);
            }

            // Otherwise check per-word
            for (String word : text.split("\\s+")) {
                if (word.contains(query)) {
                    return true;
                }
            }
            return false;
        };
    }
}
