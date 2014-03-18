package blogparser

class StringReplacingInputStream extends ReplacingInputStream {

	StringReplacingInputStream(inputStream, search, replacement, charset = "UTF-8") {
		super(inputStream, search.getBytes(charset), replacement.getBytes(charset))
	}
}
