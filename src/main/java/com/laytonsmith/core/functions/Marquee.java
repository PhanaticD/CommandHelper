package com.laytonsmith.core.functions;

import com.laytonsmith.abstraction.StaticLayer;
import com.laytonsmith.annotations.api;
import com.laytonsmith.core.CHVersion;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.constructs.CClosure;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Construct;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.functions.Exceptions.ExceptionType;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author lsmith
 */
public class Marquee {
	public static String docs(){
		return "This class provides methods for making a text \"marquee\", like a stock ticker. Because this is a threading operation, and could be potentially"
				+ " resource intensive, the heavy lifting has been implemented natively.";
	}
		
	//TODO: This should be removed in favor of a common runtime environment stash
	private static Map<String, com.laytonsmith.PureUtilities.Marquee> marqeeMap = new HashMap<String, com.laytonsmith.PureUtilities.Marquee>();
	@api public static class marquee extends AbstractFunction{

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(final Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String marqueeName = null;
			final String text;
			final int stringWidth;
			final int delayTime;
			final CClosure callback;
			int offset = -1;
			if(args.length == 5){
				offset = 0;
				marqueeName = args[0].val();
			}
			text = args[1 + offset].val();
			stringWidth = (int)Static.getInt(args[2 + offset]);
			delayTime = (int)Static.getInt(args[3 + offset]);
			if(args[4 + offset] instanceof CClosure){
				callback = ((CClosure)args[4 + offset]);
			} else {
				throw new Exceptions.CastException("Expected argument " + (4 + offset + 1) + " to be a closure, but was not.", t);
			}
			final com.laytonsmith.PureUtilities.Marquee m = new com.laytonsmith.PureUtilities.Marquee(text, stringWidth, delayTime, new com.laytonsmith.PureUtilities.Marquee.MarqueeCallback() {

				public void stringPortion(final String portion) {
					StaticLayer.SetFutureRunnable(0, new Runnable(){

						public void run() {
							callback.execute(new Construct[]{new CString(portion, t)});
						}
					});
				}
			});
			m.start();
			StaticLayer.GetConvertor().addShutdownHook(new Runnable() {

				public void run() {
					m.stop();
				}
			});
			if(marqueeName != null){
				marqeeMap.put(marqueeName, m);
			}
			return new CVoid(t);
		}

		public String getName() {
			return "marquee";
		}

		public Integer[] numArgs() {
			return new Integer[]{4, 5};
		}

		public String docs() {
			return "void {[marqueeName], text, stringWidth, delayTime, callback} Sets up a marquee, which will automatically"
					+ " split up a given string for you, and call the callback. The split string will automatically wrap, handle"
					+ " buffering spaces, and scroll through the text. ---- marqueeName is optional, but required if you wish"
					+ " to stop the marquee at any point. text is the text that the marquee should scroll, stringWidth is the"
					+ " width of the string you wish to recieve, delayTime is the"
					+ " time between character scrolls, and callback is a closure that should recieve a string which will be exactly"
					+ " stringWidth long. (The string will have been wrapped as needed if it is less than that size.)"
					+ " This is usually used in combination with signs, but in theory could be used with anything that uses text.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
	
	@api public static class marquee_stop extends AbstractFunction {

		public ExceptionType[] thrown() {
			return new ExceptionType[]{};
		}

		public boolean isRestricted() {
			return true;
		}

		public Boolean runAsync() {
			return null;
		}

		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
			String marqueeName = args[0].val();
			if(marqeeMap.containsKey(marqueeName)){
				marqeeMap.get(marqueeName).stop();
			}
			return new CVoid(t);
		}

		public String getName() {
			return "marquee_stop";
		}

		public Integer[] numArgs() {
			return new Integer[]{1};
		}

		public String docs() {
			return "void {marqueeName} Stops a named marquee.";
		}

		public CHVersion since() {
			return CHVersion.V3_3_1;
		}
		
	}
}