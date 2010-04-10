package info.vstour.dbdoc.shared;

public class Converter {
	private final static String	NEW_LINE	   = "\n";
	private final static String	MARK_NL	       = "#NL#";

	private final static String	DS_MARK	       = "#DS#";
	private final static String	DE_MARK	       = "#DE#";
	private final static String	DOC_START	   = "/**";
	private final static String	DOC_END	       = "*/";

	private final static String	CS_MARK	       = "#CS#";
	private final static String	CE_MARK	       = "#CE#";
	private final static String	COMMENT	       = "--";
	private final static String	COMMENT_START	= "/*";
	private final static String	COMMENT_END	   = "*/";

	private final static String	AS_MARK	       = "#AS#";
	private final static String	AE_MARK	       = "#AE#";
	private final static String	APOSTROPHE	   = "'";

	private final static String	METHODS	       = "^.*\\bPROCEDURE\\b.*$|^.*\\bFUNCTION\\b.*$";
	private final static String	TYPES	       = "^.*\\bTYPE\\b.*$|^.*\\bSUBTYPE\\b.*$";
	private final static String	DATA_TYPES	   = "^.*CONSTANT.*$|"
	                                                   + "^.*\\bCHARACTER\\b.*$|^.*\\bCHAR\\b.*$|^.*\\bNCHAR\\b.*$|^.*\\bVARCHAR2\\b.*$|^.*\\bNVARCHAR2\\b.*$|"
	                                                   + "^.*\\bNUMERIC\\b.*$|^.*\\bDECIMAL\\b.*$|^.*\\bDEC\\b.*$|^.*\\bNUMBER\\b.*$|^.*\\bBINARY_FLOAT\\b.*$|^.*\\bBINARY_DOUBLE\\b.*$|"
	                                                   + "^.*\\bDATE\\b.*$|^.*\\bTIMESTAMP\\b.*$|^.*\\bINTERVAL\\b.*$|"
	                                                   + "^.*\\bINTEGER\\b.*$|^.*\\bINT\\b.*$|^.*\\bSMALLINT\\b.*$|"
	                                                   + "^.*\\bLONG\\b.*$|^.*\\bRAW\\b.*$|"
	                                                   + "^.*\\bFLOAT\\b.*$|^.*\\bDOUBLE\\b.*$|^.*\\bREAL\\b.*$|"
	                                                   + "^.*\\bRECORD\\b.*|^.*\\bCURSOR\\b.*$|^.*\\bTABLE\\b.*$|^.*\\bROWTYPE\\b.*$";

	private final static String	KEY_WORDS	   = METHODS + "|" + TYPES + "|" + DATA_TYPES;

	private static String	    summary	       = "";
	private static String	    methodName	   = "";
	private static String	    methodDesc	   = "";
	private static boolean	    isDocBlock	   = false;
	private static boolean	    isMethod	   = false;
	private static boolean	    isCommentBlock	= false;
	private static boolean	    isHasDoc	   = false;	                                                                           // is previous line has doc end sign

	public static void init() {
		isDocBlock = false;
		isMethod = false;
		isHasDoc = false;
		isCommentBlock = false;
	}

