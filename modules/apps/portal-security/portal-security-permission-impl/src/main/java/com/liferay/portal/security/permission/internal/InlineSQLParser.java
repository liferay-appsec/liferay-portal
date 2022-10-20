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

package com.liferay.portal.security.permission.internal;

import com.liferay.petra.string.CharPool;
import com.liferay.petra.string.StringBundler;
import com.liferay.petra.string.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.kernel.util.Validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author Michael Bowerman
 */
public class InlineSQLParser {

	public InlineSQLParser(
		String originalSQLQuery, String resourcePermissionFilterConditionSQL,
		String tableName) {

		_originalSQLQuery = originalSQLQuery;
		_resourcePermissionFilterConditionSQL =
			resourcePermissionFilterConditionSQL;
		_tableName = tableName;
	}

	public String parseAndInsertResourcePermissionFilterConditionSQL() {
		String lowerCaseSql = StringUtil.toLowerCase(_originalSQLQuery);

		_computeSelectQueries(lowerCaseSql, 0);

		TreeMap<Integer, String> resourcePermissionSQLInsertions =
			new TreeMap<>();

		for (SelectQuery selectQuery : _flattenedSelectQueries) {
			String resourcePermissionSQL =
				selectQuery.getResourcePermissionSQL();

			if (resourcePermissionSQL != null) {
				resourcePermissionSQLInsertions.put(
					selectQuery.getResourcePermissionSQLIndex(),
					resourcePermissionSQL);
			}
		}

		StringBundler sb = new StringBundler(
			1 + (2 * resourcePermissionSQLInsertions.size()));

		int previousIndex = 0;

		for (Map.Entry<Integer, String> resourcePermissionSQLInsertionEntry :
				resourcePermissionSQLInsertions.entrySet()) {

			int index = resourcePermissionSQLInsertionEntry.getKey();

			sb.append(_originalSQLQuery.substring(previousIndex, index));

			sb.append(resourcePermissionSQLInsertionEntry.getValue());

			previousIndex = index;
		}

		sb.append(_originalSQLQuery.substring(previousIndex));

		return sb.toString();
	}

	private List<SelectQuery> _computeSelectQueries(
		String sql, int originalParenthesesDepth) {

		List<SelectQuery> selectQueries = new ArrayList<>();

		_index = StringUtil.indexOfAny(sql, _SELECT_PARSE_KEYWORDS, _index);

		int parenthesesDepth = originalParenthesesDepth;

		SelectQuery curSelectQuery = null;

		while ((_index >= 0) && (_index < sql.length())) {
			if (sql.charAt(_index) == CharPool.OPEN_PARENTHESIS) {
				parenthesesDepth++;

				if (curSelectQuery == null) {
					_index++;

					return _computeSelectQueries(sql, parenthesesDepth);
				}
			}
			else if (sql.charAt(_index) == CharPool.CLOSE_PARENTHESIS) {
				parenthesesDepth--;

				if (parenthesesDepth < originalParenthesesDepth) {
					if (curSelectQuery != null) {
						curSelectQuery.setEnd(_index);

						_flattenedSelectQueries.add(curSelectQuery);
						selectQueries.add(curSelectQuery);
					}

					_index--;

					return selectQueries;
				}
			}
			else if ((_index < sql.length()) &&
					 (sql.charAt(_index + 1) == CharPool.LOWER_CASE_U)) {

				curSelectQuery.setEnd(_index);

				_flattenedSelectQueries.add(curSelectQuery);
				selectQueries.add(curSelectQuery);

				curSelectQuery = null;
			}
			else if ((_index == 0) ||
					 Validator.isWhitespace(sql.charAt(_index - 1)) ||
					 (sql.charAt(_index - 1) == CharPool.OPEN_PARENTHESIS)) {

				if ((parenthesesDepth > originalParenthesesDepth) &&
					(curSelectQuery != null)) {

					curSelectQuery.addSubqueries(
						_computeSelectQueries(sql, parenthesesDepth));
				}
				else {
					curSelectQuery = new SelectQuery(_index);
				}
			}

			_index = StringUtil.indexOfAny(
				sql, _SELECT_PARSE_KEYWORDS, _index + 1);
		}

		if (curSelectQuery != null) {
			curSelectQuery.setEnd(sql.length());

			_flattenedSelectQueries.add(curSelectQuery);
			selectQueries.add(curSelectQuery);
		}

		return selectQueries;
	}

	private static final String _FROM = " from ";

	private static final String _GROUP_BY = " group by ";

	private static final String _JOIN = " join ";

	private static final String _ORDER_BY = " order by ";

	private static final String[] _SELECT_PARSE_KEYWORDS = {
		StringPool.OPEN_PARENTHESIS, StringPool.CLOSE_PARENTHESIS, "select ",
		" union "
	};

	private static final String _WHERE = " where ";

