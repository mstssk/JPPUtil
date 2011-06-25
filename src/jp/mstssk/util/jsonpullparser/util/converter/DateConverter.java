package jp.mstssk.util.jsonpullparser.util.converter;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.vvakame.util.jsonpullparser.JsonFormatException;
import net.vvakame.util.jsonpullparser.JsonPullParser;
import net.vvakame.util.jsonpullparser.util.JsonUtil;
import net.vvakame.util.jsonpullparser.util.OnJsonObjectAddListener;
import net.vvakame.util.jsonpullparser.util.TokenConverter;

/**
 * DateConverter<br>
 * ECMA-262 5th EditionのDate.prototype.toJSONに対応
 * 
 * @author mstssk
 */
public class DateConverter extends TokenConverter<Date> {

	/**
	 * ISO-8601拡張日付フォーマット<br>
	 * json2.jsのJSON.stringifyではミリ秒を出力しない
	 */
	private static final UTCDateFormat dateFormat = new UTCDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

	/**
	 * 日付パターン<br>
	 * ミリ秒の省略に対応（ECMA-262 5th Edition # 15.9.1.15 Date Time String Format）
	 */
	private static final Pattern datePattern = Pattern
			.compile("^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})(?:.(\\d{3}))?Z$");

	/**
	 * getInstance
	 * 
	 * @return DateConverter
	 */
	public static DateConverter getInstance() {
		return new DateConverter();
	}

	/**
	 * パース
	 */
	@Override
	public Date parse(JsonPullParser parser, OnJsonObjectAddListener listener)
			throws IOException, JsonFormatException {

		Date date = null;

		switch (parser.getEventType()) {
			case VALUE_NULL:
				// null
				break;
			case VALUE_STRING:
				date = parseDateStr(parser.getValueString());
				break;
			default:
				throw new JsonFormatException("Illegal format for date string.");
		}

		if (listener != null) {
			listener.onAdd(date);
		}

		return date;
	}

	/**
	 * エンコード
	 */
	@Override
	public void encodeNullToNull(Writer writer, Date obj) throws IOException {

		JsonUtil.put(writer, formatDateStr(obj));

	}

	/**
	 * Dateオブジェクトを日付文字列へ
	 * 
	 * @param date Date
	 * @return String
	 */
	private String formatDateStr(Date date) {
		if (date == null) {
			return null;
		} else {
			return dateFormat.format(date);
		}
	}

	/**
	 * 日付文字列をパース
	 * 
	 * @param dateStr 日付文字列
	 * @return Date
	 * @throws JsonFormatException
	 */
	@Deprecated
	private Date parseDateString(String dateStr) throws JsonFormatException {
		Date date = null;
		try {
			date = dateFormat.parse(dateStr);
		} catch (ParseException e) {
			throw new JsonFormatException(e);
		}
		return date;
	}

	/**
	 * 日付文字列を正規表現でパース
	 * 
	 * @param str JSON日付文字列
	 * @return Date
	 * @throws JsonFormatException
	 */
	private Date parseDateStr(String str) throws JsonFormatException {

		try {

			Matcher matcher = datePattern.matcher(str);
			matcher.find();
			int year = parseInt(matcher.group(1));
			int month = parseInt(matcher.group(2));
			int day = parseInt(matcher.group(3));
			int hour = parseInt(matcher.group(4));
			int min = parseInt(matcher.group(5));
			int sec = parseInt(matcher.group(6));
			int milli = 0;
			String milliStr = matcher.group(7);
			if (milliStr != null) {
				milli = parseInt(milliStr);
			}

			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			cal.set(year, month, day, hour, min, sec);
			cal.set(Calendar.MILLISECOND, milli);

			return cal.getTime();

		} catch (IllegalStateException e) {
			throw new JsonFormatException(e);
		}
	}

	/**
	 * Integer.parseInt
	 * 
	 * @see java.lang.Integer.parseInt
	 * @param str String
	 * @return int
	 */
	private int parseInt(String str) {
		return Integer.parseInt(str);
	}

	/**
	 * UTC DateFormat
	 * 
	 * @author mstssk
	 */
	private static class UTCDateFormat extends SimpleDateFormat {

		/** シリアルバージョン */
		private static final long serialVersionUID = 7578410179499211362L;

		/**
		 * コンストラクタ
		 */
		public UTCDateFormat(String pattern) {
			applyPattern(pattern);
			setTimeZone(TimeZone.getTimeZone("UTC"));
		}

	}

}
