/**
 * @project  : WSM
 * @package  : kr.esob.wsm.commonlogic.util
 * @filename : SqlLogUtil.java
 * @author   : yukil
 * @date     : 2018. 8. 21. 오후 3:13:00
 * @desc     :
 */
package kr.esob.fdms.util;

import org.slf4j.LoggerFactory;

import net.sf.log4jdbc.log.slf4j.Slf4jSpyLogDelegator;
import net.sf.log4jdbc.sql.Spy;

/**
 * @classname : SqlLogUtil
 * @author    : yukil
 * @date      : 2018. 8. 21. 오후 3:13:00
 * @desc      :
 */
public class SqlLogUtil extends Slf4jSpyLogDelegator{
	
	private String sqlPrefix = "SQL:";
	private String margin = String.format("%1$" + 8 + "s", "");

	@Override
	public void sqlOccurred(Spy spy, String methodCall, String sql) {
		sql = sql.replaceAll("(?m)^[ \t]*\r?\n", "");
        sql = sql.replaceAll("\t", "    ");
		LoggerFactory.getLogger("jdbc.sqlonly").info(sqlPrefix + "\n" + "\n" + margin + sql + "\n");
	}

	



}
