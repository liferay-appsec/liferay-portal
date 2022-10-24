/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.security.permission;

import com.liferay.petra.string.StringBundler;
import com.liferay.portal.security.permission.internal.InlineSQLParser;
import com.liferay.portal.test.rule.LiferayUnitTestRule;

import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Michael Bowerman
 */
public class InlineSQLParserTest {

	@ClassRule
	@Rule
	public static final LiferayUnitTestRule liferayUnitTestRule =
		LiferayUnitTestRule.INSTANCE;

	@Test
	public void testComplexSQL() {
		_testInlineSQLParser(
			StringBundler.concat(
				"((SELECT * FROM Table1 INNER JOIN ((SELECT * FROM Table2 ",
				"LEFT JOIN (SELECT * FROM Table1 WHERE field1 LIKE '%test%') ",
				"UNION ALL SELECT * FROM Table3 WHERE field2 > 0)) UNION ",
				"(SELECT * FROM Table2 INNER JOIN ((SELECT * FROM Table2 LEFT ",
				"JOIN Table1 ON Table1.field1 = Table2.field1 UNION ALL ",
				"SELECT * FROM Table1 WHERE field2 > 0)))))"),
			StringBundler.concat(
				"((SELECT * FROM Table1 INNER JOIN ((SELECT * FROM Table2 ",
				"LEFT JOIN (SELECT * FROM Table1 WHERE ",
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL, " AND field1 LIKE ",
				"'%test%') UNION ALL SELECT * FROM Table3 WHERE field2 > 0)) ",
				"WHERE ", _RESOURCE_PERMISSION_FILTER_CONDITION_SQL, " UNION ",
				"(SELECT * FROM Table2 INNER JOIN ((SELECT * FROM Table2 LEFT ",
				"JOIN Table1 ON Table1.field1 = Table2.field1 WHERE ",
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL, " UNION ALL SELECT ",
				"* FROM Table1 WHERE ",
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL, " AND field2 > ",
				"0)))))"));
	}

	@Test
	public void testSelectFromDifferentTable() {
		String sql = "SELECT * FROM Table2";

		_testInlineSQLParser(sql, sql);
	}

	@Test
	public void testSelectFromDifferentTableJoinedOnDifferentTable() {
		String sql = "SELECT * FROM Table2 INNER JOIN Table3";

		_testInlineSQLParser(sql, sql);
	}

	@Test
	public void testSelectFromDifferentTableJoinedOnSelectFromTable() {
		_testInlineSQLParser(
			"SELECT * FROM Table2 INNER JOIN (SELECT * FROM Table1)",
			"SELECT * FROM Table2 INNER JOIN (SELECT * FROM Table1 WHERE " +
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL + ")");
	}

	@Test
	public void testSelectFromDifferentTableJoinedOnTable() {
		_testInlineSQLParser(
			"SELECT * FROM Table2 INNER JOIN Table1",
			"SELECT * FROM Table2 INNER JOIN Table1 WHERE " +
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL);
	}

	@Test
	public void testSelectFromDifferentTableWithWhereJoinedOnSelectFromTable() {
		_testInlineSQLParser(
			"SELECT * FROM Table2 INNER JOIN (SELECT * FROM Table1) WHERE id " +
				"> 10000",
			StringBundler.concat(
				"SELECT * FROM Table2 INNER JOIN (SELECT * FROM Table1 WHERE ",
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL, ") WHERE id > ",
				"10000"));
	}

	@Test
	public void testSelectFromTable() {
		_testInlineSQLParser(
			"SELECT * FROM Table1",
			"SELECT * FROM Table1 WHERE " +
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL);
	}

	@Test
	public void testSelectFromTableInParentheses() {
		_testInlineSQLParser(
			"(SELECT * FROM Table1)",
			"(SELECT * FROM Table1 WHERE " +
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL + ")");
	}

	@Test
	public void testSelectFromTableJoinedOnSelectFromDifferentTable() {
		_testInlineSQLParser(
			"SELECT * FROM Table1 INNER JOIN (SELECT * FROM Table2)",
			"SELECT * FROM Table1 INNER JOIN (SELECT * FROM Table2) WHERE " +
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL);
	}

