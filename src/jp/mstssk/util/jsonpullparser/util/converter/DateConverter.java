package jp.mstssk.util.jsonpullparser.util.converter;

import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import net.vvakame.util.jsonpullparser.JsonFormatException;
import net.vvakame.util.jsonpullparser.JsonPullParser;
import net.vvakame.util.jsonpullparser.util.JsonUtil;
import net.vvakame.util.jsonpullparser.util.OnJsonObjectAddListener;
import net.vvakame.util.jsonpullparser.util.TokenConverter;

/**
 * DateConverter
 * 
 * @author mstssk
 */
public class DateConverter extends TokenConverter<Date> {

	/**
	 * ISO-8601<br>
	 * XXX json2.jsのJSON.stringifyではミリ秒を出力しない
	 */
	private static UTCDateFormat dateFormat = new UTCDateFormat(
			"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

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
				date = parseDateString(parser.getValueString());
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

		if (obj == null) {
			JsonUtil.put(writer, obj);
		} else {
			JsonUtil.put(writer, dateFormat.format(obj));
		}

	}

	/**
	 * 日付文字列をパース
	 * 
	 * @param dateStr 日付文字列
	 * @return Date
	 * @throws JsonFormatException
	 */
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
