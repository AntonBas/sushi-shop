package com.sushishop.shared.service;

import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;

@Service
public class SlugService {

    private static final int MAX_SLUG_LENGTH = 100;
    private static final String FALLBACK_SLUG = "item";

    private static final Map<Character, String> UKRAINIAN = Map.ofEntries(
            Map.entry('а', "a"), Map.entry('б', "b"), Map.entry('в', "v"), Map.entry('г', "h"),
            Map.entry('ґ', "g"), Map.entry('д', "d"), Map.entry('е', "e"), Map.entry('є', "ie"),
            Map.entry('ж', "zh"), Map.entry('з', "z"), Map.entry('и', "y"), Map.entry('і', "i"),
            Map.entry('ї', "i"), Map.entry('й', "i"), Map.entry('к', "k"), Map.entry('л', "l"),
            Map.entry('м', "m"), Map.entry('н', "n"), Map.entry('о', "o"), Map.entry('п', "p"),
            Map.entry('р', "r"), Map.entry('с', "s"), Map.entry('т', "t"), Map.entry('у', "u"),
            Map.entry('ф', "f"), Map.entry('х', "kh"), Map.entry('ц', "ts"), Map.entry('ч', "ch"),
            Map.entry('ш', "sh"), Map.entry('щ', "shch"), Map.entry('ь', ""), Map.entry('ю', "iu"),
            Map.entry('я', "ia"), Map.entry('\'', ""), Map.entry('’', ""), Map.entry('ʼ', "")
    );

    private static final Map<Character, String> UKRAINIAN_WORD_START = Map.of(
            'є', "ye", 'ї', "yi", 'й', "y", 'ю', "yu", 'я', "ya"
    );

    public String generateSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String slug = Normalizer.normalize(transliterate(input.toLowerCase(Locale.ROOT)), Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("[\\s-]+", "-");

        if (slug.length() > MAX_SLUG_LENGTH) {
            slug = slug.substring(0, MAX_SLUG_LENGTH).replaceAll("-+$", "");
        }

        return slug.isEmpty() ? FALLBACK_SLUG : slug;
    }

    public String generateUniqueSlug(String input, Predicate<String> slugExists) {
        var baseSlug = generateSlug(input);
        var uniqueSlug = baseSlug;
        int counter = 1;

        while (slugExists.test(uniqueSlug)) {
            uniqueSlug = baseSlug + "-" + counter;
            counter++;
        }
        return uniqueSlug;
    }

    private String transliterate(String input) {
        var result = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char current = input.charAt(i);
            boolean wordStart = i == 0 || !Character.isLetter(input.charAt(i - 1));
            String replacement = wordStart ? UKRAINIAN_WORD_START.get(current) : null;
            if (replacement == null) {
                replacement = UKRAINIAN.get(current);
            }
            result.append(replacement != null ? replacement : String.valueOf(current));
        }
        return result.toString();
    }
}
