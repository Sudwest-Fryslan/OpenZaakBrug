package nl.haarlem.translations.zdstozgw.utils;

public class StringUtils {
	public static int MAX_MESSAGE_SIZE = 2 * 1024;
	public static int MAX_ERROR_SIZE = 2 * 1024;

	public static String shortenLongString(String message, int maxLength) {
		if(message.length() > maxLength) {
			var niceEnding = "...(" + (message.length() - maxLength) + " characters have been trimmed)..";
			return message.substring(0, maxLength) + niceEnding;
		}
		else {
			// do nothing
			return message;
		}
	}
}
