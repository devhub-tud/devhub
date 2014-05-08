package nl.tudelft.ewi.devhub.server.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Data;
import nl.tudelft.ewi.git.models.DiffModel;

import com.google.common.collect.Lists;

/**
 * The {@code DiffLine} class is a small parser used for fetching the
 * modifications from a git diff output. The implementation is based on the bash
 * function from
 * http://stackoverflow.com/questions/8259851/using-git-diff-how-can
 * -i-get-added-and-modified-lines-numbers
 * 
 * @author Jan-Willem
 * 
 */
@Data
public class DiffLine {

	private final String oldLineNumber;
	private final String newLineNumber;
	private final char modifier;
	private final String contents;
	
	public static final char MODIFIER_UNCHANGED = ' ';
	public static final char MODIFIER_ADDED = '+';
	public static final char MODIFIER_REMOVED = '-';
	
	/**
	 * @return true if this line was added to the file between these commits
	 */
	public boolean isAdded() {
		return modifier == MODIFIER_ADDED;
	}
	
	/**
	 * @return true if this line was removed from the file between these commits
	 */
	public boolean isRemoved() {
		return modifier == MODIFIER_REMOVED;
	}
	
	/**
	 * @return true if this line was not changed between these commits
	 */
	public boolean isUnchanged() {
		return modifier == MODIFIER_UNCHANGED;
	}
	
	private static final String FILE_CHANGE_PATTERN = "^(---|\\+\\+\\+)\\s(a/)?.*$";
	private static final String BEGIN_END_LINES_PATTERN = "^@@\\s-(\\d+)?(,\\d+){0,1}\\s+[+](\\d+)?(,\\d+){0,1}\\s@@$";
	private static final String LINE_IN_FILE = "^(\\s|[+-])(.*)$";
	
	/**
	 * @param diffModel
	 * @return a list of lines for this {@link DiffModel}
	 */
	public static List<DiffLine> getLinesFor(DiffModel diffModel) {
		return getLinesFor(diffModel.getRaw());
	}

	private static List<DiffLine> getLinesFor(String[] raw) {
		List<DiffLine> lines = Lists.newArrayList();
		int newLineNumber = 0;
		int oldLineNumber = 0;
		
		Pattern belp = Pattern.compile(BEGIN_END_LINES_PATTERN),
				lif = Pattern.compile(LINE_IN_FILE);
		
		for(String str : raw) {
			
			Matcher belpMatcher = belp.matcher(str),
					lifMatcher = lif.matcher(str);
			
			if(str.matches(FILE_CHANGE_PATTERN)) {
				// Skip +++ and --- lines
				continue;
			} else if (belpMatcher.matches()) {
				// Start counting line numbers
				oldLineNumber = Integer.parseInt(belpMatcher.group(1));
				newLineNumber = Integer.parseInt(belpMatcher.group(3));
			} else if (lifMatcher.matches()) {
				// Create the line object and add it to the list
				char modifier = lifMatcher.group(1).charAt(0);
				String contents = lifMatcher.group(2);
				String olnStr = "", nlnStr = "";
				
				switch(modifier) {
				case MODIFIER_UNCHANGED:
					olnStr = Integer.toString(oldLineNumber);
					nlnStr = Integer.toString(newLineNumber);
					oldLineNumber++;
					newLineNumber++;
					break;
				case MODIFIER_ADDED:
					nlnStr = Integer.toString(newLineNumber);
					newLineNumber++;
					break;
				case MODIFIER_REMOVED:
					olnStr = Integer.toString(oldLineNumber);
					oldLineNumber++;
					break;
				}
				
				DiffLine line = new DiffLine(olnStr, nlnStr, modifier, contents);
				lines.add(line);
			}
		}
		
		return lines;
	}
	
	public static void main(String... args) {
		String[] input = { "diff --git a/readme.md b/readme.md",
				"index 983cc05..da041cc 100644", "--- a/readme.md",
				"+++ b/readme.md", "@@ -1 +1,2 @@",
				" A readme file with a bit of contents",
				"+Now we've altered the readme a bit to work on the diffs" };
		List<DiffLine> lines = getLinesFor(input);
		for(DiffLine line : lines) {
			System.out.println(line);
		}
	}

}