	/**
	 * Mark up documentation. Documentation is started with slash and two asterisks and
	 * ended with with an asterisk and a slash.
	 * 
	 * @param string
	 *            to mark up
	 * @return string formatted with special signs
	 */
	public static String markUp(String string) {

		// Check if documentation or comments block is marked
		if (!isDocBlock && !isCommentBlock) {
			// Check if string has documentation start sign
			// Ignore documentation inside the method text block
			if (string.trim().indexOf(DOC_START) == 0 && !isMethod) {
				// Check if string has documentation end sign
				if (string.contains(DOC_END)) {
					string = DS_MARK + string + DE_MARK;
					isDocBlock = true;
				} else {
					string = DS_MARK + string;
					isDocBlock = true;
				}
			} else if (string.trim().indexOf(COMMENT_START) == 0) {
				string = CS_MARK + string;
				if (string.contains(COMMENT_END)) {
					string = string + CE_MARK;
				} else
					isCommentBlock = true;
			} else if (string.trim().indexOf(COMMENT) == 0) {
				string = CS_MARK + string + CE_MARK;
			} else {
				// Set indication of method text block
				if (string.trim().matches(KEY_WORDS) && !isMethod) {
					// Add markers of empty documentation to method that do not has one.
					if (!isHasDoc) {
						// Skip commented lines
						if (!(string.trim().indexOf(COMMENT) == 0))
							string = DS_MARK + DE_MARK + MARK_NL + string;
					}
					if (!string.contains(";")) {
						isMethod = true;
					}
				} else if (string.contains(";"))
					isMethod = false;

				if (string.contains(COMMENT)) {
					int index = string.indexOf(COMMENT);
					string = string.substring(0, index) + CS_MARK + string.substring(index) + CE_MARK;
				} else if (string.contains(COMMENT_START)) {
					int startIndex = string.indexOf(COMMENT_START);
					string = string.substring(0, startIndex) + CS_MARK + string.substring(startIndex);
					if (string.contains(COMMENT_END)) {
						int endIndex = string.indexOf(COMMENT_END) + COMMENT_END.length();
						string = string.substring(0, endIndex) + CE_MARK + string.substring(endIndex);
					} else
						isCommentBlock = true;
				} else if (string.contains(APOSTROPHE)) {
					int fIndex = string.indexOf(APOSTROPHE);
					int lIndex = string.indexOf(APOSTROPHE, fIndex + 1);
					while (fIndex > 0 && lIndex > 0) {
						string = string.substring(0, fIndex) + AS_MARK + string.substring(fIndex + 1, lIndex) + AE_MARK
						        + string.substring(lIndex + 1);
						fIndex = string.indexOf(APOSTROPHE);
						lIndex = string.indexOf(APOSTROPHE, fIndex + 1);
					}
				}
			}
		}

		if (string.contains(DOC_END) && isDocBlock) {
			isHasDoc = true;
			isDocBlock = false;
			if (!string.contains(DE_MARK))
				string = string + DE_MARK;
		} else
			isHasDoc = false;

		if (string.contains(COMMENT_END) && isCommentBlock) {
			int endIndex = string.indexOf(COMMENT_END) + COMMENT_END.length();
			string = string.substring(0, endIndex) + CE_MARK + string.substring(endIndex);
			isCommentBlock = false;
		}

		if (!string.contains(MARK_NL))
			string = MARK_NL + string;
		return string;
	}

	/**
	 * Mark up documentation. All comments before declaration treated as documentation.
	 * 
	 * @param string
	 * @return
	 */
	public static String markUpComments(String string) {

		boolean commentLine = string.trim().indexOf(COMMENT) == 0;
		boolean commented = commentLine || isCommentBlock;
		if (isDocBlock && !commented && (string.trim().matches(KEY_WORDS + "|" + "\\bEND\\b.*;") || !isCommentBlock)) {
			isCommentBlock = string.trim().indexOf(COMMENT_START) == 0;
			if (isCommentBlock)
				string = DE_MARK + MARK_NL + DS_MARK + string;
			else {
				string = DE_MARK + MARK_NL + string;
				isDocBlock = false;
			}
		} else if (isDocBlock && (commentLine && string.trim().length() == 2)) {
			string = DE_MARK + MARK_NL + DS_MARK + string;
		}

		if (string.contains(DE_MARK)) {
			isHasDoc = true;
		} else
			isHasDoc = false;

		if (!isDocBlock) {
			// Check if string has comment block start sign
			// Ignore documentation inside the method text block
			isCommentBlock = string.trim().indexOf(COMMENT_START) == 0;
			if ((commentLine || isCommentBlock) && !isMethod) {
				string = DS_MARK + string;
				isDocBlock = true;
			} else {
				// Set indication of method text block
				if (string.trim().matches(KEY_WORDS) && !isMethod) {
					// Add markers of empty documentation to method that do not has one.
					if (!isHasDoc) {
						// Skip commented lines
						if (!commentLine)
							string = DS_MARK + DE_MARK + MARK_NL + string;
					}
					if (!string.contains(";")) {
						isMethod = true;
					}
				} else if (string.contains(";"))
					isMethod = false;

				// Mark comments inside declaration
				if (string.contains(COMMENT)) {
					int index = string.indexOf(COMMENT);
					string = string.substring(0, index) + CS_MARK + string.substring(index) + CE_MARK;
				} else if (string.contains(COMMENT_START)) {
					int startIndex = string.indexOf(COMMENT_START);
					string = string.substring(0, startIndex) + CS_MARK + string.substring(startIndex);
					if (string.contains(COMMENT_END)) {
						int endIndex = string.indexOf(COMMENT_END) + COMMENT_END.length();
						string = string.substring(0, endIndex) + CE_MARK + string.substring(endIndex);
					} else
						isCommentBlock = true;
				} else if (string.contains(APOSTROPHE)) {
					int fIndex = string.indexOf(APOSTROPHE);
					int lIndex = string.indexOf(APOSTROPHE, fIndex + 1);
					while (fIndex > 0 && lIndex > 0) {
						string = string.substring(0, fIndex) + AS_MARK + string.substring(fIndex + 1, lIndex) + AE_MARK
						        + string.substring(lIndex + 1);
						fIndex = string.indexOf(APOSTROPHE);
						lIndex = string.indexOf(APOSTROPHE, fIndex + 1);
					}
				}
			}
		}

		if (string.contains(COMMENT_END)) {
			int endIndex = string.indexOf(COMMENT_END) + COMMENT_END.length();
			string = string.substring(0, endIndex) + CE_MARK + string.substring(endIndex);
			isCommentBlock = false;
		}

		if (!string.contains(MARK_NL))
			string = MARK_NL + string;
		return string;
	}

