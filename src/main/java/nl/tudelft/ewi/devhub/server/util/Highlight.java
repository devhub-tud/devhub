package nl.tudelft.ewi.devhub.server.util;

public enum Highlight {
	NONE(null), AUTO("auto"), PYTHON("python", "py", "pyw", "pyc", "pyo", "pyd"), RUBY(
			"ruby", "rb"), HAML("haml"), PERL("perl"), PHP("php", "php"), SCALA(
			"scala", "scala"), GO("go", "go"), XML("html", "html", "xhtml",
			"xml", "atom", "rss", "xsl", "plist"), CSS("css", "css"), SCSS(
			"scss", "scss"), MARKDOWN("markdown", "md"), JSON("json", "json"), JAVASCRIPT(
			"javascript", "js"), TYPESCRIPT("typescript", "ts"), COFFEESCRIPT(
			"coffeescript", "coffee", "cson", "iced"), ACTIONSCRIPT(
			"actionscript", "as"), HAXE("haxe", "hx"), VBSCRIPT("vbscript",
			"vbs"), VBNET("vbnet", "vb"), PROTOCOL_BUFFERS("protobuf"), HTTP(
			"http"), LUA("lua", "lua"), DELPHI("delphi"), OXYGENE("oxygene"), JAVA(
			"java", "java", "jsp"), C("cpp", "cpp", "c", "h", "c++", "h++"), OBJECTIVEC(
			"objectivec", "m", "mm", "objc", "obj-c"), VALA("vala"), CSHARP(
			"cs", "cs", "csharp"), FSHARP("fsharp", "fs"), D("d", "d"), RSL(
			"rsl", "rsl"), RIB("rib", "rib"), MAYA("mel", "mel"), SQL("sql",
			"sql"), SMALLTALK("smalltalk", "st"), LISP("lisp", "lisp"), CLOJURE(
			"clojure", "clj"), INI("ini", "ini"), APACHE("apache", "apacheconf"), NGINX(
			"nginx", "nginxconf"), DIFF("diff", "diff", "patch"), DOS("dos",
			"dos", "bat", "cmd"), BASH("bash", "bash", "sh", "zsh"), MAKE(
			"makefile", "mk", "make", "mak"), CMAKE("cmake", "cmake",
			"cmake.in"), NIX("nix", "nix"), ASM86("x86asm", "asm"), TEX("tex",
			"tex"), HASKELL("haskell", "hs"), MATLAB("matlab", "matlab"), APPLESCRIPT(
			"applescript", "applescript", "osascript"), BRAINFUCK("brainfuck",
			"bf"), SWIFT("swift", "swift");

	private final String className;
	private final String[] fileTypes;

	private Highlight(String className, String... fileTypes) {
		this.fileTypes = fileTypes;
		this.className = className;
	}
	
	public boolean isHighlight() {
		return className != null;
	}

	public String getClassName() {
		return className;
	}

	public static Highlight forFileName(final String fileName) {
		int index = fileName.lastIndexOf('.');
		final String extension = index != -1 ? fileName.substring(index + 1)
				: fileName;

		for (Highlight highlight : Highlight.values()) {
			for (String fileType : highlight.fileTypes) {
				if (fileType.equalsIgnoreCase(extension)) {
					return highlight;
				}
			}
		}

		return Highlight.NONE;
	}
}