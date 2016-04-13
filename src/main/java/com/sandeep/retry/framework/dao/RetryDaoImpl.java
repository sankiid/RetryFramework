package com.sandeep.retry.framework.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import com.google.gson.Gson;
/**
 * 
 * @author sandeep
 *
 */
@Repository("retryDao")
public class RetryDaoImpl implements IRetryDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(RetryDaoImpl.class);

	@Autowired
	private JdbcTemplate	jdbcTemplate;
	private Gson			gson;

	@PostConstruct
	public void init() {
		gson = new Gson();
	}

	@Override
	public <T> void save(final List<T> obj,  final String source, final String destination, final int maxRetryCount,
			final int timeInterval, final Class<T> klass) {
		LOGGER.info("Inside save()");
		try {
			long newTime = System.currentTimeMillis() + (timeInterval * 1000);
			final Timestamp nextTime = new Timestamp(newTime);
			String sql = "INSERT INTO `retry_events` ( `request`, `update_time`, `source`, `destination`,`max_retry_count`,`time_interval`,`next_time`) VALUES(?,now(),?,?,?,?,?)";
			jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					T request = obj.get(i);
					ps.setString(1, gson.toJson(request, klass));
					ps.setString(2, source);
					ps.setString(3, destination);
					ps.setInt(4, maxRetryCount);
					ps.setInt(5, timeInterval);
					ps.setTimestamp(6, nextTime);
				}

				@Override
				public int getBatchSize() {
					return obj.size();
				}
			});

		} catch (Exception ex) {
			LOGGER.error("Error::: ", ex);
		}
		LOGGER.info("Exiting save()");
	}

	@Override
	public <T> Map<Integer, T> getData(String destination, final Class<T> klass) {
		LOGGER.info("Inside getData()");
		try {
			String sql = "SELECT `request`,`id` FROM `cashback_automation`.`retry_events` where `destination` = ? AND `next_time` <= now() AND `fail_count` < `max_retry_count` order by `id` ASC LIMIT 50000";

			return jdbcTemplate.query(sql, new Object[] { destination }, new ResultSetExtractor<Map<Integer, T>>() {
				@Override
				public Map<Integer, T> extractData(ResultSet rs) throws SQLException, DataAccessException {
					Map<Integer, T> map = new HashMap<>();
					while (rs.next()) {
						map.put(rs.getInt("id"), gson.fromJson(rs.getString("request"), klass));
					}
					return map;
				}
			});
		} catch (Exception e) {
			LOGGER.error("Error in getData:::", e);
		}
		LOGGER.info("Exiting getData()");
		return Collections.emptyMap();
	}

	@Override
	public void updateData(Map<Integer, Boolean> executeResponse) {
		LOGGER.info("Inside updateData()");
		try {
			StringBuilder updateBuilder = new StringBuilder("(");
			StringBuilder deleteBuilder = new StringBuilder("(");
			int i = 0;
			int j = 0;
			Set<Integer> keys = executeResponse.keySet();
			for (Integer key : keys) {
				if (executeResponse.get(key)) {
					if (i++ > 0) {
						deleteBuilder.append(",");
					}
					deleteBuilder.append(key);
				} else if (!executeResponse.get(key)) {
					if (j++ > 0) {
						updateBuilder.append(",");
					}
					updateBuilder.append(key);
				}
			}
			updateBuilder.append(")");
			deleteBuilder.append(")");
			final String sqlUpdate = "UPDATE `cashback_automation`.`retry_events` set `fail_count` = `fail_count`+1 , `update_time` = now() , `next_time` = date_add(now(), INTERVAL `time_interval` SECOND) where id IN ";
			final String sqlDelete = "DELETE FROM `cashback_automation`.`retry_events` where id IN ";
			if (j > 0) {
				jdbcTemplate.update(sqlUpdate + updateBuilder.toString());
				LOGGER.info("Updated id: {}", updateBuilder.toString());
			}
			if (i > 0) {
				jdbcTemplate.update(sqlDelete + deleteBuilder.toString());
				LOGGER.info("deleted id: {}", deleteBuilder.toString());
			}
		} catch (Exception e) {
			LOGGER.error("Error in updateData:::", e);
		}
		LOGGER.info("Exiting updateData()");
	}
}
