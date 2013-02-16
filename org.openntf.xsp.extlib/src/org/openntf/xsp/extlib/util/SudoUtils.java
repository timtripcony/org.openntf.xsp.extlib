package org.openntf.xsp.extlib.util;

import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

public class SudoUtils {

	public interface SudoCallback {
		public Object run(Session session);
	}

	public static Object runOnBehalfOf(String userName, SudoCallback callback) {
		Object result = null;
		try {
			NotesThread.sinitThread();
			Session s = NotesFactory.createSession((String) null, userName, "");
			result = callback.run(s);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			NotesThread.stermThread();
		}
		return result;
	}

}
