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
	private static final Logger				LOGGER				= LoggerFactory.getLogger(RetryDaoImpl.class);

	private static final int				UPDATE_BATCH_SIZE	= 50;

	@Autowired
	private JdbcTemplate					jdbcTemplate;
	private Gson							gson;
	private ConcurrentMap<Long, Boolean>	producedIds			= new ConcurrentHashMap<>();

	@PostConstruct
	public void init() {
		gson = new Gson();
	}

	@SuppressWarnings("resource")
	@Override
	public <T> void save(final RetryEntity<T> entity, final String source, final String destination, final Class<T> klass) {
		if (source == null || destination == null || klass == null) {
			throw new RuntimeException("Please register retry service before use");
		}
		final Timestamp nextTime = new Timestamp(System.currentTimeMillis());
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO `cashback_automation`.`retry_events` ")
				.append("(`request`,`update_time`,`source`,`destination`,`max_retry_count`,`time_interval`,`next_time`)")
				.append(" VALUES(?,?,?,?,?,?,?)");

		jdbcTemplate.update(query.toString(), new PreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement ps) throws SQLException {
				final String jsonReq = gson.toJson(entity.getRequest(), klass);

				ps.setString(1, jsonReq);
				ps.setTimestamp(2, new Timestamp(new Date().getTime()));
				ps.setString(3, source);
				ps.setString(4, destination);
				ps.setInt(5, entity.getMaxRetryCount());
				ps.setInt(6, entity.getTimeInterval());
				ps.setTimestamp(7, nextTime);
			}
		});
	}

	@Override
	public <T> void getRetryableTask(String source, String destination, final Class<T> klass, final BlockingQueue<RetryTask<T>> queue) {
		try {
			String sql = "SELECT `request`,`id` FROM `cashback_automation`.`retry_events` where `source`=? AND `destination` = ? AND `next_time` <= now() AND `fail_count` < `max_retry_count` order by `id` ASC LIMIT 10000";
			jdbcTemplate.query(sql, new Object[] { source, destination }, new ResultSetExtractor<Void>() {
				@Override
				public Void extractData(ResultSet rs) throws SQLException, DataAccessException {
					while (rs.next()) {
						try {
							if (!producedIds.containsKey(rs.getLong("id"))) {
								queue.put(new RetryTask<T>(rs.getLong("id"), gson.fromJson(rs.getString("request"), klass)));
								producedIds.putIfAbsent(rs.getLong("id"), Boolean.TRUE);
							}
						} catch (Exception e) {
							LOGGER.error("Error in db get ", e);
						}
					}
					return null;
				}
			});
		} catch (Exception e) {
			LOGGER.error("Error in getData:::", e);
		}
	}

	@Override
	public void removeEntryBasedOnExternalAppId(List<String> externalAppUid) {
		LOGGER.info("Inside removeEntryBasedOnExternalAppId");
		try {
			StringBuilder builder = new StringBuilder();
			int idx = 0;
			for (String uid : externalAppUid) {
				if (idx > 0) {
					builder.append(",");
				}
				builder.append(uid);
				if (++idx == UPDATE_BATCH_SIZE) {
					try {
						String sql = "DELETE FROM cashback_automation.retry_events WHERE external_application_uid in ( " + builder.toString() + ")";
						jdbcTemplate.update(sql);
					} catch (Exception e) {
						LOGGER.error("Error in update ", e);
					}
					idx = 0;
					builder = new StringBuilder();
				}
			}
			if (idx > 0) {
				try {
					String sql = "DELETE FROM cashback_automation.retry_events WHERE external_application_uid in ( " + builder.toString() + ")";
					jdbcTemplate.update(sql);
				} catch (Exception e) {
					LOGGER.error("Error in update ", e);
				}
			}
		} catch (Exception e) {
			LOGGER.error("Error in deleteRetryEventList ", e);
		}
		LOGGER.info("exiting removeEntryBasedOnExternalAppId");
	}

	@Override
	public void updateTaskResponse(RetryTaskResponse response) {
		LOGGER.debug("Inside updateTaskResponse");
		try {
			if (response.isStatus()) {
				String sql = "DELETE FROM `cashback_automation`.`retry_events` where id = ?";
				jdbcTemplate.update(sql, response.getId());
			} else {
				String sql = "UPDATE `cashback_automation`.`retry_events` set `fail_count` = `fail_count`+1 , `update_time` = now() , `next_time` = date_add(now(), INTERVAL `time_interval` SECOND) where id = ?";
				jdbcTemplate.update(sql, response.getId());
			}
		} catch (Exception e) {
			LOGGER.error("Error in updateTaskResponse ", e);
		}
		producedIds.remove(response.getId());
		LOGGER.debug("exiting updateTaskResponse");
	}

}
