package SpellFlowLanguage.diagram.helperClasses;

import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.RGB;

/**
 * Class that creates syntax highlighted labels. It is used by the FancyTooltip class.
 * 
 * @author susann.gottmann
 */
public class ParseSPELL2SyntaxHighlightingLabel extends RectangleFigure {

	/**
	 * The following three lists are taken from SPELL.xtext grammar.
	 * They contain all SPELL keyword which shall be highlighted.
	 */
	private static String[] spellkeywords = {"Prompt","GetTM","Verify","Var","Type","Range","Default","Confirm","Expected","Display","DisplayStep",
			"BuildTC","Send","command","WaitFor","WaitFor_TimeOut","condition","ForDelay","SetGroundParameter","GetLimits","SetLimits","EnableAlarm",
			"DisableAlarm","IsAlarmed","Event","SetResource","GetResource","OpenDisplay","PrintDisplay","Pause","Abort","Finish","SetUserAction",
			"EnableUserAction","DisableUserAction","DismissUserAction","CreateDictionary","SaveDictionary","StartProc","ChangeLanguageConfig","Goto",
			"Step","Paragraph","LoadDictionary",
			// ENDIF, etc.
			"#ENDIF", "#ENDFOR", "#ENDWHILE", "#ENDDEF", "#ENDTRY", "#ENDWITH", 
			// Python keywords
			"if", "else", "elif", "for", "while", "in", "try", "finally", "with",  
			"del", "pass", "break", "continue", "return", "raise", "from", "import",
			"as", "global", "nonlocal", "assert", "excerpt", "lambda",
			"or", "and", "not", "is", "class", "yield"
	}; 
	
	private static String[] SPELLConstant={"NOW","TODAY","TOMORROW","YESTERDAY","HOUR","MINUTE","SECOND","DATE","DATETIME","RELTIME","ABSTIME","RAW",
			"ENG","SKIP","LONG","STRING","BOOLEAN","TIME","FLOAT","DEC","HEX","OCT","BIN","VALUE","ALL","ACTIVE","LIST","NUM","OK","ALPHA",
			"OK_CANCEL","CANCEL","YES","NO","YES_NO","INFORMATION","WARNING","ERROR","NOACTION","ABORT","REPEAT","RESEND","RECHECK","FIXME_unknownConstant"};

	private static String[] SPELLModifier	= {"AdjLimits","Automatic","Block","Blocking","Confirm","Default","Delay","HandleError","HiBoth","HiRed","HiYel",
			"Host","IgnoreCase","Interval","LoadOnly","LoBoth","LoRed","LoYel","Message","Midpoint","Notify","OnFailure","OnTrue","OnFalse","Printer",
			"PromptUser","Radix","Retries","SendDelay","Severity","Time","Timeout","Tolerance","Type","Units","Until","ValueFormat","ValueType",
			"Visible","Wait","Extended" ,"args","command","ReleaseTime","ConfirmCritical","sequence","group","Group","addInfo","verify","Select",
			"Nominal","Warning","Error","Ignore","Delta","Format" ,"FIXME_ModifierUnknown"};

	/**
	 * Colors used for syntax highlighting.
	 */
	private static Color keywordColor = new Color(null, 140, 60, 80);
	private static Color normalColor = new Color(null, 50, 50, 50);
	private static Color modifierColor = new Color(null, 140, 60, 80);
	private static Color stringColor = new Color(null, new RGB(0,0,255));
	private static Color commentColor = new Color(null, new RGB(80,160,120));
	private static Color backgroundColor = new Color(null, new RGB(255,255,255));
	
	/**
	 * Constructor
	 * 
	 * @param code
	 * @param w
	 * @param h
	 */
	public ParseSPELL2SyntaxHighlightingLabel(String code, int w, int h) {
		this.setFont(new Font(null, "Courier New", 9, SWT.NORMAL));
		this.setForegroundColor(normalColor);
		this.setBackgroundColor(backgroundColor);
		this.setBorder(new MarginBorder(3));
		this.setOutline(false);
		this.setLineWidth(0);
		this.setMinimumSize(new Dimension(w, h));
		this.setLayoutManager(new FlowLayout(false));
		
		this.setText(code);
	}
	
	/**
	 * Set syntax highlighting:
	 * - bold + pink for SPELL keywords
	 * - bold + black (default color) for constants
	 * - bold + blue for SPELL modifiers
	 * - italic + green for strings
	 * - italic + red for comments
	 * 
	 * @param s
	 */
	public void setText(String s) {
		// Do nothing
		if(s == null) {
			return;
		}

		String[] lines = s.split(System.getProperty("line.separator"));
		for(String line : lines) {

			RectangleFigure r = new RectangleFigure();
			r.setLayoutManager(new FlowLayout(true));
			r.setOutline(false);

			// Do the interesting stuff
			parseString(line, r);

			this.add(r);
		}
	}