	/**
	 * Converts text to html.
	 * 
	 * @param text
	 *            text to convert
	 * @param viewId
	 *            0 - source; 1 - source highlighted; 2 - documentation
	 * @param search
	 *            highlights search string
	 * @return
	 */
	public static String textToHtml(String text, int viewId, String search) {
		try {
			switch (viewId) {
				case 1:
					text = replaceEntities(text);
					text = highlightKeywords(text);
					text = text.replace(MARK_NL, "");
					text = text.replace(DS_MARK, "<span class='docs'>");
					text = text.replace(DE_MARK, "</span>");
					text = text.replace(CS_MARK, "<span class='comments'>");
					text = text.replace(CE_MARK, "</span>");
					text = text.replace(AS_MARK, "<span class='value'>'");
					text = text.replace(AE_MARK, "'</span>");
					text = "<pre>" + text + "</pre>";
					if (search.length() > 0) {
						text = text.replace(search, "<span class='fnd'>" + search + "</span>");
					}
					break;
				case 2:
					String[] splitDoc = text.split(DS_MARK);
					text = "";
					summary = "";
					for (int i = 0; i < splitDoc.length; i++) {
						String[] split = splitDoc[i].split(DE_MARK);
						if (split.length > 1) {
							String method = methodsToHtml(split[1]);
							String javaDoc = docToHtml(split[0]);
							if (method.length() > 0)
								text = text + method + javaDoc + NEW_LINE + "<hr>";
							else
								methodDesc = "<pre>" + methodDesc + "</pre>";
							if (i % 2 == 0)
								summary = summary + "<tr class='alt'><td>" + methodName + "</td><td>" + methodDesc + "</td></tr>";
							else
								summary = summary + "<tr><td>" + methodName + "</td><td>" + methodDesc + "</td></tr>";
						}
					}
					summary = "<table id='smTable'><tbody><tr class='smth'><th colspan='2'>Summary</th></tr>" + summary
					        + "</tbody></table><hr>";
					text = summary + text;
					text = text.replace(CS_MARK, "<span class='comments'>");
					text = text.replace(CE_MARK, "</span>");
					text = text.replace(AS_MARK, "<span class='value'>'");
					text = text.replace(AE_MARK, "'</span>");
					if ((search.length() > 0) && viewId < 2) {
						text = text.replace(search, "<span class='fnd'>" + search + "</span>");
					}
					break;
				default:
					text = replaceEntities(text);
					text = text.replace(DS_MARK, "");
					text = text.replace(DE_MARK, "");
					text = text.replace(CS_MARK, "");
					text = text.replace(CE_MARK, "");
					text = text.replace(AS_MARK, "'");
					text = text.replace(AE_MARK, "'");
					text = text.replace(MARK_NL, "");
					text = "<pre>" + text + "</pre>";
					break;
			}
		}
		catch (Exception e) {
			text = "<span class='error'>" + e.toString() + "</span>";
		}
		return text;
	}