	private final List<SelectQuery> _flattenedSelectQueries = new ArrayList<>();
	private int _index;
	private final String _originalSQLQuery;
	private final String _resourcePermissionFilterConditionSQL;
	private final String _tableName;

	private class SelectQuery {

		public SelectQuery(int start) {
			_start = start;
		}

		public void addSubqueries(List<SelectQuery> selectSubqueries) {
			_selectSubqueries.addAll(selectSubqueries);
		}

		public int getEnd() {
			return _end;
		}

		public String getResourcePermissionSQL() {
			return _resourcePermissionSQL;
		}

		public int getResourcePermissionSQLIndex() {
			return _resourcePermissionSQLIndex;
		}

		public int getStart() {
			return _start;
		}

		public void setEnd(int end) {
			_end = end;

			_selectSQL = _originalSQLQuery.substring(_start, _end);

			_computeResourcePermissionSQLParameters();
		}

		private void _computeResourcePermissionSQLParameters() {
			if (!_selectsFromTableName()) {
				_resourcePermissionSQL = null;
				_resourcePermissionSQLIndex = -1;

				return;
			}

			int index = _indexOfExcludingSubqueries(_WHERE);

			if (index >= 0) {
				_resourcePermissionSQL =
					_resourcePermissionFilterConditionSQL + " AND ";
				_resourcePermissionSQLIndex = index + _start + _WHERE.length();

				return;
			}

			_resourcePermissionSQL =
				" WHERE " + _resourcePermissionFilterConditionSQL;

			index = _indexOfExcludingSubqueries(_GROUP_BY);

			if (index == -1) {
				index = _indexOfExcludingSubqueries(_ORDER_BY);
			}

			if (index == -1) {
				_resourcePermissionSQLIndex = _end;

				return;
			}

			_resourcePermissionSQLIndex = index + _start;
		}

		private String[] _getSelectedTableNames() {
			Set<String> selectedTableNameSet = new HashSet<>();

			int index = 0;

			while ((index = _indexOfExcludingSubqueries(_FROM, index)) != -1) {
				index += _FROM.length();

				int[] indexes = _getTableNameIndexes(index);

				if (indexes != null) {
					selectedTableNameSet.add(
						_selectSQL.substring(indexes[0], indexes[1]));
				}
			}

			index = 0;

			while ((index = _indexOfExcludingSubqueries(_JOIN, index)) != -1) {
				index += _JOIN.length();

				int[] indexes = _getTableNameIndexes(index);

				if (indexes != null) {
					selectedTableNameSet.add(
						_selectSQL.substring(indexes[0], indexes[1]));
				}
			}

			return selectedTableNameSet.toArray(new String[0]);
		}

		private int[] _getTableNameIndexes(int index) {
			int start = -1;
			int end = _selectSQL.length();

			for (int i = index; i < _selectSQL.length(); i++) {
				char c = _selectSQL.charAt(i);

				if (c == CharPool.OPEN_PARENTHESIS) {

					// There is a subquery in the current clause, so no need to
					// parse for a table name

					break;
				}
				else if ((c == CharPool.SPACE) ||
						 (c == CharPool.CLOSE_PARENTHESIS)) {

					if (start != -1) {
						end = i;

						break;
					}
				}
				else if (start == -1) {
					start = i;
				}
			}

			if (start == -1) {
				return null;
			}

			return new int[] {start, end};
		}

		private int _indexOfExcludingSubqueries(String keyword) {
			return _indexOfExcludingSubqueries(keyword, 0);
		}

		private int _indexOfExcludingSubqueries(String keyword, int fromIndex) {
			String lowerCaseSQL = StringUtil.toLowerCase(_selectSQL);
			String lowerCaseKeyword = StringUtil.toLowerCase(keyword);

			int index = lowerCaseSQL.indexOf(lowerCaseKeyword, fromIndex);

			while (index >= 0) {
				if (!_isInSubqueryBounds(index + _start)) {
					return index;
				}

				index = lowerCaseSQL.indexOf(keyword, index + 1);
			}

			return -1;
		}

		private boolean _isInSubqueryBounds(int index) {
			for (SelectQuery selectSubquery : _selectSubqueries) {
				if ((index >= selectSubquery.getStart()) &&
					(index < selectSubquery.getEnd())) {

					return true;
				}
			}

			return false;
		}

		private boolean _selectsFromTableName() {
			for (String selectedTableName : _getSelectedTableNames()) {
				if (StringUtil.equalsIgnoreCase(
						selectedTableName, _tableName)) {

					return true;
				}
			}

			return false;
		}

		private int _end;
		private String _resourcePermissionSQL;
		private int _resourcePermissionSQLIndex;
		private String _selectSQL;
		private List<SelectQuery> _selectSubqueries = new ArrayList<>();
		private final int _start;

	}

}