	/**
	 * 
	 * @param s
	 * @param r
	 * @return
	 */
	private void parseString(String s, RectangleFigure r) {
		// Split string on delimiters, operators, spaces & breaks
		// help from: http://stackoverflow.com/questions/9856916/java-string-split-regex
		String[] ops = s.split("[a-zA-Z0-9\"\'#\\_\\$]+");
		String[] notops = s.split("\\s*[^a-zA-Z0-9\"\'#\\_\\$]+\\s*");

		int len = ops.length + notops.length - 1 < 1 ? 1 : ops.length + notops.length - 1;
		String[] res = new String[len];
		
		if(notops.length > 0 && notops[0].equals("")) 
			notops = switchFirstandLastElement(notops);

		// Prepare string to check: Includes everything, but split into an array according to the regexp
		for(int i = 0; i < res.length; i++) {
			if(notops == null) {
				res[i] = ops[i];
			}
			if(ops == null) {
				res[i] = notops[i];
			}
			try {
				res[i] = i%2==0 ? notops[i/2] : ops[i/2+1];
			} catch (Exception e) {
				// It happens very rarely, that we get an ArrayIndexOutOfBoundsException. 
				// Then, assign "" (nothing special) to res[i]. 
				System.out.println(e);
				res[i] = "";
				
				for (String string : res) {
					System.out.print(string + "|-|");
				}
				System.out.println();
			}
			
		}

		boolean inString = false;
		// Check whole array
		for (String part : res) {
			
			// Search for SPELL & Python keywords
			if(isSpellKeyWord(part)) {
				Label l = new Label(part);
				l.setFont(new Font(null, "Courier New", 9, SWT.BOLD));
				l.setForegroundColor(keywordColor);
				r.add(l);
				continue;
			}
			
			// Search for comments (full line & inline)
			if(part.startsWith("#")) {
				int i = s.indexOf(part);
				// Make a label out of the whole string starting at part until the end of line
				Label l = new Label(s.substring(i, s.length()));
				l.setFont(new Font(null, "Courier New", 9, SWT.ITALIC));
				l.setForegroundColor(commentColor);
				r.add(l);
				// Stop here
				return;
			}

			// Search for SPELL constants
			if(isSpellConstant(part)) {
				Label l = new Label(part);
				l.setFont(new Font(null, "Courier New", 9, SWT.BOLD));
				r.add(l);
				continue;
			}
			
			// Search for SPELL modifiers
			if(isSpellModifier(part)) {
				Label l = new Label(part);
				l.setFont(new Font(null, "Courier New", 9, SWT.BOLD));
				l.setForegroundColor(modifierColor);
				r.add(l);
				continue;
			}

			if(isString(part)) {
				Label l = new Label(part);
				l.setFont(new Font(null, "Courier New", 9, SWT.ITALIC));
				l.setForegroundColor(stringColor);
				r.add(l);
				continue;
			}
			
			if(!inString) {
				inString = startString(part);
			}

			if(inString) {
				Label l = new Label(part);
				l.setFont(new Font(null, "Courier New", 9, SWT.ITALIC));
				l.setForegroundColor(stringColor);
				r.add(l);
				inString = !endString(part);
			} else {
				Label l = new Label(part);
				l.setFont(new Font(null, "Courier New", 9, SWT.NORMAL));
				l.setForegroundColor(normalColor);
				r.add(l);
			}
		}
	}

	private boolean endString(String part) {
		return part.endsWith("\'") || part.endsWith("\"");
	}

	private boolean startString(String part) {
		return part.startsWith("\'") || part.startsWith("\"");
	}

	private String[] switchFirstandLastElement(String[] notops) {
		String[] temp = new String[notops.length];
		for(int i = 0; i < notops.length-1; i++) {
			temp[i] = notops[i+1];
		}
		temp[temp.length-1] = notops[0];
		
		return temp;
	}

	/**
	 * If string starts with " or ' and ends with " or ', then it is a string.
	 * 
	 * @param string
	 * @return
	 */
	private boolean isString(String string) {
		if ( (string.startsWith("\"") || string.startsWith("\'")) &&
			 (string.endsWith("\"") || string.endsWith("\'") || 
					 string.endsWith("\",") || string.endsWith("\',") ||
					 string.endsWith("\";") || string.endsWith("\';") ||
					 string.endsWith("\"|") || string.endsWith("\'|") 
			) )
			return true;
		
		return false;
	}

	/**
	 * Check, if string is an entry in SPELLModifier list.
	 * 
	 * @param string
	 * @return
	 */
	private boolean isSpellModifier(String string) {
		for(String s : SPELLModifier) {
			if(s.equals(string)) return true;
		}
		
		return false;
	}

	/**
	 * Check, if string contains at least one modifier.
	 * Why "contains"? Because of expressions like "LIST|ALPHA".
	 * 
	 * Problem: It is not the best solution / or accurate, because it
	 * detects also strings like abcALPHA123.
	 * 
	 * @param string
	 * @return
	 */
	private boolean isSpellConstant(String string) {
		for(String s : SPELLConstant) {
			if(string.contains(s)) return true;
		}

		return false;
	}

	/**
	 * Check, if string is an entry in spellkeywords list.
	 * 
	 * @param string
	 * @return
	 */	
	private boolean isSpellKeyWord(String string) {
		for(String s : spellkeywords) {
			if(string.equals(s)) { 
				return true;
			}
		}
		return false;
	}
}

