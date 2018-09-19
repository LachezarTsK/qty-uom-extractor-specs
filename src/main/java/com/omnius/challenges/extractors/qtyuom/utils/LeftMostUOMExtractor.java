package com.omnius.challenges.extractors.qtyuom.utils;

import com.omnius.challenges.extractors.qtyuom.QtyUomExtractor;
import com.omnius.challenges.extractors.qtyuom.utils.Pair;

/**
 * Implements {@link QtyUomExtractor} identifying as <strong>the most relevant UOM</strong> the leftmost UOM found in the articleDescription.
 * The {@link UOM} array contains the list of valid UOMs. The algorithm search for the leftmost occurence of UOM[i], if there are no occurrences then tries UOM[i+1].
 * 
 * Example
 * <ul>
 * <li>article description: "black steel bar 35 mm 77 stck"</li>
 * <li>QTY: "77" (and NOT "35")</li>
 * <li>UOM: "stck" (and not "mm" since "stck" has an higher priority as UOM )</li>
 * </ul>
 *
 * @author <a href="mailto:damiano@searchink.com">Damiano Giampaoli</a>
 * @since 4 May 2018
 */
public class LeftMostUOMExtractor implements QtyUomExtractor {

    /**
     * Array of valid UOM to match. The elements with lower index in the array have higher priority
     */
    public static String[] UOM = {"stk", "stk.", "stck", "stück", "stg", "stg.", "st", "st.", "stange", "stange(n)", "tafel", "tfl", "taf", "mtr", "meter", "qm", "kg", "lfm", "mm", "m"};
    
    public LeftMostUOMExtractor() {}
    
   	public Pair<String, String> extract(String articleDescription) {
		Pair<String, String> results = null;

		if (articleDescription == null || articleDescription.trim().isEmpty()) {
			return results;
		}

		for (String str : UOM) {

			String regex_UnitOfMeasure = str;

			if (articleDescription.toUpperCase().contains(regex_UnitOfMeasure.toUpperCase() + " ")) {
				if (regex_UnitOfMeasure.contains("(n)")) {
					regex_UnitOfMeasure = regex_UnitOfMeasure.substring(0, str.length() - 3) + "\\(n\\)";
				}

				String optionalIntegerWithCommaOrSpaceSeparator_optionalDecimalWithDot = "\\b[0-9]{1,3}(("
						+ THOUSAND_SEPARATOR[0] + "|" + THOUSAND_SEPARATOR[1] + ")[0-9]{3})*(((\\"
						+ DECIMAL_SEPARATOR[0] + ")|(\\s+\\" + DECIMAL_SEPARATOR[0] + "\\s+))[0-9]+)?\\b|((\\"
						+ DECIMAL_SEPARATOR[0] + ")|(\\s+\\" + DECIMAL_SEPARATOR[0] + "\\s+))[0-9]+\\b";
				String optionalIntegerWithSpaceSeparator_optionalDecimalWithComma = "\\b[0-9]{1,3}("
						+ THOUSAND_SEPARATOR[0] + "[0-9]{3})*(((" + DECIMAL_SEPARATOR[1] + ")|(\\s+"
						+ DECIMAL_SEPARATOR[1] + "\\s+))[0-9]+)?\\b|((" + DECIMAL_SEPARATOR[1] + ")|(\\s+"
						+ DECIMAL_SEPARATOR[1] + "\\s+))[0-9]+\\b";
				String optionalIntegerWithoutAnySeparator_optionalDecimalWithDotOrComma = "\\b(\\d+)*((((("
						+ DECIMAL_SEPARATOR[1] + ")|(\\s+" + DECIMAL_SEPARATOR[1] + "\\s+))[0-9]+)?\\b|(("
						+ DECIMAL_SEPARATOR[1] + ")|(\\s+" + DECIMAL_SEPARATOR[1] + "\\s+))[0-9]+)|(((\\"
						+ DECIMAL_SEPARATOR[0] + ")|(\\s+\\" + DECIMAL_SEPARATOR[0] + "\\s+))[0-9]+)?\\b|((\\"
						+ DECIMAL_SEPARATOR[0] + ")|(\\s+\\" + DECIMAL_SEPARATOR[0] + "\\s+))[0-9]+\\b)";
				String allRegexRulesCombined = "(" + optionalIntegerWithCommaOrSpaceSeparator_optionalDecimalWithDot
						+ "|" + optionalIntegerWithSpaceSeparator_optionalDecimalWithComma + "|"
						+ optionalIntegerWithoutAnySeparator_optionalDecimalWithDotOrComma + ")(\\s+"
						+ regex_UnitOfMeasure + "\\s+)";

				Pattern p = Pattern.compile(allRegexRulesCombined, Pattern.CASE_INSENSITIVE);
				Matcher m = p.matcher(articleDescription);
				String quantity = null;
				if (m.find()) {

					quantity = m.group(1).split(regex_UnitOfMeasure)[0].trim().toUpperCase().replaceAll("\\s+", "")
							.replaceAll(regex_UnitOfMeasure.toUpperCase(), "");
					results = new Pair<String, String>(quantity, str);
					break;
				}
			}
		}

		return results;
	}

	public Pair<Double, String> extractAsDouble(String articleDescription) {

		Pair<Double, String> results = null;
		if (articleDescription == null || articleDescription.trim().isEmpty()) {
			return results;
		}
		Pair<String, String> extractAsString = this.extract(articleDescription);
		try {
			double quantity = Double.parseDouble(extractAsString.getFirst().replace(",", "."));
			String unitOfMeasure = extractAsString.getSecond();
			results = new Pair<Double, String>(quantity, unitOfMeasure);
		} catch (NullPointerException e) {
			System.out.println("Parsing String to Double: NullPointerException");
		}
		return results;
	}
}
