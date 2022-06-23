package gui;

public class HtmlTemplates {
	
	public static final String styles = "" + 
			"<style>" + 
			"	body {" + 
			"		padding: 8px;" + 
			"		color: #cccccc;" + 
			"		maxWidth: 100vw;" + 
			"		word-wrap: break-word" + 
			"	}" + 
			"	" + 
			"	.time {" + 
			"		color: #aaaaaa;" + 
			"		font-style: italic;" + 
			"	}" + 
			"	" + 
			"	.author {" + 
			"		" + 
			"	}" + 
			"	" + 
			"	.text {" + 
			"		" + 
			"	}"
			+ "table { border: 1px solid #999999; border-collapse: collapse; }"
			+ " a { color: #3DAABF; }" + 
			"</style>" + 
			"";
	
	public static final String normal_message = "<div style=\"margin-top: 10px;\">" + 
			"	<span class=\"time\">" + 
			"		{time}" + 
			"	</span>" + 
			"	<span>" + 
			"		{author}" + 
			"	</span>" + 
			"	<span class=\"text\">" + 
			"		{text}" + 
			"	</span>" + 
			"</div>";
	
}
