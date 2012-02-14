package jp.mstssk.util.jsonpullparser.util.converter;

import java.io.IOException;
import java.io.Writer;
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
	 * 正規表現 日付パターン<br>
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

		// 任意にコメントアウトを解除して使用
		// if (listener != null) {
		// listener.onAdd(date);
		// }

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
	 * Dateオブジェクトを日付文字列へ<br>
	 * ISO-8601拡張日付フォーマット<br>
	 * yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
	 * 
	 * @param date Date
	 * @return String
	 */
	private String formatDateStr(Date date) {

		if (date == null) {
			return null;
		}

		StringBuilder builder = new StringBuilder();

		Calendar calender = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calender.setTime(date);

		formatDigit(builder, calender.get(Calendar.YEAR), 4);
		builder.append('-');
		formatDigit(builder, calender.get(Calendar.MONTH) + 1, 2);
		builder.append('-');
		formatDigit(builder, calender.get(Calendar.DAY_OF_MONTH), 2);
		builder.append('T');
		formatDigit(builder, calender.get(Calendar.HOUR_OF_DAY), 2);
		builder.append(':');
		formatDigit(builder, calender.get(Calendar.MINUTE), 2);
		builder.append(':');
		formatDigit(builder, calender.get(Calendar.SECOND), 2);
		builder.append('.');
		formatDigit(builder, calender.get(Calendar.MILLISECOND), 3);
		builder.append('Z');

		return builder.toString();
	}

	/**
	 * 指定した桁数でゼロ埋め
	 * 
	 * @param builder
	 * @param value
	 * @param length
	 */
	private void formatDigit(StringBuilder builder, int value, int length) {

		int digit = 1;
		for (int i = 1; i < length; i++) {
			digit *= 10;
			if (digit > value) {
				builder.append('0');
			}
		}
		builder.append(value);

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
			int year = Integer.parseInt(matcher.group(1));
			int month = Integer.parseInt(matcher.group(2));
			int day = Integer.parseInt(matcher.group(3));
			int hour = Integer.parseInt(matcher.group(4));
			int min = Integer.parseInt(matcher.group(5));
			int sec = Integer.parseInt(matcher.group(6));
			int milli = 0;
			String milliStr = matcher.group(7);
			if (milliStr != null) {
				milli = Integer.parseInt(milliStr);
			}

			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			cal.set(year, month, day, hour, min, sec);
			cal.set(Calendar.MILLISECOND, milli);

			return cal.getTime();

		} catch (IllegalStateException e) {
			throw new JsonFormatException(e);
		}
	}

}