	private static String methodsToHtml(String string) throws Exception {
		methodName = "";
		string = string.trim();
		String[] lines = string.split(MARK_NL);
		if (lines.length > 1) {
			String line = lines[1].trim();
			if (line.replace(NEW_LINE, "").matches(KEY_WORDS)) {
				String name = "";
				String tmp = "";
				int index = 0;
				String[] words = line.trim().split(" ");
				for (int i = 0; i < words.length; i++) {
					if (words[i].length() > 2) {
						index++;
						String word = words[i].trim();
						if (index == 1) {
							tmp = word;
						}
						if (index == 2) {
							if (word.toUpperCase().matches(KEY_WORDS))
								name = tmp;
							else
								name = words[i];
							break;
						}
					}
				}
				if (name.indexOf("(") > 0)
					name = name.substring(0, name.indexOf("("));
				int indexSN = string.indexOf(name);
				int indexEN = indexSN + name.length();
				int indexEM = string.indexOf(";");
				if (indexEM > 0)
					string = string.substring(0, indexEM + 1);
				if (indexSN >= 0 && (indexEN) > 0) {
					// ID for methods with the same name
					int id = Double.valueOf(Math.floor(Math.random() * 999)).intValue();
					methodName = "<a href='#" + name + id + "'>" + name + "</a>";
					string = "<h3><a name='" + name + id + "'>" + name + "</a></h3><pre>" + string.substring(0, indexSN) + "<b>"
					        + name + "</b>" + string.substring(indexEN) + "</pre>";
					string = string.replace(MARK_NL, "");
				}
			} else
				string = "";
		} else
			string = "";
		return string;
	}

	private static String docToHtml(String doc) throws Exception {
		String desc = "";
		String params = "";
		String rtrn = "";
		String thrws = "";
		String see = "";
		doc = doc.replace(DOC_START, "");
		doc = doc.replace(DOC_END, "");
		doc = doc.replace(COMMENT, "");
		doc = doc.replace(COMMENT_START, "");
		doc = doc.replace(COMMENT_END, "");
		String[] split = doc.split(MARK_NL);
		for (String string : split) {
			if (string.trim().length() > 2) {
				if (string.trim().indexOf("*") == 0)
					string = string.trim().replaceAll("^\\*", "");
				if (string.contains("@param")) {
					params = params + "<dd>" + string.trim().replace("@param", "") + "</dd>";
				} else if (string.contains("@return")) {
					rtrn = rtrn + "<dd>" + string.trim().replace("@return", "") + "</dd>";
				} else if (string.contains("@throws")) {
					thrws = thrws + "<dd>" + string.trim().replace("@throws", "") + "</dd>";
				} else if (string.contains("@see")) {
					see = see + "<dd>" + string.trim().replace("@see", "") + "</dd>";
				} else {
					if (string.contains(NEW_LINE) || string.contains("<br/>") || string.contains("<br>"))
						desc = desc + string;
					else
						desc = desc + string + NEW_LINE;
				}
			}
		}
		methodDesc = desc;
		String text = NEW_LINE + "<dd>" + desc + "</dd><dd><p>";
		if (params.length() > 0)
			text = text + "<dt><b>Parameters:</b></dt>" + params;
		if (rtrn.length() > 0)
			text = text + "<dt><b>Returns:</b></dt>" + rtrn;
		if (thrws.length() > 0)
			text = text + "<dt><b>Throws:</b></dt>" + thrws;
		if (see.length() > 0)
			text = text + "<dt><b>See:</b></dt>" + see;
		return "<dl>" + text + "</dd></dl>";
	}

	private static String replaceEntities(String text) throws Exception {
		// " &#34; &quot; quotation mark
		// ' &#39; &apos; (does not work in IE) apostrophe 
		// & &#38; &amp; ampersand 
		// < &#60; &lt; less-than 
		// > &#62; &gt; greater-than
		text = text.replace("<", "&lt;");
		text = text.replace(">", "&gt;");
		return text;
	};

	private static String highlightKeywords(String text) throws Exception {
		text = replaceAll(text, "PACKAGE");
		text = replaceAll(text, "AS");
		text = replaceAll(text, "CONSTANT");
		text = replaceAll(text, "SUBTYPE");
		text = replaceAll(text, "TYPE");
		text = replaceAll(text, "ROWTYPE");
		text = replaceAll(text, "RECORD");
		text = replaceAll(text, "IS");
		text = replaceAll(text, "TABLE");
		text = replaceAll(text, "OF");
		text = replaceAll(text, "REF");
		text = replaceAll(text, "CURSOR");
		text = replaceAll(text, "RETURN");
		text = replaceAll(text, "PROCEDURE");
		text = replaceAll(text, "FUNCTION");
		text = replaceAll(text, "BEGIN");
		text = replaceAll(text, "END");
		text = replaceAll(text, "TRIGGER");
		text = replaceAll(text, "DECLARE");
		text = replaceAll(text, "IF");
		text = replaceAll(text, "THEN");
		text = replaceAll(text, "ELSE");
		text = replaceAll(text, "ELSIF");
		return text;
	}

	private static String replaceAll(String text, String kwd) {
		String sKwd = "<span class='kwd'>";
		String eKwd = "</span>";
		return text.replaceAll("\\b" + kwd + "\\b", sKwd + kwd + eKwd);
	}
}
