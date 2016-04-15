# RetryFramework
It will persist the request object as json in database and retry with given time-interval and attempts.
https://sankiid.wordpress.com/2016/04/14/retry-framework/

It will need the below database schema:

CREATE TABLE `retry_events` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `request` varchar(500) NOT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `next_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `fail_count` smallint(6) DEFAULT '0',
  `max_retry_count` smallint(6) DEFAULT '1',
  `source` varchar(10) DEFAULT NULL,
  `destination` varchar(10) DEFAULT NULL,
  `time_interval` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `destination` (`destination`,`next_time`,`fail_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
