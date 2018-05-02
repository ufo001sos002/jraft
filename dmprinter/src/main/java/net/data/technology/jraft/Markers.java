package net.data.technology.jraft;

import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.slf4j.Log4jMarker;

/**
 * 日志标记类
 */
public interface Markers {
    /**
     * 与management通讯相关
     */
    Log4jMarker SSLCLIENT = new Log4jMarker(MarkerManager.getMarker("SSLCLIENT"));
    /**
     * 与management通讯相关 获取配置
     */
    Log4jMarker CONFIG = new Log4jMarker(MarkerManager.getMarker("CONFIG"));
    /**
     * 与 StateMachine 相关
     */
    Log4jMarker STATEMACHINE = new Log4jMarker(MarkerManager.getMarker("STATEMACHINE"));
    /**
     * 连接相关
     */
    Log4jMarker CONNECTION = new Log4jMarker(MarkerManager.getMarker("CONNECTION"));
    /**
     * 服务监听相关
     */
    Log4jMarker SERVERLISTEN = new Log4jMarker(MarkerManager.getMarker("SERVERLISTEN"));
}
