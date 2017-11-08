package spell_2_spell_flow_button.command;

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.henshin.interpreter.RuleApplication;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.BasicApplicationMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

/**
 * Applies transformation units or atomic rules using the Henshin framework.
 * During application, a progress bar is visible. 
 * 
 * @author susann.gottmann
 *
 */
public class HenshinApplicationMonitor extends BasicApplicationMonitor {
	
	private HashMap<String, Integer> ruleApps = new HashMap<String, Integer>();
	//private String appliedRules = "";
	private IProgressMonitor GUIMonitor = null;
	
	private int c = 0;
	
	public HenshinApplicationMonitor(IProgressMonitor GUIMonitor) {
		//appliedRules = "";
		
		if(GUIMonitor != null) {
			this.GUIMonitor = GUIMonitor;
		} else {
			this.GUIMonitor = (IProgressMonitor) new ProgressMonitorDialog(null);
		}
	}
	
	@Override
	public void notifyExecute(UnitApplication application, boolean success) {
		super.notifyExecute(application, success);
		if (this.GUIMonitor.isCanceled()) {
			this.cancel();
		}
		if (success && application instanceof RuleApplication) {
			String ruleName = ((RuleApplication)application).getRule().getName();
			//appliedRules += ruleName + "\n";
			Integer apps = ruleApps.get(ruleName);
			if (apps == null) {
				apps = new Integer(1);
			} else {
				apps++;
			}
			ruleApps.put(ruleName, apps);	
			this.GUIMonitor.subTask("(" + ++c + ")");

			System.out.println(((RuleApplication)application).getRule().getName());

		}
	}
	
}
