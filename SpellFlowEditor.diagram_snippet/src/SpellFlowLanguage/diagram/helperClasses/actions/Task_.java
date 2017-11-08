package SpellFlowLanguage.diagram.helperClasses.actions;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.draw2d.IFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * A helper class derived from TimerTask which enables us to do the fading.
 * 
 * @author susann.gottmann
 *
 */
public class Task_ extends TimerTask {
	private int i = 100;
	public Color b;
	public IFigure s;
	public Timer t;
	
	@Override
	public void run() {
		Color help = s.getBackgroundColor();
		//Color c = new Color(null, new RGB(help.getRed() + 1, help.getGreen() + 1, help.getBlue() + 1));
		Color c = new Color(null, new RGB(help.getRed() + 1, help.getGreen() + 1, 255));
		s.setBackgroundColor(c);
		i -= 1;
		if (i == 0) {
			s.setBackgroundColor(b);
			this.cancel();
			t.cancel();
		}
	}
}