	@Test
	public void testSelectFromTableJoinedOnSelectFromDifferentTableWithWhere() {
		_testInlineSQLParser(
			"SELECT * FROM Table1 INNER JOIN (SELECT * FROM Table2 WHERE id " +
				"> 10000)",
			"SELECT * FROM Table1 INNER JOIN (SELECT * FROM Table2 WHERE id " +
				"> 10000) WHERE " + _RESOURCE_PERMISSION_FILTER_CONDITION_SQL);
	}

	@Test
	public void testSelectFromTableWithGroupBy() {
		_testInlineSQLParser(
			"SELECT * FROM Table1 GROUP BY field1",
			"SELECT * FROM Table1 WHERE " +
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL + " GROUP BY field1");
	}

	@Test
	public void testSelectFromTableWithGroupByAndOrderBy() {
		_testInlineSQLParser(
			"SELECT * FROM Table1 GROUP BY field1 ORDER BY field2",
			StringBundler.concat(
				"SELECT * FROM Table1 WHERE ",
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL, " GROUP BY field1 ",
				"ORDER BY field2"));
	}

	@Test
	public void testSelectFromTableWithOrderBy() {
		_testInlineSQLParser(
			"SELECT * FROM Table1 ORDER BY field1",
			"SELECT * FROM Table1 WHERE " +
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL + " ORDER BY field1");
	}

	@Test
	public void testSelectFromTableWithWhere() {
		_testInlineSQLParser(
			"SELECT * FROM Table1 WHERE id > 10000",
			StringBundler.concat(
				"SELECT * FROM Table1 WHERE ",
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL, " AND id > 10000"));
	}

	@Test
	public void testUnionAllBothOnTable() {
		_testInlineSQLParser(
			"SELECT * FROM Table1 UNION ALL SELECT * FROM Table1",
			StringBundler.concat(
				"SELECT * FROM Table1 WHERE ",
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL, " UNION ALL SELECT ",
				"* FROM Table1 WHERE ",
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL));
	}

	@Test
	public void testUnionAllFirstOnTable() {
		_testInlineSQLParser(
			"SELECT * FROM Table1 UNION ALL SELECT * FROM Table2",
			StringBundler.concat(
				"SELECT * FROM Table1 WHERE ",
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL, " UNION ALL SELECT ",
				"* FROM Table2"));
	}

	@Test
	public void testUnionAllSecondOnTable() {
		_testInlineSQLParser(
			"SELECT * FROM Table2 UNION ALL SELECT * FROM Table1",
			"SELECT * FROM Table2 UNION ALL SELECT * FROM Table1 WHERE " +
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL);
	}

	@Test
	public void testUnionBothOnTable() {
		_testInlineSQLParser(
			"SELECT * FROM Table1 UNION SELECT * FROM Table1",
			StringBundler.concat(
				"SELECT * FROM Table1 WHERE ",
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL, " UNION SELECT * ",
				"FROM Table1 WHERE ",
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL));
	}

	@Test
	public void testUnionFirstOnTable() {
		_testInlineSQLParser(
			"SELECT * FROM Table1 UNION SELECT * FROM Table2",
			StringBundler.concat(
				"SELECT * FROM Table1 WHERE ",
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL, " UNION SELECT * ",
				"FROM Table2"));
	}

	@Test
	public void testUnionSecondOnTable() {
		_testInlineSQLParser(
			"SELECT * FROM Table2 UNION SELECT * FROM Table1",
			"SELECT * FROM Table2 UNION SELECT * FROM Table1 WHERE " +
				_RESOURCE_PERMISSION_FILTER_CONDITION_SQL);
	}

	private void _testInlineSQLParser(String originalSQL, String expectedSQL) {
		InlineSQLParser inlineSQLParser = new InlineSQLParser(
			originalSQL, _RESOURCE_PERMISSION_FILTER_CONDITION_SQL,
			_TABLE_NAME);

		Assert.assertEquals(
			expectedSQL,
			inlineSQLParser.
				parseAndInsertResourcePermissionFilterConditionSQL());
	}

	private static final String _RESOURCE_PERMISSION_FILTER_CONDITION_SQL =
		"[$RESOURCE_PERMISSION_FILTER_CONDITION$]";

	private static final String _TABLE_NAME = "Table1";

}